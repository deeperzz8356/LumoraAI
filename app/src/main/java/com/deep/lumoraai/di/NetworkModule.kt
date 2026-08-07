package com.deep.lumoraai.di

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import com.deep.lumoraai.api.ImageGenerationService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val TAG = "NetworkModule"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Determines the backend base URL based on the build environment.
     * - Emulator: http://10.0.2.2:8000 (special alias to host machine)
     * - Physical device: Set via BuildConfig or environment variable
     * - Production: Use your deployed backend URL
     */
    private fun getBaseUrl(): String {
        // You can replace this logic with BuildConfig.BACKEND_URL if you set it in build.gradle
        // For now, we'll use localhost which works with emulator
        return "http://10.0.2.2:8000" // Use 10.0.2.2 for Android emulator
        // For physical device, replace with your machine's IP: "http://192.168.x.x:8000"
        // For production: "https://your-backend-domain.com"
    }

    @Singleton
    @Provides
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager {
        return CredentialManager.create(context)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val originalRequest = chain.request()

                // Get Firebase auth token and add to request
                val firebaseUser = Firebase.auth.currentUser
                val requestWithAuth = if (firebaseUser != null) {
                    try {
                        // This should be done in a coroutine context, but OkHttp interceptor is sync
                        // For now, we'll handle this in the ViewModel using proper async calls
                        // The actual token addition will happen via a custom interceptor
                        originalRequest.newBuilder()
                            .addHeader("User-Agent", "LumoraAI/1.0")
                            .build()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to get Firebase token", e)
                        originalRequest
                    }
                } else {
                    Log.w(TAG, "No Firebase user authenticated")
                    originalRequest
                }

                chain.proceed(requestWithAuth)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideImageGenerationService(retrofit: Retrofit): ImageGenerationService {
        return retrofit.create(ImageGenerationService::class.java)
    }
}
