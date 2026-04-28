package com.sajun.sapjil.screen.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SensorData(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f)
data class RotationData(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f, val w: Float = 1f)

interface ISensorTestViewModel {
    val isUpdating: StateFlow<Boolean>
    val accelerometer: StateFlow<SensorData>
    val gyroscope: StateFlow<SensorData>
    val magnetometer: StateFlow<SensorData>
    val rotationVector: StateFlow<RotationData>

    fun toggleUpdate()
}

@HiltViewModel
class SensorTestViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel(), ISensorTestViewModel, SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _isUpdating = MutableStateFlow(false)
    private val _accelerometer = MutableStateFlow(SensorData())
    private val _gyroscope = MutableStateFlow(SensorData())
    private val _magnetometer = MutableStateFlow(SensorData())
    private val _rotationVector = MutableStateFlow(RotationData())

    override val isUpdating = _isUpdating.asStateFlow()
    override val accelerometer = _accelerometer.asStateFlow()
    override val gyroscope = _gyroscope.asStateFlow()
    override val magnetometer = _magnetometer.asStateFlow()
    override val rotationVector = _rotationVector.asStateFlow()

    override fun toggleUpdate() {
        if (_isUpdating.value) stopSensors() else startSensors()
    }

    private fun startSensors() {
        listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_ROTATION_VECTOR
        ).forEach { type ->
            sensorManager.getDefaultSensor(type)?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        _isUpdating.value = true
    }

    private fun stopSensors() {
        sensorManager.unregisterListener(this)
        _isUpdating.value = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER   -> _accelerometer.value = SensorData(event.values[0], event.values[1], event.values[2])
            Sensor.TYPE_GYROSCOPE       -> _gyroscope.value = SensorData(event.values[0], event.values[1], event.values[2])
            Sensor.TYPE_MAGNETIC_FIELD  -> _magnetometer.value = SensorData(event.values[0], event.values[1], event.values[2])
            Sensor.TYPE_ROTATION_VECTOR -> _rotationVector.value = RotationData(
                x = event.values[0],
                y = event.values[1],
                z = event.values[2],
                w = if (event.values.size > 3) event.values[3] else 0f
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    override fun onCleared() {
        super.onCleared()
        stopSensors()
    }
}

object FakeSensorTestViewModel : ISensorTestViewModel {
    override val isUpdating = MutableStateFlow(false)
    override val accelerometer = MutableStateFlow(SensorData())
    override val gyroscope = MutableStateFlow(SensorData())
    override val magnetometer = MutableStateFlow(SensorData())
    override val rotationVector = MutableStateFlow(RotationData())

    override fun toggleUpdate() = Unit
}
