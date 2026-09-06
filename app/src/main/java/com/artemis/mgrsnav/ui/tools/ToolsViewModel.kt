package com.artemis.mgrsnav.ui.tools

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemis.mgrsnav.MeridianApp
import com.artemis.mgrsnav.domain.astronomy.SunMoonTimes
import com.artemis.mgrsnav.domain.declination.DeclinationModel
import com.artemis.mgrsnav.domain.pacecount.PaceCounter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ToolsUiState(
    val paceCount: Int = 0,
    val paceMeters: Double = 0.0,
    val declinationLabel: String = "—",
    val declinationDeg: Double = 0.0,
    val dayEvents: SunMoonTimes.DayEvents? = null
)

class ToolsViewModel : ViewModel() {
    private val pace = PaceCounter()
    private val _state = MutableStateFlow(ToolsUiState())
    val state: StateFlow<ToolsUiState> = _state.asStateFlow()

    fun start(context: Context) {
        val app = context.applicationContext as MeridianApp
        viewModelScope.launch {
            app.locationRepository.observeFixes().collect { fix ->
                if (fix == null) return@collect
                val decl = DeclinationModel.estimate(fix.latitude, fix.longitude)
                val events = SunMoonTimes.forDay(LocalDate.now(), fix.latitude, fix.longitude)
                _state.update {
                    it.copy(
                        declinationLabel = decl.label,
                        declinationDeg = decl.declinationDeg,
                        dayEvents = events
                    )
                }
            }
        }
        // Demo declination without fix
        val demo = DeclinationModel.estimate(38.0, -77.0)
        _state.update { it.copy(declinationLabel = demo.label, declinationDeg = demo.declinationDeg) }
    }

    fun paceTap(n: Int = 1) {
        pace.tap(n)
        publishPace()
    }

    fun paceReset() {
        pace.reset()
        publishPace()
    }

    fun setPaceCalibration(pacesPer100: Double) {
        pace.pacesPerHundredMeters = pacesPer100
        publishPace()
    }

    fun magneticToTrue(mag: Double): Double =
        DeclinationModel.magneticToTrue(mag, _state.value.declinationDeg)

    private fun publishPace() {
        _state.update {
            it.copy(paceCount = pace.paceCount, paceMeters = pace.distanceMeters())
        }
    }
}
