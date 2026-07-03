package net.bypasspaywalls.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.util.UUID
import net.bypasspaywalls.android.ui.theme.BypassPaywallsAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BypassPaywallsAndroidTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BypassPaywallsAndroidScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BypassPaywallsAndroidScreen() {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context) }

    var services by remember { mutableStateOf(repository.getAllServices()) }
    var selectedId by remember { mutableStateOf(repository.getSelectedServiceId()) }
    var testUrl by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    fun refresh() {
        services = repository.getAllServices()
        selectedId = repository.getSelectedServiceId()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                text = stringResource(R.string.instructions),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = stringResource(R.string.bypass_service_label), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(services, key = { it.id }) { service ->
                    ServiceRow(
                        service = service,
                        selected = service.id == selectedId,
                        onSelect = {
                            repository.setSelectedServiceId(service.id)
                            selectedId = service.id
                        },
                        onDelete = if (service.isCustom) {
                            {
                                repository.removeCustomService(service.id)
                                refresh()
                            }
                        } else null,
                    )
                }
            }

            TextButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(stringResource(R.string.add_custom_service))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.test_url_label), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = testUrl,
                onValueChange = { testUrl = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.test_url_placeholder)) },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val url = extractUrl(testUrl) ?: return@Button
                    val service = repository.getSelectedService()
                    openInBrowser(context, service.buildRedirectUrl(url))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = testUrl.isNotBlank(),
            ) {
                Text(stringResource(R.string.open_button))
            }
        }
    }

    if (showAddDialog) {
        AddServiceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, template, encode ->
                repository.addCustomService(
                    BypassService(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        urlTemplate = template,
                        encodeUrl = encode,
                        isCustom = true,
                    ),
                )
                refresh()
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun ServiceRow(
    service: BypassService,
    selected: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .selectable(selected = selected, onClick = onSelect),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onSelect)
            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Text(text = service.name, style = MaterialTheme.typography.bodyLarge)
                Text(text = service.urlTemplate, style = MaterialTheme.typography.bodySmall)
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        }
    }
}

@Composable
private fun AddServiceDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, template: String, encode: Boolean) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var template by remember { mutableStateOf("") }
    var encode by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_custom_service)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.service_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = template,
                    onValueChange = { template = it },
                    label = { Text(stringResource(R.string.service_template_label)) },
                    placeholder = { Text("https://example.com/{url}") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.service_template_hint),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Checkbox(checked = encode, onCheckedChange = { encode = it })
                    Text(stringResource(R.string.encode_url_label))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), template.trim(), encode) },
                enabled = name.isNotBlank() && template.contains(BypassService.URL_TOKEN),
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
