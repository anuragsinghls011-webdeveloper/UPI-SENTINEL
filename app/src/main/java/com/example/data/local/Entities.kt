package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val transactionType: String,
    val description: String,
    val receiverName: String,
    val receiverId: String,
    val receiverType: String,
    val receiverVerified: Boolean,
    val merchantName: String,
    val merchantId: String,
    val merchantVerified: Boolean,
    val paymentUrl: String,
    val domain: String,
    val invoiceId: String,
    val invoiceAmount: Double,
    val invoiceMerchant: String,
    val invoiceReference: String,
    val userTransactionCount24h: Int,
    val averageTransactionAmount: Double,
    val previousTransactionAmount: Double,
    val lastTransactionTimestamp: Long,
    val receiverFirstSeen: Boolean,
    val merchantFirstSeen: Boolean,
    val failedAttempts: Int,
    val transactionFrequency: Double,
    val unusualTimeFlag: Boolean
)

@Entity(tableName = "risk_analyses")
data class RiskAnalysisEntity(
    @PrimaryKey
    val transactionId: String,
    val amount: Double,
    val riskScore: Int,
    val riskLevel: String,
    val confidence: Double,
    val mlScore: Double,
    val anomalyScore: Double,
    val receiverName: String,
    val merchantName: String,
    val domain: String,
    val signalsJson: String,
    val explanationsJson: String,
    val counterfactualsJson: String,
    val recommendedAction: String,
    val actionType: String,
    val modelVersion: String,
    val featureVectorJson: String,
    val latencyJson: String,
    val reviewStatus: String,
    val reviewNote: String,
    val createdAt: Long
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String,
    val actor: String,
    val entityId: String,
    val timestamp: Long,
    val metadata: String
)
