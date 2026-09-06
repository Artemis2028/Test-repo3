package com.artemis.mgrsnav.ui.waypoints

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun WaypointsScreen(vm: WaypointsViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }
    var showFolder by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var folderName by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (text != null) {
                val n = vm.importGpx(text)
                status = "Imported $n waypoints"
            }
        }
    }

    LaunchedEffect(Unit) { vm.start(context) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Waypoints", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            TextButton(onClick = { showFolder = true }) { Icon(Icons.Default.Folder, null); Text(" Folder") }
            TextButton(onClick = {
                scope.launch {
                    val gpx = vm.exportGpx()
                    val file = File(context.cacheDir, "meridian-waypoints.gpx")
                    file.writeText(gpx)
                    // Share via plain text intent if FileProvider not wired — write + share text
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "application/gpx+xml"
                        putExtra(Intent.EXTRA_TEXT, gpx)
                        putExtra(Intent.EXTRA_TITLE, "meridian-waypoints.gpx")
                    }
                    context.startActivity(Intent.createChooser(send, "Export GPX"))
                    status = "Exported ${state.waypoints.size} waypoints"
                }
            }) { Text("Export GPX") }
            TextButton(onClick = {
                importLauncher.launch(arrayOf("*/*", "application/gpx+xml", "text/xml", "application/xml"))
            }) { Text("Import") }
        }
        status?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }

        if (state.folders.isNotEmpty()) {
            Text("Folders", style = MaterialTheme.typography.titleSmall)
            state.folders.forEach { f ->
                TextButton(onClick = { vm.toggleFolder(f) }) {
                    Text("${f.name} · ${if (f.visible) "visible" else "hidden"}")
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.waypoints, key = { it.id }) { wp ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(wp.name, style = MaterialTheme.typography.titleMedium)
                            Text(wp.mgrs, fontFamily = FontFamily.Monospace)
                            wp.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                        IconButton(onClick = { vm.delete(wp.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
        FloatingActionButton(onClick = { showAdd = true }, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Save current fix") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    Text("Uses last GPS fix / map center fallback.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.addFromFix(name.ifBlank { "WP" })
                    name = ""
                    showAdd = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }
    if (showFolder) {
        AlertDialog(
            onDismissRequest = { showFolder = false },
            title = { Text("New folder") },
            text = { OutlinedTextField(value = folderName, onValueChange = { folderName = it }, label = { Text("Folder name") }) },
            confirmButton = {
                TextButton(onClick = {
                    vm.addFolder(folderName.ifBlank { "Folder" })
                    folderName = ""
                    showFolder = false
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showFolder = false }) { Text("Cancel") } }
        )
    }
}
