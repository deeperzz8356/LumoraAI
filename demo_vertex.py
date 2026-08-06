import asyncio
import os
import sys

sys.path.append(os.path.join(os.path.dirname(__file__), "backend"))

from app.core.config import get_settings
from app.providers.cloudflare_provider import CloudflareProvider
from app.schemas.images import ImageGenerateRequest


async def main() -> None:
    get_settings.cache_clear()
    settings = get_settings()
    
    print("=" * 60)
    print("IMAGE GENERATION DEMO - CLOUDFLARE WORKERS AI")
    print("=" * 60)
    print(f"\n📍 SOURCE: Cloudflare Workers AI (NOT Vertex AI)")
    print(f"🎨 MODEL: {settings.workers_ai_model} (Flux 1 Schnell)")
    print(f"💰 COST: Free tier (10,000 requests/day)")
    print(f"🔑 PROVIDER: {CloudflareProvider.__name__}")

    provider = CloudflareProvider()
    request = ImageGenerateRequest(
        prompt="A red apple on a wooden table, soft natural light, photorealistic",
        width=1024,
        height=1024,
    )
    print(f"\n📝 PROMPT: {request.prompt!r}")
    print("\n⏳ Generating image...\n")
    
    result = await provider.generate_image(request)
    
    print("✅ SUCCESS!")
    print("-" * 60)
    print(f"📍 IMAGE SOURCE: {result.model}")
    print(f"📦 FORMAT: {result.mime_type}")
    print(f"💾 SIZE: {len(result.image_bytes):,} bytes ({len(result.image_bytes)/1024/1024:.2f} MB)")

    output_file = "demo_output.png"
    with open(output_file, "wb") as handle:
        handle.write(result.image_bytes)
    
    print(f"✨ SAVED: {output_file}")
    print("=" * 60)


if __name__ == "__main__":
    asyncio.run(main())
