package com.deep.lumoraai.core.utils

import android.content.Context
import android.content.pm.PackageManager
import com.google.firebase.auth.FirebaseAuth
import java.io.File

object InstallStateCleaner {
    private const val SENTINEL_FILE = "lumora_install_sentinel"
    private const val DATABASE_NAME = "lumora_database"
    private const val NEW_INSTALL_WINDOW_MS = 2 * 60 * 1000L

    fun clearRestoredStateOnFreshInstall(context: Context) {
        val appContext = context.applicationContext
        val sentinel = File(appContext.noBackupFilesDir, SENTINEL_FILE)
        if (sentinel.exists()) return

        if (isFreshInstall(appContext)) {
            clearRestoredLocalState(appContext)
        }

        runCatching {
            sentinel.parentFile?.mkdirs()
            sentinel.createNewFile()
        }
    }

    private fun isFreshInstall(context: Context): Boolean {
        return runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.lastUpdateTime - packageInfo.firstInstallTime <= NEW_INSTALL_WINDOW_MS
        }.getOrElse { error ->
            error is PackageManager.NameNotFoundException
        }
    }

    private fun clearRestoredLocalState(context: Context) {
        clearSharedPreferences(context)
        clearDataStore(context)
        context.deleteDatabase(DATABASE_NAME)
        runCatching { FirebaseAuth.getInstance().signOut() }
    }

    private fun clearSharedPreferences(context: Context) {
        val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        sharedPrefsDir.listFiles { file -> file.extension == "xml" }
            ?.forEach { file ->
                context.getSharedPreferences(file.nameWithoutExtension, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .commit()
                file.delete()
            }
    }

    private fun clearDataStore(context: Context) {
        File(context.filesDir, "datastore").deleteRecursively()
    }
}
