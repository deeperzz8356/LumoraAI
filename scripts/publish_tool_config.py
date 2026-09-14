"""Merge new global tool parameters into Firebase Remote Config using its ETag.

Existing ad parameters and conditions are preserved. No existing value is replaced.
"""
import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "backend"))
from app.services.tool_policy import DEFAULT_COSTS


def parameters():
    result = {}
    for tool, cost in DEFAULT_COSTS.items():
        result[f"tool_{tool}_enabled"] = {"defaultValue": {"value": "true"}, "valueType": "BOOLEAN", "description": f"Allow new {tool} operations. Existing jobs finish."}
        result[f"tool_{tool}_credit_cost"] = {"defaultValue": {"value": str(cost)}, "valueType": "NUMBER", "description": f"Global credits per {tool} output. Integer 0–100000; do not add audience conditions."}
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--publish", action="store_true")
    args = parser.parse_args()
    addition = parameters()
    (ROOT / "docs/tool_remote_config_template.json").write_text(json.dumps({"parameters": addition}, indent=2), encoding="utf-8")
    if not args.publish:
        print(f"Prepared {len(addition)} tool parameters.")
        return
    from app.core.firebase import initialize_firebase_app
    from google.auth.transport.requests import AuthorizedSession
    app = initialize_firebase_app()
    endpoint = f"https://firebaseremoteconfig.googleapis.com/v1/projects/{app.project_id}/remoteConfig"
    with AuthorizedSession(app.credential.get_credential()) as session:
        response = session.get(endpoint, timeout=15)
        response.raise_for_status()
        template = response.json()
        template.pop("version", None)
        existing = template.setdefault("parameters", {})
        count = 0
        for key, value in addition.items():
            if key not in existing:
                existing[key] = value
                count += 1
        if count:
            result = session.put(endpoint, json=template, headers={"If-Match": response.headers["ETag"]}, timeout=20)
            result.raise_for_status()
        verified = session.get(endpoint, timeout=15)
        verified.raise_for_status()
        assert all(key in verified.json().get("parameters", {}) for key in addition)
    print(f"Published and verified {count} new parameters; preserved existing settings.")


if __name__ == "__main__":
    main()
