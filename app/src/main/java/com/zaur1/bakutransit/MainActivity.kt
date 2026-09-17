package com.zaur1.bakutransit

import android.os.Bundle
import android.view.Window
import android.graphics.Color as AndroidColor
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.maptiler.maptilersdk.MTConfig
import com.zaur1.bakutransit.ui.screens.MainScreen
import com.zaur1.bakutransit.ui.theme.BakuTransitTheme
import com.zaur1.bakutransit.ui.viewmodel.SettingsViewModel
import com.zaur1.bakutransit.ui.viewmodel.TransitViewModel
import com.zaur1.bakutransit.ui.viewmodel.TransitViewModelFactory
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {
    
    private val transitViewModel: TransitViewModel by viewModels {
        TransitViewModelFactory((application as TransitApplication).repository)
    }
    
    private val settingsViewModel: SettingsViewModel by viewModels()
    private var isMapReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT

        splashScreen.setKeepOnScreenCondition { !isMapReady }
        MTConfig.apiKey = BuildConfig.MAPTILER_API_KEY

        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            val recreateCount by settingsViewModel.recreateTrigger.collectAsState()
            var isChangingLanguage by remember { mutableStateOf(false) }

            // Handle language change with loading
            LaunchedEffect(recreateCount) {
                if (recreateCount > 0) {
                    isChangingLanguage = true
                    delay(800) // Show loading for smooth transition
                    recreate()
                }
            }

            BakuTransitTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isChangingLanguage) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        MainScreen(
                            transitViewModel = transitViewModel, 
                            settingsViewModel = settingsViewModel,
                            onMapReady = { isMapReady = true }
                        )
                    }
                }
            }
        }
    }
}
