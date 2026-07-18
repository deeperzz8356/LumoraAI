package com.deep.lumoraai.data.repository

import com.deep.lumoraai.data.local.room.dao.HistoryDao
import com.deep.lumoraai.data.mapper.toDomainModel
import com.deep.lumoraai.data.mapper.toEntity
import com.deep.lumoraai.data.model.HistoryModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao
) {
    fun getHistory(): Flow<List<HistoryModel>> {
        return historyDao.getAllHistory().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    suspend fun addHistory(historyModel: HistoryModel, type: String, mediaUrl: String? = null) {
        historyDao.insertHistory(historyModel.toEntity(type, mediaUrl))
    }

    suspend fun deleteHistory(id: String) {
        historyDao.deleteHistory(id)
    }
}
