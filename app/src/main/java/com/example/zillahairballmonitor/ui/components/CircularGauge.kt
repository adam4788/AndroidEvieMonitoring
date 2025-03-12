package com.example.zillahairballmonitor.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zillahairballmonitor.ui.theme.GaugeBackground
import com.example.zillahairballmonitor.ui.theme.GaugeCritical
import com.example.zillahairballmonitor.ui.theme.GaugeNormal
import com.example.zillahairballmonitor.ui.theme.GaugeWarning
import com.example.zillahairballmonitor.ui.theme.Red500
import kotlin.math.roundToInt

/**
 * A circular gauge component for displaying metrics
 * 
 * @param value Current value to display in the gauge
 * @param maxValue Maximum value for the gauge
 * @param minValue Minimum value for the gauge
 * @param title Title of the gauge
 * @param unit Unit to display after the value (e.g., "V" for volts)
 * @param warningThreshold Threshold for warning color (percentage of maxValue)
 * @param criticalThreshold Threshold for critical color (percentage of maxValue)
 * @param modifier Modifier for the component
 */
@Composable
fun CircularGauge(
    value: Float,
    maxValue: Float,
    minValue: Float = 0f,
    title: String,
    unit: String,
    warningThreshold: Float = 0.7f,
    criticalThreshold: Float = 0.9f,
    modifier: Modifier = Modifier
) {
    val normalizedValue = (value - minValue) / (maxValue - minValue)
    val sweepAngle = 240f * normalizedValue.coerceIn(0f, 1f)
    
    val animatedSweepAngle by animateFloatAsState(
        targetValue = sweepAngle,
        animationSpec = tween(durationMillis = 500)
    )
    
    val gaugeColor = remember(value) {
        when {
            normalizedValue >= criticalThreshold -> GaugeCritical
            normalizedValue >= warningThreshold -> GaugeWarning
            else -> GaugeNormal
        }
    }
    
    Box(
        modifier = modifier
            .size(150.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Draw gauge
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val center = Offset(canvasWidth / 2, canvasHeight / 2)
            val radius = (canvasWidth.coerceAtMost(canvasHeight) / 2) * 0.8f
            
            // Background arc
            drawArc(
                color = GaugeBackground,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Value arc
            drawArc(
                color = gaugeColor,
                startAngle = 150f,
                sweepAngle = animatedSweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Draw tick marks
            val tickLength = 10.dp.toPx()
            val numTicks = 5
            val angleIncrement = 240f / (numTicks - 1)
            
            for (i in 0 until numTicks) {
                val angle = 150f + (i * angleIncrement)
                val angleRadians = Math.toRadians(angle.toDouble())
                val startX = center.x + (radius - tickLength / 2) * Math.cos(angleRadians).toFloat()
                val startY = center.y + (radius - tickLength / 2) * Math.sin(angleRadians).toFloat()
                val endX = center.x + (radius + tickLength / 2) * Math.cos(angleRadians).toFloat()
                val endY = center.y + (radius + tickLength / 2) * Math.sin(angleRadians).toFloat()
                
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        
        // Value text
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val displayValue = if (value >= 100f) value.roundToInt() else value
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = gaugeColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(
                            if (displayValue is Int || displayValue == displayValue.toInt().toFloat()) {
                                displayValue.toInt().toString()
                            } else {
                                String.format("%.1f", displayValue)
                            }
                        )
                    }
                    withStyle(
                        style = SpanStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    ) {
                        append(" $unit")
                    }
                },
                textAlign = TextAlign.Center
            )
        }
        
        // Title
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.caption.copy(
                    color = Color.White,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
