package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TransactionEntity::class,
        RiskAnalysisEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SentinelDatabase : RoomDatabase() {
    abstract fun sentinelDao(): SentinelDao

    companion object {
        @Volatile
        private var INSTANCE: SentinelDatabase? = null

        fun getDatabase(context: Context): SentinelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SentinelDatabase::class.java,
                    "upi_sentinel_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
