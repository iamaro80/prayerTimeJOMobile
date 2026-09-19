package jo.aliftaa.prayertimes.qibla

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import jo.aliftaa.prayertimes.R
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaCompassDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember { QiblaSensorManager(context) }
    var currentAzimuth by remember { mutableStateOf(0f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }

    // Resolve user location for Qibla calculation, falling back to Amman
    val userBearing = remember(context) {
        var userLat = QiblaSensorManager.AMMAN_LAT
        var userLng = QiblaSensorManager.AMMAN_LNG
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCoarse || hasFine) {
            try {
                val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val providers = locManager?.getProviders(true) ?: emptyList()
                var bestLocation: Location? = null
                for (provider in providers) {
                    val l = locManager?.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || l.time > bestLocation.time) {
                        bestLocation = l
                    }
                }
                if (bestLocation != null) {
                    userLat = bestLocation.latitude
                    userLng = bestLocation.longitude
                }
            } catch (_: SecurityException) {
                // Graceful fallback to Amman
            }
        }
        QiblaSensorManager.calculateQiblaBearing(userLat, userLng)
    }

    DisposableEffect(sensorManager) {
        sensorManager.onAzimuthChanged = { azimuth ->
            currentAzimuth = azimuth
        }
        sensorManager.onAccuracyChanged = { accuracy ->
            sensorAccuracy = accuracy
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
    val relativeQiblaAngle = (userBearing - currentAzimuth + 360f) % 360f
    val isFacingQibla = abs((relativeQiblaAngle + 180f) % 360f - 180f) < 5f

    // Needs calibration if accuracy is low or unreliable
    val needsCalibration = sensorAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW ||
            sensorAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE

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
                    // Calibration banner when accuracy is LOW or UNRELIABLE
                    AnimatedVisibility(visible = needsCalibration) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.qibla_calibration_needed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

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

                        // Qibla needle & Kaaba Icon pointing towards Makkah
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(relativeQiblaAngle),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 10.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_kaaba),
                                    contentDescription = stringResource(R.string.qibla_compass),
                                    modifier = Modifier.size(36.dp),
                                    tint = if (isFacingQibla) Color(0xFF10B981) else Color.Unspecified
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Pointer arrow
                                Canvas(modifier = Modifier.size(16.dp, 24.dp)) {
                                    val arrowPath = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(size.width / 2f, 0f)
                                        lineTo(size.width, size.height)
                                        lineTo(size.width / 2f, size.height * 0.7f)
                                        lineTo(0f, size.height)
                                        close()
                                    }
                                    drawPath(
                                        path = arrowPath,
                                        color = if (isFacingQibla) Color(0xFF10B981) else Color(0xFFE11D48)
                                    )
                                }
                            }
                        }

                        // Center pin
                        Surface(
                            shape = CircleShape,
                            color = if (isFacingQibla) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        ) {}
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = String.format(Locale.US, stringResource(R.string.qibla_bearing), userBearing),
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
