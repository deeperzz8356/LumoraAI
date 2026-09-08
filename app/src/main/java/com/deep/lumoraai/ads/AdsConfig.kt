package com.deep.lumoraai.ads

import com.deep.lumoraai.BuildConfig

/** The five AdMob ad formats supported by the app. */
enum class AdFormat { BANNER, NATIVE, INTERSTITIAL, REWARDED, APP_OPEN }

/**
 * Remote-Config-compatible advertising configuration with mandatory local
 * defaults. Every value here can later be overridden by Firebase Remote Config
 * via [AdsConfigStore] parsing; nothing in the ads layer reads raw ID strings
 * or Remote Config directly — they all read this snapshot.
 *
 * This is a pure immutable data holder (configuration only — never secrets).
 */
data class AdsConfig(
    // ---- Global + format toggles ----
    val adsEnabled: Boolean = false,
    val bannerEnabled: Boolean = true,
    val nativeEnabled: Boolean = true,
    val interstitialEnabled: Boolean = true,
    val rewardedEnabled: Boolean = true,
    val appOpenEnabled: Boolean = true,

    /** When true, Google's official test ad units always take precedence. */
    val testMode: Boolean = true,

    // ---- Per-placement enable map (key -> enabled). Missing key = enabled. ----
    val placementEnabled: Map<String, Boolean> = emptyMap(),

    // ---- Frequency / cooldown ----
    val interstitialTriggerCount: Int = 3,
    val interstitialPlacementCooldownMs: Long = 30_000L,
    val globalFullScreenCooldownMs: Long = 30_000L,
    val maxInterstitialsPerSession: Int = 20,
    val appOpenMinIntervalMs: Long = 60_000L,
    val appOpenMaxCacheMs: Long = 4 * 60 * 60 * 1000L,
    val splashMaxWaitMs: Long = 6_000L,
    val fullScreenLoadTimeoutMs: Long = 8_000L,

    // ---- Rewarded ----
    val rewardCreditsAmount: Int = 2,
    val rewardMaxClaimsPerDay: Int = 3,

    // ---- List insertion intervals for native-in-list placements ----
    val nativeTemplateInterval: Int = 3,
    val nativeHistoryInterval: Int = 8,

    // ---- Native styling (validated; malformed values fall back to defaults) ----
    val nativeStyle: NativeStyleConfig = NativeStyleConfig(),

    // ---- Production unit IDs (used only when testMode == false) ----
    val prodBannerUnitId: String? = null,
    val prodNativeUnitId: String? = null,
    val prodInterstitialUnitId: String? = null,
    val prodRewardedUnitId: String? = null,
    val prodAppOpenUnitId: String? = null,
) {
    fun formatEnabled(format: AdFormat): Boolean = adsEnabled && when (format) {
        AdFormat.BANNER -> bannerEnabled
        AdFormat.NATIVE -> nativeEnabled
        AdFormat.INTERSTITIAL -> interstitialEnabled
        AdFormat.REWARDED -> rewardedEnabled
        AdFormat.APP_OPEN -> appOpenEnabled
    }

    fun isPlacementEnabled(placement: AdPlacement): Boolean =
        placementEnabled[placement.key] ?: true

    /** Resolve the effective ad unit ID, honouring test mode precedence. */
    fun unitIdFor(format: AdFormat): String {
        if (testMode) return TestIds.of(format)
        val prod = when (format) {
            AdFormat.BANNER -> prodBannerUnitId
            AdFormat.NATIVE -> prodNativeUnitId
            AdFormat.INTERSTITIAL -> prodInterstitialUnitId
            AdFormat.REWARDED -> prodRewardedUnitId
            AdFormat.APP_OPEN -> prodAppOpenUnitId
        }
        // No production ID configured yet -> stay safe on the test unit.
        return prod?.takeIf { it.isNotBlank() } ?: TestIds.of(format)
    }

    companion object {
        /** Google's official sample App ID (also declared in the manifest). */
        const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"

        /** The immutable local default used before Remote Config resolves. */
        val DEFAULT = AdsConfig(testMode = BuildConfig.DEBUG || true)
    }
}

/** Google's official Android test ad unit IDs, centralized in one place. */
object TestIds {
    const val BANNER = "ca-app-pub-3940256099942544/9214589741"
    const val NATIVE = "ca-app-pub-3940256099942544/2247696110"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
    const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"

    fun of(format: AdFormat): String = when (format) {
        AdFormat.BANNER -> BANNER
        AdFormat.NATIVE -> NATIVE
        AdFormat.INTERSTITIAL -> INTERSTITIAL
        AdFormat.REWARDED -> REWARDED
        AdFormat.APP_OPEN -> APP_OPEN
    }
}

/**
 * App-owned native container styling. All values validated on parse; malformed
 * remote values are replaced with these defaults so rendering never crashes.
 */
data class NativeStyleConfig(
    val backgroundColor: Long = 0xFF0E172A,
    val titleColor: Long = 0xFFFFFFFF,
    val bodyColor: Long = 0xFF94A0B8,
    val ctaBackgroundColor: Long = 0xFFD6FF2F,
    val ctaTextColor: Long = 0xFF081020,
    val cornerRadiusDp: Int = 14,
    val paddingDp: Int = 14,
    val titleSp: Int = 15,
    val bodySp: Int = 12,
    val shimmerEnabled: Boolean = true,
)
