package com.saicharan.moneymate.domain

import com.saicharan.moneymate.domain.model.Category
import com.saicharan.moneymate.domain.model.Transaction
import com.saicharan.moneymate.domain.model.TransactionType
import java.math.BigDecimal
import java.time.LocalDate

internal fun income(amount: String, date: LocalDate, title: String = "Income", category: Category = Category.SALARY, id: Long = 0) =
    Transaction(id, title, BigDecimal(amount), TransactionType.INCOME, category, date)

internal fun expense(amount: String, date: LocalDate, title: String = "Expense", category: Category = Category.FOOD, id: Long = 0) =
    Transaction(id, title, BigDecimal(amount), TransactionType.EXPENSE, category, date)
