package com.deep.lumoraai.ads

import org.json.JSONObject
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the live [AdsConfig] snapshot.
 *
 * Starts from mandatory local defaults so the app is fully functional with no
 * network/Firebase. A production Remote Config layer can call [applyRemoteJson]
 * with the fetched JSON; parsing is fully defensive so malformed/partial/absent
 * values simply keep the previous (or default) value — never a crash.
 *
 * This is the ONLY place that knows the Remote Config JSON shape, keeping the
 * rest of the ads layer decoupled from Firebase.
 */
@Singleton
class AdsConfigStore @Inject constructor() {

    private val ref = AtomicReference(AdsConfig.DEFAULT)

    val current: AdsConfig get() = ref.get()

    /**
     * Apply a Remote Config JSON payload on top of the current config. Any field
     * that is missing or malformed keeps its existing value. Never throws.
     */
    fun applyRemoteJson(json: String) {
        val parsed = runCatching { JSONObject(json) }.getOrNull() ?: run {
            AdsLogger.w("AdsConfig: remote JSON unparseable, keeping current config")
            return
        }
        val base = ref.get()
        val updated = base.copy(
            adsEnabled = parsed.optBoolOr("ads_enabled", base.adsEnabled),
            bannerEnabled = parsed.optBoolOr("ads_banner_enabled", base.bannerEnabled),
            nativeEnabled = parsed.optBoolOr("ads_native_enabled", base.nativeEnabled),
            interstitialEnabled = parsed.optBoolOr("ads_interstitial_enabled", base.interstitialEnabled),
            rewardedEnabled = parsed.optBoolOr("ads_rewarded_enabled", base.rewardedEnabled),
            appOpenEnabled = parsed.optBoolOr("ads_app_open_enabled", base.appOpenEnabled),
            testMode = parsed.optBoolOr("ads_test_mode", base.testMode),
            placementEnabled = parsePlacementEnabled(parsed, base.placementEnabled),
            interstitialTriggerCount = parsed.optIntIn("inter_trigger_count", 1, 50, base.interstitialTriggerCount),
            interstitialPlacementCooldownMs = parsed.optLongIn("inter_cooldown_ms", 0, 600_000, base.interstitialPlacementCooldownMs),
            globalFullScreenCooldownMs = parsed.optLongIn("fullscreen_cooldown_ms", 0, 600_000, base.globalFullScreenCooldownMs),
            maxInterstitialsPerSession = parsed.optIntIn("max_inter_per_session", 0, 200, base.maxInterstitialsPerSession),
            appOpenMinIntervalMs = parsed.optLongIn("app_open_min_interval_ms", 0, 3_600_000, base.appOpenMinIntervalMs),
            splashMaxWaitMs = parsed.optLongIn("splash_max_wait_ms", 0, 20_000, base.splashMaxWaitMs),
            fullScreenLoadTimeoutMs = parsed.optLongIn("fullscreen_load_timeout_ms", 1_000, 30_000, base.fullScreenLoadTimeoutMs),
            rewardCreditsAmount = parsed.optIntIn("reward_credits_amount", 1, 1_000, base.rewardCreditsAmount),
            rewardMaxClaimsPerDay = parsed.optIntIn("reward_max_claims_per_day", 0, 100, base.rewardMaxClaimsPerDay),
            nativeTemplateInterval = parsed.optIntIn("native_template_interval", 2, 50, base.nativeTemplateInterval),
            nativeHistoryInterval = parsed.optIntIn("native_history_interval", 2, 50, base.nativeHistoryInterval),
            nativeStyle = parseNativeStyle(parsed, base.nativeStyle),
            prodBannerUnitId = parsed.optStringOrNull("ads_banner_unit_id") ?: base.prodBannerUnitId,
            prodNativeUnitId = parsed.optStringOrNull("ads_native_unit_id") ?: base.prodNativeUnitId,
            prodInterstitialUnitId = parsed.optStringOrNull("ads_interstitial_unit_id") ?: base.prodInterstitialUnitId,
            prodRewardedUnitId = parsed.optStringOrNull("ads_rewarded_unit_id") ?: base.prodRewardedUnitId,
            prodAppOpenUnitId = parsed.optStringOrNull("ads_app_open_unit_id") ?: base.prodAppOpenUnitId,
        )
        ref.set(updated)
        AdsLogger.d("AdsConfig updated from remote (testMode=${updated.testMode}, adsEnabled=${updated.adsEnabled})")
    }

    private fun parsePlacementEnabled(json: JSONObject, current: Map<String, Boolean>): Map<String, Boolean> {
        val obj = json.optJSONObject("placement_enabled") ?: return current
        val result = current.toMutableMap()
        AdPlacement.entries.forEach { placement ->
            if (obj.has(placement.key)) {
                result[placement.key] = obj.optBoolean(placement.key, true)
            }
        }
        return result
    }

    private fun parseNativeStyle(json: JSONObject, base: NativeStyleConfig): NativeStyleConfig {
        val s = json.optJSONObject("native_style") ?: return base
        return base.copy(
            backgroundColor = s.optColor("ads_native_style_background_color", base.backgroundColor),
            titleColor = s.optColor("ads_native_style_title_color", base.titleColor),
            bodyColor = s.optColor("ads_native_style_body_color", base.bodyColor),
            ctaBackgroundColor = s.optColor("ads_native_style_cta_background_color", base.ctaBackgroundColor),
            ctaTextColor = s.optColor("ads_native_style_cta_text_color", base.ctaTextColor),
            cornerRadiusDp = s.optIntIn("ads_native_style_corner_radius_dp", 0, 48, base.cornerRadiusDp),
            paddingDp = s.optIntIn("ads_native_style_padding_dp", 0, 48, base.paddingDp),
            titleSp = s.optIntIn("ads_native_style_title_sp", 8, 40, base.titleSp),
            bodySp = s.optIntIn("ads_native_style_body_sp", 8, 40, base.bodySp),
            shimmerEnabled = s.optBoolOr("ads_native_style_shimmer_enabled", base.shimmerEnabled),
        )
    }
}

// ---- Defensive JSON helpers (bounded, type-tolerant, never throw) ----

private fun JSONObject.optBoolOr(key: String, fallback: Boolean): Boolean =
    if (has(key)) optBoolean(key, fallback) else fallback

private fun JSONObject.optStringOrNull(key: String): String? =
    optString(key, "").takeIf { it.isNotBlank() }

private fun JSONObject.optIntIn(key: String, min: Int, max: Int, fallback: Int): Int {
    if (!has(key)) return fallback
    val v = optInt(key, fallback)
    return v.coerceIn(min, max)
}

private fun JSONObject.optLongIn(key: String, min: Long, max: Long, fallback: Long): Long {
    if (!has(key)) return fallback
    val v = optLong(key, fallback)
    return v.coerceIn(min, max)
}

/** Parse a "#RRGGBB" / "#AARRGGBB" color string into an ARGB long. */
private fun JSONObject.optColor(key: String, fallback: Long): Long {
    val raw = optStringOrNull(key) ?: return fallback
    return runCatching {
        val hex = raw.removePrefix("#")
        val value = hex.toLong(16)
        if (hex.length <= 6) 0xFF000000L or value else value
    }.getOrDefault(fallback)
}
