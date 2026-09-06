package com.artemis.mgrsnav.domain.navigation

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

/** WGS84 geodesic helpers for navigate screen (azimuth / distance / ETA). */
object GeoMath {
    private const val EARTH_RADIUS_M = 6_371_000.0

    data class NavVector(
        val distanceMeters: Double,
        val azimuthDeg: Double,
        val backAzimuthDeg: Double
    )

    fun wrap360(degrees: Double): Double {
        var d = degrees % 360.0
        if (d < 0) d += 360.0
        return d
    }

    fun bearingDegrees(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double): Double {
        val φ1 = Math.toRadians(fromLat)
        val φ2 = Math.toRadians(toLat)
        val Δλ = Math.toRadians(toLon - fromLon)
        val y = sin(Δλ) * cos(φ2)
        val x = cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(Δλ)
        return wrap360(Math.toDegrees(atan2(y, x)))
    }

    fun haversineMeters(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double): Double {
        val φ1 = Math.toRadians(fromLat)
        val φ2 = Math.toRadians(toLat)
        val Δφ = Math.toRadians(toLat - fromLat)
        val Δλ = Math.toRadians(toLon - fromLon)
        val a = sin(Δφ / 2) * sin(Δφ / 2) +
            cos(φ1) * cos(φ2) * sin(Δλ / 2) * sin(Δλ / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }

    fun vector(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double): NavVector {
        val az = bearingDegrees(fromLat, fromLon, toLat, toLon)
        return NavVector(
            distanceMeters = haversineMeters(fromLat, fromLon, toLat, toLon),
            azimuthDeg = az,
            backAzimuthDeg = wrap360(az + 180.0)
        )
    }

    fun etaMillis(distanceMeters: Double, speedMps: Double?): Long? {
        if (speedMps == null || speedMps < 0.3) {
            // Assume hiking pace ~1.2 m/s when unknown
            val assumed = 1.2
            return ((distanceMeters / assumed) * 1000.0).roundToLong()
        }
        return ((distanceMeters / speedMps) * 1000.0).roundToLong()
    }

    fun formatDistance(meters: Double): String = when {
        meters < 1000 -> "${meters.roundToLong()} m"
        else -> String.format("%.2f km", meters / 1000.0)
    }

    fun formatAzimuth(deg: Double): String = String.format("%03.0f°", wrap360(deg))

    fun formatEta(millis: Long): String {
        val totalSec = millis / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return when {
            h > 0 -> "${h}h ${m}m"
            m > 0 -> "${m}m ${s}s"
            else -> "${s}s"
        }
    }

    /** True when within arrival radius (default 15 m). */
    fun hasArrived(distanceMeters: Double, radiusMeters: Double = 15.0): Boolean =
        distanceMeters <= radiusMeters
}
