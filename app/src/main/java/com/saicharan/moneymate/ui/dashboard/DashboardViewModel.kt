package com.saicharan.moneymate.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicharan.moneymate.data.repository.SettingsRepository
import com.saicharan.moneymate.data.repository.TransactionRepository
import com.saicharan.moneymate.domain.model.BudgetStatus
import com.saicharan.moneymate.domain.model.CategorySpend
import com.saicharan.moneymate.domain.model.CurrencyOption
import com.saicharan.moneymate.domain.model.Transaction
import com.saicharan.moneymate.domain.usecase.BudgetCalculator
import com.saicharan.moneymate.domain.usecase.FinanceCalculator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.YearMonth

data class DashboardData(
    val currency: CurrencyOption,
    val month: YearMonth,
    val totalBalance: BigDecimal,
    val monthIncome: BigDecimal,
    val monthExpense: BigDecimal,
    val budget: BigDecimal?,
    val remaining: BigDecimal?,
    val usagePercent: Double,
    val progress: Float,
    val budgetStatus: BudgetStatus,
    val recent: List<Transaction>,
    val breakdown: List<CategorySpend>,
    val hasTransactions: Boolean
)

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Error(val message: String) : DashboardUiState
    data class Success(val data: DashboardData) : DashboardUiState
}

class DashboardViewModel(
    private val transactions: TransactionRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        transactions.observeTransactions(),
        settings.budget,
        settings.currency
    ) { list, budget, currency -> build(list, budget, currency) }
        .catch { emit(DashboardUiState.Error(it.message ?: "Unable to load your data")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    private fun build(list: List<Transaction>, budget: BigDecimal?, currency: CurrencyOption): DashboardUiState {
        val month = YearMonth.now()
        val income = FinanceCalculator.monthIncome(list, month)
        val expense = FinanceCalculator.monthExpense(list, month)
        val status = BudgetCalculator.status(expense, budget)
        val hasBudget = budget != null && status != BudgetStatus.NOT_SET
        return DashboardUiState.Success(
            DashboardData(
                currency = currency,
                month = month,
                totalBalance = FinanceCalculator.totalBalance(list),
                monthIncome = income,
                monthExpense = expense,
                budget = budget,
                remaining = if (hasBudget && budget != null) BudgetCalculator.remaining(budget, expense) else null,
                usagePercent = BudgetCalculator.usagePercent(expense, budget),
                progress = BudgetCalculator.progress(expense, budget),
                budgetStatus = status,
                recent = list.take(5),
                breakdown = FinanceCalculator.categoryBreakdown(list, month),
                hasTransactions = list.isNotEmpty()
            )
        )
    }

    fun setBudget(amount: BigDecimal?) {
        viewModelScope.launch {
            try {
                settings.setBudget(amount)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Storage failure is non-fatal; the previous value remains.
            }
        }
    }
}
