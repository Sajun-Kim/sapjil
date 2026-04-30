package com.sajun.sapjil.screen

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sajun.sapjil.R
import com.wedrive.designsystem.R as DR
import com.sajun.sapjil.Route
import com.sajun.sapjil.ui.theme.SapjilTheme
import com.wedrive.core.composable.DoubleTapBackToExit
import com.wedrive.core.util.PermissionUtil
import com.wedrive.designsystem.PrimaryButton
import timber.log.Timber

@Composable
fun MainScreen(
    navController: NavController
) {
    val context = LocalContext.current

    val callNumber = "01064077053"
    val emergencyNumber = "119"
    val callLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_CALL, "tel:$callNumber".toUri())
            context.startActivity(intent)
        }
    }

    fun call(number: String) {
        PermissionUtil.checkPermission(
            context = context,
            permissions = arrayOf(Manifest.permission.CALL_PHONE),
            onSuccess = {
                val intent = Intent(Intent.ACTION_CALL, "tel:$number".toUri())
                context.startActivity(intent)
            },
            onFailure = {
                callLauncher.launch(Manifest.permission.CALL_PHONE)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Dial Test",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            PrimaryButton(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, "tel:$callNumber".toUri())
                        context.startActivity(intent)
                    } catch(e: Exception) {
                        Timber.tag("callError").e(e)
                    }
                },
                text = callNumber,
                widthInDp = 200,
                heightInDp = 40,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(4.dp))
            PrimaryButton(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, "tel:$emergencyNumber".toUri())
                        context.startActivity(intent)
                    } catch(e: Exception) {
                        Timber.tag("callError").e(e)
                    }
                },
                text = emergencyNumber,
                widthInDp = 200,
                heightInDp = 40,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = colorResource(DR.color.neutral_010),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = "Call Test",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            PrimaryButton(
                onClick = { call(callNumber) },
                text = callNumber,
                widthInDp = 200,
                heightInDp = 40,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(4.dp))
            PrimaryButton(
                onClick = { call(emergencyNumber) },
                text = emergencyNumber,
                widthInDp = 200,
                heightInDp = 40,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = colorResource(DR.color.neutral_010),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = "Sensor Test",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            PrimaryButton(
                onClick = { navController.navigate(Route.TEST_SENSOR) },
                text = "Sensor Test",
                widthInDp = 200,
                heightInDp = 40,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(4.dp))
            Spacer(Modifier.weight(1f))
        }
    }

    DoubleTapBackToExit(toastMessageRes = R.string.back_exit_toast)
}

@Composable
@Preview(showBackground = true)
fun MainScreenPreview() {
    SapjilTheme {
        MainScreen(
            navController = rememberNavController()
        )
    }
}