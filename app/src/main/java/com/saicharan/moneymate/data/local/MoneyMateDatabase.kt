package com.saicharan.moneymate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class MoneyMateDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        fun create(context: Context): MoneyMateDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                MoneyMateDatabase::class.java,
                "moneymate.db"
            ).build()
    }
}
