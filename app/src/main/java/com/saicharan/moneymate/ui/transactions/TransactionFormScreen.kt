package com.saicharan.moneymate.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saicharan.moneymate.domain.model.Category
import com.saicharan.moneymate.domain.model.TransactionType
import com.saicharan.moneymate.domain.usecase.FormField
import com.saicharan.moneymate.domain.usecase.TransactionValidator
import com.saicharan.moneymate.ui.AppViewModelProvider
import com.saicharan.moneymate.ui.components.ErrorState
import com.saicharan.moneymate.ui.components.LoadingState
import com.saicharan.moneymate.ui.components.icon
import com.saicharan.moneymate.util.fromPickerMillis
import com.saicharan.moneymate.util.toDisplayDate
import com.saicharan.moneymate.util.toPickerMillis
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionFormScreen(
    onBack: () -> Unit,
    onFinished: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.completedMessage) {
        state.completedMessage?.let(onFinished)
    }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                if (state.isEditing) "Edit transaction" else "Add transaction",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            if (state.isEditing && !state.isLoading && state.loadError == null) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete transaction", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        when {
            state.isLoading -> LoadingState()
            state.loadError != null -> ErrorState(state.loadError ?: "")
            else -> Column(
                modifier = Modifier
                    .weight(1f)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TransactionType.entries.forEach { type ->
                        FilterChip(
                            selected = state.type == type,
                            onClick = { viewModel.onTypeChange(type) },
                            label = { Text(type.label, modifier = Modifier.padding(vertical = 6.dp)) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("type_${type.name}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (type == TransactionType.INCOME)
                                    MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = if (type == TransactionType.INCOME)
                                    MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("Title") },
                    singleLine = true,
                    isError = FormField.TITLE in state.errors,
                    supportingText = state.errors[FormField.TITLE]?.let { m -> { Text(m) } },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("field_title")
                )

                OutlinedTextField(
                    value = state.amount,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text("Amount") },
                    prefix = { Text(state.currency.symbol) },
                    singleLine = true,
                    isError = FormField.AMOUNT in state.errors,
                    supportingText = state.errors[FormField.AMOUNT]?.let { m -> { Text(m) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("field_amount")
                )

                Column {
                    Text("Category", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Category.forType(state.type).forEach { category ->
                            FilterChip(
                                selected = state.category == category,
                                onClick = { viewModel.onCategoryChange(category) },
                                label = { Text(category.label) },
                                leadingIcon = {
                                    Icon(category.icon(), contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            )
                        }
                    }
                    state.errors[FormField.CATEGORY]?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                Column {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("field_date")
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Date: ${state.date.toDisplayDate()}", modifier = Modifier.weight(1f))
                    }
                    state.errors[FormField.DATE]?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = { Text("Notes (optional)") },
                    minLines = 2,
                    maxLines = 4,
                    isError = FormField.NOTES in state.errors,
                    supportingText = {
                        Text(state.errors[FormField.NOTES] ?: "${state.notes.length}/${TransactionValidator.MAX_NOTES_LENGTH}")
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("field_notes")
                )

                state.saveError?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_button")
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (state.isEditing) "Save changes" else "Add transaction")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showDatePicker) {
        PastOrTodayDatePicker(
            initial = state.date,
            onDismiss = { showDatePicker = false },
            onSelected = viewModel::onDateChange
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete transaction?") },
            text = { Text("This transaction will be permanently removed. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PastOrTodayDatePicker(
    initial: LocalDate,
    onDismiss: () -> Unit,
    onSelected: (LocalDate) -> Unit
) {
    val todayMillis = remember { LocalDate.now().toPickerMillis() }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial.toPickerMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayMillis
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { onSelected(it.fromPickerMillis()) }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = pickerState)
    }
}
