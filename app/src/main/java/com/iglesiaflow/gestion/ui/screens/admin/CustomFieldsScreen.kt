@file:OptIn(ExperimentalMaterial3Api::class)

package com.iglesiaflow.gestion.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.data.local.entity.CustomFieldDefEntity
import com.iglesiaflow.gestion.domain.model.CustomFieldEntity
import com.iglesiaflow.gestion.domain.model.CustomFieldType
import com.iglesiaflow.gestion.ui.components.DropdownField
import com.iglesiaflow.gestion.ui.components.FormDialog
import com.iglesiaflow.gestion.ui.components.FormTextField
import com.iglesiaflow.gestion.ui.components.InfoBanner
import com.iglesiaflow.gestion.ui.components.SwitchRow

@Composable
fun CustomFieldsScreen(
    onBack: () -> Unit,
    viewModel: CustomFieldsViewModel = hiltViewModel()
) {
    val fields by viewModel.fields.collectAsStateWithLifecycle()
    val notice by viewModel.notice.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editing by remember { mutableStateOf<CustomFieldDefEntity?>(null) }
    var creating by remember { mutableStateOf(false) }

    LaunchedEffect(notice) {
        notice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = { Text("Campos personalizados") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { creating = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo campo")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                InfoBanner(
                    "Los campos definidos aquí aparecen automáticamente en las fichas de miembros, " +
                        "familias, eventos y grupos, sin necesidad de actualizar la APK."
                )
            }
            items(fields, key = { it.id }) { field ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(field.label, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${field.entityType.label} · ${field.fieldType.label}" +
                                    if (field.required) " · obligatorio" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (field.optionsCsv.isNotBlank()) {
                                Text("Opciones: ${field.optionsCsv}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = { editing = field }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { viewModel.delete(field.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
            if (fields.isEmpty()) {
                item { Text("Todavía no hay campos personalizados.") }
            }
        }
    }

    if (creating || editing != null) {
        val current = editing
        var label by remember { mutableStateOf(current?.label.orEmpty()) }
        var entityType by remember { mutableStateOf(current?.entityType ?: CustomFieldEntity.MIEMBRO) }
        var fieldType by remember { mutableStateOf(current?.fieldType ?: CustomFieldType.TEXTO) }
        var options by remember { mutableStateOf(current?.optionsCsv.orEmpty()) }
        var required by remember { mutableStateOf(current?.required ?: false) }
        var position by remember { mutableStateOf((current?.position ?: 0).toString()) }
        FormDialog(
            title = if (current == null) "Nuevo campo" else "Editar campo",
            onDismiss = { creating = false; editing = null },
            confirmEnabled = label.isNotBlank(),
            onConfirm = {
                viewModel.save(
                    (current ?: CustomFieldDefEntity(entityType = entityType, label = label)).copy(
                        label = label.trim(),
                        entityType = entityType,
                        fieldType = fieldType,
                        optionsCsv = options.trim(),
                        required = required,
                        position = position.toIntOrNull() ?: 0
                    )
                )
                creating = false
                editing = null
            }
        ) {
            FormTextField("Etiqueta", label, { label = it })
            DropdownField("Aplica a", CustomFieldEntity.entries, entityType, { it.label }, { entityType = it })
            DropdownField("Tipo", CustomFieldType.entries, fieldType, { it.label }, { fieldType = it })
            if (fieldType == CustomFieldType.SELECCION) {
                FormTextField("Opciones (separadas por coma)", options, { options = it })
            }
            FormTextField("Orden", position, { position = it }, keyboardType = KeyboardType.Number)
            SwitchRow("Obligatorio", null, required) { required = it }
        }
    }
}
