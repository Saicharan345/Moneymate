package com.saicharan.moneymate.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saicharan.moneymate.BuildConfig
import com.saicharan.moneymate.domain.model.CurrencyOption
import com.saicharan.moneymate.ui.AppViewModelProvider
import com.saicharan.moneymate.ui.budget.BudgetDialog
import com.saicharan.moneymate.ui.components.ErrorState
import com.saicharan.moneymate.ui.components.LoadingState
import com.saicharan.moneymate.ui.components.ScreenHeader
import com.saicharan.moneymate.util.MoneyFormatter

private enum class ConfirmAction { CLEAR_SAMPLES, CLEAR_ALL }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirm by remember { mutableStateOf<ConfirmAction?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    Column(modifier.fillMaxSize()) {
        ScreenHeader("Settings", Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(state.error ?: "")
            else -> Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsCard("Monthly budget") {
                    Text(
                        state.budget?.let { MoneyFormatter.format(it, state.currency) } ?: "Not set",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { showBudgetDialog = true }) {
                        Text(if (state.budget == null) "Set budget" else "Change budget")
                    }
                }

                SettingsCard("Currency display") {
                    Text(
                        "Changes the symbol shown next to amounts. Values are not converted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CurrencyOption.entries.forEach { option ->
                            FilterChip(
                                selected = state.currency == option,
                                onClick = { viewModel.setCurrency(option) },
                                label = { Text("${option.symbol} ${option.code}") }
                            )
                        }
                    }
                }

                SettingsCard("Data") {
                    Text(
                        if (state.sampleCount > 0) "${state.sampleCount} sample transactions are included so you can explore the app."
                        else "No sample data is present.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { confirm = ConfirmAction.CLEAR_SAMPLES },
                        enabled = state.sampleCount > 0
                    ) { Text("Clear sample data") }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { confirm = ConfirmAction.CLEAR_ALL },
                        enabled = state.totalCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete all transactions") }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text("Privacy", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(
                                "Your transactions and budget are stored only on this device. MoneyMate has no account, " +
                                    "no server and no internet permission, so your financial data is never sent anywhere.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                SettingsCard("About") {
                    Text("MoneyMate — Personal Finance Manager", style = MaterialTheme.typography.titleSmall)
                    Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "A portfolio project built with Kotlin, Jetpack Compose, Room and MVVM. " +
                            "Not affiliated with or endorsed by Moneyview.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showBudgetDialog) {
        BudgetDialog(
            currentBudget = state.budget,
            currency = state.currency,
            onDismiss = { showBudgetDialog = false },
            onSave = {
                viewModel.setBudget(it)
                showBudgetDialog = false
            }
        )
    }

    confirm?.let { action ->
        val isAll = action == ConfirmAction.CLEAR_ALL
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text(if (isAll) "Delete all transactions?" else "Clear sample data?") },
            text = {
                Text(
                    if (isAll) "Every transaction, including your own, will be permanently deleted. This can't be undone."
                    else "Only the sample transactions will be removed. Your own transactions stay."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (isAll) viewModel.clearAllTransactions() else viewModel.clearSampleData()
                    confirm = null
                }) { Text(if (isAll) "Delete all" else "Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
