@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.sync

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Icono de estado de la réplica en la nube: de un vistazo se sabe si lo que
 * hay en pantalla está al día en todos los dispositivos de la iglesia.
 */
@Composable
fun SyncIndicator(viewModel: SyncStatusViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val status = state.status
    if (!status.enabled) return

    val icon = when {
        !status.available || status.error != null -> Icons.Filled.CloudOff
        status.syncing -> Icons.Filled.CloudSync
        state.pending > 0 -> Icons.Filled.CloudQueue
        status.connected -> Icons.Filled.CloudDone
        else -> Icons.Filled.Cloud
    }
    val tint: Color = when {
        !status.available || status.error != null -> MaterialTheme.colorScheme.error
        state.pending > 0 -> MaterialTheme.colorScheme.tertiary
        status.connected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    IconButton(onClick = { viewModel.syncNow() }) {
        if (state.pending > 0) {
            BadgedBox(badge = { Badge { Text(state.pending.coerceAtMost(99).toString()) } }) {
                Icon(icon, contentDescription = state.label, tint = tint, modifier = Modifier.size(22.dp))
            }
        } else {
            Icon(icon, contentDescription = state.label, tint = tint, modifier = Modifier.size(22.dp))
        }
    }
}
