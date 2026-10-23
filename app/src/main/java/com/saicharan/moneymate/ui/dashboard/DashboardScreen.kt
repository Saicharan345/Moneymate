package com.saicharan.moneymate.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saicharan.moneymate.ui.AppViewModelProvider
import com.saicharan.moneymate.ui.budget.BudgetCard
import com.saicharan.moneymate.ui.budget.BudgetDialog
import com.saicharan.moneymate.ui.components.CategoryBreakdownList
import com.saicharan.moneymate.ui.components.EmptyState
import com.saicharan.moneymate.ui.components.ErrorState
import com.saicharan.moneymate.ui.components.LoadingState
import com.saicharan.moneymate.ui.components.ScreenHeader
import com.saicharan.moneymate.ui.components.SectionHeader
import com.saicharan.moneymate.ui.components.TransactionRow
import com.saicharan.moneymate.ui.components.color
import com.saicharan.moneymate.ui.theme.ExpenseCoral
import com.saicharan.moneymate.ui.theme.IncomeGreen
import com.saicharan.moneymate.ui.theme.Navy
import com.saicharan.moneymate.util.MoneyFormatter
import com.saicharan.moneymate.util.toLongLabel
import java.math.BigDecimal

@Composable
fun DashboardScreen(
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onSeeAllTransactions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (val s = state) {
        DashboardUiState.Loading -> LoadingState(modifier)
        is DashboardUiState.Error -> ErrorState(s.message, modifier)
        is DashboardUiState.Success -> DashboardContent(
            data = s.data,
            onAddTransaction = onAddTransaction,
            onEditTransaction = onEditTransaction,
            onSeeAll = onSeeAllTransactions,
            onSetBudget = viewModel::setBudget,
            modifier = modifier
        )
    }
}

@Composable
private fun DashboardContent(
    data: DashboardData,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onSeeAll: () -> Unit,
    onSetBudget: (BigDecimal?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBudgetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenHeader(title = "MoneyMate", subtitle = "Overview for ${data.month.toLongLabel()}") }

        item { BalanceCard(data) }

        item {
            BudgetCard(
                budget = data.budget,
                spent = data.monthExpense,
                remaining = data.remaining,
                usagePercent = data.usagePercent,
                progress = data.progress,
                status = data.budgetStatus,
                currency = data.currency,
                onEditBudget = { showBudgetDialog = true }
            )
        }

        if (!data.hasTransactions) {
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    EmptyState(
                        icon = Icons.Filled.AccountBalanceWallet,
                        title = "No transactions yet",
                        message = "Add your first income or expense to see your balance and spending breakdown.",
                        actionLabel = "Add transaction",
                        onAction = onAddTransaction
                    )
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Spending by category", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        if (data.breakdown.isEmpty()) {
                            EmptyState(
                                icon = Icons.Filled.PieChart,
                                title = "No expenses this month",
                                message = "Your category breakdown will appear once you record an expense."
                            )
                        } else {
                            StackedShareBar(data)
                            Spacer(Modifier.height(16.dp))
                            CategoryBreakdownList(data.breakdown, data.currency, maxItems = 5)
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Recent transactions", actionLabel = "See all", onAction = onSeeAll)
            }
            items(data.recent, key = { it.id }) { tx ->
                TransactionRow(tx, data.currency, onClick = { onEditTransaction(tx.id) })
            }
        }
    }

    if (showBudgetDialog) {
        BudgetDialog(
            currentBudget = data.budget,
            currency = data.currency,
            onDismiss = { showBudgetDialog = false },
            onSave = {
                onSetBudget(it)
                showBudgetDialog = false
            }
        )
    }
}

@Composable
private fun BalanceCard(data: DashboardData) {
    val negative = data.totalBalance.signum() < 0
    val balanceText = MoneyFormatter.format(data.totalBalance, data.currency)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Total balance $balanceText. This month income " +
                    "${MoneyFormatter.format(data.monthIncome, data.currency)}, expenses " +
                    MoneyFormatter.format(data.monthExpense, data.currency)
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Navy, contentColor = Color.White)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Total balance", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.75f))
            Spacer(Modifier.height(4.dp))
            Text(
                balanceText,
                style = MaterialTheme.typography.headlineMedium,
                color = if (negative) Color(0xFFFF9EA1) else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                StatTile("Income this month", MoneyFormatter.format(data.monthIncome, data.currency), Color(0xFF6EE7A8), Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                StatTile("Expenses this month", MoneyFormatter.format(data.monthExpense, data.currency), Color(0xFFFF9EA1), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f), maxLines = 2)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** One horizontal bar split into category-coloured segments. */
@Composable
private fun StackedShareBar(data: DashboardData) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(7.dp))
            .semantics { contentDescription = "Spending share by category" }
    ) {
        data.breakdown.forEach { slice ->
            Box(
                Modifier
                    .weight(slice.fraction.coerceAtLeast(0.01f))
                    .fillMaxSize()
                    .background(slice.category.color())
            )
        }
    }
}
