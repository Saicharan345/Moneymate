package com.saicharan.moneymate.domain

import com.saicharan.moneymate.domain.model.BudgetStatus
import com.saicharan.moneymate.domain.usecase.BudgetCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class BudgetCalculatorTest {

    private fun bd(v: String) = BigDecimal(v)

    @Test
    fun `usage percent is spent over budget`() {
        assertEquals(25.0, BudgetCalculator.usagePercent(bd("250"), bd("1000")), 0.001)
        assertEquals(33.33, BudgetCalculator.usagePercent(bd("100"), bd("300")), 0.001)
    }

    @Test
    fun `usage percent can exceed 100`() {
        assertEquals(150.0, BudgetCalculator.usagePercent(bd("1500"), bd("1000")), 0.001)
    }

    @Test
    fun `usage percent is zero without a budget`() {
        assertEquals(0.0, BudgetCalculator.usagePercent(bd("100"), null), 0.0)
        assertEquals(0.0, BudgetCalculator.usagePercent(bd("100"), BigDecimal.ZERO), 0.0)
    }

    @Test
    fun `remaining budget is budget minus spent and may be negative`() {
        assertEquals(bd("700"), BudgetCalculator.remaining(bd("1000"), bd("300")))
        assertEquals(bd("-200"), BudgetCalculator.remaining(bd("1000"), bd("1200")))
    }

    @Test
    fun `progress is clamped between 0 and 1`() {
        assertEquals(0.5f, BudgetCalculator.progress(bd("500"), bd("1000")), 0.0001f)
        assertEquals(1f, BudgetCalculator.progress(bd("5000"), bd("1000")), 0.0001f)
        assertEquals(0f, BudgetCalculator.progress(bd("5"), null), 0.0001f)
    }

    @Test
    fun `status is not set without a positive budget`() {
        assertEquals(BudgetStatus.NOT_SET, BudgetCalculator.status(bd("10"), null))
        assertEquals(BudgetStatus.NOT_SET, BudgetCalculator.status(bd("10"), BigDecimal.ZERO))
    }

    @Test
    fun `status is on track below 80 percent`() {
        assertEquals(BudgetStatus.ON_TRACK, BudgetCalculator.status(bd("799.99"), bd("1000")))
        assertEquals(BudgetStatus.ON_TRACK, BudgetCalculator.status(BigDecimal.ZERO, bd("1000")))
    }

    @Test
    fun `status warns at exactly 80 percent`() {
        assertEquals(BudgetStatus.WARNING, BudgetCalculator.status(bd("800"), bd("1000")))
    }

    @Test
    fun `status warns between 80 and 100 percent inclusive`() {
        assertEquals(BudgetStatus.WARNING, BudgetCalculator.status(bd("950"), bd("1000")))
        assertEquals(BudgetStatus.WARNING, BudgetCalculator.status(bd("1000"), bd("1000")))
    }

    @Test
    fun `status is exceeded above 100 percent`() {
        assertEquals(BudgetStatus.EXCEEDED, BudgetCalculator.status(bd("1000.01"), bd("1000")))
    }
}
