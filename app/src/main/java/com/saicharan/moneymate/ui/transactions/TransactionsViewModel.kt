package com.saicharan.moneymate.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicharan.moneymate.data.repository.SettingsRepository
import com.saicharan.moneymate.data.repository.TransactionRepository
import com.saicharan.moneymate.domain.model.Category
import com.saicharan.moneymate.domain.model.CurrencyOption
import com.saicharan.moneymate.domain.model.Transaction
import com.saicharan.moneymate.domain.model.TransactionType
import com.saicharan.moneymate.domain.usecase.TransactionFilter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val items: List<Transaction> = emptyList(),
    val totalCount: Int = 0,
    val filter: TransactionFilter = TransactionFilter(),
    val currency: CurrencyOption = CurrencyOption.INR,
    val message: String? = null
)

class TransactionsViewModel(
    private val repository: TransactionRepository,
    settings: SettingsRepository
) : ViewModel() {

    private val filter = MutableStateFlow(TransactionFilter())
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TransactionsUiState> = combine(
        repository.observeTransactions(),
        settings.currency,
        filter,
        message
    ) { all, currency, f, msg ->
        TransactionsUiState(
            isLoading = false,
            items = f.apply(all),
            totalCount = all.size,
            filter = f,
            currency = currency,
            message = msg
        )
    }
        .catch { emit(TransactionsUiState(isLoading = false, error = it.message ?: "Unable to load transactions")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState())

    fun onQueryChange(query: String) = filter.update { it.copy(query = query) }

    fun onTypeFilter(type: TransactionType?) = filter.update { current ->
        val keepCategory = current.category?.takeIf { type == null || it.type == type }
        current.copy(type = type, category = keepCategory)
    }

    fun onCategoryFilter(category: Category?) = filter.update { it.copy(category = category) }

    fun clearFilters() {
        filter.value = TransactionFilter()
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(id)
                message.value = "Transaction deleted"
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                message.value = "Couldn't delete the transaction. Please try again."
            }
        }
    }

    fun messageShown() {
        message.value = null
    }
}
