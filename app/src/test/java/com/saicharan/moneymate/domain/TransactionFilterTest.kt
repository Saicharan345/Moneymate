package com.saicharan.moneymate.domain

import com.saicharan.moneymate.domain.model.Category
import com.saicharan.moneymate.domain.model.TransactionType
import com.saicharan.moneymate.domain.usecase.TransactionFilter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TransactionFilterTest {

    private val d = LocalDate.of(2026, 10, 1)
    private val list = listOf(
        expense("10", d, "Coffee beans", Category.FOOD, id = 1),
        expense("20", d.plusDays(5), "Bus pass", Category.TRANSPORT, id = 2),
        income("30", d.plusDays(2), "Salary", Category.SALARY, id = 3),
        expense("40", d.plusDays(5), "Coffee shop", Category.FOOD, id = 4)
    )

    @Test
    fun `no filter returns all sorted newest first with id as tie breaker`() {
        assertEquals(listOf(4L, 2L, 3L, 1L), TransactionFilter().apply(list).map { it.id })
    }

    @Test
    fun `search matches title case-insensitively`() {
        assertEquals(listOf(4L, 1L), TransactionFilter(query = "COFFEE").apply(list).map { it.id })
    }

    @Test
    fun `type filter works`() {
        assertEquals(listOf(3L), TransactionFilter(type = TransactionType.INCOME).apply(list).map { it.id })
    }

    @Test
    fun `category filter works and combines with search`() {
        assertEquals(listOf(4L, 1L), TransactionFilter(category = Category.FOOD).apply(list).map { it.id })
        assertEquals(listOf(4L), TransactionFilter(query = "shop", category = Category.FOOD).apply(list).map { it.id })
    }

    @Test
    fun `no match returns empty list`() {
        assertEquals(emptyList<Long>(), TransactionFilter(query = "zzz").apply(list).map { it.id })
    }
}
