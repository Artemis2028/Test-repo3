package com.artemis.mgrsnav.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassDial(
    headingDeg: Float,
    mgrs: String,
    qualityLabel: String,
    modifier: Modifier = Modifier,
    ringColor: Color = MaterialTheme.colorScheme.primary,
    needleColor: Color = MaterialTheme.colorScheme.error
) {
    Box(modifier = modifier.fillMaxWidth().aspectRatio(1f).padding(16.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val r = size.minDimension / 2 * 0.92f
            drawCircle(color = ringColor.copy(alpha = 0.25f), radius = r, center = Offset(cx, cy), style = Stroke(width = 4.dp.toPx()))
            drawCircle(color = ringColor, radius = r, center = Offset(cx, cy), style = Stroke(width = 2.dp.toPx()))
            for (i in 0 until 360 step 10) {
                val rad = Math.toRadians(i.toDouble() - headingDeg)
                val outer = r
                val inner = if (i % 90 == 0) r * 0.82f else if (i % 30 == 0) r * 0.88f else r * 0.93f
                val x1 = cx + outer * sin(rad).toFloat()
                val y1 = cy - outer * cos(rad).toFloat()
                val x2 = cx + inner * sin(rad).toFloat()
                val y2 = cy - inner * cos(rad).toFloat()
                drawLine(ringColor, Offset(x1, y1), Offset(x2, y2), strokeWidth = if (i % 90 == 0) 3.dp.toPx() else 1.5.dp.toPx(), cap = StrokeCap.Round)
            }
            rotate(degrees = -headingDeg, pivot = Offset(cx, cy)) {
                // North needle
                drawLine(needleColor, Offset(cx, cy + r * 0.15f), Offset(cx, cy - r * 0.75f), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                drawLine(ringColor.copy(alpha = 0.7f), Offset(cx, cy - r * 0.1f), Offset(cx, cy + r * 0.55f), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
            }
        }
        Text(
            text = mgrs,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center).padding(top = 48.dp)
        )
        Text(
            text = qualityLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 28.dp)
        )
    }
}
