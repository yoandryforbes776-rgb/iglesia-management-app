package com.iglesiaflow.gestion.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iglesiaflow.gestion.ui.theme.ChartPalette

/** Gráfico de barras vertical simple, sin dependencias externas. */
@Composable
fun BarChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    height: Int = 160
) {
    if (data.isEmpty()) {
        Text("Sin datos en el periodo", style = MaterialTheme.typography.bodySmall)
        return
    }
    val maxValue = data.maxOf { it.second }.coerceAtLeast(1.0)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, value) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .width(28.dp)
                        .height((height * (value / maxValue)).dp.coerceAtLeast(4.dp))
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(barColor)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(44.dp)
                )
            }
        }
    }
}

/** Gráfico de líneas para tendencias (asistencia, ingresos). */
@Composable
fun LineChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.size < 2) {
        BarChart(data = data, modifier = modifier)
        return
    }
    val maxValue = data.maxOf { it.second }.coerceAtLeast(1.0)
    Column(modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(vertical = 8.dp)
        ) {
            val stepX = size.width / (data.size - 1)
            val path = Path()
            data.forEachIndexed { index, entry ->
                val x = stepX * index
                val y = size.height - (entry.second / maxValue * size.height).toFloat()
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = lineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f))
            data.forEachIndexed { index, entry ->
                val x = stepX * index
                val y = size.height - (entry.second / maxValue * size.height).toFloat()
                drawCircle(color = lineColor, radius = 7f, center = Offset(x, y))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(data.first().first, style = MaterialTheme.typography.labelSmall)
            Text(data.last().first, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/** Barras horizontales con leyenda, útil para distribución por fondo o categoría. */
@Composable
fun HorizontalBreakdown(
    data: List<Pair<String, Double>>,
    valueFormatter: (Double) -> String,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Text("Sin datos", style = MaterialTheme.typography.bodySmall)
        return
    }
    val total = data.sumOf { it.second }.coerceAtLeast(0.01)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        data.forEachIndexed { index, (label, value) ->
            val color = ChartPalette[index % ChartPalette.size]
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
                        Spacer(Modifier.width(6.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(valueFormatter(value), style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (value / total).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = color
                )
            }
        }
    }
}
