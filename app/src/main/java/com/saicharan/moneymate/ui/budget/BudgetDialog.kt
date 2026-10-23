package com.saicharan.moneymate.ui.budget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.saicharan.moneymate.domain.model.CurrencyOption
import com.saicharan.moneymate.domain.usecase.MoneyParseResult
import com.saicharan.moneymate.domain.usecase.MoneyParser
import com.saicharan.moneymate.util.sanitizeAmountInput
import java.math.BigDecimal

/** Dialog to set, change or remove the monthly budget. [onSave] receives null when the budget is removed. */
@Composable
fun BudgetDialog(
    currentBudget: BigDecimal?,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onSave: (BigDecimal?) -> Unit
) {
    var text by remember { mutableStateOf(currentBudget?.stripTrailingZeros()?.toPlainString() ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly budget") },
        text = {
            Column {
                Text(
                    "Your spending limit for each calendar month. Stored only on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = sanitizeAmountInput(it)
                        error = null
                    },
                    label = { Text("Budget amount") },
                    prefix = { Text(currency.symbol) },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { message -> { Text(message) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_input")
                )
                if (currentBudget != null) {
                    TextButton(onClick = { onSave(null) }) { Text("Remove budget") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when (val result = MoneyParser.parse(text)) {
                    is MoneyParseResult.Success -> onSave(result.value)
                    is MoneyParseResult.Failure -> error = result.message
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
