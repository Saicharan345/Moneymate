package com.saicharan.moneymate.domain

import com.saicharan.moneymate.domain.model.Category
import com.saicharan.moneymate.domain.usecase.FinanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class FinanceCalculatorTest {

    private val oct = YearMonth.of(2026, 10)
    private val d1 = LocalDate.of(2026, 10, 5)
    private val sep = LocalDate.of(2026, 9, 28)

    @Test
    fun `balance is income minus expenses across all months`() {
        val list = listOf(income("1000.00", d1), income("500.50", sep), expense("250.25", d1))
        assertEquals(BigDecimal("1250.25"), FinanceCalculator.totalBalance(list))
    }

    @Test
    fun `balance of empty list is zero`() {
        assertEquals(0, FinanceCalculator.totalBalance(emptyList()).compareTo(BigDecimal.ZERO))
    }

    @Test
    fun `balance can be negative`() {
        assertEquals(BigDecimal("-40.00"), FinanceCalculator.totalBalance(listOf(income("10.00", d1), expense("50.00", d1))))
    }

    @Test
    fun `decimal sums are exact with no floating point drift`() {
        val list = List(10) { expense("0.10", d1) }
        assertEquals(BigDecimal("1.00"), FinanceCalculator.totalExpense(list))
    }

    @Test
    fun `monthly totals only include the requested month`() {
        val list = listOf(
            income("1000.00", d1), income("200.00", sep),
            expense("300.00", d1), expense("999.00", sep)
        )
        assertEquals(BigDecimal("1000.00"), FinanceCalculator.monthIncome(list, oct))
        assertEquals(BigDecimal("300.00"), FinanceCalculator.monthExpense(list, oct))
    }

    @Test
    fun `month boundaries are respected`() {
        val list = listOf(
            expense("1.00", LocalDate.of(2026, 10, 1)),
            expense("2.00", LocalDate.of(2026, 10, 31)),
            expense("4.00", LocalDate.of(2026, 11, 1))
        )
        assertEquals(BigDecimal("3.00"), FinanceCalculator.monthExpense(list, oct))
    }

    @Test
    fun `category breakdown sums expenses, ignores income and sorts descending`() {
        val list = listOf(
            expense("100.00", d1, category = Category.FOOD),
            expense("50.00", d1, category = Category.FOOD),
            expense("50.00", d1, category = Category.BILLS),
            income("5000.00", d1)
        )
        val result = FinanceCalculator.categoryBreakdown(list, oct)
        assertEquals(2, result.size)
        assertEquals(Category.FOOD, result[0].category)
        assertEquals(BigDecimal("150.00"), result[0].amount)
        assertEquals(0.75f, result[0].fraction, 0.0001f)
        assertEquals(0.25f, result[1].fraction, 0.0001f)
    }

    @Test
    fun `category breakdown is empty when there are no expenses`() {
        assertTrue(FinanceCalculator.categoryBreakdown(listOf(income("10.00", d1)), oct).isEmpty())
    }

    @Test
    fun `monthly totals returns requested count oldest first`() {
        val list = listOf(expense("10.00", d1), expense("20.00", sep))
        val trend = FinanceCalculator.monthlyTotals(list, oct, 3)
        assertEquals(3, trend.size)
        assertEquals(YearMonth.of(2026, 8), trend[0].month)
        assertEquals(YearMonth.of(2026, 10), trend[2].month)
        assertEquals(BigDecimal("20.00"), trend[1].expense)
        assertEquals(BigDecimal("10.00"), trend[2].expense)
    }
}
