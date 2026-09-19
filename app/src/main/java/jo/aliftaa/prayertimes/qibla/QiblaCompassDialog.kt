package jo.aliftaa.prayertimes.qibla

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import jo.aliftaa.prayertimes.R
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun QiblaCompassDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember { QiblaSensorManager(context) }
    var currentAzimuth by remember { mutableStateOf(0f) }
    val qiblaBearing = remember { QiblaSensorManager.calculateQiblaBearing() } // ~161° for Amman

    DisposableEffect(sensorManager) {
        sensorManager.onAzimuthChanged = { azimuth ->
            currentAzimuth = azimuth
        }
        sensorManager.start()
        onDispose {
            sensorManager.stop()
        }
    }

    // Smooth rotation angle
    val animatedCompassRotation by animateFloatAsState(
        targetValue = -currentAzimuth,
        animationSpec = tween(durationMillis = 200),
        label = "compassRotation"
    )

    // Qibla needle angle relative to device top
    val relativeQiblaAngle = (qiblaBearing - currentAzimuth + 360f) % 360f
    val isFacingQibla = abs((relativeQiblaAngle + 180f) % 360f - 180f) < 5f

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.qibla_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!sensorManager.hasSensors) {
                    // Fallback when device doesn't have sensors
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.qibla_sensor_missing),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.qibla_amman_direction),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else {
                    // Compass dial
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(240.dp)
                            .padding(8.dp)
                    ) {
                        // Dial background with cardinal directions
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(animatedCompassRotation)
                        ) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.minDimension / 2f - 12.dp.toPx()

                            // Outer ring
                            drawCircle(
                                color = Color.Gray.copy(alpha = 0.3f),
                                radius = radius,
                                center = center,
                                style = Stroke(width = 3.dp.toPx())
                            )

                            // Degree tick marks
                            for (angle in 0 until 360 step 30) {
                                val rad = Math.toRadians(angle.toDouble())
                                val startR = if (angle % 90 == 0) radius - 14.dp.toPx() else radius - 8.dp.toPx()
                                val startX = (center.x + startR * sin(rad)).toFloat()
                                val startY = (center.y - startR * cos(rad)).toFloat()
                                val endX = (center.x + radius * sin(rad)).toFloat()
                                val endY = (center.y - radius * cos(rad)).toFloat()

                                val tickColor = if (angle == 0) Color.Red else Color.Gray.copy(alpha = 0.6f)
                                drawLine(
                                    color = tickColor,
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = if (angle % 90 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                                )
                            }
                        }

                        // Qibla needle pointer pointing to Kaaba
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .rotate(relativeQiblaAngle),
                            tint = if (isFacingQibla) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                        )

                        // Center pin
                        Surface(
                            shape = CircleShape,
                            color = if (isFacingQibla) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        ) {}
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = String.format(Locale.US, stringResource(R.string.qibla_bearing), qiblaBearing),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = String.format(Locale.US, stringResource(R.string.qibla_heading), currentAzimuth),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
