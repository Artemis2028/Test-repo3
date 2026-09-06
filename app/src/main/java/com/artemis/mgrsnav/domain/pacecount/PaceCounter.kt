package com.artemis.mgrsnav.domain.pacecount

/**
 * Field pace-count helper: convert paces ↔ meters given a calibrated
 * paces-per-100 m factor (commonly ~60–70 double-paces / 100 m).
 */
class PaceCounter(
    var pacesPerHundredMeters: Double = 65.0
) {
    var paceCount: Int = 0
        private set

    fun tap(n: Int = 1) {
        paceCount = (paceCount + n).coerceAtLeast(0)
    }

    fun reset() {
        paceCount = 0
    }

    fun setCount(value: Int) {
        paceCount = value.coerceAtLeast(0)
    }

    fun distanceMeters(): Double =
        if (pacesPerHundredMeters <= 0) 0.0
        else paceCount * (100.0 / pacesPerHundredMeters)

    fun pacesForDistance(meters: Double): Int =
        if (pacesPerHundredMeters <= 0) 0
        else kotlin.math.round(meters * pacesPerHundredMeters / 100.0).toInt()

    fun remainingPaces(targetMeters: Double): Int =
        (pacesForDistance(targetMeters) - paceCount).coerceAtLeast(0)
}
