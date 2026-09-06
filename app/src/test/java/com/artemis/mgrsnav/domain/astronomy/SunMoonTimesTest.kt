package com.artemis.mgrsnav.domain.astronomy

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class SunMoonTimesTest {

    @Test
    fun equinox_midLatitude_hasRiseAndSet() {
        val events = SunMoonTimes.forDay(LocalDate.of(2026, 3, 20), 40.0, -75.0)
        assertThat(events.sunrise).isNotNull()
        assertThat(events.sunset).isNotNull()
        assertThat(events.solarNoon).isNotNull()
        assertThat(events.sunset!!.isAfter(events.sunrise)).isTrue()
    }

    @Test
    fun civilTwilight_ordersAroundSunriseSunset() {
        val events = SunMoonTimes.forDay(LocalDate.of(2026, 6, 21), 45.0, 0.0)
        assertThat(events.civilDawn).isNotNull()
        assertThat(events.civilDusk).isNotNull()
        assertThat(events.civilDawn!!.isBefore(events.sunrise)).isTrue()
        assertThat(events.civilDusk!!.isAfter(events.sunset)).isTrue()
    }

    @Test
    fun formatUtc_nullSafe() {
        assertThat(SunMoonTimes.formatUtc(null)).isEqualTo("—")
    }
}
