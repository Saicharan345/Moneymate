package com.saicharan.moneymate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saicharan.moneymate.domain.model.Category
import com.saicharan.moneymate.domain.model.CategorySpend
import com.saicharan.moneymate.domain.model.CurrencyOption
import com.saicharan.moneymate.domain.model.Transaction
import com.saicharan.moneymate.domain.model.TransactionType
import com.saicharan.moneymate.ui.theme.ExpenseCoral
import com.saicharan.moneymate.ui.theme.IncomeGreenDark
import com.saicharan.moneymate.util.MoneyFormatter
import com.saicharan.moneymate.util.toDisplayDate
import kotlin.math.roundToInt

fun Category.icon(): ImageVector = when (this) {
    Category.FOOD -> Icons.Filled.Restaurant
    Category.TRANSPORT -> Icons.Filled.DirectionsCar
    Category.SHOPPING -> Icons.Filled.ShoppingCart
    Category.BILLS -> Icons.Filled.Receipt
    Category.ENTERTAINMENT -> Icons.Filled.Movie
    Category.HEALTH -> Icons.Filled.LocalHospital
    Category.EDUCATION -> Icons.Filled.School
    Category.SALARY -> Icons.Filled.AccountBalanceWallet
    Category.FREELANCE -> Icons.Filled.Work
    Category.INVESTMENTS -> Icons.Filled.TrendingUp
    Category.OTHER_EXPENSE, Category.OTHER_INCOME -> Icons.Filled.MoreHoriz
}

fun Category.color(): Color = Color(colorArgb)

@Composable
fun ScreenHeader(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "Loading" },
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator() }
}

@Composable
fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = ExpenseCoral, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text("Something went wrong", style = MaterialTheme.typography.titleMedium)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun CategoryAvatar(category: Category, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(category.color().copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(category.icon(), contentDescription = null, tint = category.color(), modifier = Modifier.size(22.dp))
    }
}

@Composable
fun SampleBadge(modifier: Modifier = Modifier) {
    Text(
        "SAMPLE",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun TransactionRow(
    transaction: Transaction,
    currency: CurrencyOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountText = (if (isIncome) "+" else "-") + MoneyFormatter.format(transaction.amount, currency)
    val description = "${transaction.type.label}: ${transaction.title}, $amountText, " +
        "${transaction.category.label}, ${transaction.date.toDisplayDate()}"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = description },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = "Edit transaction", onClick = onClick)
                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = if (onDelete == null) 12.dp else 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryAvatar(transaction.category)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    transaction.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${transaction.category.label} • ${transaction.date.toDisplayDate()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (transaction.isSample) {
                        Spacer(Modifier.width(6.dp))
                        SampleBadge()
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                amountText,
                style = MaterialTheme.typography.titleSmall,
                color = if (isIncome) IncomeGreenDark else ExpenseCoral,
                maxLines = 1
            )
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete ${transaction.title}")
                }
            }
        }
    }
}

/** Category rows with a coloured dot, amount, share and a thin progress bar. */
@Composable
fun CategoryBreakdownList(
    items: List<CategorySpend>,
    currency: CurrencyOption,
    modifier: Modifier = Modifier,
    maxItems: Int = Int.MAX_VALUE
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.take(maxItems).forEach { item ->
            val percent = (item.fraction * 100).roundToInt()
            Column(
                Modifier.semantics(mergeDescendants = true) {
                    contentDescription = "${item.category.label}: ${MoneyFormatter.format(item.amount, currency)}, $percent percent of spending"
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(item.category.color())
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        item.category.label,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        MoneyFormatter.format(item.amount, currency),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$percent%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { item.fraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = item.category.color(),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
