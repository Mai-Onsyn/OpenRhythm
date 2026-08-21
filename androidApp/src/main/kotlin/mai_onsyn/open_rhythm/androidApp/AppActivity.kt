package mai_onsyn.open_rhythm.androidApp

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.kmpbits.splash.SplashActivity
import kotlinx.coroutines.delay
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.bridge.initAndroid
import mai_onsyn.open_rhythm.core.log.LogManager
import mai_onsyn.open_rhythm.ui.App
import kotlin.time.Duration.Companion.milliseconds

class AppActivity: SplashActivity() {
    override fun onPreCreate() {
        enableEdgeToEdge()
    }

    override suspend fun isReady(): Boolean = true

    override fun onFinished() {
        initAndroid(this)
        LogManager.initialize()

        setContent {
            val configuration = LocalConfiguration.current
            val view = LocalView.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            DisposableEffect(isLandscape) {
                val window = (view.context as Activity).window

                if (isLandscape) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                } else {
                    window.clearFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                }

                onDispose {
                    window.clearFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                }
            }
            LaunchedEffect(Global.settings.MobileScreenOrientation) {
                requestedOrientation = getOrientation(Global.settings.MobileScreenOrientation)
            }
            App(onThemeChanged = { ThemeChanged(it) })
        }
    }
}

private fun getOrientation(settingValue: Int): Int {
    return when (settingValue) {
        0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        1 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        2 -> ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}

@Composable
private fun ThemeChanged(isDark: Boolean) {
    val view = LocalView.current
    LaunchedEffect(isDark) {
        val window = (view.context as Activity).window
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = isDark
            isAppearanceLightNavigationBars = isDark
        }
    }
}

//class _AppActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        initAndroid(this)
//        LogManager.initialize()
//
//        setContent {
//            val configuration = LocalConfiguration.current
//            val view = LocalView.current
//            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
//            DisposableEffect(isLandscape) {
//                val window = (view.context as Activity).window
//
//                if (isLandscape) {
//                    window.setFlags(
//                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                        WindowManager.LayoutParams.FLAG_FULLSCREEN
//                    )
//                } else {
//                    window.clearFlags(
//                        WindowManager.LayoutParams.FLAG_FULLSCREEN
//                    )
//                }
//
//                onDispose {
//                    window.clearFlags(
//                        WindowManager.LayoutParams.FLAG_FULLSCREEN
//                    )
//                }
//            }
//            LaunchedEffect(Global.settings.MobileScreenOrientation) {
//                requestedOrientation = getOrientation(Global.settings.MobileScreenOrientation)
//            }
//            App(onThemeChanged = { ThemeChanged(it) })
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        requestedOrientation = getOrientation(Global.settings.MobileScreenOrientation)
//    }
//}