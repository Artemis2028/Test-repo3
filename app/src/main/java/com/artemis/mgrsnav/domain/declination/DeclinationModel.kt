package com.artemis.mgrsnav.domain.declination

/**
 * Simple world magnetic model approximation (not WMM coefficients).
 * Returns east-positive declination in degrees for a coarse field diagram.
 * For v1 this is a smooth latitude/longitude model suitable for training UI;
 * swap in NOAA WMM later without changing call sites.
 */
object DeclinationModel {

    data class Result(
        val declinationDeg: Double,
        val eastPositive: Boolean,
        val label: String
    )

    fun estimate(latitude: Double, longitude: Double, year: Double = 2026.0): Result {
        // Coarse spherical harmonic-ish toy model (stable for UI / tests)
        val latR = Math.toRadians(latitude)
        val lonR = Math.toRadians(longitude)
        val base = 12.0 * Math.sin(lonR) * Math.cos(latR) -
            5.0 * Math.sin(2 * lonR) * Math.sin(latR) +
            0.05 * (year - 2020.0)
        val decl = base.coerceIn(-45.0, 45.0)
        val east = decl >= 0
        val label = if (east) String.format("%.1f° E", decl)
        else String.format("%.1f° W", -decl)
        return Result(declinationDeg = decl, eastPositive = east, label = label)
    }

    fun magneticToTrue(magneticDeg: Double, declinationDeg: Double): Double =
        GeoWrap.wrap(magneticDeg + declinationDeg)

    fun trueToMagnetic(trueDeg: Double, declinationDeg: Double): Double =
        GeoWrap.wrap(trueDeg - declinationDeg)

    private object GeoWrap {
        fun wrap(d: Double): Double {
            var x = d % 360.0
            if (x < 0) x += 360.0
            return x
        }
    }
}
