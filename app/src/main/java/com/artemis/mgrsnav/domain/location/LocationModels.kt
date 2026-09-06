package com.artemis.mgrsnav.domain.location

import com.artemis.mgrsnav.domain.mgrs.FixQuality
import com.artemis.mgrsnav.domain.mgrs.MgrsPrecision

data class FixSnapshot(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double?,
    val accuracyMeters: Float?,
    val speedMps: Float?,
    val bearingDeg: Float?,
    val timeMillis: Long,
    val isNetworkProvider: Boolean,
    val quality: FixQuality,
    val mgrsPrecision: MgrsPrecision,
    val mgrs: String
)
