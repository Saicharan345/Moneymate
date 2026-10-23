package com.saicharan.moneymate.data.local

import com.saicharan.moneymate.domain.model.Category
import com.saicharan.moneymate.domain.model.Transaction
import com.saicharan.moneymate.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

fun BigDecimal.toMinorUnits(): Long =
    setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact()

fun Long.fromMinorUnits(): BigDecimal = BigDecimal.valueOf(this, 2)

fun TransactionEntity.toDomain(): Transaction {
    val txType = TransactionType.fromName(type) ?: TransactionType.EXPENSE
    val cat = Category.fromName(category)
        ?: if (txType == TransactionType.INCOME) Category.OTHER_INCOME else Category.OTHER_EXPENSE
    return Transaction(
        id = id,
        title = title,
        amount = amountMinor.fromMinorUnits(),
        type = txType,
        category = cat,
        date = LocalDate.ofEpochDay(epochDay),
        notes = notes,
        isSample = isSample
    )
}

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    title = title,
    amountMinor = amount.toMinorUnits(),
    type = type.name,
    category = category.name,
    epochDay = date.toEpochDay(),
    notes = notes,
    isSample = isSample
)
