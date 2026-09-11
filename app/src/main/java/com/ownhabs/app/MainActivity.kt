package com.ownhabs.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ownhabs.app.ui.navigation.OwnHabsNavGraph
import com.ownhabs.app.ui.strings.LanguagePreference
import com.ownhabs.app.ui.strings.LocalLang
import com.ownhabs.app.ui.strings.LocalStrings
import com.ownhabs.app.ui.strings.stringsFor
import com.ownhabs.app.ui.theme.OwnHabsTheme
import kotlinx.coroutines.delay

/** Ελάχιστη διάρκεια της δικής μας splash οθόνης, σε ms. Το σύστημα (Android 12+) βάζει
 *  υποχρεωτικά κυκλική μάσκα στο windowSplashScreenAnimatedIcon — δεν υπάρχει τρόπος να
 *  δείξουμε εκεί το πλήρες, ασυμπίεστο λογότιπο. Γι' αυτό η πολύ σύντομη συστημική splash
 *  (με το κανονικό app icon) απλά "παραδίδει" αμέσως σε αυτή τη δική μας Compose οθόνη,
 *  που δείχνει το ολόκληρο λογότυπο χωρίς καμία μάσκα, για τουλάχιστον ένα δευτερόλεπτο. */
private const val SPLASH_MIN_DURATION_MS = 1100L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val container = (application as OwnHabsApp).container

        setContent {
            var showAppSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(SPLASH_MIN_DURATION_MS)
                showAppSplash = false
            }

            var lang by remember { mutableStateOf(LanguagePreference.get(this)) }

            OwnHabsTheme {
                if (showAppSplash) {
                    FullLogoSplash()
                } else {
                    RequestNotificationPermissionOnce()
                    CompositionLocalProvider(
                        LocalStrings provides stringsFor(lang),
                        LocalLang provides lang
                    ) {
                        OwnHabsNavGraph(
                            repository = container.repository,
                            categoryRepository = container.categoryRepository,
                            backupManager = container.backupManager,
                            onLanguageChange = { newLang ->
                                LanguagePreference.set(this, newLang)
                                lang = newLang
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Η δική μας splash οθόνη: το πλήρες λογότυπο (logo_full.png — ήδη περιέχει το wordmark
 * "OwnHabs" και το tagline), κεντραρισμένο, χωρίς καμία κυκλική ή άλλη μάσκα.
 */
@Composable
private fun FullLogoSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070C15)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_full),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.62f),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Στο Android 13+ (API 33) το POST_NOTIFICATIONS πρέπει να ζητηθεί ρητά κατά τη
 * λειτουργία της εφαρμογής — η δήλωση στο Manifest από μόνη της δεν αρκεί.
 * Χωρίς αυτό, οι τοπικές υπενθυμίσεις απλά δεν θα εμφανίζονται ποτέ σε νεότερο Android,
 * χωρίς κανένα ορατό σφάλμα.
 */
@Composable
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
