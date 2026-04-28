package com.sajun.sapjil.screen.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import com.wedrive.core.util.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.sqrt

// 3축(X/Y/Z) 센서 데이터 + 벡터 크기(magnitude)
data class SensorData(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    // magnitude = √(x² + y² + z²): 방향 무관한 전체 크기
    val magnitude: Float = 0f
)

// 회전 벡터(쿼터니언) 데이터
data class RotationData(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f, val w: Float = 1f)

// 충격 감지 상태
data class ImpactState(
    val isDetected: Boolean = false,
    // 마지막으로 감지된 충격의 고주파 가속도 크기 (m/s²)
    val lastMagnitude: Float = 0f
)

interface ISensorTestViewModel {
    val isUpdating: StateFlow<Boolean>
    val accelerometer: StateFlow<SensorData>
    val gyroscope: StateFlow<SensorData>
    val magnetometer: StateFlow<SensorData>
    val rotationVector: StateFlow<RotationData>
    val impactState: StateFlow<ImpactState>
    val IMPACT_THRESHOLD: Float
    val impactThreshold: StateFlow<Float>

    fun toggleUpdate()
    fun setImpactThreshold(threshold: Float)
}

@HiltViewModel
class SensorTestViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel(), ISensorTestViewModel, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _isUpdating = MutableStateFlow(false)
    private val _accelerometer = MutableStateFlow(SensorData())
    private val _gyroscope = MutableStateFlow(SensorData())
    private val _magnetometer = MutableStateFlow(SensorData())
    private val _rotationVector = MutableStateFlow(RotationData())
    private val _impactState = MutableStateFlow(ImpactState())

    override val isUpdating = _isUpdating.asStateFlow()
    override val accelerometer = _accelerometer.asStateFlow()
    override val gyroscope = _gyroscope.asStateFlow()
    override val magnetometer = _magnetometer.asStateFlow()
    override val rotationVector = _rotationVector.asStateFlow()
    override val impactState = _impactState.asStateFlow()

    // ── 저주파 필터 상태값 ──────────────────────────────────────────
    // 저주파 필터: 느리게 변하는 중력 성분만 남긴다
    // α(알파)가 클수록 이전 값 비중이 높아 더 부드럽게 변함
    // α=0.8 → 새 값을 20%만 반영 → 중력처럼 서서히 변하는 성분 추출에 적합
    private val ALPHA = 0.8f
    private var lowPassX = 0f
    private var lowPassY = 0f
    private var lowPassZ = 0f

    // ── 충격 감지 파라미터 ──────────────────────────────────────────
    // 충격 판정 임계값 (m/s²): 중력 제거 후 순수 충격 성분 기준
    // 정지 시 0
    //! 실사용 시 고정값으로 설정, 현재 테스트용이라 수정 가능
    override val IMPACT_THRESHOLD = 80f // 실사용 시 고정값
    private val _impactThreshold = MutableStateFlow(80f)
    override val impactThreshold = _impactThreshold.asStateFlow()

    // 동일 충격이 연속으로 감지되는 것을 막기 위한 대기 시간 (ms)
    private val COOLDOWN_MS = 2000L
    private var lastImpactTime = 0L

    override fun toggleUpdate() {
        if (_isUpdating.value) stopSensors() else startSensors()
    }

    override fun setImpactThreshold(threshold: Float) {
        _impactThreshold.value = threshold
    }

    private fun startSensors() {
        listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_ROTATION_VECTOR
        ).forEach { type ->
            sensorManager.getDefaultSensor(type)?.let {
                // SENSOR_DELAY_GAME: 약 20ms 간격 — 충격처럼 순간적인 변화를 놓치지 않도록
                // (SENSOR_DELAY_NORMAL은 약 200ms로 너무 느려 충격 감지 부적합)
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
        _isUpdating.value = true
    }

    private fun stopSensors() {
        sensorManager.unregisterListener(this)
        _isUpdating.value = false
        _impactState.value = ImpactState()
        // 필터 상태도 초기화 (재시작 시 이전 값 영향 제거)
        lowPassX = 0f
        lowPassY = 0f
        lowPassZ = 0f
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> processAccelerometer(event.values)
            Sensor.TYPE_GYROSCOPE -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                _gyroscope.value = SensorData(x, y, z, sqrt(x * x + y * y + z * z))
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                _magnetometer.value = SensorData(x, y, z, sqrt(x * x + y * y + z * z))
            }
            Sensor.TYPE_ROTATION_VECTOR -> _rotationVector.value = RotationData(
                x = event.values[0],
                y = event.values[1],
                z = event.values[2],
                w = if (event.values.size > 3) event.values[3] else 0f
            )
        }
    }

    private fun processAccelerometer(values: FloatArray) {
        val rawX = values[0]
        val rawY = values[1]
        val rawZ = values[2]

        // ── 저주파 필터 적용 (중력 성분 추정) ──────────────────────
        // 공식: lowPass = α * lowPass + (1 - α) * raw
        // 결과: 빠르게 변하는 충격은 걸러지고, 중력처럼 느린 변화만 남음
        lowPassX = ALPHA * lowPassX + (1 - ALPHA) * rawX
        lowPassY = ALPHA * lowPassY + (1 - ALPHA) * rawY
        lowPassZ = ALPHA * lowPassZ + (1 - ALPHA) * rawZ

        // ── 고주파 성분 추출 (실제 충격/진동 성분) ─────────────────
        // 원시값 - 저주파(중력) = 순수 동적 가속도
        // 정지 시 0, 충격 발생 시 순간적으로 크게 증가
        val impactX = rawX - lowPassX
        val impactY = rawY - lowPassY
        val impactZ = rawZ - lowPassZ

        // ── 벡터 크기 계산 ──────────────────────────────────────────
        // 원시 magnitude (화면 표시용): 정지 시 중력만 있으므로 ≈ 9.8 m/s²
        val rawMagnitude = sqrt(rawX * rawX + rawY * rawY + rawZ * rawZ)

        // 충격 magnitude (감지 판단용): 중력 제거 후이므로 정지 시 0 m/s²
        val impactMagnitude = sqrt(impactX * impactX + impactY * impactY + impactZ * impactZ)

        _accelerometer.value = SensorData(rawX, rawY, rawZ, rawMagnitude)

        checkImpact(impactMagnitude)
    }

    private fun checkImpact(impactMagnitude: Float) {
        val now = System.currentTimeMillis()

        if (impactMagnitude > _impactThreshold.value && now - lastImpactTime > COOLDOWN_MS) {
            // 임계값 초과 + 쿨다운 경과 → 새 충격 이벤트
            lastImpactTime = now
            _impactState.value = ImpactState(isDetected = true, lastMagnitude = impactMagnitude)
            context.showToast("충격 감지! 강도: ${"%.1f".format(impactMagnitude)} m/s²")
        } else if (_impactState.value.isDetected && now - lastImpactTime > COOLDOWN_MS) {
            // 충격 감지 후 쿨다운이 지나면 감지 상태 해제 (lastMagnitude는 유지)
            _impactState.value = _impactState.value.copy(isDetected = false)
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
    override val impactState = MutableStateFlow(ImpactState())
    override val IMPACT_THRESHOLD = 80f
    override val impactThreshold = MutableStateFlow(80f)

    override fun toggleUpdate() = Unit
    override fun setImpactThreshold(threshold: Float) {}
}
