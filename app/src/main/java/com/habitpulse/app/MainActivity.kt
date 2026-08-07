package com.habitpulse.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.habitpulse.app.ui.navigation.HabitPulseNavGraph
import com.habitpulse.app.ui.theme.HabitPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val container = (application as HabitPulseApp).container

        setContent {
            HabitPulseTheme {
                RequestNotificationPermissionOnce()
                HabitPulseNavGraph(
                    repository = container.repository,
                    categoryRepository = container.categoryRepository,
                    backupManager = container.backupManager
                )
            }
        }
    }
}

/**
 * Στο Android 13+ (API 33) το POST_NOTIFICATIONS πρέπει να ζητηθεί ρητά κατά τη
 * λειτουργία της εφαρμογής — η δήλωση στο Manifest από μόνη της δεν αρκεί.
 * Χωρίς αυτό, οι τοπικές υπενθυμίσεις απλά δεν θα εμφανίζονται ποτέ σε νεότερο Android,
 * χωρίς κανένα ορατό σφάλμα.
 */
@androidx.compose.runtime.Composable
private fun RequestNotificationPermissionOnce() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Αν το αρνηθεί, η εφαρμογή συνεχίζει κανονικά — απλά χωρίς υπενθυμίσεις. */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
