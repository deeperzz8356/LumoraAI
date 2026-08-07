package com.deep.lumoraai.feature.subscription.model

data class SubscriptionPlan(
    val code: String,
    val name: String,
    val priceUsd: Double,
    val monthlyCredits: Int,
    val videoCredits: Int,
    val features: List<String>,
    val isPopular: Boolean = false,
    val signupBonusCredits: Int = 0,
)
