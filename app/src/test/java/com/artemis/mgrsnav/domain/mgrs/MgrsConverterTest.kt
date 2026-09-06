package com.artemis.mgrsnav.domain.mgrs

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

class MgrsConverterTest {

    @Test
    fun roundTrip_washingtonDc_10digits() {
        val lat = 38.8977
        val lon = -77.0365
        assertThat(MgrsConverter.roundTripOk(lat, lon, MgrsPrecision.DIGITS_10)).isTrue()
        val text = MgrsConverter.toMgrs(lat, lon, MgrsPrecision.DIGITS_10)
        assertThat(text).contains("18S") // DC is 18S
        val back = MgrsConverter.parse(text)
        assertThat(back.latitude).isWithin(0.0001).of(lat)
        assertThat(back.longitude).isWithin(0.0001).of(lon)
    }

    @Test
    fun roundTrip_allPrecisions_knownPoints() {
        val points = listOf(
            51.5074 to -0.1278,   // London
            35.6762 to 139.6503,  // Tokyo
            -33.8688 to 151.2093, // Sydney
            64.1466 to -21.9426,  // Reykjavik
            0.0 to 0.0            // Null Island
        )
        for ((lat, lon) in points) {
            for (p in MgrsPrecision.entries) {
                assertWithMessage("lat=$lat lon=$lon p=$p")
                    .that(MgrsConverter.roundTripOk(lat, lon, p))
                    .isTrue()
            }
        }
    }

    @Test
    fun beautify_insertsSpaces() {
        val compact = "18SUJ2348306477"
        val pretty = MgrsConverter.beautify(compact, 10)
        assertThat(pretty).contains(" ")
        assertThat(pretty.replace(" ", "")).isEqualTo(compact)
    }

    @Test
    fun parse_acceptsSpacedAndCompact() {
        val lat = 40.0
        val lon = -75.0
        val spaced = MgrsConverter.toMgrs(lat, lon, MgrsPrecision.DIGITS_8)
        val compact = spaced.replace(" ", "")
        val a = MgrsConverter.parse(spaced)
        val b = MgrsConverter.parse(compact)
        assertThat(a.latitude).isWithin(1e-6).of(b.latitude)
        assertThat(a.longitude).isWithin(1e-6).of(b.longitude)
    }

    @Test
    fun precision_cappedByAccuracy() {
        assertThat(MgrsPrecision.cappedByAccuracyMeters(1f)).isEqualTo(MgrsPrecision.DIGITS_10)
        assertThat(MgrsPrecision.cappedByAccuracyMeters(10f)).isEqualTo(MgrsPrecision.DIGITS_8)
        assertThat(MgrsPrecision.cappedByAccuracyMeters(40f)).isEqualTo(MgrsPrecision.DIGITS_6)
        assertThat(MgrsPrecision.cappedByAccuracyMeters(100f)).isEqualTo(MgrsPrecision.DIGITS_4)
    }

    @Test
    fun fixQuality_words() {
        assertThat(FixQuality.from(3f, 1000L, false)).isEqualTo(FixQuality.EXCELLENT)
        assertThat(FixQuality.from(10f, 1000L, false)).isEqualTo(FixQuality.GOOD)
        assertThat(FixQuality.from(30f, 1000L, false)).isEqualTo(FixQuality.FAIR)
        assertThat(FixQuality.from(80f, 1000L, false)).isEqualTo(FixQuality.DEGRADED)
        assertThat(FixQuality.from(5f, 60_000L, false)).isEqualTo(FixQuality.STALE)
        assertThat(FixQuality.from(5f, 1000L, true)).isEqualTo(FixQuality.NETWORK)
        assertThat(FixQuality.from(null, null, false)).isEqualTo(FixQuality.NONE)
    }

    @Test
    fun effectivePrecision_neverExceedsQuality() {
        val q = FixQuality.FAIR
        val capped = MgrsConverter.effectivePrecision(MgrsPrecision.DIGITS_10, q)
        assertThat(capped).isEqualTo(MgrsPrecision.DIGITS_6)
    }
}
