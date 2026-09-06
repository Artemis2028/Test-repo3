package com.artemis.mgrsnav.domain.navigation

/**
 * Eyes-free haptic guide pattern stubs. On devices without a vibrator
 * these remain no-ops at the UI layer; patterns are still computed for tests.
 */
object HapticGuide {
    enum class Cue { ON_COURSE, DRIFT_LEFT, DRIFT_RIGHT, ARRIVAL, OFF }

    /** Relative bearing error: positive = target is to the right of heading. */
    fun cueFor(headingDeg: Double, targetAzimuthDeg: Double, deadbandDeg: Double = 8.0): Cue {
        val err = GeoMath.wrap360(targetAzimuthDeg - headingDeg)
        val signed = if (err > 180) err - 360 else err
        return when {
            kotlin.math.abs(signed) <= deadbandDeg -> Cue.ON_COURSE
            signed > 0 -> Cue.DRIFT_RIGHT
            else -> Cue.DRIFT_LEFT
        }
    }

    /** Millisecond on/off pairs for VibrationEffect.createWaveform. */
    fun patternFor(cue: Cue): LongArray = when (cue) {
        Cue.ON_COURSE -> longArrayOf(0, 40)
        Cue.DRIFT_LEFT -> longArrayOf(0, 80, 80, 80)
        Cue.DRIFT_RIGHT -> longArrayOf(0, 40, 40, 40, 40, 40)
        Cue.ARRIVAL -> longArrayOf(0, 200, 100, 200, 100, 400)
        Cue.OFF -> longArrayOf(0)
    }
}
