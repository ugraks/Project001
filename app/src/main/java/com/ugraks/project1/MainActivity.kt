package com.ugraks.project1

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import com.ugraks.project1.AppNavigation.SayfaGecisleri
import com.ugraks.project1.AppNavigation.Screens
import com.ugraks.project1.ui.theme.Project1Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val isRestarting = savedInstanceState != null

        // Process kill sonrası yeniden başlatılıyorsa (savedInstanceState null değilse),
        // Activity'nin state kurtarma mekanizmasını engellemek için super.onCreate'e null geçiyoruz.
        val stateToPassToSuper = if (isRestarting) {
            null
        } else {
            savedInstanceState
        }

        super.onCreate(stateToPassToSuper)

        enableEdgeToEdge()

        setContent {
            Project1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Eğer Activity yeniden başlatılıyorsa (isRestarting true ise),
                    // navigasyon kurtarma hatasını önlemek ve başlangıç sayfasına yönlendirmek için
                    LaunchedEffect(isRestarting, navController) {
                        if (isRestarting) {
                            // Başlangıç destinasyonuna programatik olarak git
                            navController.navigate(Screens.ScreenHomePage) {
                                // Geri yığınındaki her şeyi (başlangıç dahil) temizle
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                    saveState = false
                                }
                                // Aynı hedefe birden çok kez gitmeyi engelle
                                launchSingleTop = true
                                // Navigasyon sırasında durumu kurtarma girişimini yoksay
                                restoreState = false
                            }
                        }
                    }

                    // Ana Navigasyon Host Composable'ını çağır
                    SayfaGecisleri(navController = navController)
                }
            }
        }
    }
}