package com.sajun.sapjil

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.sajun.sapjil.ui.theme.SapjilTheme
import com.wedrive.designsystem.R as DR

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SapjilTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestButtons(innerPadding)
                }
            }
        }
    }
}

@Composable
fun TestButtons(innerPadding: PaddingValues) {
    val context = LocalContext.current
    val callNumber = "119"

    val callLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_CALL, "tel:$callNumber".toUri())
            context.startActivity(intent)
        }
    }

    fun call() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            val intent = Intent(Intent.ACTION_CALL, "tel:$callNumber".toUri())
            context.startActivity(intent)
        } else {
            callLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL, "tel:$callNumber".toUri())
                context.startActivity(intent)
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(DR.color.main_blue)
            )
        ) {
            Text("Dial test")
        }

        Button(
            onClick = { call() },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(DR.color.main_blue)
            )
        ) {
            Text("Call test")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestButtonsPreview() {
    SapjilTheme {
        TestButtons(innerPadding = PaddingValues(0.dp))
    }
}
