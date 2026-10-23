package com.saicharan.moneymate.ui.analytics

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saicharan.moneymate.domain.model.CurrencyOption
import com.saicharan.moneymate.ui.AppViewModelProvider
import com.saicharan.moneymate.ui.budget.BudgetCard
import com.saicharan.moneymate.ui.components.CategoryBreakdownList
import com.saicharan.moneymate.ui.components.EmptyState
import com.saicharan.moneymate.ui.components.ErrorState
import com.saicharan.moneymate.ui.components.LoadingState
import com.saicharan.moneymate.ui.components.ScreenHeader
import com.saicharan.moneymate.ui.theme.ExpenseCoral
import com.saicharan.moneymate.ui.theme.IncomeGreenDark
import com.saicharan.moneymate.util.MoneyFormatter
import com.saicharan.moneymate.util.toLongLabel
import java.math.BigDecimal

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize()) {
        ScreenHeader("Analytics", Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        when (val s = state) {
            AnalyticsUiState.Loading -> LoadingState()
            is AnalyticsUiState.Error -> ErrorState(s.message)
            is AnalyticsUiState.Success -> {
                if (!s.data.hasAnyTransactions) {
                    EmptyState(
                        icon = Icons.Filled.PieChart,
                        title = "Nothing to analyse yet",
                        message = "Add some transactions and your spending insights will appear here."
                    )
                } else {
                    AnalyticsContent(s.data, viewModel::previousMonth, viewModel::nextMonth)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsContent(data: AnalyticsData, onPrevious: () -> Unit, onNext: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { MonthSwitcher(data, onPrevious, onNext) }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TotalTile("Income", data.income, data.currency, IncomeGreenDark, Modifier.weight(1f))
                TotalTile("Expenses", data.expense, data.currency, ExpenseCoral, Modifier.weight(1f))
            }
        }
        item {
            val positive = data.net.signum() >= 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Net for the month", style = MaterialTheme.typography.titleSmall)
                    Text(
                        MoneyFormatter.format(data.net, data.currency),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (positive) IncomeGreenDark else ExpenseCoral,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            BudgetCard(
                budget = data.budget,
                spent = data.expense,
                remaining = data.remaining,
                usagePercent = data.usagePercent,
                progress = data.progress,
                status = data.budgetStatus,
                currency = data.currency
            )
        }

        item {
            ChartCard(title = "Expenses by category") {
                if (data.breakdown.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.PieChart,
                        title = "No expenses in ${data.month.toLongLabel()}",
                        message = "Pick another month or add an expense to see the breakdown."
                    )
                } else {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        DonutChart(
                            slices = data.breakdown,
                            centerCaption = "Total spent",
                            centerValue = MoneyFormatter.format(data.expense, data.currency)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    CategoryBreakdownList(data.breakdown, data.currency)
                }
            }
        }

        item {
            ChartCard(title = "Last 6 months") {
                TrendBarChart(data.trend, data.currency)
            }
        }
    }
}

@Composable
private fun MonthSwitcher(data: AnalyticsData, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        Text(
            data.month.toLongLabel(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { contentDescription = "Showing ${data.month.toLongLabel()}" }
        )
        IconButton(onClick = onNext, enabled = !data.isCurrentMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun TotalTile(label: String, amount: BigDecimal, currency: CurrencyOption, accent: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(
                MoneyFormatter.format(amount, currency),
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
