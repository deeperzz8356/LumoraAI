package com.deep.lumoraai.data.repository

import com.deep.lumoraai.data.model.CreditModel
import com.deep.lumoraai.data.model.HistoryModel
import com.deep.lumoraai.data.model.NotificationModel
import com.deep.lumoraai.data.model.ProfileModel
import com.deep.lumoraai.data.model.QueueModel
import com.deep.lumoraai.data.model.ResultModel
import com.deep.lumoraai.data.model.TemplateModel

class FakeRepository {
    fun getTemplates() = listOf(
        TemplateModel("template-1", "Cinematic Portrait", "Image"),
        TemplateModel("template-2", "Product Reveal", "Video"),
        TemplateModel("template-3", "Social Story", "Template")
    )

    fun getHistory() = listOf(
        HistoryModel("history-1", "Neon city prompt", "Today"),
        HistoryModel("history-2", "Studio product render", "Yesterday")
    )

    fun getCredits() = listOf(
        CreditModel("credit-1", "Starter balance", 120),
        CreditModel("credit-2", "Weekly reward", 40)
    )

    fun getNotifications() = listOf(
        NotificationModel("notification-1", "Render complete", "Your image is ready."),
        NotificationModel("notification-2", "Credits added", "Weekly credits were added to your account.")
    )

    fun getQueue() = listOf(
        QueueModel("queue-1", "Image upscale", 0.7f),
        QueueModel("queue-2", "Text to video", 0.35f)
    )

    fun getResults() = listOf(
        ResultModel("result-1", "Aurora concept", "Ready"),
        ResultModel("result-2", "Fashion reel", "Ready")
    )

    fun getProfile() = ProfileModel(name = "Lumora Creator", plan = "Free", credits = 160)
}