package com.artemis.mgrsnav.ui.navigate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemis.mgrsnav.MeridianApp
import com.artemis.mgrsnav.data.db.WaypointEntity
import com.artemis.mgrsnav.domain.location.FixSnapshot
import com.artemis.mgrsnav.domain.navigation.GeoMath
import com.artemis.mgrsnav.domain.navigation.HapticGuide
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NavigateUiState(
    val waypoints: List<WaypointEntity> = emptyList(),
    val target: WaypointEntity? = null,
    val fix: FixSnapshot? = null,
    val vector: GeoMath.NavVector? = null,
    val etaLabel: String? = null,
    val cue: HapticGuide.Cue? = null,
    val arrived: Boolean = false,
    val hapticEnabled: Boolean = true
)

class NavigateViewModel : ViewModel() {
    private val _state = MutableStateFlow(NavigateUiState())
    val state: StateFlow<NavigateUiState> = _state.asStateFlow()
    private var jobs = mutableListOf<Job>()

    fun start(context: Context) {
        jobs.forEach { it.cancel() }
        jobs.clear()
        val app = context.applicationContext as MeridianApp
        jobs += viewModelScope.launch {
            app.waypointRepository.observeWaypoints().collect { list ->
                _state.update { it.copy(waypoints = list) }
            }
        }
        jobs += viewModelScope.launch {
            app.locationRepository.observeFixes().collect { fix ->
                recompute(fix)
            }
        }
    }

    fun selectTarget(wp: WaypointEntity) {
        _state.update { it.copy(target = wp, arrived = false) }
        recompute(_state.value.fix)
    }

    fun clearTarget() {
        _state.update { it.copy(target = null, vector = null, cue = null, arrived = false, etaLabel = null) }
    }

    fun toggleHaptic() {
        _state.update { it.copy(hapticEnabled = !it.hapticEnabled) }
    }

    private fun recompute(fix: FixSnapshot?) {
        val target = _state.value.target
        if (fix == null || target == null) {
            _state.update { it.copy(fix = fix, vector = null) }
            return
        }
        val v = GeoMath.vector(fix.latitude, fix.longitude, target.latitude, target.longitude)
        val eta = GeoMath.etaMillis(v.distanceMeters, fix.speedMps?.toDouble())
        val arrived = GeoMath.hasArrived(v.distanceMeters)
        val heading = fix.bearingDeg?.toDouble() ?: v.azimuthDeg
        val cue = if (arrived) HapticGuide.Cue.ARRIVAL else HapticGuide.cueFor(heading, v.azimuthDeg)
        _state.update {
            it.copy(
                fix = fix,
                vector = v,
                etaLabel = eta?.let { ms -> GeoMath.formatEta(ms) },
                cue = cue,
                arrived = arrived
            )
        }
    }
}
