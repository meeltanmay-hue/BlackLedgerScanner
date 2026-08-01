package com.blackledger.scanner.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [AllianceEntity::class, PlayerEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun allianceDao(): AllianceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "black_ledger_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
