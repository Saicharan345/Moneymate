package com.saicharan.moneymate.util

import com.saicharan.moneymate.domain.model.CurrencyOption
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class MoneyFormatterTest {

    private fun inr(v: String) = MoneyFormatter.format(BigDecimal(v), CurrencyOption.INR)

    @Test
    fun `INR uses lakh and crore grouping`() {
        assertEquals("₹0.00", inr("0"))
        assertEquals("₹999.00", inr("999"))
        assertEquals("₹1,000.00", inr("1000"))
        assertEquals("₹1,00,000.00", inr("100000"))
        assertEquals("₹12,34,567.89", inr("1234567.89"))
        assertEquals("₹1,23,45,678.00", inr("12345678"))
    }

    @Test
    fun `negative amounts get a leading minus`() {
        assertEquals("-₹1,500.50", inr("-1500.50"))
    }

    @Test
    fun `values are rounded to two decimals`() {
        assertEquals("₹10.13", inr("10.125"))
    }

    @Test
    fun `other currencies use western grouping`() {
        assertEquals("$1,234,567.00", MoneyFormatter.format(BigDecimal("1234567"), CurrencyOption.USD))
    }

    @Test
    fun `amount input sanitiser keeps digits and one dot`() {
        assertEquals("12.50", sanitizeAmountInput("1a2.5.0"))
        assertEquals("", sanitizeAmountInput("abc"))
    }
}
