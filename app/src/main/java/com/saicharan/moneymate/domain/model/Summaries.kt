package com.saicharan.moneymate.domain.model

import java.math.BigDecimal
import java.time.YearMonth

enum class BudgetStatus { NOT_SET, ON_TRACK, WARNING, EXCEEDED }

enum class CurrencyOption(
    val code: String,
    val symbol: String,
    val label: String,
    val indianGrouping: Boolean
) {
    INR("INR", "₹", "Indian Rupee", true),
    USD("USD", "$", "US Dollar", false),
    EUR("EUR", "€", "Euro", false),
    GBP("GBP", "£", "British Pound", false);

    companion object {
        fun fromCode(code: String?): CurrencyOption = entries.firstOrNull { it.code == code } ?: INR
    }
}

/** Expense total for one category; [fraction] is its share (0..1) of all expenses in the period. */
data class CategorySpend(val category: Category, val amount: BigDecimal, val fraction: Float)

data class MonthTotals(val month: YearMonth, val income: BigDecimal, val expense: BigDecimal) {
    val net: BigDecimal get() = income - expense
}
