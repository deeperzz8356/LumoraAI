"""Validate, upload, verify, then package a small offline starter catalog.

Run using backend/.venv/Scripts/python.exe. --dry-run never writes or uploads.
--publish uploads and verifies without changing packaged assets.
--package requires a successful remote verification in the same invocation.
"""
import argparse
import hashlib
import json
import mimetypes
import shutil
import sys
import uuid
from pathlib import Path
from urllib.parse import quote

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets/templates"
ARCHIVE = ROOT / "migration/templates-originals"


def catalog(source):
    data = json.loads((source / "template.json").read_text(encoding="utf-8"))
    seen = set()
    for category in data["categories"]:
        for section in category["sections"]:
            for item in section["templates"]:
                if item["id"] in seen:
                    raise ValueError("Duplicate template ID: " + item["id"])
                seen.add(item["id"])
                for key in ("assetFileName", "previewAssetFileName"):
                    name = item.get(key)
                    if name and (Path(name).name != name or not (source / name).is_file()):
                        raise ValueError("Missing or unsafe media filename: " + str(name))
    return data


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--publish", action="store_true")
    parser.add_argument("--package", action="store_true")
    args = parser.parse_args()
    source = ARCHIVE if (ARCHIVE / "template.json").exists() else ASSETS
    data = catalog(source)
    files = sorted({i[k] for c in data["categories"] for s in c["sections"] for i in s["templates"]
                    for k in ("assetFileName", "previewAssetFileName") if i.get(k)})
    print(json.dumps({"templates": sum(len(s["templates"]) for c in data["categories"] for s in c["sections"]),
                      "media_files": len(files), "source_bytes": sum((source/f).stat().st_size for f in files)}))
    if args.dry_run or not (args.publish or args.package):
        return
    sys.path.insert(0, str(ROOT / "backend"))
    from app.core.firebase import get_storage_bucket, get_firestore_client
    bucket, db = get_storage_bucket(), get_firestore_client()
    urls = {}
    for name in files:
        path = source / name
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        blob = bucket.blob(f"template-media/{digest}/{name}")
        if not blob.exists():
            blob.metadata = {"sha256": digest, "firebaseStorageDownloadTokens": str(uuid.uuid4())}
            blob.upload_from_filename(str(path), content_type=mimetypes.guess_type(name)[0] or "application/octet-stream")
        blob.reload()
        if blob.size != path.stat().st_size or hashlib.sha256(blob.download_as_bytes()).hexdigest() != digest:
            raise RuntimeError("Remote verification failed: " + name)
        token = (blob.metadata or {}).get("firebaseStorageDownloadTokens", "").split(",")[0]
        if not token:
            raise RuntimeError("Missing download token: " + name)
        urls[name] = f"https://firebasestorage.googleapis.com/v0/b/{bucket.name}/o/{quote(blob.name, safe='')}?alt=media&token={token}"
    for c in data["categories"]:
        for s in c["sections"]:
            for item in s["templates"]:
                item["mediaUrl"] = urls.get(item.get("assetFileName"), "")
                item["previewUrl"] = urls.get(item.get("previewAssetFileName"), "")
    payload = {"schemaVersion": 1, "catalog": json.dumps(data, ensure_ascii=False)}
    if len(json.dumps(payload).encode()) > 900000:
        raise ValueError("Catalog exceeds safe document size; split before publishing")
    ref = db.collection("template_catalogs").document("v1")
    ref.set(payload)
    if ref.get().to_dict() != payload:
        raise RuntimeError("Firestore read-back did not match upload")
    print("Remote media and Firestore catalog verified.")
    if not args.package:
        return
    from PIL import Image
    if source == ASSETS:
        ARCHIVE.mkdir(parents=True, exist_ok=True)
        for path in ASSETS.iterdir():
            if path.is_file():
                shutil.copy2(path, ARCHIVE / path.name)
    original = catalog(ARCHIVE)
    starter = {"categories": []}
    retained = []
    for c in original["categories"]:
        sections = []
        for s in c["sections"]:
            templates = []
            for item in s["templates"]:
                if c["id"] != "image" or len(retained) >= 4:
                    continue
                name = item.get("previewAssetFileName") or item["assetFileName"]
                output = item["id"] + ".webp"
                with Image.open(ARCHIVE / name) as image:
                    image.thumbnail((640, 640))
                    image.convert("RGB").save(ASSETS / output, "WEBP", quality=76, method=6)
                retained.append(output)
                templates.append({**item, "assetFileName": output, "previewAssetFileName": output})
            if templates:
                sections.append({**s, "templates": templates})
        starter["categories"].append({**c, "sections": sections})
    size = sum((ASSETS / name).stat().st_size for name in retained)
    if len(retained) != 4 or size >= 1000000:
        raise RuntimeError("Starter set exceeds its size budget or is incomplete")
    # All originals have a verified cloud copy and an unmodified local archive.
    root = ASSETS.resolve()
    for path in ASSETS.iterdir():
        if path.is_file() and path.name not in retained and path.name != "template.json":
            if path.resolve().parent != root:
                raise ValueError("Refusing to remove outside template assets")
            path.unlink()
    (ASSETS / "template.json").write_text(json.dumps(starter, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"starter_count": 4, "starter_media_bytes": size, "archive": str(ARCHIVE)}))


if __name__ == "__main__":
    main()
