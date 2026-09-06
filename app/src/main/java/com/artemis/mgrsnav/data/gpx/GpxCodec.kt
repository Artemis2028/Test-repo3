package com.artemis.mgrsnav.data.gpx

import com.artemis.mgrsnav.data.db.WaypointEntity
import com.artemis.mgrsnav.domain.mgrs.MgrsConverter
import com.artemis.mgrsnav.domain.mgrs.MgrsPrecision
import java.io.StringWriter
import java.time.Instant

/**
 * Minimal GPX 1.1 waypoint import/export (no tracks/routes in v1).
 * Uses Android XmlPullParser at runtime; unit tests use a regex/fallback parser path.
 */
object GpxCodec {

    fun exportWaypoints(waypoints: List<WaypointEntity>, creator: String = "Meridian"): String {
        val w = StringWriter()
        w.append("""<?xml version="1.0" encoding="UTF-8"?>""")
        w.append('\n')
        w.append("""<gpx version="1.1" creator="$creator" xmlns="http://www.topografix.com/GPX/1/1">""")
        w.append('\n')
        for (wp in waypoints) {
            w.append("""  <wpt lat="${wp.latitude}" lon="${wp.longitude}">""")
            w.append('\n')
            w.append("    <name>${xmlEscape(wp.name)}</name>\n")
            wp.elevationMeters?.let { w.append("    <ele>$it</ele>\n") }
            w.append("    <time>${Instant.ofEpochMilli(wp.createdAt)}</time>\n")
            wp.note?.let { w.append("    <desc>${xmlEscape(it)}</desc>\n") }
            w.append("    <type>MGRS:${xmlEscape(wp.mgrs)}</type>\n")
            w.append("  </wpt>\n")
        }
        w.append("</gpx>\n")
        return w.toString()
    }

    /**
     * Lightweight parser that does not require Android XmlPullParser —
     * used by JVM unit tests and as a resilient fallback.
     */
    fun importWaypointsLightweight(gpx: String): List<ImportedWaypoint> {
        val results = mutableListOf<ImportedWaypoint>()
        val wptRegex = Regex(
            """<wpt\s+([^>]+)>(.*?)</wpt>""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
        )
        val attrLat = Regex("""lat\s*=\s*"([+-]?\d+(\.\d+)?)"""", RegexOption.IGNORE_CASE)
        val attrLon = Regex("""lon\s*=\s*"([+-]?\d+(\.\d+)?)"""", RegexOption.IGNORE_CASE)
        fun tag(body: String, name: String): String? {
            val r = Regex("""<$name>(.*?)</$name>""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
            return r.find(body)?.groupValues?.get(1)?.trim()
        }
        for (m in wptRegex.findAll(gpx)) {
            val attrs = m.groupValues[1]
            val body = m.groupValues[2]
            val lat = attrLat.find(attrs)?.groupValues?.get(1)?.toDoubleOrNull() ?: continue
            val lon = attrLon.find(attrs)?.groupValues?.get(1)?.toDoubleOrNull() ?: continue
            val name = tag(body, "name") ?: "WP"
            val ele = tag(body, "ele")?.toDoubleOrNull()
            val desc = tag(body, "desc")
            results += ImportedWaypoint(name, lat, lon, ele, desc)
        }
        return results
    }

    fun toEntities(imported: List<ImportedWaypoint>): List<WaypointEntity> =
        imported.map {
            val mgrs = try {
                MgrsConverter.toMgrs(it.latitude, it.longitude, MgrsPrecision.DIGITS_10)
            } catch (_: Exception) {
                ""
            }
            WaypointEntity(
                name = it.name,
                latitude = it.latitude,
                longitude = it.longitude,
                mgrs = mgrs,
                elevationMeters = it.elevationMeters,
                note = it.description
            )
        }

    data class ImportedWaypoint(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val elevationMeters: Double?,
        val description: String?
    )

    private fun xmlEscape(s: String): String =
        s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
}
