package com.artemis.mgrsnav.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artemis.mgrsnav.domain.astronomy.SunMoonTimes
import com.artemis.mgrsnav.domain.navigation.Resection

@Composable
fun ToolsScreen(vm: ToolsViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.start(context) }

    var pacesPer100 by remember { mutableFloatStateOf(65f) }
    var magHeading by remember { mutableStateOf("0") }
    var resectResult by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Field tools", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pace count", style = MaterialTheme.typography.titleMedium)
                Text("Paces / 100 m: ${pacesPer100.toInt()}")
                Slider(
                    value = pacesPer100,
                    onValueChange = {
                        pacesPer100 = it
                        vm.setPaceCalibration(it.toDouble())
                    },
                    valueRange = 40f..90f
                )
                Text("Count: ${state.paceCount}", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.headlineMedium)
                Text("Distance: ${String.format("%.0f m", state.paceMeters)}", fontFamily = FontFamily.Monospace)
                Row {
                    TextButton(onClick = { vm.paceTap() }) { Text("+1 pace") }
                    TextButton(onClick = { vm.paceTap(10) }) { Text("+10") }
                    TextButton(onClick = { vm.paceReset() }) { Text("Reset") }
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Declination", style = MaterialTheme.typography.titleMedium)
                Text(state.declinationLabel, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = magHeading,
                    onValueChange = { magHeading = it },
                    label = { Text("Magnetic heading °") },
                    modifier = Modifier.fillMaxWidth()
                )
                val mag = magHeading.toDoubleOrNull()
                if (mag != null) {
                    val trueH = vm.magneticToTrue(mag)
                    Text("True heading: ${String.format("%03.0f°", trueH)}", fontFamily = FontFamily.Monospace)
                }
                Text("Toy WMM-style estimate for UI — swap NOAA WMM later.", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Sun & moon", style = MaterialTheme.typography.titleMedium)
                val ev = state.dayEvents
                if (ev == null) {
                    Text("Need a fix for local times.")
                } else {
                    Text("Date ${ev.date}")
                    Text("Sunrise  ${SunMoonTimes.formatUtc(ev.sunrise)}", fontFamily = FontFamily.Monospace)
                    Text("Sunset   ${SunMoonTimes.formatUtc(ev.sunset)}", fontFamily = FontFamily.Monospace)
                    Text("Noon     ${SunMoonTimes.formatUtc(ev.solarNoon)}", fontFamily = FontFamily.Monospace)
                    Text("Civil ↑  ${SunMoonTimes.formatUtc(ev.civilDawn)}", fontFamily = FontFamily.Monospace)
                    Text("Civil ↓  ${SunMoonTimes.formatUtc(ev.civilDusk)}", fontFamily = FontFamily.Monospace)
                    Text("Moonrise ${SunMoonTimes.formatUtc(ev.moonrise)}", fontFamily = FontFamily.Monospace)
                    Text("Moonset  ${SunMoonTimes.formatUtc(ev.moonset)}", fontFamily = FontFamily.Monospace)
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Resection (stretch)", style = MaterialTheme.typography.titleMedium)
                Text("Demo: intersect two rays from known points.", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = {
                    val a = Resection.LatLon(38.0, -77.0)
                    val b = Resection.LatLon(38.1, -77.1)
                    val hit = Resection.rayIntersection(a, 45.0, b, 135.0)
                    resectResult = hit?.let { String.format("%.5f, %.5f", it.lat, it.lon) } ?: "parallel / no fix"
                }) { Text("Run sample intersection") }
                resectResult?.let { Text("Result: $it", fontFamily = FontFamily.Monospace) }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
