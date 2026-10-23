package com.saicharan.moneymate.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saicharan.moneymate.domain.model.BudgetStatus
import com.saicharan.moneymate.domain.model.CurrencyOption
import com.saicharan.moneymate.ui.theme.ExpenseCoral
import com.saicharan.moneymate.ui.theme.ExpenseCoralBg
import com.saicharan.moneymate.ui.theme.IncomeGreen
import com.saicharan.moneymate.ui.theme.WarningAmber
import com.saicharan.moneymate.ui.theme.WarningAmberBg
import com.saicharan.moneymate.util.MoneyFormatter
import java.math.BigDecimal
import kotlin.math.roundToInt

/** Budget usage summary with progress bar and 80% / exceeded warnings. */
@Composable
fun BudgetCard(
    budget: BigDecimal?,
    spent: BigDecimal,
    remaining: BigDecimal?,
    usagePercent: Double,
    progress: Float,
    status: BudgetStatus,
    currency: CurrencyOption,
    modifier: Modifier = Modifier,
    onEditBudget: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Monthly budget", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (budget == null || status == BudgetStatus.NOT_SET) {
                Text(
                    "Set a monthly spending budget to track how much you have left.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (onEditBudget != null) {
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onEditBudget) { Text("Set budget") }
                }
            } else {
                val barColor = when (status) {
                    BudgetStatus.EXCEEDED -> ExpenseCoral
                    BudgetStatus.WARNING -> WarningAmber
                    else -> IncomeGreen
                }
                val percentInt = usagePercent.roundToInt()
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "${MoneyFormatter.format(spent, currency)} spent",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("$percentInt%", style = MaterialTheme.typography.titleMedium, color = barColor)
                }
                Text(
                    "of ${MoneyFormatter.format(budget, currency)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .semantics { contentDescription = "Budget used: $percentInt percent" },
                    color = barColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                if (remaining != null) {
                    val over = remaining.signum() < 0
                    Text(
                        if (over) "Over budget by ${MoneyFormatter.format(remaining.negate(), currency)}"
                        else "${MoneyFormatter.format(remaining, currency)} remaining",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (over) ExpenseCoral else MaterialTheme.colorScheme.onSurface
                    )
                }
                when (status) {
                    BudgetStatus.WARNING -> {
                        Spacer(Modifier.height(12.dp))
                        BudgetBanner(
                            "You've used $percentInt% of this month's budget. Spending is close to your limit.",
                            background = WarningAmberBg,
                            content = WarningAmber
                        )
                    }
                    BudgetStatus.EXCEEDED -> {
                        Spacer(Modifier.height(12.dp))
                        BudgetBanner(
                            "Budget exceeded. Your expenses are above your monthly limit.",
                            background = ExpenseCoralBg,
                            content = ExpenseCoral
                        )
                    }
                    else -> Unit
                }
                if (onEditBudget != null) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onEditBudget) { Text("Edit budget") }
                }
            }
        }
    }
}

@Composable
private fun BudgetBanner(text: String, background: Color, content: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(12.dp)
            .semantics(mergeDescendants = true) { contentDescription = "Warning: $text" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = content)
    }
}
