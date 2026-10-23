package com.saicharan.moneymate.data.repository

import com.saicharan.moneymate.data.local.TransactionDao
import com.saicharan.moneymate.data.local.toDomain
import com.saicharan.moneymate.data.local.toEntity
import com.saicharan.moneymate.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    suspend fun getTransaction(id: Long): Transaction?
    suspend fun addTransaction(transaction: Transaction): Long
    suspend fun addAll(transactions: List<Transaction>)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: Long)
    suspend fun clearSampleTransactions(): Int
    suspend fun clearAllTransactions()
}

class RoomTransactionRepository(private val dao: TransactionDao) : TransactionRepository {

    override fun observeTransactions(): Flow<List<Transaction>> =
        dao.observeAll()
            .map { list -> list.map { entity -> entity.toDomain() } }
            .flowOn(Dispatchers.Default)

    override suspend fun getTransaction(id: Long): Transaction? = dao.getById(id)?.toDomain()

    override suspend fun addTransaction(transaction: Transaction): Long =
        dao.insert(transaction.copy(id = 0L).toEntity())

    override suspend fun addAll(transactions: List<Transaction>) =
        dao.insertAll(transactions.map { it.copy(id = 0L).toEntity() })

    override suspend fun updateTransaction(transaction: Transaction) = dao.update(transaction.toEntity())

    override suspend fun deleteTransaction(id: Long) = dao.deleteById(id)

    override suspend fun clearSampleTransactions(): Int = dao.deleteSamples()

    override suspend fun clearAllTransactions() = dao.deleteAll()
}
