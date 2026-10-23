package com.saicharan.moneymate.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicharan.moneymate.data.repository.SettingsRepository
import com.saicharan.moneymate.data.repository.TransactionRepository
import com.saicharan.moneymate.domain.model.CurrencyOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class SettingsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val currency: CurrencyOption = CurrencyOption.INR,
    val budget: BigDecimal? = null,
    val sampleCount: Int = 0,
    val totalCount: Int = 0,
    val message: String? = null
)

class SettingsViewModel(
    private val transactions: TransactionRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settings.currency,
        settings.budget,
        transactions.observeTransactions(),
        message
    ) { currency, budget, list, msg ->
        SettingsUiState(
            isLoading = false,
            currency = currency,
            budget = budget,
            sampleCount = list.count { it.isSample },
            totalCount = list.size,
            message = msg
        )
    }
        .catch { emit(SettingsUiState(isLoading = false, error = it.message ?: "Unable to load settings")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setCurrency(currency: CurrencyOption) = launchAction("Currency set to ${currency.code}") { settings.setCurrency(currency) }

    fun setBudget(amount: BigDecimal?) =
        launchAction(if (amount == null) "Budget removed" else "Budget saved") { settings.setBudget(amount) }

    fun clearSampleData() = launchAction("Sample data cleared") { transactions.clearSampleTransactions() }

    fun clearAllTransactions() = launchAction("All transactions deleted") { transactions.clearAllTransactions() }

    fun messageShown() {
        message.value = null
    }

    private fun launchAction(success: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                message.value = success
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                message.value = "Something went wrong. Please try again."
            }
        }
    }
}
