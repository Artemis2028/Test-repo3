package com.artemis.mgrsnav.ui.map

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import com.artemis.mgrsnav.domain.mgrs.MgrsConverter
import com.artemis.mgrsnav.domain.mgrs.MgrsPrecision
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

/**
 * Conceptual MGRS grid overlay: labels the viewport center and draws a
 * coarse 1 km-ish crosshair lattice in local ENU. Full zone-correct grid
 * lines can replace the lattice later without changing map screen wiring.
 */
class MgrsGridOverlay : Overlay() {
    private val linePaint = Paint().apply {
        color = Color.argb(140, 196, 163, 90)
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        color = Color.argb(220, 232, 226, 212)
        textSize = 28f
        isAntiAlias = true
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    var showRuler: Boolean = false
    var rulerStart: GeoPoint? = null
    var rulerEnd: GeoPoint? = null

    private val rulerPaint = Paint().apply {
        color = Color.argb(220, 61, 154, 95)
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    override fun draw(c: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val proj = mapView.projection
        val center = mapView.mapCenter
        val lat = center.latitude
        val lon = center.longitude
        val mgrs = try {
            MgrsConverter.toMgrs(lat, lon, MgrsPrecision.DIGITS_8)
        } catch (_: Exception) {
            "—"
        }
        c.drawText(mgrs, 24f, 64f, textPaint)

        // Local meter grid approximation (~1 km)
        val mPerDegLat = 111_320.0
        val mPerDegLon = 111_320.0 * Math.cos(Math.toRadians(lat))
        val stepM = when {
            mapView.zoomLevelDouble >= 15 -> 100.0
            mapView.zoomLevelDouble >= 13 -> 1000.0
            else -> 10_000.0
        }
        val half = 3
        val screen = Point()
        for (i in -half..half) {
            for (j in -half..half) {
                val glat = lat + (i * stepM) / mPerDegLat
                val glon = lon + (j * stepM) / mPerDegLon
                proj.toPixels(GeoPoint(glat, glon), screen)
                c.drawCircle(screen.x.toFloat(), screen.y.toFloat(), 3f, linePaint)
            }
            // horizontal-ish line through row
            val a = GeoPoint(lat + (i * stepM) / mPerDegLat, lon - (half * stepM) / mPerDegLon)
            val b = GeoPoint(lat + (i * stepM) / mPerDegLat, lon + (half * stepM) / mPerDegLon)
            val pa = Point(); val pb = Point()
            proj.toPixels(a, pa); proj.toPixels(b, pb)
            c.drawLine(pa.x.toFloat(), pa.y.toFloat(), pb.x.toFloat(), pb.y.toFloat(), linePaint)
            val c1 = GeoPoint(lat - (half * stepM) / mPerDegLat, lon + (i * stepM) / mPerDegLon)
            val c2 = GeoPoint(lat + (half * stepM) / mPerDegLat, lon + (i * stepM) / mPerDegLon)
            proj.toPixels(c1, pa); proj.toPixels(c2, pb)
            c.drawLine(pa.x.toFloat(), pa.y.toFloat(), pb.x.toFloat(), pb.y.toFloat(), linePaint)
        }

        if (showRuler) {
            val s = rulerStart; val e = rulerEnd
            if (s != null && e != null) {
                val ps = Point(); val pe = Point()
                proj.toPixels(s, ps); proj.toPixels(e, pe)
                c.drawLine(ps.x.toFloat(), ps.y.toFloat(), pe.x.toFloat(), pe.y.toFloat(), rulerPaint)
                val dist = s.distanceToAsDouble(e)
                c.drawText(String.format("%.0f m", dist), ((ps.x + pe.x) / 2f), ((ps.y + pe.y) / 2f) - 12f, textPaint)
            }
        }
    }
}
