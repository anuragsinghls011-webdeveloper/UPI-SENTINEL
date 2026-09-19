package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SentinelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE transactionId = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiskAnalysis(analysis: RiskAnalysisEntity)

    @Query("SELECT * FROM risk_analyses ORDER BY createdAt DESC")
    fun getAllRiskAnalyses(): Flow<List<RiskAnalysisEntity>>

    @Query("SELECT * FROM risk_analyses WHERE transactionId = :id")
    fun getRiskAnalysisById(id: String): Flow<RiskAnalysisEntity?>

    @Query("SELECT * FROM risk_analyses WHERE transactionId = :id")
    suspend fun getRiskAnalysisDirect(id: String): RiskAnalysisEntity?

    @Query("UPDATE risk_analyses SET reviewStatus = :status, reviewNote = :note WHERE transactionId = :id")
    suspend fun updateReviewStatus(id: String, status: String, note: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT COUNT(*) FROM risk_analyses")
    fun getTotalAnalysesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM risk_analyses WHERE riskLevel = :level")
    fun getAnalysesCountByLevel(level: String): Flow<Int>

    @Query("SELECT AVG(riskScore) FROM risk_analyses")
    fun getAverageRiskScore(): Flow<Double?>
}
