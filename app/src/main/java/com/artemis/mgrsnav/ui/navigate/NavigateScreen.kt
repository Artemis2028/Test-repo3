package com.artemis.mgrsnav.ui.navigate

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artemis.mgrsnav.domain.navigation.GeoMath
import com.artemis.mgrsnav.domain.navigation.HapticGuide

@Composable
fun NavigateScreen(vm: NavigateViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { vm.start(context) }

    LaunchedEffect(state.cue, state.arrived) {
        if (state.arrived) {
            buzz(context, HapticGuide.patternFor(HapticGuide.Cue.ARRIVAL))
        } else if (state.hapticEnabled && state.cue != null) {
            buzz(context, HapticGuide.patternFor(state.cue!!))
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Navigate", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))

        val target = state.target
        if (target == null) {
            Text("Select a waypoint to navigate to.", style = MaterialTheme.typography.bodyMedium)
        } else {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(target.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(target.mgrs, fontFamily = FontFamily.Monospace)
                    state.vector?.let { v ->
                        Text("Azimuth  ${GeoMath.formatAzimuth(v.azimuthDeg)}", fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                        Text("Back az  ${GeoMath.formatAzimuth(v.backAzimuthDeg)}", fontFamily = FontFamily.Monospace)
                        Text("Distance ${GeoMath.formatDistance(v.distanceMeters)}", fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                        state.etaLabel?.let { Text("ETA      $it", fontFamily = FontFamily.Monospace) }
                        if (state.arrived) {
                            Text("ARRIVED", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Guide: ${state.cue?.name ?: "—"}", style = MaterialTheme.typography.labelLarge)
                        }
                    } ?: Text("Waiting for GPS fix…")
                }
            }
            TextButton(onClick = { vm.toggleHaptic() }) {
                Text(if (state.hapticEnabled) "Haptic guide: ON" else "Haptic guide: OFF (stub OK)")
            }
            TextButton(onClick = { vm.clearTarget() }) { Text("Clear target") }
        }

        Spacer(Modifier.height(12.dp))
        Text("Waypoints", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(state.waypoints, key = { it.id }) { wp ->
                TextButton(onClick = { vm.selectTarget(wp) }, modifier = Modifier.fillMaxWidth()) {
                    Text("${wp.name}  ·  ${wp.mgrs}", fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun buzz(context: Context, pattern: LongArray) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    } catch (_: Exception) {
        // Emulator / no vibrator — haptic stub silently ignored
    }
}
