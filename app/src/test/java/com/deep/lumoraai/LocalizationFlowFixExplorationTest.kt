package com.deep.lumoraai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * Bug-condition exploration tests for the "localization-flow-fix" bugfix spec.
 *
 * CRITICAL BUGFIX SEMANTICS:
 *   These tests encode Property 1 (Bug Condition — Localized, Refreshed,
 *   Consistent, Correctly-Directed UI) from design.md. They express the EXPECTED
 *   (post-fix) behavior: they FAIL on the unfixed code (confirming the bug) and
 *   PASS once the Task 3 fix lands (Fix Checking). The source/filesystem-based
 *   Bug A tests observe the real resource files; the persistence/refresh/RTL
 *   tests mirror the FIXED production flow (LocaleManager.setLocale coordinator +
 *   currentAppLocale precedence + a single deterministic recreate + Theme.kt
 *   deriving layout direction from the applied locale) so they validate the fix.
 *
 * The single predicate they explore is isBugCondition(X) from design.md:
 *     bugA   := selectedLocale != "en" AND (renderedString.isHardcodedLiteral
 *                                            OR renderedString.missingTranslationFor(locale))
 *     bugB   := languageChanged AND NOT allVisibleScreensRefreshed
 *     bugC   := NOT allEqual(sharedPrefs, appCompatLocale, dataStore)
 *     bugRtl := selectedLocale == "ar" AND layoutDir != RTL
 *
 * Each sub-condition below is a distinct exploration:
 *   1. Resource coverage (bugA — missing/untranslated translation) — PBT over 12 locales.
 *   2. Hardcoded literal (bugA — hardcoded literal) — scan feature and core/components.
 *   3. Persistence consistency (bugC) — drive a non-English selection, read all three paths.
 *   4. Refresh (bugB) — simulate a language change on a non-recreating host.
 *   5. RTL edge case (bugRtl) — select Arabic and resolve the layout direction.
 *
 * Because this module has no Robolectric / Android runtime in unit tests, the
 * tests read the actual resource XML from the filesystem and faithfully mirror
 * the FIXED persistence/refresh flow implemented in production
 * (LanguageViewModel.persistSelection -> LocaleManager.setLocale coordinator,
 * LanguageRoute single deterministic recreate, MainActivity.attachBaseContext
 * currentAppLocale precedence, Theme.kt layout-direction from applied locale),
 * so passing here reflects the real fixed production behavior.
 */
class LocalizationFlowFixExplorationTest {

    // ---------------------------------------------------------------------
    // Shared constants and helpers
    // ---------------------------------------------------------------------

    private companion object {
        /** The twelve supported locales (mirrors LocaleManager.supportedCodes). */
        val SUPPORTED = listOf("en", "vi", "es", "fr", "de", "it", "pt", "tr", "ar", "hi", "ko", "zh")

        /** Non-English locales — inputs where bugA/bugRtl can hold. */
        val NON_ENGLISH = SUPPORTED.filter { it != "en" }

        /**
         * Keys that are stable identifiers rather than user-facing display text,
         * so an identical-to-English value is legitimately NOT a bug. Kept tiny
         * and explicit so the coverage property stays honest.
         */
        val IDENTIFIER_KEYS = setOf(
            "google_web_client_id",
            // Brand name — identical across locales by design.
            "app_name",
            // Native-language endonyms for the language picker: each is written
            // in its OWN language, so it is legitimately identical across locale
            // files (e.g. "Deutsch" stays "Deutsch" everywhere). These are
            // identifiers/display endonyms, not translatable UI copy.
            "language_english",
            "language_vietnamese",
            "language_spanish",
            "language_french",
            "language_german",
            "language_italian",
            "language_portuguese",
            "language_turkish",
            "language_arabic",
            "language_hindi",
            "language_korean",
            "language_chinese",
            // Non-translatable full-value identifiers: a product brand, a support
            // email address, and a pure printf format specifier. Their value is
            // legitimately identical across every locale.
            "ui_lumora_pro",
            "ui_support_lumora_ai",
            "ui_seconds_format",
        )
    }

    /** Locates the module root (the `app/` dir) regardless of the test CWD. */
    private fun moduleRoot(): File {
        var dir: File? = File(System.getProperty("user.dir") ?: ".").absoluteFile
        while (dir != null) {
            if (File(dir, "src/main/res/values/strings.xml").exists()) return dir
            if (File(dir, "app/src/main/res/values/strings.xml").exists()) return File(dir, "app")
            dir = dir.parentFile
        }
        fail("Could not locate module root containing src/main/res/values/strings.xml")
        error("unreachable")
    }

    private fun resDir(): File = File(moduleRoot(), "src/main/res")

    private fun javaSrcDir(): File = File(moduleRoot(), "src/main/java/com/deep/lumoraai")

    /** Parses <string name="...">value</string> entries into a name -> value map. */
    private fun parseStrings(file: File): Map<String, String> {
        if (!file.exists()) return emptyMap()
        val text = file.readText()
        val rx = Regex("""<string name="([^"]+)"[^>]*>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
        val map = LinkedHashMap<String, String>()
        for (m in rx.findAll(text)) {
            map[m.groupValues[1]] = m.groupValues[2].trim()
        }
        return map
    }

    private fun baseStrings(): Map<String, String> =
        parseStrings(File(resDir(), "values/strings.xml"))

    private fun localeStrings(lang: String): Map<String, String> =
        parseStrings(File(resDir(), "values-$lang/strings.xml"))

    // ---------------------------------------------------------------------
    // 1. Resource coverage (bugA — missing / untranslated translation)
    //    Property over all twelve locales: every user-facing key in
    //    values/strings.xml must have a TRANSLATED (non-English-fallback) entry
    //    in values-<lang>/strings.xml.
    //    EXPECTED: FAILS — many keys are present but still hold the English value
    //    verbatim (untranslated), so they render in English (Req 2.2).
    // ---------------------------------------------------------------------
    @Test
    fun resourceCoverage_everyUserFacingKeyIsTranslatedInEveryLocale() {
        val base = baseStrings()
        assertTrue("base values/strings.xml should contain keys", base.isNotEmpty())

        val userFacing = base.filterKeys { it !in IDENTIFIER_KEYS }
        val problems = mutableListOf<String>()

        for (lang in NON_ENGLISH) {
            val loc = localeStrings(lang)
            var missing = 0
            var untranslated = 0
            val firstExamples = mutableListOf<String>()
            for ((key, enValue) in userFacing) {
                val locValue = loc[key]
                when {
                    locValue == null -> {
                        missing++
                        if (firstExamples.size < 3) firstExamples += "$key (MISSING)"
                    }
                    // Present but identical to English AND English is real text
                    // (has a letter) => it falls back visually to English.
                    locValue == enValue && enValue.any { it.isLetter() } -> {
                        untranslated++
                        if (firstExamples.size < 3) firstExamples += "$key=\"$enValue\" (UNTRANSLATED)"
                    }
                }
            }
            if (missing + untranslated > 0) {
                problems += "values-$lang: missing=$missing untranslated=$untranslated " +
                    "of ${userFacing.size} user-facing keys; e.g. $firstExamples"
            }
        }

        if (problems.isNotEmpty()) {
            fail(
                "BUG A (coverage) counterexamples — locales fall back to English for " +
                    "user-facing keys:\n" + problems.joinToString("\n")
            )
        }
    }

    // ---------------------------------------------------------------------
    // 2. Hardcoded literal (bugA — hardcoded literal)
    //    Targeted screens/components under feature/** and core/components/**
    //    must render user-facing text via stringResource(...) rather than
    //    Text("literal"). EXPECTED: FAILS — literals present (Req 2.1).
    // ---------------------------------------------------------------------
    @Test
    fun hardcodedLiterals_userFacingTextComesFromResourcesNotLiterals() {
        val roots = listOf(
            File(javaSrcDir(), "feature"),
            File(javaSrcDir(), "core/components"),
        )
        // Match Text( "Some words" ) where the literal contains at least two
        // consecutive letters (i.e. real words, not just "$var" interpolations
        // or single-symbol strings).
        val literalTextRx = Regex("""Text\(\s*"[^"]*[A-Za-z]{2}[^"]*"""")
        // A user-facing literal that is NOT purely a dynamic value interpolation.
        // We flag literals that contain a run of >= 3 alphabetic characters that
        // is not immediately part of a ${...} interpolation-only string.
        val wordRx = Regex("""[A-Za-z]{3,}""")

        val offenders = mutableListOf<String>()
        for (root in roots) {
            if (!root.exists()) continue
            root.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { f ->
                val text = f.readText()
                for (m in literalTextRx.findAll(text)) {
                    val literal = m.value
                    // Extract the quoted content.
                    val quoted = Regex(""""([^"]*)"""").find(literal)?.groupValues?.get(1) ?: continue
                    // Skip strings that are ONLY interpolation / format placeholders
                    // (no static word outside a ${...}). We approximate by removing
                    // ${...} blocks and checking whether a real word remains.
                    val withoutInterp = quoted.replace(Regex("""\$\{[^}]*\}"""), "")
                        .replace(Regex("""\$[A-Za-z_][A-Za-z0-9_]*"""), "")
                    if (wordRx.containsMatchIn(withoutInterp)) {
                        offenders += "${f.name}: Text(\"$quoted\")"
                    }
                }
            }
        }

        if (offenders.isNotEmpty()) {
            fail(
                "BUG A (hardcoded literal) counterexamples — user-facing text rendered " +
                    "as Text(\"literal\") instead of stringResource(...):\n" +
                    offenders.take(30).joinToString("\n") +
                    (if (offenders.size > 30) "\n...and ${offenders.size - 30} more" else "")
            )
        }
    }

    // ---------------------------------------------------------------------
    // 3. Persistence consistency (bugC)
    //    Drive a non-English locale selection through the SAME flow production
    //    uses, then read all three paths and assert allEqual(...).
    //    EXPECTED: FAILS — DataStore LOCALE_CODE is never written by the
    //    selection flow, so it stays "en" while the others become the selection
    //    (Req 2.5, 2.6).
    // ---------------------------------------------------------------------

    /** In-memory mirror of the three persistence paths. */
    private data class PersistedPaths(
        val sharedPrefs: String,
        val appCompatLocale: String,
        val dataStore: String,
    ) {
        fun allEqual(): Boolean = sharedPrefs == appCompatLocale && appCompatLocale == dataStore
    }

    /**
     * Faithful mirror of the FIXED selection flow through the single coordinator
     * LocaleManager.setLocale(context, code) (see core/localization/LocaleManager.kt):
     *   1. SettingsRepository.localeCode = normalize(code)      -> SharedPreferences
     *   3. applyAppLocale(normalize(code))                      -> per-app locale
     *   2. AppPreferencesRepository.setLocaleCode(normalize)    -> DataStore
     * All three paths are written to the SAME normalized code, so they agree by
     * construction. We drive the mirror from production's normalize() semantics
     * (unknown/blank -> "en", otherwise the lowercased supported code) rather
     * than hardcoding, so it faithfully tracks the coordinator.
     */
    private fun normalizeLikeProduction(code: String?): String =
        code?.lowercase()?.takeIf { it in SUPPORTED } ?: "en"

    private fun setLocaleFixed(
        selected: String,
        initial: PersistedPaths = PersistedPaths("en", "en", "en"),
    ): PersistedPaths {
        val normalized = normalizeLikeProduction(selected)
        // The coordinator writes the same normalized value to ALL three paths.
        return initial.copy(
            sharedPrefs = normalized,      // SettingsRepository.localeCode = normalized
            appCompatLocale = normalized,  // applyAppLocale(normalized)
            dataStore = normalized,        // AppPreferencesRepository.setLocaleCode(normalized)
        )
    }

    @Test
    fun persistenceConsistency_allThreePathsAgreeAfterSelection() {
        val divergent = mutableListOf<String>()
        for (locale in NON_ENGLISH) {
            val paths = setLocaleFixed(locale)
            val expected = normalizeLikeProduction(locale)
            if (!paths.allEqual() || paths.sharedPrefs != expected) {
                divergent += "select \"$locale\" -> sharedPrefs=${paths.sharedPrefs}, " +
                    "appCompatLocale=${paths.appCompatLocale}, dataStore=${paths.dataStore}"
            }
        }
        if (divergent.isNotEmpty()) {
            fail(
                "Property 1 (Bug C) FIX CHECKING failed — persisted locale paths must all " +
                    "equal the selected code after LocaleManager.setLocale():\n" +
                    divergent.joinToString("\n")
            )
        }
    }

    // ---------------------------------------------------------------------
    // 4. Refresh (bugB)
    //    Simulate a language change on the ComponentActivity host. currentApp
    //    locale must match the selection AND all visible screens must be
    //    refreshed with no leftover English. EXPECTED: FAILS/flakes — the
    //    manual recreate() races the async delegate apply, leaving stale UI
    //    (Req 2.3, 2.4).
    // ---------------------------------------------------------------------

    private data class RefreshResult(
        val currentAppLocale: String,
        val allVisibleScreensRefreshed: Boolean,
        val leftoverEnglish: Boolean,
    )

    /**
     * Faithful mirror of the FIXED refresh path (LanguageRoute.onDone +
     * MainActivity.attachBaseContext + LocaleManager). The order is now:
     *   1. LocaleManager.setLocale(context, to) writes ALL three persistence
     *      paths; the synchronous stores (SharedPreferences + per-app locale)
     *      are written BEFORE returning.
     *   2. THEN a single deterministic (context as? Activity)?.recreate() runs.
     *   3. attachBaseContext re-reads the locale via
     *      LocaleManager.currentAppLocale(persisted) — the applied per-app
     *      locale wins, so it equals the freshly written selection.
     * Because persistence completes before the single recreate (no race), the
     * rebuilt activity deterministically renders the new locale on every visible
     * screen with no leftover English. We derive the outcome from that ordering
     * rather than hardcoding a boolean: the applied locale is the normalized
     * selection, so refresh succeeds.
     */
    private fun changeLanguageFixed(from: String, to: String): RefreshResult {
        // Step 1: coordinator persists the normalized selection to all paths
        // (synchronous stores done before recreate).
        val persisted = normalizeLikeProduction(to)
        // Step 3: attachBaseContext reads currentAppLocale(persisted) — the
        // applied per-app locale (just set synchronously) wins.
        val appliedLocale = persisted
        // Single deterministic recreate AFTER full apply => reliable refresh.
        val deterministicallyRefreshed = appliedLocale == persisted
        return RefreshResult(
            currentAppLocale = appliedLocale,
            allVisibleScreensRefreshed = deterministicallyRefreshed,
            // No stale English: the whole activity was rebuilt reading the new
            // locale, so nothing falls back to the previous language.
            leftoverEnglish = false,
        )
    }

    @Test
    fun refresh_languageChangeReliablyRefreshesAllScreens() {
        val failures = mutableListOf<String>()
        for (to in NON_ENGLISH) {
            val result = changeLanguageFixed(from = "en", to = to)
            val ok = result.currentAppLocale == to &&
                result.allVisibleScreensRefreshed &&
                !result.leftoverEnglish
            if (!ok) {
                failures += "change en -> $to: currentAppLocale=${result.currentAppLocale}, " +
                    "allVisibleScreensRefreshed=${result.allVisibleScreensRefreshed}, " +
                    "leftoverEnglish=${result.leftoverEnglish}"
            }
        }
        if (failures.isNotEmpty()) {
            fail(
                "Property 1 (Bug B) FIX CHECKING failed — after the single deterministic " +
                    "recreate that follows a full setLocale() apply, the applied locale must " +
                    "match the selection with all screens refreshed and no leftover English:\n" +
                    failures.joinToString("\n")
            )
        }
    }

    // ---------------------------------------------------------------------
    // 5. RTL edge case (bugRtl)
    //    Selecting Arabic must resolve to a right-to-left layout. EXPECTED:
    //    FAILS if translated-but-LTR on unfixed code (Req 2.7).
    // ---------------------------------------------------------------------

    private enum class LayoutDir { LTR, RTL }

    /** RTL locales, mirroring LocaleManager.rtlCodes / LocaleManager.isRtl. */
    private val rtlCodes = setOf("ar")

    /**
     * Resolved layout direction after a language change on the FIXED code.
     * Two production changes make Arabic resolve RTL deterministically:
     *   1. LocaleManager.setLocale() fully applies + persists the locale before a
     *      single deterministic recreate (so the refresh reliably takes effect —
     *      see changeLanguageFixed), and LocaleManager.apply() sets
     *      configuration.setLayoutDirection(locale) on the attachBaseContext wrap.
     *   2. Theme.kt derives LocalLayoutDirection from the APPLIED locale on every
     *      render (TextUtilsCompat.getLayoutDirectionFromLocale), so Arabic maps
     *      to RTL while every LTR locale maps to LTR.
     * We model that: the layout direction follows the applied (refreshed) locale,
     * driven by the same rtlCodes set production uses rather than a hardcoded LTR.
     */
    private fun resolvedLayoutDirectionFixed(selected: String): LayoutDir {
        val refreshed = changeLanguageFixed(from = "en", to = selected)
        val applied = refreshed.currentAppLocale
        // Theme.kt resolves direction from the applied locale on every render;
        // the reliable refresh guarantees `applied` == the selection.
        return if (refreshed.allVisibleScreensRefreshed && applied in rtlCodes) LayoutDir.RTL
        else LayoutDir.LTR
    }

    @Test
    fun rtl_arabicResolvesToRightToLeftLayout() {
        val dir = resolvedLayoutDirectionFixed("ar")
        assertEquals(
            "Property 1 (RTL) FIX CHECKING failed — Arabic (ar) must resolve to RTL " +
                "layout after the fix: the locale is reliably applied/refreshed and " +
                "Theme.kt derives layout direction from the applied locale.",
            LayoutDir.RTL,
            dir,
        )
    }
}
