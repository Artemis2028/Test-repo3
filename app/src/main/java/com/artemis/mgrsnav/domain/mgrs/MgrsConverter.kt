package com.artemis.mgrsnav.domain.mgrs

import mil.nga.mgrs.MGRS

/**
 * Thin wrapper around mil.nga:mgrs with digit formatting and safe parse.
 *
 * Note: mil.nga `coordinate(accuracy)` takes **per-axis** accuracy digits 0–5
 * (0 = 100 km square, 5 = 1 m). Our [MgrsPrecision] uses total easting+northing
 * digits (4/6/8/10), so we map digits → accuracy = digits / 2.
 */
object MgrsConverter {

    data class LatLon(val latitude: Double, val longitude: Double)

    fun toMgrs(latitude: Double, longitude: Double, precision: MgrsPrecision): String {
        require(latitude in -80.0..84.0) { "latitude out of MGRS range" }
        require(longitude in -180.0..180.0) { "longitude out of range" }
        val mgrs = MGRS.from(longitude, latitude)
        return format(mgrs, precision)
    }

    fun parse(mgrsText: String): LatLon {
        val cleaned = mgrsText.trim().uppercase().replace("\\s+".toRegex(), "")
        require(cleaned.isNotEmpty()) { "empty MGRS" }
        val mgrs = MGRS.parse(cleaned)
        val point = mgrs.toPoint()
        return LatLon(latitude = point.latitude, longitude = point.longitude)
    }

    fun roundTripOk(latitude: Double, longitude: Double, precision: MgrsPrecision): Boolean {
        return try {
            val text = toMgrs(latitude, longitude, precision)
            val back = parse(text)
            val tol = when (precision) {
                MgrsPrecision.DIGITS_4 -> 0.02
                MgrsPrecision.DIGITS_6 -> 0.002
                MgrsPrecision.DIGITS_8 -> 0.0002
                MgrsPrecision.DIGITS_10 -> 0.00005
            }
            kotlin.math.abs(back.latitude - latitude) <= tol &&
                kotlin.math.abs(back.longitude - longitude) <= tol
        } catch (_: Exception) {
            false
        }
    }

    /** Formats as spaced readable MGRS: "18S UJ 23394 07395" */
    fun format(mgrs: MGRS, precision: MgrsPrecision): String {
        val accuracy = precision.digits / 2 // mil.nga per-axis digits
        val full = mgrs.coordinate(accuracy)
        return beautify(full, precision.digits)
    }

    fun beautify(compact: String, digits: Int): String {
        val c = compact.replace("\\s+".toRegex(), "").uppercase()
        val zoneBand = Regex("^(\\d{1,2}[C-X])").find(c)?.value
            ?: return c
        val rest = c.removePrefix(zoneBand)
        if (rest.length < 2) return "$zoneBand $rest".trim()
        val square = rest.substring(0, 2)
        val nums = rest.substring(2)
        if (digits == 0 || nums.isEmpty()) return "$zoneBand $square"
        val half = nums.length / 2
        if (half == 0) return "$zoneBand $square $nums"
        val e = nums.substring(0, half)
        val n = nums.substring(half)
        return "$zoneBand $square $e $n"
    }

    fun effectivePrecision(requested: MgrsPrecision, quality: FixQuality): MgrsPrecision {
        return if (requested.digits <= quality.maxPrecision.digits) requested
        else quality.maxPrecision
    }
}
