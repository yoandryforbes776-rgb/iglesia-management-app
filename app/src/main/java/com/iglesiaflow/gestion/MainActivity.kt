package com.iglesiaflow.gestion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesiaflow.gestion.ui.RootViewModel
import com.iglesiaflow.gestion.ui.navigation.AppShell
import com.iglesiaflow.gestion.ui.screens.auth.LoginScreen
import com.iglesiaflow.gestion.ui.theme.IglesiaFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val rootViewModel: RootViewModel by viewModels()

    /** Cada toque en pantalla renueva los 30 minutos de sesión. */
    override fun onUserInteraction() {
        super.onUserInteraction()
        rootViewModel.touch()
    }

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    /** En Android 13+ hay que pedir permiso para poder avisar de las ausencias. */
    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onResume() {
        super.onResume()
        rootViewModel.touch()
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        ensureNotificationPermission()
        setContent {
            val viewModel: RootViewModel = rootViewModel
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val user by viewModel.currentUser.collectAsStateWithLifecycle()
            val sessionExpired by viewModel.sessionExpired.collectAsStateWithLifecycle()
            val windowSizeClass = calculateWindowSizeClass(this)

            IglesiaFlowTheme(settings = settings) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val currentUser = user
                    if (currentUser == null) {
                        LoginScreen(
                            settings = settings,
                            notice = if (sessionExpired) {
                                "Tu sesión se cerró automáticamente tras 30 minutos sin actividad."
                            } else {
                                null
                            }
                        )
                    } else {
                        AppShell(
                            settings = settings,
                            user = currentUser,
                            widthSizeClass = windowSizeClass.widthSizeClass,
                            onLogout = viewModel::logout
                        )
                    }
                }
            }
        }
    }
}
