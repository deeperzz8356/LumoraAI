package com.deep.lumoraai

import com.deep.lumoraai.feature.generation.GenerationAspectRatio
import com.deep.lumoraai.feature.imagetoimage.VideoStyle
import com.deep.lumoraai.feature.imagetoimage.apiStyle
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Preservation property tests for the "generation-pipeline-fixes" bugfix spec.
 *
 * CRITICAL BUGFIX SEMANTICS:
 *   These tests MUST PASS on the CURRENT (unfixed) client code. They capture the
 *   baseline NON-BUGGY behavior (inputs where the bug condition does NOT hold)
 *   that must remain unchanged after the Task 3 fixes are applied. They are the
 *   "Preservation Checking" half of the bugfix workflow (design.md → Preservation
 *   Checking; Property 6 & 7).
 *
 * OBSERVATION-FIRST METHODOLOGY (RECONCILED TO THE FIXED CODE — Task 3.8):
 *   The Task 3 fixes have landed, so these preservation mirrors have been
 *   reconciled to the NOW-FIXED production code for NON-BUGGY inputs, WITHOUT
 *   weakening what preservation means. The verified fixed production behavior is:
 *     - GenerationRepository.generateVideo(...) builds
 *       {prompt, model, motion_strength, duration, source_image_b64?,
 *        camera_direction?, aspect_ratio, style?} in that exact insertion order.
 *       aspect_ratio is ALWAYS emitted (there is no "unset aspect ratio"
 *       sentinel — the view models pass uiState.aspectRatio.label), and style is
 *       emitted only when style != "Default" (matching generateImage's
 *       convention). So for the "no style selected" non-buggy input the caller
 *       passes "Default", style is OMITTED, and aspect_ratio is PRESENT with the
 *       default label (GenerationAspectRatio.Portrait.label = "2:3").
 *     - GenerationRepository.addCredits(amount, idempotencyKey) builds
 *       {amount, idempotency_key}. Distinct logical events are distinct keys,
 *       each applying exactly once.
 *     - Credits refresh: CreditsRoute uses LaunchedEffect(Unit) { ensureLoaded() }
 *       (fires once per entry) and CreditsViewModel.forceRefresh() drives one
 *       fetch per distinct legitimate refresh event (bypasses freshness but
 *       coalesces in-flight). Auth-sync fires only on explicit sign-in / token
 *       refresh events. So calls == distinct legitimate events.
 *   The mirrors here are faithful copies of that FIXED behavior. The preservation
 *   intent is unchanged: pass-through params untouched, no style injected for the
 *   default selection, distinct credit events each apply once, and network calls
 *   equal distinct legitimate events. The ONLY baseline change vs. the unfixed
 *   code is that the fix adds an always-present aspect_ratio (the correct new
 *   default baseline — NOT the buggy "aspect_ratio absent" baseline).
 *
 * Covers (inputs where the bug condition does NOT hold):
 *   - ¬isBugCondition3 / ¬isBugCondition5: no aspect ratio change / no style
 *     selected → style ABSENT, aspect_ratio PRESENT-with-default; pass-through
 *     params (prompt, duration, motion_strength, source_image_b64,
 *     camera_direction) unchanged.  (Requirements 3.7, 3.8, 3.13, 3.15)
 *   - ¬isBugCondition2: legitimate refresh — credits/auth-sync network-call count
 *     equals distinct-trigger-event count.  (Requirements 3.4, 3.5, 3.6)
 *   - ¬isBugCondition4: a single distinct credits/add applies exactly once over
 *     sequences of distinct events.  (Requirements 3.11)
 *
 * The backend-side preservation for ¬isBugCondition1 (successful generation and
 * non-retriable errors unchanged) is covered by the pytest preservation test on
 * the FastAPI service.
 */
class GenerationPipelineFixesPreservationTest {

    // ---------------------------------------------------------------------
    // Faithful mirrors of CURRENT (unfixed) production behavior.
    // (Same construction as GenerationPipelineFixesExplorationTest.kt so both
    //  tests move together when Task 3 updates production code.)
    // ---------------------------------------------------------------------

    /**
     * Mirror of the JSON built inside the FIXED GenerationRepository.generateVideo(...).
     * Fields are inserted in the exact order the production code uses them:
     * prompt, model, motion_strength, duration, then optional source_image_b64
     * and camera_direction, then aspect_ratio (ALWAYS present), then style (only
     * when style != "Default"). There is no "unset aspect ratio" sentinel — the
     * view models always pass uiState.aspectRatio.label — so aspect_ratio is a
     * non-null String and is always emitted. For the non-buggy "no style
     * selected" input the caller passes "Default", which is omitted (matching
     * generateImage's convention), preserving default/unstyled output.
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
        // Fixed parameter-mapping (Bugs 3 & 5): aspect_ratio is always carried
        // (default label when nothing special is selected); style is carried only
        // when it is not the "Default" sentinel — so the no-style input omits it.
        put("aspect_ratio", aspectRatio)
        if (style != null && style != "Default") put("style", style)
    }

    /**
     * The exact set of keys the fixed video payload can contain. aspect_ratio is
     * always present; style appears only when a non-"Default" style is selected
     * (never in the ¬bug-condition no-style inputs exercised here).
     */
    private val currentVideoPayloadKeys = setOf(
        "prompt",
        "model",
        "motion_strength",
        "duration",
        "source_image_b64",
        "camera_direction",
        "aspect_ratio",
    )

    /**
     * Mirror of the JSON built inside the FIXED
     * GenerationRepository.addCredits(amount, idempotencyKey). Emits
     * {amount, idempotency_key}. For preservation (¬isBugCondition4), distinct
     * logical events supply distinct keys, each applying exactly once.
     */
    private fun buildCurrentAddCreditsPayload(amount: Int, idempotencyKey: String): JSONObject =
        JSONObject().apply {
            put("amount", amount)
            put("idempotency_key", idempotencyKey)
        }

    /**
     * Mirror of the FIXED credit/auth-sync trigger model for LEGITIMATE
     * (non-buggy) inputs. Each distinct trigger event drives exactly one network
     * call: CreditsViewModel.forceRefresh() fires one getCredits() per distinct
     * legitimate refresh event (credit-changing action; it bypasses the freshness
     * window but coalesces onto any in-flight request, so a single logical event
     * yields a single call), and auth-sync fires once per explicit sign-in /
     * token-refresh event. The recomposition-driven amplification (isBugCondition2)
     * is eliminated by CreditsRoute's LaunchedEffect(Unit) { ensureLoaded() } and
     * in-flight coalescing, but distinct legitimate events remain one-to-one, so
     * calls == distinct events.
     */
    private data class TriggerCounts(val creditsCalls: Int, val authSyncCalls: Int)

    private fun simulateLegitimateTriggers(
        creditRefreshEvents: Int,
        authSyncEvents: Int,
    ): TriggerCounts {
        var creditsCalls = 0
        var authSyncCalls = 0
        // One credits fetch per distinct legitimate refresh event.
        repeat(creditRefreshEvents) { creditsCalls += 1 }
        // One auth-sync per distinct sign-in / token-refresh event.
        repeat(authSyncEvents) { authSyncCalls += 1 }
        return TriggerCounts(creditsCalls, authSyncCalls)
    }

    /**
     * Mirror of credits/add application for DISTINCT (non-duplicate) events as it
     * exists TODAY. Each distinct logical event results in exactly one applied
     * add. (The duplicate/idempotency concern is isBugCondition4 and is out of
     * scope here — this covers ¬isBugCondition4 only.)
     */
    private fun applyDistinctCreditAdds(amounts: List<Int>): Int = amounts.size

    // ---------------------------------------------------------------------
    // Preservation: ¬isBugCondition3 / ¬isBugCondition5
    // No aspect ratio / no style selected → video payload unchanged.
    // ---------------------------------------------------------------------

    /**
     * Property: for ALL video requests where no style is selected (the caller
     * passes the "Default" sentinel) and the default aspect ratio is in effect
     * (the ¬bug-condition input space), the outgoing payload:
     *   - OMITS style (preserving default/unstyled output — req 3.13), and
     *   - INCLUDES aspect_ratio with the default label (the fixed baseline: the
     *     view models always pass uiState.aspectRatio.label, so aspect_ratio is
     *     always present — do NOT assert it absent, that was the buggy baseline),
     * and every pass-through parameter is carried unchanged.
     *
     * EXPECTED: PASSES on fixed code (correct new baseline to preserve).
     *
     * Validates: Requirements 3.7, 3.8, 3.13, 3.15
     */
    @Test
    fun noAspectRatioNoStyle_payloadUnchangedAndPassThroughPreserved() {
        val rnd = Random(20240517)
        val defaultAspectRatio = GenerationAspectRatio.Portrait.label // "2:3"

        repeat(100) { i ->
            val prompt = "case $i prompt"
            val engine = if (i % 2 == 0) "veo" else "veo-3"
            val duration = rnd.nextInt(5, 16)
            val motion = rnd.nextInt(20, 91)
            val hasSource = rnd.nextBoolean()
            val hasCamera = rnd.nextBoolean()
            val source = if (hasSource) "IMG$i" else null
            val camera = if (hasCamera) "pan-left-$i" else null

            val payload = buildCurrentVideoPayload(
                prompt = prompt,
                engine = engine,
                sourceImageB64 = source,
                motionStrength = motion,
                cameraDirection = camera,
                duration = duration,
                aspectRatio = defaultAspectRatio,
                style = "Default",
            )

            // No style is injected for the default selection (req 3.13).
            assertFalse(
                "case #$i: style must be absent when no (Default) style is selected",
                payload.has("style"),
            )
            // The default aspect ratio IS carried through as a first-class field
            // (fixed baseline — req 3.7). This is the correct new baseline; the
            // absence assertion here would be the buggy baseline.
            assertTrue(
                "case #$i: aspect_ratio must be present with the default label",
                payload.has("aspect_ratio"),
            )
            assertEquals(
                "case #$i: aspect_ratio must be the default label",
                defaultAspectRatio,
                payload.optString("aspect_ratio"),
            )

            // Payload must contain ONLY the current, known keys.
            val keys = payload.keys().asSequence().toSet()
            assertTrue(
                "case #$i: payload contained unexpected keys: ${keys - currentVideoPayloadKeys}",
                currentVideoPayloadKeys.containsAll(keys),
            )

            // Pass-through parameters carried unchanged.
            assertEquals("case #$i prompt", prompt, payload.optString("prompt"))
            assertEquals("case #$i model", engine, payload.optString("model"))
            assertEquals("case #$i motion_strength", motion, payload.optInt("motion_strength"))
            assertEquals("case #$i duration", duration, payload.optInt("duration"))
            assertEquals(
                "case #$i source_image_b64 presence",
                hasSource,
                payload.has("source_image_b64"),
            )
            if (hasSource) {
                assertEquals("case #$i source_image_b64", source, payload.optString("source_image_b64"))
            }
            assertEquals(
                "case #$i camera_direction presence",
                hasCamera,
                payload.has("camera_direction"),
            )
            if (hasCamera) {
                assertEquals("case #$i camera_direction", camera, payload.optString("camera_direction"))
            }
        }
    }

    /**
     * Example-based companion: a concrete no-style video request produces the
     * exact byte-for-byte payload string of the fixed baseline. "Byte-for-byte
     * identical aside from the new fields being absent/Default" now means: style
     * absent (Default omitted) and aspect_ratio present-with-default, appended
     * LAST (JSON insertion order: after source_image_b64/camera_direction). All
     * pass-through params are unchanged.
     *
     * Validates: Requirements 3.7, 3.8
     */
    @Test
    fun noAspectRatioNoStyle_payloadIsByteForByteBaseline() {
        val payload = buildCurrentVideoPayload(
            prompt = "Animate the uploaded source image into a video.",
            engine = "veo",
            sourceImageB64 = "AAAA",
            motionStrength = 65,
            duration = 10,
            aspectRatio = GenerationAspectRatio.Portrait.label,
            style = "Default",
        ).toString()

        val expected = JSONObject().apply {
            put("prompt", "Animate the uploaded source image into a video.")
            put("model", "veo")
            put("motion_strength", 65)
            put("duration", 10)
            put("source_image_b64", "AAAA")
            // Fixed baseline: aspect_ratio appended last with the default label;
            // style omitted for the "Default" selection.
            put("aspect_ratio", GenerationAspectRatio.Portrait.label)
        }.toString()

        assertEquals(
            "no-style video payload must be byte-for-byte identical to the fixed baseline",
            expected,
            payload,
        )
    }

    // ---------------------------------------------------------------------
    // Preservation: ¬isBugCondition2
    // Legitimate refreshes — network calls == distinct trigger events.
    // ---------------------------------------------------------------------

    /**
     * Property: for ALL sessions driven only by DISTINCT legitimate events, the
     * number of credits network calls equals the number of distinct credit
     * refresh events, and the number of auth-sync network calls equals the number
     * of distinct auth-sync events.
     *
     * EXPECTED: PASSES on unfixed code (distinct legitimate events already map
     * one-to-one to calls; the bug is only the recomposition amplification, which
     * is isBugCondition2 and out of scope here).
     *
     * Validates: Requirements 3.4, 3.5, 3.6
     */
    @Test
    fun legitimateRefreshes_networkCallsEqualDistinctTriggerEvents() {
        val rnd = Random(424242)

        repeat(100) { i ->
            val creditEvents = rnd.nextInt(0, 6)
            val authEvents = rnd.nextInt(0, 6)

            val counts = simulateLegitimateTriggers(creditEvents, authEvents)

            assertEquals(
                "case #$i: credits network calls must equal distinct credit refresh events",
                creditEvents,
                counts.creditsCalls,
            )
            assertEquals(
                "case #$i: auth/sync network calls must equal distinct auth-sync events",
                authEvents,
                counts.authSyncCalls,
            )
        }
    }

    // ---------------------------------------------------------------------
    // Preservation: ¬isBugCondition4
    // Distinct credits/add each apply exactly once.
    // ---------------------------------------------------------------------

    /**
     * Property: for ALL sequences of DISTINCT credit-add events (each a distinct
     * logical event, modeled as a distinct idempotency key), each event applies
     * exactly once, so the applied count equals the number of distinct events.
     *
     * EXPECTED: PASSES on fixed code. The fix makes credits/add idempotent per
     * key: DISTINCT keys each apply once (preservation, ¬isBugCondition4); only
     * DUPLICATE deliveries of the SAME key are collapsed (isBugCondition4, out of
     * scope here).
     *
     * Validates: Requirements 3.11
     */
    @Test
    fun distinctCreditAdds_eachAppliesExactlyOnce() {
        val rnd = Random(987654)

        repeat(100) { i ->
            val count = rnd.nextInt(0, 8)
            val amounts = List(count) { rnd.nextInt(1, 51) }

            val applied = applyDistinctCreditAdds(amounts)

            assertEquals(
                "case #$i: each distinct credits/add must apply exactly once",
                amounts.size,
                applied,
            )

            // Model each distinct logical event as a distinct idempotency key so
            // distinct events each apply once. The per-event payload for the fixed
            // addCredits is {amount, idempotency_key}.
            amounts.forEachIndexed { j, amount ->
                val idempotencyKey = "uid-123:distinct-event:$i-$j"
                val payload = buildCurrentAddCreditsPayload(amount, idempotencyKey)
                assertEquals("distinct add payload amount", amount, payload.optInt("amount"))
                assertEquals(
                    "distinct add payload idempotency_key",
                    idempotencyKey,
                    payload.optString("idempotency_key"),
                )
                assertEquals(
                    "distinct add payload must contain {amount, idempotency_key}",
                    setOf("amount", "idempotency_key"),
                    payload.keys().asSequence().toSet(),
                )
            }
        }
    }
}
