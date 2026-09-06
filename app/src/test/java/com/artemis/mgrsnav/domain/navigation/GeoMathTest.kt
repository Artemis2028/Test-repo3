package com.artemis.mgrsnav.domain.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GeoMathTest {

    @Test
    fun wrap360_handlesNegatives() {
        assertThat(GeoMath.wrap360(-10.0)).isWithin(1e-9).of(350.0)
        assertThat(GeoMath.wrap360(370.0)).isWithin(1e-9).of(10.0)
        assertThat(GeoMath.wrap360(0.0)).isWithin(1e-9).of(0.0)
    }

    @Test
    fun bearing_dueNorth() {
        val az = GeoMath.bearingDegrees(0.0, 0.0, 1.0, 0.0)
        assertThat(az).isWithin(1.0).of(0.0)
    }

    @Test
    fun bearing_dueEast() {
        val az = GeoMath.bearingDegrees(0.0, 0.0, 0.0, 1.0)
        assertThat(az).isWithin(1.0).of(90.0)
    }

    @Test
    fun haversine_oneDegreeLatApprox111km() {
        val d = GeoMath.haversineMeters(0.0, 0.0, 1.0, 0.0)
        assertThat(d).isWithin(500.0).of(111_000.0)
    }

    @Test
    fun vector_backAzimuthOpposite() {
        val v = GeoMath.vector(10.0, 10.0, 11.0, 10.0)
        assertThat(GeoMath.wrap360(v.backAzimuthDeg - v.azimuthDeg)).isWithin(0.01).of(180.0)
    }

    @Test
    fun arrival_radius() {
        assertThat(GeoMath.hasArrived(10.0)).isTrue()
        assertThat(GeoMath.hasArrived(50.0)).isFalse()
    }

    @Test
    fun haptic_cues() {
        assertThat(HapticGuide.cueFor(0.0, 0.0)).isEqualTo(HapticGuide.Cue.ON_COURSE)
        assertThat(HapticGuide.cueFor(0.0, 30.0)).isEqualTo(HapticGuide.Cue.DRIFT_RIGHT)
        assertThat(HapticGuide.cueFor(0.0, 330.0)).isEqualTo(HapticGuide.Cue.DRIFT_LEFT)
        assertThat(HapticGuide.patternFor(HapticGuide.Cue.ARRIVAL).size).isGreaterThan(1)
    }

    @Test
    fun resection_intersectionKnown() {
        val a = Resection.LatLon(0.0, 0.0)
        val b = Resection.LatLon(0.0, 0.01)
        // From A head NE-ish, from B head NW-ish — should meet north of midline
        val hit = Resection.rayIntersection(a, 45.0, b, 315.0)
        assertThat(hit).isNotNull()
        assertThat(hit!!.lat).isGreaterThan(0.0)
    }
}
