package com.sajun.sapjil.screen.sensor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wedrive.designsystem.R as DR
import com.wedrive.designsystem.AppCard
import com.wedrive.designsystem.AppStyle
import com.wedrive.designsystem.AppTheme
import com.wedrive.designsystem.BackTopBar
import com.wedrive.designsystem.PrimaryButton

@Composable
fun SensorTestScreen(
    navController: NavController,
    viewModel: ISensorTestViewModel = hiltViewModel<SensorTestViewModel>()
) {
    val isUpdating by viewModel.isUpdating.collectAsState()
    val accelerometer by viewModel.accelerometer.collectAsState()
    val gyroscope by viewModel.gyroscope.collectAsState()
    val magnetometer by viewModel.magnetometer.collectAsState()
    val rotationVector by viewModel.rotationVector.collectAsState()
    val impactState by viewModel.impactState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        BackTopBar(
            title = "Sensor Test",
            onBackClick = { navController.navigateUp() }
        )
        Spacer(Modifier.height(16.dp))

        // 가속도계: magnitude(벡터 크기)도 함께 표시
        SensorSection(title = "가속도계 (Accelerometer)") {
            SensorXYZ(data = accelerometer, showMagnitude = true)
        }
        Spacer(Modifier.height(16.dp))
        SensorSection(title = "자이로스코프 (Gyroscope)") {
            SensorXYZ(data = gyroscope)
        }
        Spacer(Modifier.height(16.dp))
        SensorSection(title = "자기장 (Magnetometer)") {
            SensorXYZ(data = magnetometer)
        }
        Spacer(Modifier.height(16.dp))
        SensorSection(title = "회전 벡터 (Rotation Vector)") {
            SensorXYZW(data = rotationVector)
        }
        Spacer(Modifier.height(16.dp))

        // 충격 감지 상태 패널
        ImpactStateSection(isUpdating = isUpdating, impactState = impactState)

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            onClick = viewModel::toggleUpdate,
            text = if (!isUpdating) "센서 업데이트 시작" else "센서 업데이트 종료"
        )
    }
}

@Composable
private fun SensorSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = AppStyle.typo.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            containerColorRes = DR.color.neutral_010
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SensorXYZ(data: SensorData, showMagnitude: Boolean = false) {
    Text(text = "X: %.6f".format(data.x), style = AppStyle.typo.bodyMedium)
    Spacer(Modifier.height(4.dp))
    Text(text = "Y: %.6f".format(data.y), style = AppStyle.typo.bodyMedium)
    Spacer(Modifier.height(4.dp))
    Text(text = "Z: %.6f".format(data.z), style = AppStyle.typo.bodyMedium)
    if (showMagnitude) {
        Spacer(Modifier.height(4.dp))
        // magnitude: 3축 벡터 크기 = √(x²+y²+z²), 정지 시 중력만 있으므로 ≈ 9.8 m/s²
        Text(
            text = "Magnitude: %.6f m/s²".format(data.magnitude),
            style = AppStyle.typo.bodyMedium
        )
    }
}

@Composable
private fun SensorXYZW(data: RotationData) {
    Text(text = "X: %.6f".format(data.x), style = AppStyle.typo.bodyMedium)
    Spacer(Modifier.height(4.dp))
    Text(text = "Y: %.6f".format(data.y), style = AppStyle.typo.bodyMedium)
    Spacer(Modifier.height(4.dp))
    Text(text = "Z: %.6f".format(data.z), style = AppStyle.typo.bodyMedium)
    Spacer(Modifier.height(4.dp))
    Text(text = "W: %.6f".format(data.w), style = AppStyle.typo.bodyMedium)
}

@Composable
private fun ImpactStateSection(isUpdating: Boolean, impactState: ImpactState) {
    Column {
        Text(
            text = "충격 감지 상태",
            style = AppStyle.typo.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            containerColorRes = DR.color.neutral_010
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                when {
                    !isUpdating -> {
                        Text(
                            text = "센서 비활성 — 업데이트 시작 시 활성화",
                            style = AppStyle.typo.bodyMedium,
                            color = colorResource(DR.color.neutral_040)
                        )
                    }
                    impactState.isDetected -> {
                        Text(
                            text = "⚠ 충격 감지됨",
                            style = AppStyle.typo.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = colorResource(DR.color.sub_red)
                        )
                        if (impactState.lastMagnitude > 0f) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "강도: ${"%.2f".format(impactState.lastMagnitude)} m/s²",
                                style = AppStyle.typo.bodyMedium,
                                color = colorResource(DR.color.sub_red)
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "정상 — 충격 없음",
                            style = AppStyle.typo.bodyMedium,
                            color = colorResource(DR.color.sub_green)
                        )
                        if (impactState.lastMagnitude > 0f) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "마지막 충격 강도: ${"%.2f".format(impactState.lastMagnitude)} m/s²",
                                style = AppStyle.typo.bodyMedium,
                                color = colorResource(DR.color.neutral_040)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SensorTestScreenPreview() {
    AppTheme {
        SensorTestScreen(
            viewModel = FakeSensorTestViewModel,
            navController = rememberNavController()
        )
    }
}
