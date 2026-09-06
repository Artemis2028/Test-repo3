package com.artemis.mgrsnav.domain.pacecount

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PaceCounterTest {

    @Test
    fun distance_fromCalibration() {
        val p = PaceCounter(pacesPerHundredMeters = 50.0)
        p.tap(50)
        assertThat(p.distanceMeters()).isWithin(0.01).of(100.0)
    }

    @Test
    fun pacesForDistance() {
        val p = PaceCounter(65.0)
        assertThat(p.pacesForDistance(100.0)).isEqualTo(65)
    }

    @Test
    fun remaining() {
        val p = PaceCounter(50.0)
        p.setCount(20)
        assertThat(p.remainingPaces(100.0)).isEqualTo(30)
    }
}
