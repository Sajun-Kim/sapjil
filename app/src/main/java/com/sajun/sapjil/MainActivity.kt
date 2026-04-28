package com.sajun.sapjil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sajun.sapjil.ui.theme.SapjilTheme
import com.wedrive.designsystem.AppStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SapjilTheme {
                SapjilApp()
            }
        }
    }
}

@Composable
fun SapjilApp() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppStyle.color.background
    ) { innerPadding ->
        Navigation(
            navController = rememberNavController(),
            innerPadding = innerPadding,
            startDestination = Route.MAIN
        )
    }
}
