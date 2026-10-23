package com.saicharan.moneymate.domain.usecase

import com.saicharan.moneymate.domain.model.BudgetStatus
import java.math.BigDecimal
import java.math.RoundingMode

object BudgetCalculator {

    const val WARNING_THRESHOLD_PERCENT = 80

    private val HUNDRED = BigDecimal(100)
    private val THRESHOLD = BigDecimal(WARNING_THRESHOLD_PERCENT)

    /** Percentage of the budget used, rounded to 2 decimals. 0 when no budget is set. May exceed 100. */
    fun usagePercent(spent: BigDecimal, budget: BigDecimal?): Double {
        if (budget == null || budget.signum() <= 0) return 0.0
        return spent.multiply(HUNDRED).divide(budget, 2, RoundingMode.HALF_UP).toDouble()
    }

    /** Budget minus spent. Negative when the budget has been exceeded. */
    fun remaining(budget: BigDecimal, spent: BigDecimal): BigDecimal = budget - spent

    /** Progress-bar fraction clamped to 0..1. */
    fun progress(spent: BigDecimal, budget: BigDecimal?): Float =
        (usagePercent(spent, budget) / 100.0).coerceIn(0.0, 1.0).toFloat()

    fun status(spent: BigDecimal, budget: BigDecimal?): BudgetStatus {
        if (budget == null || budget.signum() <= 0) return BudgetStatus.NOT_SET
        return when {
            spent > budget -> BudgetStatus.EXCEEDED
            spent.multiply(HUNDRED) >= budget.multiply(THRESHOLD) -> BudgetStatus.WARNING
            else -> BudgetStatus.ON_TRACK
        }
    }
}
