package com.deep.lumoraai.data.repository

import com.deep.lumoraai.data.remote.PromptDataSource
import com.deep.lumoraai.domain.model.PromptModel
import com.deep.lumoraai.domain.repository.PromptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PromptRepositoryImpl @Inject constructor(
    private val dataSource: PromptDataSource
) : PromptRepository {
    override fun getPrompts(): Flow<List<PromptModel>> {
        return dataSource.getPrompts().map { dtos ->
            dtos.map { it.toDomainModel() }
        }
    }
}
