@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.domain.model.AppModule
import com.iglesiaflow.gestion.ui.components.SectionCard
import com.iglesiaflow.gestion.ui.components.SwitchRow
import com.iglesiaflow.gestion.ui.navigation.Routes

private data class AdminEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

private val entries = listOf(
    AdminEntry("Usuarios", "Altas, roles y 2FA", Icons.Filled.People, Routes.ADMIN_USERS),
    AdminEntry("Roles y permisos", "Matriz RBAC granular", Icons.Filled.Security, Routes.ADMIN_ROLES),
    AdminEntry("Campos personalizados", "Amplía los formularios sin recompilar", Icons.Filled.Tune, Routes.ADMIN_FIELDS),
    AdminEntry("Apariencia e idioma", "Material You, logo e idiomas", Icons.Filled.Palette, Routes.ADMIN_APPEARANCE),
    AdminEntry("Backup e importación", "Copias, CSV y restauración", Icons.Filled.Backup, Routes.ADMIN_BACKUP),
    AdminEntry("Auditoría", "Registro de actividad", Icons.Filled.History, Routes.ADMIN_AUDIT)
)

@Composable
fun AdminHomeScreen(
    onNavigate: (String) -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(entries) { entry ->
            Card(Modifier.fillMaxWidth().clickable { onNavigate(entry.route) }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(entry.icon, contentDescription = null)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(entry.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            entry.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }

        item {
            SectionCard(title = "Módulos visibles") {
                Column {
                    AppModule.entries.forEach { module ->
                        SwitchRow(
                            title = module.label,
                            subtitle = if (module in AppModule.alwaysOn) "Siempre activo" else null,
                            checked = settings.isModuleEnabled(module),
                            onCheckedChange = { settingsViewModel.toggleModule(module, it) }
                        )
                    }
                }
            }
        }

        item {
            SectionCard(title = "Notificaciones") {
                Column {
                    SwitchRow("Recordatorios de eventos", null, settings.notifyEvents) {
                        settingsViewModel.setNotification("events", it)
                    }
                    SwitchRow("Cumpleaños", null, settings.notifyBirthdays) {
                        settingsViewModel.setNotification("birthdays", it)
                    }
                    SwitchRow("Check-in infantil", null, settings.notifyCheckIn) {
                        settingsViewModel.setNotification("checkin", it)
                    }
                    SwitchRow("Donaciones registradas", null, settings.notifyDonations) {
                        settingsViewModel.setNotification("donations", it)
                    }
                    SwitchRow("Peticiones de oración", null, settings.notifyPrayer) {
                        settingsViewModel.setNotification("prayer", it)
                    }
                }
            }
        }

        item {
            SectionCard(title = "Seguridad y privacidad") {
                Column {
                    SwitchRow(
                        "Exigir 2FA a administradores",
                        "Segundo factor TOTP al iniciar sesión",
                        settings.requireTwoFactorForAdmins
                    ) { settingsViewModel.setRequireTwoFactor(it) }
                    SwitchRow(
                        "Sincronización en la nube",
                        "Replica datos en Firebase cuando hay conexión",
                        settings.cloudSyncEnabled
                    ) { settingsViewModel.setCloudSync(it) }
                    SwitchRow(
                        "Modo kiosco de check-in",
                        "Bloquea la navegación en el puesto infantil",
                        settings.kioskModeEnabled
                    ) { settingsViewModel.setKioskMode(it) }
                    SwitchRow(
                        "Consentimiento de privacidad aceptado",
                        "Tratamiento de datos conforme al RGPD",
                        settings.privacyConsentAccepted
                    ) { settingsViewModel.setPrivacyConsent(it) }
                    Text(
                        "Última copia de seguridad: " +
                            (settings.lastBackupAt?.let { DateTimeUtils.formatDateTime(it) } ?: "nunca"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Dashboard, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "IglesiaFlow · gestión integral inspirada en ChurchCRM",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
