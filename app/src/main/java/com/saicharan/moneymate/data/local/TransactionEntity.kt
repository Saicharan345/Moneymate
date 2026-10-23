package com.saicharan.moneymate.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Amount is stored as minor units (paise/cents) in a Long to avoid any floating-point error.
 * Date is stored as epoch day. Type and category are stored by enum name.
 */
@Entity(tableName = "transactions", indices = [Index(value = ["epochDay"])])
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val amountMinor: Long,
    val type: String,
    val category: String,
    val epochDay: Long,
    val notes: String,
    val isSample: Boolean
)
