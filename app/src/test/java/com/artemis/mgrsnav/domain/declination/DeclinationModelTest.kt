package com.artemis.mgrsnav.domain.declination

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DeclinationModelTest {

    @Test
    fun estimate_inRange() {
        val r = DeclinationModel.estimate(38.0, -77.0)
        assertThat(r.declinationDeg).isAtLeast(-45.0)
        assertThat(r.declinationDeg).isAtMost(45.0)
        assertThat(r.label).isNotEmpty()
    }

    @Test
    fun magneticTrue_roundTrip() {
        val decl = 10.0
        val mag = 40.0
        val tru = DeclinationModel.magneticToTrue(mag, decl)
        assertThat(tru).isWithin(1e-9).of(50.0)
        assertThat(DeclinationModel.trueToMagnetic(tru, decl)).isWithin(1e-9).of(mag)
    }
}
