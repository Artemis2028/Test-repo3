package com.artemis.mgrsnav.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemis.mgrsnav.MeridianApp
import com.artemis.mgrsnav.data.db.WaypointEntity
import com.artemis.mgrsnav.domain.location.FixSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val _waypoints = MutableStateFlow<List<WaypointEntity>>(emptyList())
    val waypoints: StateFlow<List<WaypointEntity>> = _waypoints.asStateFlow()
    private val _fix = MutableStateFlow<FixSnapshot?>(null)
    val fix: StateFlow<FixSnapshot?> = _fix.asStateFlow()

    fun start(context: Context) {
        val app = context.applicationContext as MeridianApp
        viewModelScope.launch {
            app.waypointRepository.observeWaypoints().collect { _waypoints.value = it }
        }
        viewModelScope.launch {
            app.locationRepository.observeFixes().collect { _fix.value = it }
        }
    }
}
