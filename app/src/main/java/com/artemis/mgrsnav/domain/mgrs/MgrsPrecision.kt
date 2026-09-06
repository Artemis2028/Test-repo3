package com.artemis.mgrsnav.domain.mgrs

/**
 * MGRS digit precision: 4 digits ≈ 1 km, 6 ≈ 100 m, 8 ≈ 10 m, 10 ≈ 1 m.
 * Precision is capped by GPS fix quality so the readout never overclaims.
 */
enum class MgrsPrecision(val digits: Int, val approxMeters: Int, val label: String) {
    DIGITS_4(4, 1000, "1 km"),
    DIGITS_6(6, 100, "100 m"),
    DIGITS_8(8, 10, "10 m"),
    DIGITS_10(10, 1, "1 m");

    companion object {
        fun fromDigits(digits: Int): MgrsPrecision =
            entries.firstOrNull { it.digits == digits } ?: DIGITS_10

        fun cappedByAccuracyMeters(horizontalAccuracyM: Float?): MgrsPrecision {
            if (horizontalAccuracyM == null || horizontalAccuracyM.isNaN()) return DIGITS_6
            return when {
                horizontalAccuracyM <= 2f -> DIGITS_10
                horizontalAccuracyM <= 12f -> DIGITS_8
                horizontalAccuracyM <= 60f -> DIGITS_6
                else -> DIGITS_4
            }
        }
    }
}
