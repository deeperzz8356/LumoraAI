package com.deep.lumoraai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LumoraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.deep.lumoraai.data.repository.AppPreferencesRepository.getInstance(this)
    }
}
