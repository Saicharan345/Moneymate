package com.saicharan.moneymate.domain.usecase

import com.saicharan.moneymate.domain.model.CategorySpend
import com.saicharan.moneymate.domain.model.MonthTotals
import com.saicharan.moneymate.domain.model.Transaction
import com.saicharan.moneymate.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth

/** Pure, side-effect free money maths. All amounts use BigDecimal. */
object FinanceCalculator {

    fun totalIncome(transactions: List<Transaction>): BigDecimal = sumOf(transactions, TransactionType.INCOME)

    fun totalExpense(transactions: List<Transaction>): BigDecimal = sumOf(transactions, TransactionType.EXPENSE)

    /** All-time balance: total income minus total expenses. */
    fun totalBalance(transactions: List<Transaction>): BigDecimal =
        totalIncome(transactions) - totalExpense(transactions)

    fun inMonth(transactions: List<Transaction>, month: YearMonth): List<Transaction> =
        transactions.filter { YearMonth.from(it.date) == month }

    fun monthIncome(transactions: List<Transaction>, month: YearMonth): BigDecimal =
        totalIncome(inMonth(transactions, month))

    fun monthExpense(transactions: List<Transaction>, month: YearMonth): BigDecimal =
        totalExpense(inMonth(transactions, month))

    /** Expense totals per category for [month], largest first. */
    fun categoryBreakdown(transactions: List<Transaction>, month: YearMonth): List<CategorySpend> {
        val expenses = inMonth(transactions, month).filter { it.type == TransactionType.EXPENSE }
        val total = expenses.fold(BigDecimal.ZERO) { acc, t -> acc + t.amount }
        if (total.signum() == 0) return emptyList()
        return expenses
            .groupBy { it.category }
            .map { (category, items) ->
                val sum = items.fold(BigDecimal.ZERO) { acc, t -> acc + t.amount }
                val fraction = sum.divide(total, 4, RoundingMode.HALF_UP).toFloat()
                CategorySpend(category, sum, fraction)
            }
            .sortedByDescending { it.amount }
    }

    /** Totals for the [count] months ending at [endMonth], oldest first. */
    fun monthlyTotals(transactions: List<Transaction>, endMonth: YearMonth, count: Int): List<MonthTotals> =
        (count - 1 downTo 0).map { offset ->
            val month = endMonth.minusMonths(offset.toLong())
            MonthTotals(month, monthIncome(transactions, month), monthExpense(transactions, month))
        }

    private fun sumOf(transactions: List<Transaction>, type: TransactionType): BigDecimal =
        transactions.filter { it.type == type }.fold(BigDecimal.ZERO) { acc, t -> acc + t.amount }
}
