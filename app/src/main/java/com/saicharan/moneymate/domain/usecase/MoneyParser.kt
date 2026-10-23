package com.saicharan.moneymate.domain.usecase

import java.math.BigDecimal

sealed interface MoneyParseResult {
    data class Success(val value: BigDecimal) : MoneyParseResult
    data class Failure(val message: String) : MoneyParseResult
}

/** Parses user-typed amounts safely into BigDecimal (max 2 decimal places, > 0, bounded). */
object MoneyParser {

    val MAX_AMOUNT: BigDecimal = BigDecimal("999999999.99")
    private val NUMBER = Regex("""\d+(\.\d+)?""")

    fun parse(text: String): MoneyParseResult {
        val cleaned = text.trim().replace(",", "").replace(" ", "")
        if (cleaned.isEmpty()) return MoneyParseResult.Failure("Enter an amount")
        if (!NUMBER.matches(cleaned)) return MoneyParseResult.Failure("Enter a valid number")
        val decimals = cleaned.substringAfter('.', "")
        if (decimals.length > 2) return MoneyParseResult.Failure("Use at most 2 decimal places")
        val value = BigDecimal(cleaned)
        if (value.signum() <= 0) return MoneyParseResult.Failure("Amount must be greater than zero")
        if (value > MAX_AMOUNT) return MoneyParseResult.Failure("Amount is too large")
        return MoneyParseResult.Success(value)
    }
}
