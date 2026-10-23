package com.saicharan.moneymate

import android.content.Context
import com.saicharan.moneymate.data.local.MoneyMateDatabase
import com.saicharan.moneymate.data.repository.DataStoreSettingsRepository
import com.saicharan.moneymate.data.repository.RoomTransactionRepository
import com.saicharan.moneymate.data.repository.SettingsRepository
import com.saicharan.moneymate.data.repository.TransactionRepository

/** Minimal manual dependency container (keeps the project free of DI-framework build risk). */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: MoneyMateDatabase by lazy { MoneyMateDatabase.create(appContext) }

    val transactionRepository: TransactionRepository by lazy {
        RoomTransactionRepository(database.transactionDao())
    }
    val settingsRepository: SettingsRepository by lazy { DataStoreSettingsRepository(appContext) }
}
