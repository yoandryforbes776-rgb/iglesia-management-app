@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.iglesiaflow.gestion.ui.screens.admin

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.core.config.AppSettings
import com.iglesiaflow.gestion.domain.model.ThemeMode
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.InfoBanner
import com.iglesiaflow.gestion.ui.components.SectionCard
import com.iglesiaflow.gestion.ui.components.SwitchRow
import com.iglesiaflow.gestion.ui.theme.SeedPalette

private val languages = listOf(
    AppSettings.LANGUAGE_SYSTEM to "Idioma del sistema",
    "es" to "Español",
    "en" to "English",
    "pt" to "Português"
)

private val currencies = listOf("EUR", "USD", "MXN", "COP", "ARS", "BRL", "CLP", "PEN", "DOP")

@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var churchName by remember(settings.churchName) { mutableStateOf(settings.churchName) }
    var motto by remember(settings.churchMotto) { mutableStateOf(settings.churchMotto) }

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.setLogo(it.toString()) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = { Text("Apariencia e idioma") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InfoBanner("Todos estos ajustes se aplican al instante, sin necesidad de recompilar la aplicación.")
            }

            item {
                SectionCard(title = "Identidad de la iglesia") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FormTextField("Nombre", churchName, { churchName = it })
                        FormTextField("Lema", motto, { motto = it })
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                viewModel.setChurchName(churchName.trim())
                                viewModel.setMotto(motto.trim())
                            }) { Text("Guardar") }
                            OutlinedButton(onClick = { logoPicker.launch("image/*") }) { Text("Elegir logo") }
                            if (settings.logoUri != null) {
                                TextButton(onClick = { viewModel.setLogo(null) }) { Text("Quitar logo") }
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Color y tema") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            SwitchRow(
                                "Color dinámico (Material You)",
                                "Toma los colores del fondo de pantalla",
                                settings.useDynamicColor
                            ) { viewModel.setDynamicColor(it) }
                        }
                        Text("Color primario", style = MaterialTheme.typography.labelLarge)
                        ColorPicker(settings.primaryColorArgb) { viewModel.setPrimaryColor(it) }
                        Text("Color secundario", style = MaterialTheme.typography.labelLarge)
                        ColorPicker(settings.secondaryColorArgb) { viewModel.setSecondaryColor(it) }
                        Text("Modo de tema", style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = settings.themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    label = { Text(mode.label) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Idioma") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        languages.forEach { (tag, label) ->
                            FilterChip(
                                selected = settings.languageTag == tag,
                                onClick = { viewModel.setLanguage(tag) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Moneda") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        currencies.forEach { code ->
                            FilterChip(
                                selected = settings.currencyCode == code,
                                onClick = { viewModel.setCurrency(code) },
                                label = { Text(code) }
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun ColorPicker(selected: Long, onSelect: (Long) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SeedPalette.forEach { argb ->
            val isSelected = argb == selected
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(argb))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clickable { onSelect(argb) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {}
        }
    }
}
