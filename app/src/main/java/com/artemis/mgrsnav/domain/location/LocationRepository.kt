package com.artemis.mgrsnav.domain.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.artemis.mgrsnav.domain.mgrs.FixQuality
import com.artemis.mgrsnav.domain.mgrs.MgrsConverter
import com.artemis.mgrsnav.domain.mgrs.MgrsPrecision
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationRepository(private val context: Context) {

    private val fused = LocationServices.getFusedLocationProviderClient(context)

    fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun observeFixes(preferredPrecision: MgrsPrecision = MgrsPrecision.DIGITS_10): Flow<FixSnapshot?> = callbackFlow {
        if (!hasPermission()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .setWaitForAccurateLocation(false)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                trySend(toSnapshot(loc, preferredPrecision))
            }
        }
        fused.requestLocationUpdates(request, callback, Looper.getMainLooper())
        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) trySend(toSnapshot(loc, preferredPrecision))
        }
        awaitClose { fused.removeLocationUpdates(callback) }
    }

    private fun toSnapshot(loc: Location, preferred: MgrsPrecision): FixSnapshot {
        val isNetwork = loc.provider == LocationManager.NETWORK_PROVIDER
        val age = System.currentTimeMillis() - loc.time
        val quality = FixQuality.from(loc.accuracy, age, isNetwork)
        val precision = MgrsConverter.effectivePrecision(
            MgrsPrecision.cappedByAccuracyMeters(loc.accuracy).let {
                if (it.digits < preferred.digits) it else preferred
            }.let { req ->
                MgrsConverter.effectivePrecision(req, quality)
            },
            quality
        )
        val mgrs = try {
            MgrsConverter.toMgrs(loc.latitude, loc.longitude, precision)
        } catch (_: Exception) {
            "—"
        }
        return FixSnapshot(
            latitude = loc.latitude,
            longitude = loc.longitude,
            altitudeMeters = if (loc.hasAltitude()) loc.altitude else null,
            accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else null,
            speedMps = if (loc.hasSpeed()) loc.speed else null,
            bearingDeg = if (loc.hasBearing()) loc.bearing else null,
            timeMillis = loc.time,
            isNetworkProvider = isNetwork,
            quality = quality,
            mgrsPrecision = precision,
            mgrs = mgrs
        )
    }
}
