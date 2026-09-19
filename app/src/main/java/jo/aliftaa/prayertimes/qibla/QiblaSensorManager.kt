package jo.aliftaa.prayertimes.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class QiblaSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val hasSensors: Boolean = accelerometer != null && magnetometer != null

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    var onAzimuthChanged: ((Float) -> Unit)? = null

    // Kaaba coordinates (Mecca)
    companion object {
        const val MECCA_LAT = 21.422487
        const val MECCA_LNG = 39.826206

        // Amman coordinates
        const val AMMAN_LAT = 31.9539
        const val AMMAN_LNG = 35.9106

        fun calculateQiblaBearing(lat: Double = AMMAN_LAT, lng: Double = AMMAN_LNG): Float {
            val userLatRad = Math.toRadians(lat)
            val userLngRad = Math.toRadians(lng)
            val meccaLatRad = Math.toRadians(MECCA_LAT)
            val meccaLngRad = Math.toRadians(MECCA_LNG)

            val dLng = meccaLngRad - userLngRad
            val y = sin(dLng) * cos(meccaLatRad)
            val x = cos(userLatRad) * sin(meccaLatRad) - sin(userLatRad) * cos(meccaLatRad) * cos(dLng)

            var bearing = Math.toDegrees(atan2(y, x)).toFloat()
            bearing = (bearing + 360f) % 360f
            return bearing
        }
    }

    fun start() {
        if (!hasSensors) return
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val alpha = 0.97f
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0]
            geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1]
            geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2]
            hasGeomagnetic = true
        }

        if (hasGravity && hasGeomagnetic) {
            val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuthInRadians = orientation[0]
                var azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
                azimuthInDegrees = (azimuthInDegrees + 360f) % 360f
                onAzimuthChanged?.invoke(azimuthInDegrees)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
