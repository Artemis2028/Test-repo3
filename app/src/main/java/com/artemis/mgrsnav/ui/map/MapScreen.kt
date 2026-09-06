package com.artemis.mgrsnav.ui.map

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapScreen(vm: MapViewModel = viewModel()) {
    val context = LocalContext.current
    val waypoints by vm.waypoints.collectAsState()
    val fix by vm.fix.collectAsState()
    var rulerMode by remember { mutableStateOf(false) }
    val gridOverlay = remember { MgrsGridOverlay() }

    LaunchedEffect(Unit) { vm.start(context) }

    val mapView = remember {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            controller.setCenter(GeoPoint(38.8977, -77.0365))
            overlays.add(gridOverlay)
            overlays.add(CompassOverlay(context, this).apply { enableCompass() })
            val myLoc = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
            myLoc.enableMyLocation()
            overlays.add(myLoc)
        }
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }

    LaunchedEffect(fix?.latitude, fix?.longitude) {
        val f = fix ?: return@LaunchedEffect
        mapView.controller.animateTo(GeoPoint(f.latitude, f.longitude))
    }

    LaunchedEffect(waypoints) {
        // Remove old waypoint markers (keep system overlays)
        val toRemove = mapView.overlays.filterIsInstance<Marker>()
        mapView.overlays.removeAll(toRemove.toSet())
        waypoints.forEach { wp ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(wp.latitude, wp.longitude)
                title = wp.name
                snippet = wp.mgrs
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    LaunchedEffect(rulerMode) {
        gridOverlay.showRuler = rulerMode
        mapView.invalidate()
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { mv ->
                mv.setOnTouchListener { _, event ->
                    if (rulerMode && event.action == android.view.MotionEvent.ACTION_UP) {
                        val gp = mv.projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                        if (gridOverlay.rulerStart == null) {
                            gridOverlay.rulerStart = gp
                            gridOverlay.rulerEnd = null
                        } else if (gridOverlay.rulerEnd == null) {
                            gridOverlay.rulerEnd = gp
                        } else {
                            gridOverlay.rulerStart = gp
                            gridOverlay.rulerEnd = null
                        }
                        mv.invalidate()
                    }
                    false
                }
            }
        )
        Column(Modifier.align(Alignment.TopEnd).padding(12.dp)) {
            FilterChip(
                selected = rulerMode,
                onClick = {
                    rulerMode = !rulerMode
                    if (!rulerMode) {
                        gridOverlay.rulerStart = null
                        gridOverlay.rulerEnd = null
                    }
                },
                label = { Text("Ruler") }
            )
            Text(
                "MGRS grid · offline tile cache",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
