package com.saicharan.moneymate.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.saicharan.moneymate.domain.model.CurrencyOption
import com.saicharan.moneymate.ui.budget.BudgetDialog
import com.saicharan.moneymate.ui.theme.MoneyMateTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

class BudgetDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun saveWithEmptyInputShowsValidationError() {
        var saved: BigDecimal? = BigDecimal.ONE
        composeRule.setContent {
            MoneyMateTheme { BudgetDialog(null, CurrencyOption.INR, onDismiss = {}, onSave = { saved = it }) }
        }
        composeRule.onNodeWithText("Save").performClick()
        composeRule.onNodeWithText("Enter an amount").assertExists()
        assertEquals(BigDecimal.ONE, saved) // callback must not fire
    }

    @Test
    fun saveWithValidAmountInvokesCallback() {
        var saved: BigDecimal? = null
        composeRule.setContent {
            MoneyMateTheme { BudgetDialog(null, CurrencyOption.INR, onDismiss = {}, onSave = { saved = it }) }
        }
        composeRule.onNodeWithTag("budget_input").performTextInput("25000")
        composeRule.onNodeWithText("Save").performClick()
        assertEquals(BigDecimal("25000"), saved)
    }

    @Test
    fun removeBudgetPassesNull() {
        var saved: BigDecimal? = BigDecimal.TEN
        composeRule.setContent {
            MoneyMateTheme { BudgetDialog(BigDecimal("500"), CurrencyOption.INR, onDismiss = {}, onSave = { saved = it }) }
        }
        composeRule.onNodeWithText("Remove budget").performClick()
        assertNull(saved)
    }
}
