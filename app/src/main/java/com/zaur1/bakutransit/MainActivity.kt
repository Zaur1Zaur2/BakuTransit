package com.zaur1.bakutransit

import android.os.Bundle
import android.view.Window
import android.graphics.Color as AndroidColor
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.maptiler.maptilersdk.MTConfig
import com.zaur1.bakutransit.ui.screens.MainScreen
import com.zaur1.bakutransit.ui.theme.BakuTransitTheme
import com.zaur1.bakutransit.ui.viewmodel.SettingsViewModel
import com.zaur1.bakutransit.ui.viewmodel.TransitViewModel
import com.zaur1.bakutransit.ui.viewmodel.TransitViewModelFactory

/**
 * Entry point Activity.
 * Implements Android 12+ Splash Screen API and Edge-to-Edge visuals.
 */
class MainActivity : AppCompatActivity() {
    
    private val transitViewModel: TransitViewModel by viewModels {
        TransitViewModelFactory((application as TransitApplication).repository)
    }
    
    private val settingsViewModel: SettingsViewModel by viewModels()
    private var isMapReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Initialize Splash Screen (critical to call before super.onCreate)
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // 2. Window Visuals: Full Screen & Translucent Bars
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT

        // 3. Keep splash screen on until map informs us it's ready
        splashScreen.setKeepOnScreenCondition { !isMapReady }

        // 4. MapTiler Initialization
        MTConfig.apiKey = BuildConfig.MAPTILER_API_KEY

        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            
            BakuTransitTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        transitViewModel = transitViewModel, 
                        settingsViewModel = settingsViewModel,
                        onMapReady = { isMapReady = true } // Release splash screen
                    )
                }
            }
        }
    }
}
