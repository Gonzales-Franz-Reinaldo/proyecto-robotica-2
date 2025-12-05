package com.example.driverdrowsinessdetectorapp.data.repository

import com.example.driverdrowsinessdetectorapp.data.local.dao.MetricsDao
import com.example.driverdrowsinessdetectorapp.data.local.entity.MetricsEntity
import com.example.driverdrowsinessdetectorapp.domain.repository.MetricsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MetricsRepositoryImpl @Inject constructor(
    private val metricsDao: MetricsDao
) : MetricsRepository {

    override suspend fun saveMetrics(metrics: MetricsEntity): Long {
        return metricsDao.insert(metrics)
    }

    override suspend fun saveMetricsBatch(metrics: List<MetricsEntity>) {
        metricsDao.insertAll(metrics)
    }

    override fun getMetricsBySession(sessionId: Long): Flow<List<MetricsEntity>> {
        return metricsDao.getMetricsBySession(sessionId)
    }

    override fun getCriticalAlertsBySession(sessionId: Long): Flow<List<MetricsEntity>> {
        return metricsDao.getCriticalAlertsBySession(sessionId)
    }

    override suspend fun deleteOlderThan(timestamp: Long) {
        metricsDao.deleteOlderThan(timestamp)
    }

    override suspend fun getCriticalAlertsCount(sessionId: Long): Int {
        return metricsDao.getCriticalAlertsCount(sessionId)
    }
}