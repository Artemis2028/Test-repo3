package com.artemis.mgrsnav.ui.waypoints

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemis.mgrsnav.MeridianApp
import com.artemis.mgrsnav.data.db.FolderEntity
import com.artemis.mgrsnav.data.db.WaypointEntity
import com.artemis.mgrsnav.domain.location.FixSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WaypointsUiState(
    val waypoints: List<WaypointEntity> = emptyList(),
    val folders: List<FolderEntity> = emptyList(),
    val lastFix: FixSnapshot? = null
)

class WaypointsViewModel : ViewModel() {
    private val _state = MutableStateFlow(WaypointsUiState())
    val state: StateFlow<WaypointsUiState> = _state.asStateFlow()
    private lateinit var app: MeridianApp

    fun start(context: Context) {
        app = context.applicationContext as MeridianApp
        viewModelScope.launch {
            app.waypointRepository.observeWaypoints().collect { list ->
                _state.update { it.copy(waypoints = list) }
            }
        }
        viewModelScope.launch {
            app.waypointRepository.observeFolders().collect { list ->
                _state.update { it.copy(folders = list) }
            }
        }
        viewModelScope.launch {
            app.locationRepository.observeFixes().collect { fix ->
                _state.update { it.copy(lastFix = fix) }
            }
        }
    }

    fun addFromFix(name: String) {
        val fix = _state.value.lastFix ?: return
        viewModelScope.launch {
            app.waypointRepository.saveWaypoint(name, fix.latitude, fix.longitude)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { app.waypointRepository.deleteWaypoint(id) }
    }

    fun addFolder(name: String) {
        viewModelScope.launch { app.waypointRepository.upsertFolder(name) }
    }

    fun toggleFolder(folder: FolderEntity) {
        viewModelScope.launch {
            app.waypointRepository.setFolderVisible(folder, !folder.visible)
        }
    }

    suspend fun exportGpx(): String = app.waypointRepository.exportGpx()

    suspend fun importGpx(text: String): Int = app.waypointRepository.importGpx(text)
}
