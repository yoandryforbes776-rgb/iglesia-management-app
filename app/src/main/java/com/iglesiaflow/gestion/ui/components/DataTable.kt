package com.iglesiaflow.gestion.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Tabla densa para reportes y listados (equivalente al DataTable de MUI). */
@Composable
fun DataTable(
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier,
    columnWidth: Int = 130,
    onRowClick: ((Int) -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                headers.forEach { header ->
                    Text(
                        header,
                        modifier = Modifier.width(columnWidth.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            HorizontalDivider()
            rows.forEachIndexed { index, row ->
                Row(
                    Modifier
                        .then(if (onRowClick != null) Modifier.clickable { onRowClick(index) } else Modifier)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    row.forEach { cell ->
                        Text(
                            cell,
                            modifier = Modifier.width(columnWidth.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (index < rows.lastIndex) HorizontalDivider()
            }
            if (rows.isEmpty()) {
                Text(
                    "Sin resultados",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
