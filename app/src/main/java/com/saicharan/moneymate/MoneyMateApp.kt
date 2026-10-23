package com.saicharan.moneymate

import android.app.Application
import android.util.Log
import com.saicharan.moneymate.domain.usecase.SeedSampleDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

class MoneyMateApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch {
            try {
                SeedSampleDataUseCase(container.transactionRepository, container.settingsRepository)
                    .seedIfFirstLaunch(LocalDate.now())
            } catch (e: Exception) {
                Log.e("MoneyMateApp", "Sample data seeding failed", e)
            }
        }
    }
}
