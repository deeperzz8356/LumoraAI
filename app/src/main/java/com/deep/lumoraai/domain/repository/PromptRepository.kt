package com.deep.lumoraai.domain.repository

import com.deep.lumoraai.domain.model.PromptModel
import kotlinx.coroutines.flow.Flow

interface PromptRepository {
    fun getPrompts(): Flow<List<PromptModel>>
}
