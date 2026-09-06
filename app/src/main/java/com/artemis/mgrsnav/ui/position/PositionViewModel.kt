package com.artemis.mgrsnav.ui.position

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemis.mgrsnav.MeridianApp
import com.artemis.mgrsnav.domain.location.FixSnapshot
import com.artemis.mgrsnav.domain.mgrs.MgrsPrecision
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PositionUiState(val fix: FixSnapshot? = null)

class PositionViewModel : ViewModel() {
    private val _state = MutableStateFlow(PositionUiState())
    val state: StateFlow<PositionUiState> = _state.asStateFlow()

    private var precision: MgrsPrecision = MgrsPrecision.DIGITS_10
    private var job: Job? = null

    fun start(context: Context, preferred: MgrsPrecision = MgrsPrecision.DIGITS_10) {
        precision = preferred
        val repo = (context.applicationContext as? MeridianApp)?.locationRepository
            ?: MeridianApp.instance.locationRepository
        job?.cancel()
        job = viewModelScope.launch {
            repo.observeFixes(precision).collect { snap ->
                _state.update { it.copy(fix = snap) }
            }
        }
    }

    fun setPrecision(p: MgrsPrecision) {
        precision = p
        // Restart collection with new cap — UI will call start again via LaunchedEffect if needed
        try {
            start(MeridianApp.instance, p)
        } catch (_: Exception) { /* app not ready */ }
    }
}
