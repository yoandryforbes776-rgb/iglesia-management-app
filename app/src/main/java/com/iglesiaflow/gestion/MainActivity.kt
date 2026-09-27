package com.iglesiaflow.gestion

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.iglesiaflow.gestion.ui.RootViewModel
import com.iglesiaflow.gestion.ui.navigation.AppShell
import com.iglesiaflow.gestion.ui.screens.auth.LoginScreen
import com.iglesiaflow.gestion.ui.theme.IglesiaFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            val viewModel: RootViewModel = hiltViewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val user by viewModel.currentUser.collectAsStateWithLifecycle()
            val windowSizeClass = calculateWindowSizeClass(this)

            IglesiaFlowTheme(settings = settings) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val currentUser = user
                    if (currentUser == null) {
                        LoginScreen(settings = settings)
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
