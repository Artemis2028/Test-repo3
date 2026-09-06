package com.artemis.mgrsnav.domain.navigation

/**
 * Two-point resection / intersection helpers (stretch goals for v1).
 * Given two known points and measured bearings (or distances), estimate a fix.
 */
object Resection {

    data class LatLon(val lat: Double, val lon: Double)

    /**
     * Intersection of two rays: from A along bearingA, from B along bearingB.
     * Returns null if nearly parallel.
     */
    fun rayIntersection(
        a: LatLon,
        bearingADeg: Double,
        b: LatLon,
        bearingBDeg: Double
    ): LatLon? {
        // Local ENU linearization around A
        val metersPerDegLat = 111_320.0
        val metersPerDegLon = 111_320.0 * kotlin.math.cos(Math.toRadians(a.lat))
        val ax = 0.0
        val ay = 0.0
        val bx = (b.lon - a.lon) * metersPerDegLon
        val by = (b.lat - a.lat) * metersPerDegLat
        val aRad = Math.toRadians(bearingADeg)
        val bRad = Math.toRadians(bearingBDeg)
        val dax = kotlin.math.sin(aRad)
        val day = kotlin.math.cos(aRad)
        val dbx = kotlin.math.sin(bRad)
        val dby = kotlin.math.cos(bRad)
        val denom = dax * dby - day * dbx
        if (kotlin.math.abs(denom) < 1e-6) return null
        val t = ((bx - ax) * dby - (by - ay) * dbx) / denom
        if (t < 0) return null
        val x = ax + t * dax
        val y = ay + t * day
        return LatLon(
            lat = a.lat + y / metersPerDegLat,
            lon = a.lon + x / metersPerDegLon
        )
    }
}
