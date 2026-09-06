package com.artemis.mgrsnav.domain.mgrs

/**
 * Plain-language fix grades. NETWORK / STALE are informational only —
 * they never unlock finer MGRS digits than the accuracy supports.
 */
enum class FixQuality(val displayName: String, val maxPrecision: MgrsPrecision) {
    EXCELLENT("EXCELLENT", MgrsPrecision.DIGITS_10),
    GOOD("GOOD", MgrsPrecision.DIGITS_8),
    FAIR("FAIR", MgrsPrecision.DIGITS_6),
    DEGRADED("DEGRADED", MgrsPrecision.DIGITS_4),
    STALE("STALE", MgrsPrecision.DIGITS_4),
    NETWORK("NETWORK", MgrsPrecision.DIGITS_6),
    NONE("NO FIX", MgrsPrecision.DIGITS_4);

    companion object {
        fun from(
            accuracyMeters: Float?,
            ageMillis: Long?,
            isNetworkProvider: Boolean
        ): FixQuality {
            if (accuracyMeters == null) return NONE
            if (ageMillis != null && ageMillis > 30_000L) return STALE
            if (isNetworkProvider) return NETWORK
            return when {
                accuracyMeters <= 5f -> EXCELLENT
                accuracyMeters <= 15f -> GOOD
                accuracyMeters <= 50f -> FAIR
                else -> DEGRADED
            }
        }
    }
}
