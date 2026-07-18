package com.deep.lumoraai.data.model

data class TemplateModel(val id: String, val title: String, val category: String)
data class HistoryModel(val id: String, val title: String, val createdAt: String)
data class NotificationModel(val id: String, val title: String, val message: String)
data class CreditModel(val id: String, val label: String, val amount: Int)
data class QueueModel(val id: String, val title: String, val progress: Float)
data class ResultModel(val id: String, val title: String, val status: String)
data class ProfileModel(val name: String, val plan: String, val credits: Int)

data class ActiveJobInfo(
    val title: String,
    val subtitle: String,
    val badgeText: String,
    val statusText: String,
    val progressPercent: Float?,
    val isCompleted: Boolean,
    val imageRes: Int,
    val imageUrl: String? = null
)