package com.saicharan.moneymate.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saicharan.moneymate.domain.model.Category
import com.saicharan.moneymate.domain.model.Transaction
import com.saicharan.moneymate.domain.model.TransactionType
import com.saicharan.moneymate.ui.AppViewModelProvider
import com.saicharan.moneymate.ui.components.EmptyState
import com.saicharan.moneymate.ui.components.ErrorState
import com.saicharan.moneymate.ui.components.LoadingState
import com.saicharan.moneymate.ui.components.ScreenHeader
import com.saicharan.moneymate.ui.components.TransactionRow

@Composable
fun TransactionsScreen(
    snackbarHostState: SnackbarHostState,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    Column(modifier.fillMaxSize()) {
        ScreenHeader("Transactions", Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(state.error ?: "")
            state.totalCount == 0 -> EmptyState(
                icon = Icons.AutoMirrored.Filled.List,
                title = "No transactions yet",
                message = "Record your income and expenses to see them here.",
                actionLabel = "Add transaction",
                onAction = onAddTransaction
            )
            else -> {
                FilterBar(
                    state = state,
                    onQueryChange = viewModel::onQueryChange,
                    onTypeFilter = viewModel::onTypeFilter,
                    onCategoryFilter = viewModel::onCategoryFilter
                )
                if (state.items.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.Search,
                        title = "No matching transactions",
                        message = "Try a different search or clear your filters.",
                        actionLabel = "Clear filters",
                        onAction = viewModel::clearFilters
                    )
                } else {
                    Text(
                        "Showing ${state.items.size} of ${state.totalCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.items, key = { it.id }) { tx ->
                            TransactionRow(
                                transaction = tx,
                                currency = state.currency,
                                onClick = { onEditTransaction(tx.id) },
                                onDelete = { pendingDelete = tx }
                            )
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete transaction?") },
            text = { Text("\"${tx.title}\" will be permanently removed. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(tx.id)
                    pendingDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun FilterBar(
    state: TransactionsUiState,
    onQueryChange: (String) -> Unit,
    onTypeFilter: (TransactionType?) -> Unit,
    onCategoryFilter: (Category?) -> Unit
) {
    val filter = state.filter
    Column(Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = filter.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by title") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (filter.query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(selected = filter.type == null, onClick = { onTypeFilter(null) }, label = { Text("All") })
            }
            items(TransactionType.entries) { type ->
                FilterChip(
                    selected = filter.type == type,
                    onClick = { onTypeFilter(if (filter.type == type) null else type) },
                    label = { Text(type.label) }
                )
            }
        }
        val categories = if (filter.type == null) Category.entries.toList() else Category.forType(filter.type)
        Spacer(Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = filter.category == null,
                    onClick = { onCategoryFilter(null) },
                    label = { Text("All categories") }
                )
            }
            items(categories) { category ->
                FilterChip(
                    selected = filter.category == category,
                    onClick = { onCategoryFilter(if (filter.category == category) null else category) },
                    label = { Text(labelFor(category, filter.type == null)) }
                )
            }
        }
    }
}

/** "Other" exists for both income and expense, so disambiguate when both lists are shown. */
private fun labelFor(category: Category, showingAllTypes: Boolean): String =
    if (showingAllTypes && (category == Category.OTHER_EXPENSE || category == Category.OTHER_INCOME)) {
        "Other (${category.type.label.lowercase()})"
    } else category.label
