package com.saicharan.moneymate.domain.usecase

import com.saicharan.moneymate.data.repository.SettingsRepository
import com.saicharan.moneymate.data.repository.TransactionRepository
import com.saicharan.moneymate.domain.model.Category
import com.saicharan.moneymate.domain.model.Transaction
import com.saicharan.moneymate.domain.model.TransactionType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

/** Inserts clearly-flagged sample transactions on the very first launch only. */
class SeedSampleDataUseCase(
    private val transactions: TransactionRepository,
    private val settings: SettingsRepository
) {
    suspend fun seedIfFirstLaunch(today: LocalDate) {
        if (settings.isSampleSeeded()) return
        transactions.addAll(SampleData.create(today))
        if (settings.currentBudget() == null) settings.setBudget(BigDecimal("40000"))
        settings.markSampleSeeded()
    }
}

object SampleData {
    fun create(today: LocalDate): List<Transaction> {
        val month = YearMonth.from(today)
        val prev = month.minusMonths(1)
        fun cur(day: Int): LocalDate {
            val d = month.atDay(day)
            return if (d.isAfter(today)) today else d
        }
        fun old(day: Int): LocalDate = prev.atDay(day)
        fun tx(title: String, amount: String, type: TransactionType, category: Category, date: LocalDate) =
            Transaction(
                title = title,
                amount = BigDecimal(amount),
                type = type,
                category = category,
                date = date,
                notes = "Sample data",
                isSample = true
            )
        val e = TransactionType.EXPENSE
        val i = TransactionType.INCOME
        return listOf(
            tx("Monthly salary", "75000.00", i, Category.SALARY, cur(1)),
            tx("Freelance project", "12000.00", i, Category.FREELANCE, cur(5)),
            tx("House rent", "18000.00", e, Category.BILLS, cur(3)),
            tx("Groceries", "3450.75", e, Category.FOOD, cur(6)),
            tx("Cab rides", "640.00", e, Category.TRANSPORT, cur(8)),
            tx("Electricity bill", "2150.00", e, Category.BILLS, cur(10)),
            tx("Movie night", "900.00", e, Category.ENTERTAINMENT, cur(12)),
            tx("New headphones", "4299.00", e, Category.SHOPPING, cur(14)),
            tx("Pharmacy", "780.00", e, Category.HEALTH, cur(16)),
            tx("Online course", "1999.00", e, Category.EDUCATION, cur(18)),
            tx("Dinner out", "1250.00", e, Category.FOOD, cur(20)),
            tx("Monthly salary", "75000.00", i, Category.SALARY, old(1)),
            tx("House rent", "18000.00", e, Category.BILLS, old(3)),
            tx("Groceries", "8200.00", e, Category.FOOD, old(9)),
            tx("Fuel & cabs", "2300.00", e, Category.TRANSPORT, old(12)),
            tx("Weekend trip", "6500.00", e, Category.ENTERTAINMENT, old(21))
        )
    }
}
