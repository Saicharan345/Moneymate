package com.saicharan.moneymate.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicharan.moneymate.data.repository.SettingsRepository
import com.saicharan.moneymate.data.repository.TransactionRepository
import com.saicharan.moneymate.domain.model.BudgetStatus
import com.saicharan.moneymate.domain.model.CategorySpend
import com.saicharan.moneymate.domain.model.CurrencyOption
import com.saicharan.moneymate.domain.model.MonthTotals
import com.saicharan.moneymate.domain.model.Transaction
import com.saicharan.moneymate.domain.usecase.BudgetCalculator
import com.saicharan.moneymate.domain.usecase.FinanceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.time.YearMonth

data class AnalyticsData(
    val currency: CurrencyOption,
    val month: YearMonth,
    val isCurrentMonth: Boolean,
    val income: BigDecimal,
    val expense: BigDecimal,
    val net: BigDecimal,
    val breakdown: List<CategorySpend>,
    val trend: List<MonthTotals>,
    val budget: BigDecimal?,
    val remaining: BigDecimal?,
    val usagePercent: Double,
    val progress: Float,
    val budgetStatus: BudgetStatus,
    val hasAnyTransactions: Boolean
)

sealed interface AnalyticsUiState {
    data object Loading : AnalyticsUiState
    data class Error(val message: String) : AnalyticsUiState
    data class Success(val data: AnalyticsData) : AnalyticsUiState
}

class AnalyticsViewModel(
    transactions: TransactionRepository,
    settings: SettingsRepository
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<AnalyticsUiState> = combine(
        transactions.observeTransactions(),
        settings.budget,
        settings.currency,
        selectedMonth
    ) { list, budget, currency, month -> build(list, budget, currency, month) }
        .catch { emit(AnalyticsUiState.Error(it.message ?: "Unable to load analytics")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState.Loading)

    private fun build(
        list: List<Transaction>,
        budget: BigDecimal?,
        currency: CurrencyOption,
        month: YearMonth
    ): AnalyticsUiState {
        val income = FinanceCalculator.monthIncome(list, month)
        val expense = FinanceCalculator.monthExpense(list, month)
        val status = BudgetCalculator.status(expense, budget)
        return AnalyticsUiState.Success(
            AnalyticsData(
                currency = currency,
                month = month,
                isCurrentMonth = month == YearMonth.now(),
                income = income,
                expense = expense,
                net = income - expense,
                breakdown = FinanceCalculator.categoryBreakdown(list, month),
                trend = FinanceCalculator.monthlyTotals(list, month, 6),
                budget = budget,
                remaining = if (budget != null && status != BudgetStatus.NOT_SET) BudgetCalculator.remaining(budget, expense) else null,
                usagePercent = BudgetCalculator.usagePercent(expense, budget),
                progress = BudgetCalculator.progress(expense, budget),
                budgetStatus = status,
                hasAnyTransactions = list.isNotEmpty()
            )
        )
    }

    fun previousMonth() = selectedMonth.update { it.minusMonths(1) }

    fun nextMonth() = selectedMonth.update { current ->
        val next = current.plusMonths(1)
        if (next.isAfter(YearMonth.now())) current else next
    }
}
