package com.artemis.mgrsnav.data.gpx

import com.artemis.mgrsnav.data.db.WaypointEntity
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GpxCodecTest {

    @Test
    fun exportImport_roundTrip() {
        val original = listOf(
            WaypointEntity(
                name = "Alpha",
                latitude = 38.9,
                longitude = -77.0,
                mgrs = "18S UJ 00000 00000",
                note = "test & <check>"
            ),
            WaypointEntity(
                name = "Bravo",
                latitude = 39.0,
                longitude = -76.5,
                mgrs = "18S UJ 11111 11111"
            )
        )
        val gpx = GpxCodec.exportWaypoints(original)
        assertThat(gpx).contains("<wpt")
        assertThat(gpx).contains("Alpha")
        val imported = GpxCodec.importWaypointsLightweight(gpx)
        assertThat(imported).hasSize(2)
        assertThat(imported[0].name).isEqualTo("Alpha")
        assertThat(imported[0].latitude).isWithin(1e-6).of(38.9)
        assertThat(imported[0].description).contains("test")
    }

    @Test
    fun folderNaming_spacesPreservedInExport() {
        val wp = WaypointEntity(name = "OP North", latitude = 1.0, longitude = 2.0, mgrs = "X")
        val gpx = GpxCodec.exportWaypoints(listOf(wp))
        assertThat(gpx).contains("OP North")
    }
}
