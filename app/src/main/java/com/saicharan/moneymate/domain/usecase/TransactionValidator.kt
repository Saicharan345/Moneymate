package com.saicharan.moneymate.domain.usecase

import com.saicharan.moneymate.domain.model.Category
import com.saicharan.moneymate.domain.model.TransactionType
import java.math.BigDecimal
import java.time.LocalDate

enum class FormField { TITLE, AMOUNT, CATEGORY, DATE, NOTES }

data class TransactionInput(
    val title: String,
    val amountText: String,
    val type: TransactionType,
    val category: Category?,
    val date: LocalDate?,
    val notes: String
)

sealed interface ValidationResult {
    data class Valid(
        val title: String,
        val amount: BigDecimal,
        val category: Category,
        val date: LocalDate,
        val notes: String
    ) : ValidationResult

    data class Invalid(val errors: Map<FormField, String>) : ValidationResult
}

object TransactionValidator {

    const val MAX_TITLE_LENGTH = 60
    const val MAX_NOTES_LENGTH = 200

    fun validate(input: TransactionInput, today: LocalDate): ValidationResult {
        val errors = linkedMapOf<FormField, String>()

        val title = input.title.trim()
        if (title.isEmpty()) errors[FormField.TITLE] = "Enter a title"
        else if (title.length > MAX_TITLE_LENGTH) errors[FormField.TITLE] = "Title must be $MAX_TITLE_LENGTH characters or fewer"

        var amount: BigDecimal? = null
        when (val parsed = MoneyParser.parse(input.amountText)) {
            is MoneyParseResult.Success -> amount = parsed.value
            is MoneyParseResult.Failure -> errors[FormField.AMOUNT] = parsed.message
        }

        val category = input.category
        if (category == null) errors[FormField.CATEGORY] = "Choose a category"
        else if (category.type != input.type) errors[FormField.CATEGORY] = "Category doesn't match the transaction type"

        val date = input.date
        if (date == null) errors[FormField.DATE] = "Choose a date"
        else if (date.isAfter(today)) errors[FormField.DATE] = "Date can't be in the future"

        val notes = input.notes.trim()
        if (notes.length > MAX_NOTES_LENGTH) errors[FormField.NOTES] = "Notes must be $MAX_NOTES_LENGTH characters or fewer"

        if (errors.isNotEmpty() || amount == null || category == null || date == null) {
            return ValidationResult.Invalid(errors)
        }
        return ValidationResult.Valid(title, amount, category, date, notes)
    }
}
