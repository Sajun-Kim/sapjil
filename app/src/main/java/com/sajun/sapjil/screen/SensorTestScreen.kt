package com.sajun.sapjil.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sajun.sapjil.ui.theme.SapjilTheme
import com.wedrive.designsystem.BackTopBar

@Composable
fun SensorTestScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        BackTopBar(
            title = "Sensor Test",
            onBackClick = { navController.navigateUp() }
        )
        Spacer(Modifier.height(200.dp))
        Text("Sensor Test Screen")
    }
}

@Composable
@Preview(showBackground = true)
fun SensorTestScreenPreview() {
    SapjilTheme {
        SensorTestScreen(
            navController = rememberNavController()
        )
    }
}