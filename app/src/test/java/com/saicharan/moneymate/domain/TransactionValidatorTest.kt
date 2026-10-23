package com.saicharan.moneymate.domain

import com.saicharan.moneymate.domain.model.Category
import com.saicharan.moneymate.domain.model.TransactionType
import com.saicharan.moneymate.domain.usecase.FormField
import com.saicharan.moneymate.domain.usecase.MoneyParseResult
import com.saicharan.moneymate.domain.usecase.MoneyParser
import com.saicharan.moneymate.domain.usecase.TransactionInput
import com.saicharan.moneymate.domain.usecase.TransactionValidator
import com.saicharan.moneymate.domain.usecase.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class TransactionValidatorTest {

    private val today = LocalDate.of(2026, 10, 23)

    private fun input(
        title: String = "Lunch",
        amount: String = "250.50",
        type: TransactionType = TransactionType.EXPENSE,
        category: Category? = Category.FOOD,
        date: LocalDate? = today,
        notes: String = ""
    ) = TransactionInput(title, amount, type, category, date, notes)

    private fun errors(i: TransactionInput): Map<FormField, String> =
        (TransactionValidator.validate(i, today) as ValidationResult.Invalid).errors

    @Test
    fun `valid input produces trimmed values and parsed amount`() {
        val result = TransactionValidator.validate(input(title = "  Lunch  ", amount = "1,250.50", notes = " ok "), today)
        result as ValidationResult.Valid
        assertEquals("Lunch", result.title)
        assertEquals(BigDecimal("1250.50"), result.amount)
        assertEquals("ok", result.notes)
    }

    @Test
    fun `blank title is rejected`() {
        assertTrue(FormField.TITLE in errors(input(title = "   ")))
    }

    @Test
    fun `overlong title is rejected`() {
        assertTrue(FormField.TITLE in errors(input(title = "x".repeat(TransactionValidator.MAX_TITLE_LENGTH + 1))))
    }

    @Test
    fun `empty, non numeric, zero, negative and too precise amounts are rejected`() {
        listOf("", "abc", "0", "0.00", "-5", "1.234", "1.2.3").forEach {
            assertTrue("'$it' should be invalid", FormField.AMOUNT in errors(input(amount = it)))
        }
    }

    @Test
    fun `huge amount is rejected`() {
        assertTrue(FormField.AMOUNT in errors(input(amount = "1000000000")))
    }

    @Test
    fun `missing category is rejected`() {
        assertTrue(FormField.CATEGORY in errors(input(category = null)))
    }

    @Test
    fun `category must match transaction type`() {
        assertTrue(FormField.CATEGORY in errors(input(type = TransactionType.INCOME, category = Category.FOOD)))
    }

    @Test
    fun `future date is rejected and today is accepted`() {
        assertTrue(FormField.DATE in errors(input(date = today.plusDays(1))))
        assertTrue(TransactionValidator.validate(input(date = today), today) is ValidationResult.Valid)
    }

    @Test
    fun `overlong notes are rejected`() {
        assertTrue(FormField.NOTES in errors(input(notes = "n".repeat(TransactionValidator.MAX_NOTES_LENGTH + 1))))
    }

    @Test
    fun `multiple problems are all reported`() {
        val e = errors(input(title = "", amount = "", category = null))
        assertEquals(setOf(FormField.TITLE, FormField.AMOUNT, FormField.CATEGORY), e.keys)
    }

    @Test
    fun `money parser accepts whole and two decimal values`() {
        assertEquals(MoneyParseResult.Success(BigDecimal("5000")), MoneyParser.parse("5000"))
        assertEquals(MoneyParseResult.Success(BigDecimal("0.50")), MoneyParser.parse("0.50"))
    }
}
