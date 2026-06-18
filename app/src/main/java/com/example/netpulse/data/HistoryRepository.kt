package com.example.netpulse.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<SpeedResult>> = historyDao.getAllHistory()

    suspend fun insert(result: SpeedResult) {
        historyDao.insertResult(result)
    }

    suspend fun delete(result: SpeedResult) {
        historyDao.deleteResult(result)
    }

    suspend fun clearAll() {
        historyDao.deleteAll()
    }
}
