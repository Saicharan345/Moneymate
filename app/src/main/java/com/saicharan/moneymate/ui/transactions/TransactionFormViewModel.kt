package com.saicharan.moneymate.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicharan.moneymate.data.repository.SettingsRepository
import com.saicharan.moneymate.data.repository.TransactionRepository
import com.saicharan.moneymate.domain.model.Category
import com.saicharan.moneymate.domain.model.CurrencyOption
import com.saicharan.moneymate.domain.model.Transaction
import com.saicharan.moneymate.domain.model.TransactionType
import com.saicharan.moneymate.domain.usecase.FormField
import com.saicharan.moneymate.domain.usecase.TransactionInput
import com.saicharan.moneymate.domain.usecase.TransactionValidator
import com.saicharan.moneymate.domain.usecase.ValidationResult
import com.saicharan.moneymate.util.sanitizeAmountInput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

const val TRANSACTION_ID_ARG = "id"

data class TransactionFormState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val loadError: String? = null,
    val title: String = "",
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: Category? = null,
    val date: LocalDate = LocalDate.now(),
    val notes: String = "",
    val currency: CurrencyOption = CurrencyOption.INR,
    val errors: Map<FormField, String> = emptyMap(),
    val saveError: String? = null,
    /** Set once the save/delete finished; the screen shows it as a snackbar and navigates back. */
    val completedMessage: String? = null
)

class TransactionFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TransactionRepository,
    settings: SettingsRepository
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<Long>(TRANSACTION_ID_ARG) ?: -1L
    private val isEditing = transactionId > 0

    private val _state = MutableStateFlow(TransactionFormState(isEditing = isEditing, isLoading = isEditing))
    val state: StateFlow<TransactionFormState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settings.currency.collect { c -> _state.update { it.copy(currency = c) } }
        }
        if (isEditing) loadTransaction()
    }

    private fun loadTransaction() {
        viewModelScope.launch {
            try {
                val tx = repository.getTransaction(transactionId)
                if (tx == null) {
                    _state.update { it.copy(isLoading = false, loadError = "This transaction no longer exists.") }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            title = tx.title,
                            amount = tx.amount.toPlainString(),
                            type = tx.type,
                            category = tx.category,
                            date = tx.date,
                            notes = tx.notes
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _state.update { it.copy(isLoading = false, loadError = "Couldn't load this transaction.") }
            }
        }
    }

    fun onTitleChange(value: String) = _state.update { it.copy(title = value, errors = it.errors - FormField.TITLE) }

    fun onAmountChange(value: String) =
        _state.update { it.copy(amount = sanitizeAmountInput(value), errors = it.errors - FormField.AMOUNT) }

    fun onTypeChange(type: TransactionType) = _state.update {
        it.copy(
            type = type,
            category = it.category?.takeIf { c -> c.type == type },
            errors = it.errors - FormField.CATEGORY
        )
    }

    fun onCategoryChange(category: Category) =
        _state.update { it.copy(category = category, errors = it.errors - FormField.CATEGORY) }

    fun onDateChange(date: LocalDate) = _state.update { it.copy(date = date, errors = it.errors - FormField.DATE) }

    fun onNotesChange(value: String) = _state.update { it.copy(notes = value, errors = it.errors - FormField.NOTES) }

    fun save() {
        val current = _state.value
        if (current.isSaving || current.isLoading) return

        val result = TransactionValidator.validate(
            TransactionInput(current.title, current.amount, current.type, current.category, current.date, current.notes),
            today = LocalDate.now()
        )
        when (result) {
            is ValidationResult.Invalid -> _state.update { it.copy(errors = result.errors) }
            is ValidationResult.Valid -> viewModelScope.launch {
                _state.update { it.copy(isSaving = true, saveError = null, errors = emptyMap()) }
                try {
                    val tx = Transaction(
                        id = if (isEditing) transactionId else 0L,
                        title = result.title,
                        amount = result.amount,
                        type = current.type,
                        category = result.category,
                        date = result.date,
                        notes = result.notes,
                        isSample = false
                    )
                    if (isEditing) repository.updateTransaction(tx) else repository.addTransaction(tx)
                    _state.update {
                        it.copy(isSaving = false, completedMessage = if (isEditing) "Transaction updated" else "Transaction added")
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    _state.update { it.copy(isSaving = false, saveError = "Couldn't save the transaction. Please try again.") }
                }
            }
        }
    }

    fun delete() {
        if (!isEditing || _state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, saveError = null) }
            try {
                repository.deleteTransaction(transactionId)
                _state.update { it.copy(isSaving = false, completedMessage = "Transaction deleted") }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _state.update { it.copy(isSaving = false, saveError = "Couldn't delete the transaction. Please try again.") }
            }
        }
    }
}
