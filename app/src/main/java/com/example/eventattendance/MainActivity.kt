package com.example.eventattendance

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.eventattendance.domain.model.ThemeMode
import com.example.eventattendance.navigation.AppNavigation
import com.example.eventattendance.presentation.theme.CheckdTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()

            CheckdTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isReady by mainViewModel.isReady.collectAsStateWithLifecycle()

                    if (!isReady) {
                        AppSplashScreen()
                    } else {
                        val navController = rememberNavController()
                        AppNavigation(
                            navController = navController,
                            mainViewModel = mainViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_checkd_logo),
                contentDescription = "checkD Logo",
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "checkD",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Event Attendance System",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview(name = "Splash - Light")
@Composable
private fun AppSplashScreenLightPreview() {
    CheckdTheme(themeMode = ThemeMode.LIGHT) {
        AppSplashScreen()
    }
}

@Preview(name = "Splash - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppSplashScreenDarkPreview() {
    CheckdTheme(themeMode = ThemeMode.DARK) {
        AppSplashScreen()
    }
}

