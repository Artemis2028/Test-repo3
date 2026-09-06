package com.artemis.mgrsnav.domain.astronomy

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Approximate sun / moon rise-set using NOAA-style solar equations and a
 * simplified lunar elongation model. Good enough for field planning; not
 * a substitute for a nautical almanac.
 */
object SunMoonTimes {

    data class DayEvents(
        val date: LocalDate,
        val sunrise: Instant?,
        val sunset: Instant?,
        val solarNoon: Instant?,
        val moonrise: Instant?,
        val moonset: Instant?,
        val civilDawn: Instant?,
        val civilDusk: Instant?
    )

    fun forDay(date: LocalDate, latitude: Double, longitude: Double): DayEvents {
        val jd = julianDay(date)
        val sun = solarEvents(jd, latitude, longitude)
        val moon = lunarEventsApprox(jd, latitude, longitude)
        return DayEvents(
            date = date,
            sunrise = sun.rise,
            sunset = sun.set,
            solarNoon = sun.noon,
            moonrise = moon.rise,
            moonset = moon.set,
            civilDawn = sun.civilDawn,
            civilDusk = sun.civilDusk
        )
    }

    private data class RiseSet(
        val rise: Instant?,
        val set: Instant?,
        val noon: Instant? = null,
        val civilDawn: Instant? = null,
        val civilDusk: Instant? = null
    )

    private fun julianDay(date: LocalDate): Double {
        val y = date.year
        val m = date.monthValue
        val d = date.dayOfMonth
        val a = (14 - m) / 12
        val yy = y + 4800 - a
        val mm = m + 12 * a - 3
        return d + (153 * mm + 2) / 5.0 + 365 * yy + yy / 4.0 - yy / 100.0 + yy / 400.0 - 32045.0
    }

    private fun solarEvents(jd: Double, lat: Double, lon: Double): RiseSet {
        val n = jd - 2451545.0 + 0.0008
        val jStar = n - lon / 360.0
        val M = (357.5291 + 0.98560028 * jStar) % 360.0
        val Mr = Math.toRadians(M)
        val C = 1.9148 * sin(Mr) + 0.02 * sin(2 * Mr) + 0.0003 * sin(3 * Mr)
        val λ = Math.toRadians((M + C + 180.0 + 102.9372) % 360.0)
        val decl = asin(sin(λ) * sin(Math.toRadians(23.4397)))
        val latR = Math.toRadians(lat)
        fun hourAngle(elevationDeg: Double): Double? {
            val cosH = (sin(Math.toRadians(elevationDeg)) - sin(latR) * sin(decl)) /
                (cos(latR) * cos(decl))
            if (cosH < -1 || cosH > 1) return null
            return Math.toDegrees(acos(cosH))
        }
        val haRise = hourAngle(-0.833) ?: return RiseSet(null, null)
        val haCivil = hourAngle(-6.0)
        val jTransit = 2451545.0 + jStar + 0.0053 * sin(Mr) - 0.0069 * sin(2 * λ)
        fun jdToInstant(j: Double): Instant {
            val epochDay = j - 2440587.5
            val millis = (epochDay * 86_400_000.0).toLong()
            return Instant.ofEpochMilli(millis)
        }
        val noon = jdToInstant(jTransit)
        val rise = jdToInstant(jTransit - haRise / 360.0)
        val set = jdToInstant(jTransit + haRise / 360.0)
        val dawn = haCivil?.let { jdToInstant(jTransit - it / 360.0) }
        val dusk = haCivil?.let { jdToInstant(jTransit + it / 360.0) }
        return RiseSet(rise, set, noon, dawn, dusk)
    }

    /** Very rough moon rise/set: offset from solar by lunar elongation proxy. */
    private fun lunarEventsApprox(jd: Double, lat: Double, lon: Double): RiseSet {
        val sun = solarEvents(jd, lat, lon)
        // Synodic month phase proxy
        val daysSinceNew = ((jd - 2451550.1) % 29.530588) .let { if (it < 0) it + 29.530588 else it }
        val lagHours = (daysSinceNew / 29.530588) * 24.0
        fun shift(i: Instant?): Instant? = i?.plusSeconds((lagHours * 3600).toLong())
        return RiseSet(shift(sun.rise), shift(sun.set))
    }

    fun formatUtc(instant: Instant?): String {
        if (instant == null) return "—"
        val z = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
        return String.format("%02d:%02dZ", z.hour, z.minute)
    }
}
