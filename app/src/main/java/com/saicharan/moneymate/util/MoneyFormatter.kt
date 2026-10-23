package com.saicharan.moneymate.util

import com.saicharan.moneymate.domain.model.CurrencyOption
import java.math.BigDecimal
import java.math.RoundingMode

/** Locale-independent money formatting (INR uses lakh/crore grouping, e.g. ₹1,23,456.00). */
object MoneyFormatter {

    fun format(amount: BigDecimal, currency: CurrencyOption = CurrencyOption.INR): String {
        val rounded = amount.setScale(2, RoundingMode.HALF_UP)
        val plain = rounded.abs().toPlainString()
        val whole = plain.substringBefore('.')
        val fraction = plain.substringAfter('.', "00")
        val grouped = if (currency.indianGrouping) groupIndian(whole) else groupWestern(whole)
        val sign = if (rounded.signum() < 0) "-" else ""
        return "$sign${currency.symbol}$grouped.$fraction"
    }

    internal fun groupIndian(digits: String): String {
        if (digits.length <= 3) return digits
        val lastThree = digits.takeLast(3)
        var rest = digits.dropLast(3)
        val parts = mutableListOf<String>()
        while (rest.isNotEmpty()) {
            parts.add(0, rest.takeLast(2))
            rest = rest.dropLast(2)
        }
        return parts.joinToString(",") + "," + lastThree
    }

    internal fun groupWestern(digits: String): String {
        var rest = digits
        val parts = mutableListOf<String>()
        while (rest.isNotEmpty()) {
            parts.add(0, rest.takeLast(3))
            rest = rest.dropLast(3)
        }
        return parts.joinToString(",")
    }
}
