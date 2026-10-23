package com.saicharan.moneymate.domain.usecase

import com.saicharan.moneymate.domain.model.Category
import com.saicharan.moneymate.domain.model.Transaction
import com.saicharan.moneymate.domain.model.TransactionType

data class TransactionFilter(
    val query: String = "",
    val type: TransactionType? = null,
    val category: Category? = null
) {
    val isActive: Boolean get() = query.isNotBlank() || type != null || category != null

    /** Applies title search + type + category filters; result is sorted newest first. */
    fun apply(transactions: List<Transaction>): List<Transaction> {
        val q = query.trim()
        return transactions
            .filter { q.isEmpty() || it.title.contains(q, ignoreCase = true) }
            .filter { type == null || it.type == type }
            .filter { category == null || it.category == category }
            .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id })
    }
}
