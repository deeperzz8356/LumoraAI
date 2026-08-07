package com.deep.lumoraai

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.logInWith
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LumoraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        configureRevenueCat()
    }

    private fun configureRevenueCat() {
        Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.INFO
        Purchases.configure(
            PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT_API_KEY).build()
        )

        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            Purchases.sharedInstance.logInWith(
                appUserID = uid,
                onError = { error ->
                    Log.e(TAG, "RevenueCat login failed: ${error.message}")
                },
                onSuccess = { customerInfo, created ->
                    Log.d(
                        TAG,
                        "RevenueCat login ok created=$created active=${customerInfo.entitlements.active.keys}"
                    )
                }
            )
        }
    }

    companion object {
        private const val TAG = "LumoraApplication"
    }
}
