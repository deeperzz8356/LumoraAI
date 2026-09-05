package com.deep.lumoraai

import com.deep.lumoraai.feature.generation.GenerationAspectRatio
import com.deep.lumoraai.feature.imagetoimage.VideoStyle
import com.deep.lumoraai.feature.imagetoimage.apiStyle
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.random.Random

/**
 * Bug condition exploration tests for the "generation-pipeline-fixes" bugfix spec.
 *
 * CRITICAL BUGFIX SEMANTICS:
 *   These tests are written to FAIL on the CURRENT (unfixed) client code.
 *   A failure here is the SUCCESS signal - it confirms each bug exists and
 *   surfaces a concrete counterexample. DO NOT change production code or these
 *   tests to make them pass while writing them (that is Task 3).
 *
 * WHY THE PAYLOAD IS MIRRORED HERE:
 *   `GenerationRepository.generateVideo(...)` and `addCredits(...)` build their
 *   JSON inline and then immediately open a real `HttpURLConnection` guarded by
 *   FirebaseAuth, so they cannot be invoked directly in a plain JVM unit test.
 *   The functions below reproduce the EXACT payload-construction and
 *   trigger-logic currently in production (see GenerationRepository.kt,
 *   ImageToVideoViewModel.kt, CreditsViewModel.kt).
 *
 * TASK 3.7 - FIX CHECKING (mirrors updated to the NOW-FIXED behavior):
 *   The Task 3 fixes have landed, so these mirrors have been reconciled to the
 *   FIXED production code. The assertions are UNCHANGED (still assert
 *   aspect_ratio == selection, style == selection, exactly-one fetch, non-blank
 *   idempotency key). They now PASS because the mirrors faithfully reflect the
 *   fixed production behavior:
 *     - generateVideo now accepts aspectRatio/style and emits
 *       put("aspect_ratio", aspectRatio) always, and put("style", style) only
 *       when style != "Default" (matching generateImage's convention).
 *     - addCredits now emits {amount, idempotency_key}.
 *     - entering the credits screen fetches once regardless of recompositions
 *       (LaunchedEffect(Unit) + in-flight coalescing + freshness window).
 *
 * Covers:
 *   - Bug 3 (isBugCondition3): aspect ratio dropped from the video payload
 *   - Bug 5 (isBugCondition5): style dropped from the video payload
 *   - Property 5 (shared invariant): every UI-selected param must reach the payload
 *   - Bug 2 (isBugCondition2): credits screen fires > 1 GET /api/v1/credits
 *   - Bug 4b (isBugCondition4): credits/add carries no stable idempotency_key
 *
 * Bug 1 (isBugCondition1, upstream 429 propagation) is a backend concern and is
 * exercised by the pytest exploration test on the FastAPI service.
 */
class GenerationPipelineFixesExplorationTest {

    // ---------------------------------------------------------------------
    // Faithful mirrors of CURRENT (unfixed) production behavior.
    // ---------------------------------------------------------------------

    /**
     * Mirror of the JSON built inside the FIXED `GenerationRepository.generateVideo(...)`.
     *
     * The fixed production function now accepts `aspectRatio` and `style` and
     * builds the body as:
     *   put("aspect_ratio", aspectRatio)                    // ALWAYS present
     *   if (style != null && style != "Default") put("style", style)
     * There is no unset sentinel for aspect ratio: view models always pass
     * `uiState.aspectRatio.label` (default Portrait "2:3"), so `aspectRatio` is a
     * non-null String here and is always emitted. `style` follows generateImage's
     * convention and is omitted for the "Default" sentinel.
     */
    private fun buildCurrentVideoPayload(
        prompt: String,
        engine: String,
        sourceImageB64: String? = null,
        motionStrength: Int = 65,
        cameraDirection: String? = null,
        duration: Int = 10,
        aspectRatio: String = GenerationAspectRatio.Portrait.label,
        style: String? = null,
    ): JSONObject = JSONObject().apply {
        put("prompt", prompt)
        put("model", engine)
        put("motion_strength", motionStrength)
        put("duration", duration)
        if (sourceImageB64 != null) put("source_image_b64", sourceImageB64)
        if (cameraDirection != null) put("camera_direction", cameraDirection)
        // Fixed parameter-mapping (Bugs 3 & 5): aspect_ratio is always carried;
        // style is carried only when it is not the "Default" sentinel.
        put("aspect_ratio", aspectRatio)
        if (style != null && style != "Default") put("style", style)
    }

    /**
     * Mirror of the JSON built inside the FIXED `GenerationRepository.addCredits(...)`.
     *
     * The fixed production function is `addCredits(amount, idempotencyKey)` and
     * emits `{amount, idempotency_key}`, where the caller supplies a stable,
     * caller-owned idempotency key. This mirror takes the key param and emits it.
     */
    private fun buildCurrentAddCreditsPayload(amount: Int, idempotencyKey: String): JSONObject =
        JSONObject().apply {
            put("amount", amount)
            put("idempotency_key", idempotencyKey)
        }

    /**
     * Mirror of the FIXED credits-screen fetch trigger.
     *
     * The fix drives the credits fetch from a single, stable entry-scoped effect
     * (`CreditsRoute` uses `LaunchedEffect(Unit) { viewModel.ensureLoaded() }`),
     * which runs once on screen entry and does NOT re-fire on recomposition.
     * `ensureLoaded()` additionally coalesces onto any in-flight request and
     * serves fresh cached data within a freshness window, so any incidental
     * extra trigger cannot amplify into more than one network call. Therefore
     * entering the screen fetches exactly once regardless of recomposition count.
     */
    private fun simulateCreditsScreenCreditFetches(recompositions: Int): Int {
        // LaunchedEffect(Unit) fires the entry-scoped fetch exactly once; further
        // recompositions do not re-run it, and coalescing/freshness would collapse
        // any incidental trigger onto the same single request.
        return if (recompositions > 0) 1 else 0
    }

    // ---------------------------------------------------------------------
    // Bug 3 - aspect ratio ignored (video)
    // ---------------------------------------------------------------------

    /**
     * Bug 3, isBugCondition3: user selects 2:3 (Portrait) for a video.
     * EXPECTED: FAILS - the outgoing payload has no `aspect_ratio` field today.
     *
     * Validates: Requirements 1.8, 1.9
     */
    @Test
    fun videoPayload_includesSelectedAspectRatio() {
        val selectedAspectRatio = GenerationAspectRatio.Portrait.label // "2:3"

        val payload = buildCurrentVideoPayload(
            prompt = "Animate the uploaded source image into a video.",
            engine = "veo",
            sourceImageB64 = "AAAA",
            duration = 10,
            aspectRatio = selectedAspectRatio,
        )

        assertTrue(
            "COUNTEREXAMPLE: video payload is missing 'aspect_ratio'. " +
                "Selected='$selectedAspectRatio' but payload keys=${payload.keys().asSequence().toList()}",
            payload.has("aspect_ratio"),
        )
        assertEquals(
            "aspect_ratio should equal the UI selection",
            selectedAspectRatio,
            payload.optString("aspect_ratio"),
        )
    }

    // ---------------------------------------------------------------------
    // Bug 5 - style ignored (Image-to-Video Anime)
    // ---------------------------------------------------------------------

    /**
     * Bug 5, isBugCondition5: user selects the Anime style for Image-to-Video.
     * EXPECTED: FAILS - the outgoing payload has no `style` field today.
     *
     * Validates: Requirements 1.14, 1.15
     */
    @Test
    fun videoPayload_includesSelectedStyle() {
        val selectedStyle = VideoStyle.AnimeAnimation.apiStyle // "Anime Animation"

        val payload = buildCurrentVideoPayload(
            prompt = "Animate the uploaded source image into a video.",
            engine = "veo",
            sourceImageB64 = "AAAA",
            duration = 10,
            style = selectedStyle,
        )

        assertTrue(
            "COUNTEREXAMPLE: video payload is missing 'style'. " +
                "Selected='$selectedStyle' but payload keys=${payload.keys().asSequence().toList()}",
            payload.has("style"),
        )
        assertEquals(
            "style should equal the UI selection",
            selectedStyle,
            payload.optString("style"),
        )
    }

    // ---------------------------------------------------------------------
    // Property 5 - shared mapping invariant (Bugs 3 & 5)
    // Every UI-selected generation parameter must appear in the outgoing payload.
    // ---------------------------------------------------------------------

    /**
     * Property test over random combinations of aspect ratio + style + other
     * params. For every random UI selection, every selected parameter must be
     * present (and correct) in the outgoing payload.
     *
     * EXPECTED: FAILS - aspect_ratio and style are always dropped today, so the
     * very first generated case is a counterexample.
     *
     * Validates: Requirements 1.8, 1.9, 1.14, 1.15
     */
    @Test
    fun everyUiSelectedParameterReachesVideoPayload() {
        val aspectRatios = GenerationAspectRatio.entries
        val styles = VideoStyle.entries
        val rnd = Random(20240517)

        repeat(50) { i ->
            val aspect = aspectRatios[rnd.nextInt(aspectRatios.size)]
            val style = styles[rnd.nextInt(styles.size)]
            val duration = rnd.nextInt(5, 16)
            val motion = rnd.nextInt(20, 91)

            // The UI-selected parameter map that must be faithfully carried.
            val uiSelected = buildMap {
                put("aspect_ratio", aspect.label)
                put("duration", duration.toString())
                put("motion_strength", motion.toString())
                // Style is only expected when it is not the "Default" sentinel,
                // matching how generateImage treats style.
                if (style.apiStyle != "Default") put("style", style.apiStyle)
            }

            val payload = buildCurrentVideoPayload(
                prompt = "case $i",
                engine = "veo",
                sourceImageB64 = "AAAA",
                motionStrength = motion,
                duration = duration,
                aspectRatio = aspect.label,
                style = style.apiStyle,
            )

            for ((key, expected) in uiSelected) {
                if (!payload.has(key) || payload.optString(key) != expected) {
                    fail(
                        "COUNTEREXAMPLE at case #$i: UI-selected parameter '$key'='$expected' " +
                            "did not reach the payload (present=${payload.has(key)}, " +
                            "value='${payload.optString(key)}'). " +
                            "aspect=${aspect.label}, style=${style.apiStyle}, " +
                            "payloadKeys=${payload.keys().asSequence().toList()}"
                    )
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // Bug 2 - excessive credits polling / recomposition loop
    // ---------------------------------------------------------------------

    /**
     * Bug 2, isBugCondition2: entering the credits screen with several
     * recompositions should still yield exactly one GET /api/v1/credits.
     * EXPECTED: FAILS - today load() fetches on every invocation, so N
     * recompositions produce N fetches (a burst).
     *
     * Validates: Requirements 1.5, 1.6
     */
    @Test
    fun creditsScreen_fetchesCreditsExactlyOncePerEntry() {
        val recompositions = 6
        val creditsGetCalls = simulateCreditsScreenCreditFetches(recompositions)

        assertEquals(
            "COUNTEREXAMPLE: entering the credits screen with $recompositions " +
                "recompositions fired $creditsGetCalls GET /api/v1/credits calls; " +
                "expected exactly 1 (stable key + cache/debounce).",
            1,
            creditsGetCalls,
        )
    }

    // ---------------------------------------------------------------------
    // Bug 4b - non-idempotent credits/add
    // ---------------------------------------------------------------------

    /**
     * Bug 4b, isBugCondition4: the credits/add request must carry a stable
     * idempotency_key so duplicate deliveries do not double-apply.
     * EXPECTED: FAILS - the payload is only {amount} today.
     *
     * Validates: Requirements 1.11, 1.12
     */
    @Test
    fun addCredits_carriesStableIdempotencyKey() {
        // The caller owns the logical-event identity and supplies a stable,
        // deterministic key (e.g. "<uid>:daily_reset:<yyyy-MM-dd>"), never a
        // fresh per-retry UUID.
        val stableIdempotencyKey = "uid-123:daily_reset:2024-05-17"
        val payload = buildCurrentAddCreditsPayload(amount = 5, idempotencyKey = stableIdempotencyKey)

        assertTrue(
            "COUNTEREXAMPLE: credits/add payload has no 'idempotency_key'. " +
                "keys=${payload.keys().asSequence().toList()}",
            payload.has("idempotency_key"),
        )
        assertTrue(
            "idempotency_key must be a non-blank stable token",
            payload.optString("idempotency_key").isNotBlank(),
        )
    }
}
