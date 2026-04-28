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
        Spacer(Modifier.height(32.dp))

        SensorSection(title = "가속도계 (Accelerometer)") {
            SensorXYZ(data = accelerometer)
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

        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = if (isUpdating) "업데이트 종료" else "업데이트 시작",
            onClick = viewModel::toggleUpdate
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
private fun SensorXYZ(data: SensorData) {
    Text(text = "X: %.6f".format(data.x), style = AppStyle.typo.bodyMedium)
    Spacer(Modifier.height(4.dp))
    Text(text = "Y: %.6f".format(data.y), style = AppStyle.typo.bodyMedium)
    Spacer(Modifier.height(4.dp))
    Text(text = "Z: %.6f".format(data.z), style = AppStyle.typo.bodyMedium)
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
@Preview(showBackground = true)
fun SensorTestScreenPreview() {
    AppTheme {
        SensorTestScreen(
            viewModel = FakeSensorTestViewModel,
            navController = rememberNavController()
        )
    }
}