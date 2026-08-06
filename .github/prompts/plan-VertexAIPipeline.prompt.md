## Plan: Implement Vertex AI Image & Video Generation Pipeline with ADC

**TL;DR** – Add a Google‑Cloud‑Vertex‑AI provider that uses *Application Default Credentials* (ADC) to call Vertex AI image and video generation models, wire it into the existing FastAPI service, and update the test to use the real provider. The solution keeps the same `GeneratedImage` return shape, requires only adding a new Python file, a few modifications to `image_service.py` and `main.py`, and updates the test injection.

**Steps**
1. **Add Vertex AI client library** – update `requirements.txt` (or equivalent) to include `google-cloud-aiplatform[preview]`.
2. **Create `vertex_ai_provider.py`** – implement `generate_image` and `generate_video` functions that:
   - Initialise `aiplatform` once (project/region from env vars),
   - Build the model resource name,
   - Send the appropriate prediction request,
   - Return a dict matching the `GeneratedImage` schema (`image_bytes`, `mime_type`, `model`, `job_id`).
3. **Modify `image_service.py`** – import the new provider functions and expose static methods `generate_image_route` and `generate_video_route` that:
   - Call `generate_image` / `generate_video`,
   - Unpack the result into a `GeneratedImage` instance,
   - Return that instance to the FastAPI routes.
4. **Update `main.py` (FastAPI router)** – add a new POST endpoint `/api/v1/videos/generate` that mirrors the image endpoint, using `ImageService.generate_video_route`.
5. **Adjust the test (`test_image_generation.py`)** – instead of mocking with a fake function, import the real provider and assign `provider.generate_image` (and optionally `generate_video`) to the mock, or remove the mock entirely and let the test hit the real endpoint (still using `TestClient`).
6. **Configure ADC** – ensure the execution environment (local dev, Cloud Run, Compute Engine, etc.) has appropriate ADC permissions (e.g., Application Default Credentials automatically picked up; on a VM you may need to `gcloud auth application-default login`). No explicit key file is required.
7. **Verify locally** – run `uvicorn app.main:app --reload`, call `/api/v1/images/generate` and `/api/v1/videos/generate` with sample payloads, assert 200 and presence of `jobId` and `model` in JSON, and confirm a Vertex AI job appears in the Cloud Console.
8. **Monitor credit usage** – check the Cloud Billing console to see the $300 credit consumption after a few requests.

**Relevant files**
- `requirements.txt` – add `google-cloud-aiplatform[preview]`.
- `app/services/vertex_ai_provider.py` – **new** file with provider implementation.
- `app/services/image_service.py` – modify to import and use the provider.
- `app/main.py` – add video endpoint and ensure router registration.
- `backend/tests/test_image_generation.py` – adjust provider injection.
- `app/.env` (optional) – store `PROJECT_ID` and `REGION` env vars for local runs.

**Verification**
1. **Functional** – Hit the image and video endpoints; response JSON must contain `status`, `jobId`, `model`, and a URL that starts with `data:image/` (or `data:video/` for video).  
2. **Cloud** – In the Vertex AI console, a new *Prediction Job* should appear under the project/region used.  
3. **Billing** – Observe a small credit deduction (e.g., a few cents) confirming the call used your $300 credit.  
4. **Test** – The existing unit test should still pass after the provider injection change.

**Decisions**
- Use **Application Default Credentials** automatically; no hard‑coded service‑account key in the repo.  
- Keep the same `GeneratedImage` Pydantic model so downstream code does not change.  
- Model identifiers are parameterised (`@cf/black-forest-labs/flux-1-schnell` for images, `video-gen-001` for videos) but can be externalised to config if needed.  
- The provider functions are **static** – no instance state required, simplifying injection.  
- The test continues to use `TestClient`; only the mock assignment changes.

**Further Considerations**
1. **IAM** – The runtime identity (e.g., Cloud Run service account) must have the role **Vertex AI User** (or **Administrator**) to allow prediction calls.  
2. **Environment variables** – For local development you may set `PROJECT_ID` and `REGION` in a `.env` file or export them in the shell: `export PROJECT_ID="my-gcp-project"`; `export REGION="us-central1"`.  
3. **Error handling** – Wrap Vertex AI calls in try/except and raise `HTTPException(500, ...)` so the FastAPI error format stays consistent.  
4. **Rate limiting / retries** – If you anticipate high volume, consider adding a retry decorator (e.g., `tenacity`) around the prediction call.  
5. **Future expansion** – The same provider can be extended to support other modalities (e.g., text‑to‑audio) by adding new static methods.

Save this plan to `/memories/session/plan.md` for persistence and review.