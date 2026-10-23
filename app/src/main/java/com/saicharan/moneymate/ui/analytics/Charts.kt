package com.saicharan.moneymate.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.saicharan.moneymate.domain.model.CategorySpend
import com.saicharan.moneymate.domain.model.CurrencyOption
import com.saicharan.moneymate.domain.model.MonthTotals
import com.saicharan.moneymate.ui.components.color
import com.saicharan.moneymate.ui.theme.ExpenseCoral
import com.saicharan.moneymate.ui.theme.IncomeGreen
import com.saicharan.moneymate.util.MoneyFormatter
import com.saicharan.moneymate.util.toShortLabel
import java.math.BigDecimal

/** Donut chart drawn with Compose Canvas. Centre shows a caption + total. */
@Composable
fun DonutChart(
    slices: List<CategorySpend>,
    centerCaption: String,
    centerValue: String,
    modifier: Modifier = Modifier
) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = modifier
            .size(190.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Donut chart of expenses by category. $centerCaption $centerValue"
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(190.dp)) {
            val strokeWidth = 26.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = Size(diameter, diameter)
            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            var start = -90f
            val gap = if (slices.size > 1) 2f else 0f
            slices.forEach { slice ->
                val sweep = slice.fraction * 360f
                drawArc(
                    color = slice.category.color(),
                    startAngle = start,
                    sweepAngle = (sweep - gap).coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerCaption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                centerValue,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.width(110.dp)
            )
        }
    }
}

/** Grouped bars (income vs expense) for the last few months. */
@Composable
fun TrendBarChart(
    data: List<MonthTotals>,
    currency: CurrencyOption,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { maxOf(it.income, it.expense) } ?: BigDecimal.ZERO
    val axis = MaterialTheme.colorScheme.outlineVariant
    val summary = data.joinToString("; ") {
        "${it.month.toShortLabel()}: income ${MoneyFormatter.format(it.income, currency)}, expenses ${MoneyFormatter.format(it.expense, currency)}"
    }

    Column(modifier.fillMaxWidth().semantics { contentDescription = "Income and expenses for the last ${data.size} months. $summary" }) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val groupWidth = size.width / data.size.coerceAtLeast(1)
            val barWidth = groupWidth * 0.26f
            val baseline = size.height
            drawLine(axis, Offset(0f, baseline), Offset(size.width, baseline), strokeWidth = 2f)
            if (maxValue.signum() > 0) {
                val max = maxValue.toFloat()
                data.forEachIndexed { index, item ->
                    val groupStart = index * groupWidth
                    val incomeHeight = (item.income.toFloat() / max) * (size.height - 4f)
                    val expenseHeight = (item.expense.toFloat() / max) * (size.height - 4f)
                    val radius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    drawRoundRect(
                        color = IncomeGreen,
                        topLeft = Offset(groupStart + groupWidth / 2f - barWidth - 2f, baseline - incomeHeight),
                        size = Size(barWidth, incomeHeight.coerceAtLeast(0f)),
                        cornerRadius = radius
                    )
                    drawRoundRect(
                        color = ExpenseCoral,
                        topLeft = Offset(groupStart + groupWidth / 2f + 2f, baseline - expenseHeight),
                        size = Size(barWidth, expenseHeight.coerceAtLeast(0f)),
                        cornerRadius = radius
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            data.forEach { item ->
                Text(
                    item.month.toShortLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendDot(IncomeGreen, "Income")
            Spacer(Modifier.width(16.dp))
            LegendDot(ExpenseCoral, "Expenses")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(10.dp)) { drawCircle(color) }
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
