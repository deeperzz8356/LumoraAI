package com.deep.lumoraai.feature.subscription.model

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val price: String,
    val billingPeriod: String,
    val features: List<String>,
    val highlighted: Boolean = false
)
