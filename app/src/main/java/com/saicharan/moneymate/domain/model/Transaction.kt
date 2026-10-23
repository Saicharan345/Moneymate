package com.saicharan.moneymate.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class Transaction(
    val id: Long = 0L,
    val title: String,
    val amount: BigDecimal,
    val type: TransactionType,
    val category: Category,
    val date: LocalDate,
    val notes: String = "",
    val isSample: Boolean = false
)
