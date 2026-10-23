package com.saicharan.moneymate.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.saicharan.moneymate.domain.model.CurrencyOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.math.BigDecimal

interface SettingsRepository {
    val budget: Flow<BigDecimal?>
    val currency: Flow<CurrencyOption>
    suspend fun currentBudget(): BigDecimal?
    suspend fun setBudget(amount: BigDecimal?)
    suspend fun setCurrency(currency: CurrencyOption)
    suspend fun isSampleSeeded(): Boolean
    suspend fun markSampleSeeded()
}

private val Context.moneyMateDataStore: DataStore<Preferences> by preferencesDataStore(name = "moneymate_prefs")

class DataStoreSettingsRepository(context: Context) : SettingsRepository {

    private val dataStore = context.applicationContext.moneyMateDataStore

    private object Keys {
        val BUDGET = stringPreferencesKey("monthly_budget")
        val CURRENCY = stringPreferencesKey("currency")
        val SAMPLE_SEEDED = booleanPreferencesKey("sample_seeded")
    }

    private val safeData: Flow<Preferences> = dataStore.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    override val budget: Flow<BigDecimal?> =
        safeData.map { it[Keys.BUDGET]?.toBigDecimalOrNull() }.distinctUntilChanged()

    override val currency: Flow<CurrencyOption> =
        safeData.map { CurrencyOption.fromCode(it[Keys.CURRENCY]) }.distinctUntilChanged()

    override suspend fun currentBudget(): BigDecimal? = budget.first()

    override suspend fun setBudget(amount: BigDecimal?) {
        dataStore.edit { prefs ->
            if (amount == null) prefs.remove(Keys.BUDGET) else prefs[Keys.BUDGET] = amount.toPlainString()
        }
    }

    override suspend fun setCurrency(currency: CurrencyOption) {
        dataStore.edit { it[Keys.CURRENCY] = currency.code }
    }

    override suspend fun isSampleSeeded(): Boolean = safeData.first()[Keys.SAMPLE_SEEDED] ?: false

    override suspend fun markSampleSeeded() {
        dataStore.edit { it[Keys.SAMPLE_SEEDED] = true }
    }
}
