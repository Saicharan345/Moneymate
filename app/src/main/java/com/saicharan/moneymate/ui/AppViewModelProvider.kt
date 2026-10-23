package com.saicharan.moneymate.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.saicharan.moneymate.MoneyMateApp
import com.saicharan.moneymate.ui.analytics.AnalyticsViewModel
import com.saicharan.moneymate.ui.dashboard.DashboardViewModel
import com.saicharan.moneymate.ui.settings.SettingsViewModel
import com.saicharan.moneymate.ui.transactions.TransactionFormViewModel
import com.saicharan.moneymate.ui.transactions.TransactionsViewModel

private fun CreationExtras.app(): MoneyMateApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MoneyMateApp

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val c = app().container
            DashboardViewModel(c.transactionRepository, c.settingsRepository)
        }
        initializer {
            val c = app().container
            TransactionsViewModel(c.transactionRepository, c.settingsRepository)
        }
        initializer {
            val c = app().container
            TransactionFormViewModel(createSavedStateHandle(), c.transactionRepository, c.settingsRepository)
        }
        initializer {
            val c = app().container
            AnalyticsViewModel(c.transactionRepository, c.settingsRepository)
        }
        initializer {
            val c = app().container
            SettingsViewModel(c.transactionRepository, c.settingsRepository)
        }
    }
}
