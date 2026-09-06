package com.artemis.mgrsnav.ui.position

import android.Manifest
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artemis.mgrsnav.domain.mgrs.MgrsPrecision
import com.artemis.mgrsnav.ui.components.CompassDial

enum class PositionFace { GLANCE, DIAL }

@Composable
fun PositionScreen(vm: PositionViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var face by remember { mutableStateOf(PositionFace.GLANCE) }
    var digitsIdx by remember { mutableIntStateOf(3) }
    val digitOptions = listOf(MgrsPrecision.DIGITS_4, MgrsPrecision.DIGITS_6, MgrsPrecision.DIGITS_8, MgrsPrecision.DIGITS_10)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.any { it }) vm.start(context, digitOptions[digitsIdx])
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Meridian", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text("Position", style = MaterialTheme.typography.labelLarge)

        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = face == PositionFace.GLANCE,
                onClick = { face = PositionFace.GLANCE },
                shape = SegmentedButtonDefaults.itemShape(0, 2)
            ) { Text("Glance") }
            SegmentedButton(
                selected = face == PositionFace.DIAL,
                onClick = { face = PositionFace.DIAL },
                shape = SegmentedButtonDefaults.itemShape(1, 2)
            ) { Text("Dial") }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            digitOptions.forEachIndexed { i, p ->
                FilterChip(
                    selected = digitsIdx == i,
                    onClick = {
                        digitsIdx = i
                        vm.setPrecision(p)
                    },
                    label = { Text("${p.digits}") }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        val fix = state.fix
        if (fix == null) {
            Text("Waiting for fix…", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            TextButton(onClick = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }) { Text("Grant location") }
        } else {
            when (face) {
                PositionFace.GLANCE -> {
                    Text(
                        text = fix.mgrs,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${fix.quality.displayName} · ±${fix.accuracyMeters?.toInt() ?: "?"} m · ${fix.mgrsPrecision.label}",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        String.format("%.5f, %.5f", fix.latitude, fix.longitude),
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    fix.altitudeMeters?.let {
                        Text(String.format("Alt %.0f m", it), style = MaterialTheme.typography.bodySmall)
                    }
                }
                PositionFace.DIAL -> {
                    CompassDial(
                        headingDeg = fix.bearingDeg ?: 0f,
                        mgrs = fix.mgrs,
                        qualityLabel = "${fix.quality.displayName} · ${fix.mgrsPrecision.label}"
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "A phone GPS is an aid. Confirm every grid against the ground.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        )
    }
}
