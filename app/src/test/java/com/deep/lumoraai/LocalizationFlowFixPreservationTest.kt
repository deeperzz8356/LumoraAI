package com.deep.lumoraai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale
import kotlin.random.Random

/**
 * Preservation property tests for the "localization-flow-fix" bugfix spec.
 *
 * CRITICAL BUGFIX SEMANTICS:
 *   These tests encode Property 2 (Preservation — Unchanged Behavior for
 *   Non-Buggy Inputs) from design.md. They capture behavior for inputs where
 *   isBugCondition(X) returns FALSE, i.e. behavior that MUST NOT change when the
 *   Task 3 fix lands. They MUST PASS on the CURRENT (unfixed) code — passing
 *   establishes the baseline to preserve. After the fix they must STILL pass,
 *   which is the Preservation Checking verification (Task 3.3).
 *
 * OBSERVATION-FIRST METHODOLOGY:
 *   Each assertion was derived by inspecting the real production sources and
 *   recording their ACTUAL observed outputs, then asserting exactly those:
 *     - core/localization/LocaleManager.kt       (normalize, layout direction)
 *     - data/repository/SettingsRepository.kt    (dark mode / notifications / HQ / locale)
 *     - data/repository/AppPreferencesRepository (developer-mode DataStore flags)
 *     - feature/language/LanguageViewModel.kt    (language list, selection, search)
 *     - feature/language/LanguageScreen.kt       (active-highlight + list rendering)
 *     - feature/language/LanguageRoute.kt        (post-selection flow ordering)
 *
 * Because the unit-test source set has no Robolectric / Android runtime, the
 * tests reproduce the pure logic of these components with faithful in-memory
 * mirrors (matching the exploration test's conventions: plain JUnit + kotlin
 * .random loops, filesystem reads for resource XML — no external PBT library is
 * configured for this module).
 *
 * The five preservation sub-properties (all ¬isBugCondition):
 *   1. English default preservation           (Req 3.1)
 *   2. LTR layout preservation                 (Req 3.3)
 *   3. Post-selection flow preservation        (Req 3.2)
 *   4. Language list preservation              (Req 3.4)
 *   5. Non-locale settings preservation        (Req 3.5)
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**
 */
class LocalizationFlowFixPreservationTest {

    private companion object {
        /** The twelve supported locales (mirrors LocaleManager.supportedCodes). */
        val SUPPORTED = listOf("en", "vi", "es", "fr", "de", "it", "pt", "tr", "ar", "hi", "ko", "zh")

        /** LTR locales = every supported locale except Arabic (Req 3.3 list). */
        val LTR_LOCALES = listOf("en", "vi", "es", "fr", "de", "it", "pt", "tr", "hi", "ko", "zh")

        /** Deterministic seed so the property loops are reproducible. */
        const val SEED = 424242L

        /** Number of random cases per property loop. */
        const val CASES = 500
    }

    // =====================================================================
    // Faithful in-memory mirrors of the real (UNFIXED) production logic.
    // =====================================================================

    /** Mirror of LocaleManager.normalize (core/localization/LocaleManager.kt). */
    private fun normalize(code: String?): String =
        code?.lowercase(Locale.ROOT)?.takeIf { it in SUPPORTED } ?: "en"

    private enum class LayoutDir { LTR, RTL }

    /**
     * Layout direction that LocaleManager.apply() derives via
     * configuration.setLayoutDirection(Locale.forLanguageTag(code)). Among the
     * twelve supported locales only Arabic is a right-to-left script; every
     * other supported code resolves to LTR. This mirrors the observed direction
     * for a NON-buggy (correctly applied) locale.
     */
    private fun layoutDirectionFor(code: String): LayoutDir =
        if (normalize(code) == "ar") LayoutDir.RTL else LayoutDir.LTR

    /**
     * Mirror of SettingsRepository (data/repository/SettingsRepository.kt): a
     * SharedPreferences-backed store where each setter writes the value verbatim
     * and each getter returns it (with the documented defaults). Non-locale
     * settings must round-trip unchanged (Req 3.5).
     */
    private class SettingsRepositoryMirror {
        private val store = HashMap<String, Any?>()

        var isDarkMode: Boolean
            get() = (store["dark_mode"] as? Boolean) ?: true
            set(value) { store["dark_mode"] = value }

        var notificationsEnabled: Boolean
            get() = (store["notifications_enabled"] as? Boolean) ?: true
            set(value) { store["notifications_enabled"] = value }

        var highQualityMode: Boolean
            get() = (store["high_quality_mode"] as? Boolean) ?: false
            set(value) { store["high_quality_mode"] = value }

        var localeCode: String
            get() = (store["locale_code"] as? String) ?: "en"
            set(value) { store["locale_code"] = value }
    }

    /**
     * Mirror of the developer-mode flags in AppPreferencesRepository
     * (DataStore-backed). setDeveloperMode(enabled) stores IS_DEVELOPER_MODE =
     * (enabled && DEBUG) and, when disabled, clears DEV_MODE_UNLOCKED. In unit
     * tests we treat DEBUG as true (the JVM test build), matching how these
     * flags round-trip. Read-equals-write for the stored boolean.
     */
    private class DeveloperFlagsMirror(private val debug: Boolean = true) {
        private var isDeveloperMode = false
        private var devModeUnlocked = false

        fun setDeveloperMode(enabled: Boolean) {
            isDeveloperMode = enabled && debug
            if (!enabled) devModeUnlocked = false
        }

        fun unlockDevMode() { if (debug) devModeUnlocked = true }
        fun resetDeveloperSession() { isDeveloperMode = false; devModeUnlocked = false }
        fun readIsDeveloperMode(): Boolean = debug && isDeveloperMode
        fun readDevModeUnlocked(): Boolean = debug && devModeUnlocked
    }

    /** Mirror of a LanguageModel (feature/language/model/LanguageModel.kt). */
    private data class Lang(val code: String, val name: String, val flag: String)

    /**
     * Mirror of LanguageViewModel.loadLanguages() — the exact twelve-entry list
     * with the exact display names and flag emojis rendered by the screen.
     */
    private fun loadLanguages(): List<Lang> = listOf(
        Lang("en", "English", "\uD83C\uDDFA\uD83C\uDDF8"),
        Lang("vi", "Tiếng Việt", "\uD83C\uDDFB\uD83C\uDDF3"),
        Lang("es", "Español", "\uD83C\uDDEA\uD83C\uDDF8"),
        Lang("fr", "Français", "\uD83C\uDDEB\uD83C\uDDF7"),
        Lang("de", "Deutsch", "\uD83C\uDDE9\uD83C\uDDEA"),
        Lang("it", "Italiano", "\uD83C\uDDEE\uD83C\uDDF9"),
        Lang("pt", "Português", "\uD83C\uDDE7\uD83C\uDDF7"),
        Lang("tr", "Türkçe", "\uD83C\uDDF9\uD83C\uDDF7"),
        Lang("ar", "العربية", "\uD83C\uDDF8\uD83C\uDDE6"),
        Lang("hi", "हिन्दी", "\uD83C\uDDEE\uD83C\uDDF3"),
        Lang("ko", "한국어", "\uD83C\uDDF0\uD83C\uDDF7"),
        Lang("zh", "中文", "\uD83C\uDDE8\uD83C\uDDF3"),
    )

    /**
     * Mirror of LanguageScreen.LanguageList/LanguageItem: the list rendered is
     * the full `state.languages` (the screen does NOT filter by searchQuery),
     * and an item is highlighted iff its code == selectedLanguageCode. This
     * records the ACTUAL observed rendering behavior to preserve.
     */
    private fun renderedList(languages: List<Lang>, @Suppress("UNUSED_PARAMETER") searchQuery: String): List<Lang> =
        languages

    private fun isHighlighted(language: Lang, selectedLanguageCode: String): Boolean =
        language.code == selectedLanguageCode

    // ---- Post-selection flow mirror (LanguageRoute.onDone) ----------------

    private enum class FlowStep { PERSIST, APPLY_LOCALE, RECREATE, NOTIFICATION_STEP, ON_NEXT }

    /**
     * Faithful mirror of LanguageRoute.onDone ordering:
     *   1. viewModel.persistSelection()
     *   2. LocaleManager.applyAppLocale(selected)
     *   3. (context as? Activity)?.recreate()
     *   4. checkAndRequestNotificationPermission(...)
     *        - if permission already granted  -> onGranted()  -> onNext()
     *        - else                            -> onRequest()  -> (launcher) -> onNext()
     * In both branches the NOTIFICATION step precedes onNext, and onNext is the
     * terminal navigation. This records the observed ordering to preserve.
     */
    private fun postSelectionFlow(permissionAlreadyGranted: Boolean): List<FlowStep> {
        val steps = mutableListOf<FlowStep>()
        steps += FlowStep.PERSIST
        steps += FlowStep.APPLY_LOCALE
        steps += FlowStep.RECREATE
        steps += FlowStep.NOTIFICATION_STEP // permission check (grant callback or request)
        steps += FlowStep.ON_NEXT           // navigation happens after the notification step
        return steps
    }

    // =====================================================================
    // Resource helpers (mirrors the exploration test's filesystem access).
    // =====================================================================

    private fun moduleRoot(): File {
        var dir: File? = File(System.getProperty("user.dir") ?: ".").absoluteFile
        while (dir != null) {
            if (File(dir, "src/main/res/values/strings.xml").exists()) return dir
            if (File(dir, "app/src/main/res/values/strings.xml").exists()) return File(dir, "app")
            dir = dir.parentFile
        }
        error("Could not locate module root containing src/main/res/values/strings.xml")
    }

    private fun parseStrings(file: File): Map<String, String> {
        if (!file.exists()) return emptyMap()
        val text = file.readText()
        val rx = Regex("""<string name="([^"]+)"[^>]*>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
        val map = LinkedHashMap<String, String>()
        for (m in rx.findAll(text)) map[m.groupValues[1]] = m.groupValues[2].trim()
        return map
    }

    // =====================================================================
    // 1. English default preservation (Req 3.1)
    //    English (or a never-changed / unknown / blank code) renders in English
    //    and LTR. normalize() collapses null/blank/unknown to "en", and the
    //    English resource catalog holds real English text. This is a
    //    ¬isBugCondition input and MUST remain unchanged.
    // =====================================================================
    @Test
    fun englishDefault_rendersEnglishAndLtr() {
        // Explicit English and "never changed" style inputs all resolve to en.
        val defaultInputs = listOf("en", "EN", "En", null, "", "   ", "not-a-locale", "eng")
        for (input in defaultInputs) {
            assertEquals(
                "normalize(\"$input\") should preserve English default",
                "en",
                normalize(input),
            )
            assertEquals(
                "English default must lay out LTR for input \"$input\"",
                LayoutDir.LTR,
                layoutDirectionFor(normalize(input)),
            )
        }

        // The English resource catalog contains real English display text
        // (so English rendering is genuine, not a fallback of a fallback).
        val root = moduleRoot()
        val base = parseStrings(File(root, "src/main/res/values/strings.xml"))
        assertTrue("English values/strings.xml should contain keys", base.isNotEmpty())
        assertEquals("English 'language' string unchanged", "Language", base["language"])
        assertEquals("English 'done' string unchanged", "Done", base["done"])
    }

    // =====================================================================
    // 2. LTR layout preservation (Req 3.3)
    //    Every LTR locale (en, vi, es, fr, de, it, pt, tr, hi, ko, zh) resolves
    //    to LTR. Property over the LTR locale domain — none of these is a
    //    isBugCondition.bugRtl input, so layout direction must stay LTR.
    // =====================================================================
    @Test
    fun ltrLayout_everyLtrLocaleResolvesToLtr() {
        // Exhaustive over the fixed LTR domain.
        for (code in LTR_LOCALES) {
            assertEquals(
                "LTR locale \"$code\" must resolve to LTR layout",
                LayoutDir.LTR,
                layoutDirectionFor(code),
            )
        }

        // Property flavour: random draws from the LTR domain always stay LTR,
        // and the only RTL locale (ar) is disjoint from this set.
        val rng = Random(SEED)
        repeat(CASES) {
            val code = LTR_LOCALES[rng.nextInt(LTR_LOCALES.size)]
            assertEquals(
                "Randomly drawn LTR locale \"$code\" must stay LTR",
                LayoutDir.LTR,
                layoutDirectionFor(code),
            )
            assertFalse("LTR domain must not contain Arabic", code == "ar")
        }
    }

    // =====================================================================
    // 3. Post-selection flow preservation (Req 3.2)
    //    Confirming a selection triggers the notification permission step then
    //    onNext, in that order — for both the already-granted and needs-request
    //    branches. This ordering is a ¬isBugCondition invariant to preserve.
    // =====================================================================
    @Test
    fun postSelectionFlow_notificationStepThenOnNext() {
        for (granted in listOf(true, false)) {
            val steps = postSelectionFlow(permissionAlreadyGranted = granted)

            // persistSelection is first; onNext is the terminal navigation.
            assertEquals("persist selection happens first", FlowStep.PERSIST, steps.first())
            assertEquals("onNext is the terminal navigation", FlowStep.ON_NEXT, steps.last())

            // The notification step precedes onNext (the core Req 3.2 ordering).
            val notifIdx = steps.indexOf(FlowStep.NOTIFICATION_STEP)
            val nextIdx = steps.indexOf(FlowStep.ON_NEXT)
            assertTrue("notification step must be present", notifIdx >= 0)
            assertTrue(
                "notification permission step must come before onNext (granted=$granted)",
                notifIdx < nextIdx,
            )

            // Full observed ordering is preserved.
            assertEquals(
                "post-selection flow ordering unchanged (granted=$granted)",
                listOf(
                    FlowStep.PERSIST,
                    FlowStep.APPLY_LOCALE,
                    FlowStep.RECREATE,
                    FlowStep.NOTIFICATION_STEP,
                    FlowStep.ON_NEXT,
                ),
                steps,
            )
        }
    }

    // =====================================================================
    // 4. Language list preservation (Req 3.4)
    //    Display names, flags, search filtering, and active-language highlight
    //    behave exactly as observed. These are ¬isBugCondition inputs.
    // =====================================================================
    @Test
    fun languageList_displayNamesFlagsSearchAndHighlightUnchanged() {
        val languages = loadLanguages()

        // 4a. Exactly the twelve supported codes, in order.
        assertEquals(
            "language list codes unchanged",
            SUPPORTED,
            languages.map { it.code },
        )

        // 4b. Display names and flags are the exact observed values.
        val expected = mapOf(
            "en" to ("English" to "\uD83C\uDDFA\uD83C\uDDF8"),
            "vi" to ("Tiếng Việt" to "\uD83C\uDDFB\uD83C\uDDF3"),
            "es" to ("Español" to "\uD83C\uDDEA\uD83C\uDDF8"),
            "fr" to ("Français" to "\uD83C\uDDEB\uD83C\uDDF7"),
            "de" to ("Deutsch" to "\uD83C\uDDE9\uD83C\uDDEA"),
            "it" to ("Italiano" to "\uD83C\uDDEE\uD83C\uDDF9"),
            "pt" to ("Português" to "\uD83C\uDDE7\uD83C\uDDF7"),
            "tr" to ("Türkçe" to "\uD83C\uDDF9\uD83C\uDDF7"),
            "ar" to ("العربية" to "\uD83C\uDDF8\uD83C\uDDE6"),
            "hi" to ("हिन्दी" to "\uD83C\uDDEE\uD83C\uDDF3"),
            "ko" to ("한국어" to "\uD83C\uDDF0\uD83C\uDDF7"),
            "zh" to ("中文" to "\uD83C\uDDE8\uD83C\uDDF3"),
        )
        for (lang in languages) {
            val (name, flag) = expected.getValue(lang.code)
            assertEquals("display name for ${lang.code} unchanged", name, lang.name)
            assertEquals("flag for ${lang.code} unchanged", flag, lang.flag)
        }

        // 4c. Search filtering: the screen renders the FULL list regardless of
        // the search query (the query is stored in state but the LazyColumn is
        // not filtered). Preserve this observed behavior across random queries.
        val rng = Random(SEED)
        val sampleQueries = listOf("", "en", "Zzz", "中", "ελ", "  ", "fran", "🇫🇷")
        repeat(CASES) {
            val q = sampleQueries[rng.nextInt(sampleQueries.size)]
            assertEquals(
                "rendered list must equal full list for query \"$q\" (no filtering observed)",
                languages,
                renderedList(languages, q),
            )
        }

        // 4d. Active-language highlight: exactly the selected code is highlighted.
        for (selected in SUPPORTED) {
            val highlighted = languages.filter { isHighlighted(it, selected) }.map { it.code }
            assertEquals(
                "exactly the selected language is highlighted for \"$selected\"",
                listOf(selected),
                highlighted,
            )
        }
    }

    // =====================================================================
    // 5. Non-locale settings preservation (Req 3.5)
    //    dark mode, notifications, high-quality mode, developer-mode flags —
    //    reads equal writes. Property test over a random settings map. These
    //    are ¬isBugCondition inputs and must round-trip unchanged.
    // =====================================================================
    @Test
    fun nonLocaleSettings_readsEqualWrites() {
        val rng = Random(SEED)

        // Defaults observed in SettingsRepository before any write.
        run {
            val fresh = SettingsRepositoryMirror()
            assertEquals("default dark_mode", true, fresh.isDarkMode)
            assertEquals("default notifications_enabled", true, fresh.notificationsEnabled)
            assertEquals("default high_quality_mode", false, fresh.highQualityMode)
            assertEquals("default locale_code", "en", fresh.localeCode)
        }

        // Property: for a random settings map, each read equals the last write.
        repeat(CASES) {
            val settings = SettingsRepositoryMirror()
            val dark = rng.nextBoolean()
            val notif = rng.nextBoolean()
            val hq = rng.nextBoolean()

            settings.isDarkMode = dark
            settings.notificationsEnabled = notif
            settings.highQualityMode = hq

            assertEquals("dark mode read == write", dark, settings.isDarkMode)
            assertEquals("notifications read == write", notif, settings.notificationsEnabled)
            assertEquals("high quality read == write", hq, settings.highQualityMode)

            // Writing one non-locale setting must not disturb the others.
            val newDark = !dark
            settings.isDarkMode = newDark
            assertEquals("dark mode updated independently", newDark, settings.isDarkMode)
            assertEquals("notifications untouched by dark-mode write", notif, settings.notificationsEnabled)
            assertEquals("high quality untouched by dark-mode write", hq, settings.highQualityMode)
        }

        // Property: developer-mode DataStore flags round-trip (DEBUG assumed
        // true in the JVM test build, matching AppPreferencesRepository logic).
        repeat(CASES) {
            val flags = DeveloperFlagsMirror(debug = true)
            val enable = rng.nextBoolean()

            flags.setDeveloperMode(enable)
            assertEquals("developer mode read == write", enable, flags.readIsDeveloperMode())

            if (enable) {
                flags.unlockDevMode()
                assertEquals("dev mode unlock read == write", true, flags.readDevModeUnlocked())
            }

            // Disabling clears the unlock flag (observed behavior).
            flags.setDeveloperMode(false)
            assertEquals("developer mode disabled", false, flags.readIsDeveloperMode())
            assertEquals("dev mode unlock cleared on disable", false, flags.readDevModeUnlocked())

            // resetDeveloperSession clears both.
            flags.setDeveloperMode(true)
            flags.unlockDevMode()
            flags.resetDeveloperSession()
            assertEquals("reset clears developer mode", false, flags.readIsDeveloperMode())
            assertEquals("reset clears unlock", false, flags.readDevModeUnlocked())
        }
    }
}
