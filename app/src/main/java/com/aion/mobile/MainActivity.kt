package com.aion.mobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aion.mobile.data.prefs.AppPreferences
import com.aion.mobile.navigation.Screen
import com.aion.mobile.ui.screen.AddReminderScreen
import com.aion.mobile.ui.screen.ConnectScreen
import com.aion.mobile.ui.screen.AddServerScreen
import com.aion.mobile.ui.screen.HomeScreen
import com.aion.mobile.ui.screen.RemindersScreen
import com.aion.mobile.ui.screen.ServersScreen
import com.aion.mobile.ui.screen.SettingsScreen
import com.aion.mobile.ui.screen.SplashScreen
import com.aion.mobile.ui.theme.AionMobileTheme

class MainActivity : ComponentActivity() {

    private lateinit var appPreferences: AppPreferences

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appPreferences = AppPreferences(this)

        requestNotificationPermission()

        setContent {
            val darkMode by appPreferences.darkMode.collectAsState(initial = false)
            val servers by appPreferences.servers.collectAsState(initial = emptyList())
            val activeServerId by appPreferences.activeServerId.collectAsState(initial = null)

            val activeServer = servers.find { it.id == activeServerId }
                ?: servers.firstOrNull()

            AionMobileTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route
                    ) {
                        composable(Screen.Splash.route) {
                            SplashScreen(
                                onNavigateToHome = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Home.route) {
                            val servers by appPreferences.servers.collectAsState(initial = emptyList())

                            if (servers.isEmpty()) {
                                ConnectScreen(
                                    appPreferences = appPreferences,
                                    onConnected = {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Home.route) { inclusive = true }
                                        }
                                    }
                                )
                            } else {
                                val activeServer = servers.find { it.id == activeServerId }
                                    ?: servers.firstOrNull()

                                HomeScreen(
                                    serverUrl = activeServer?.url,
                                    serverName = activeServer?.name ?: "",
                                    appPreferences = appPreferences,
                                    onNavigateToSplash = {
                                        navController.navigate(Screen.AddServer.route)
                                    }
                                )
                            }
                        }

                        composable(Screen.Servers.route) {
                            ServersScreen(
                                appPreferences = appPreferences,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAddServer = {
                                    navController.navigate(Screen.AddServer.route)
                                }
                            )
                        }

                        composable(Screen.AddServer.route) {
                            AddServerScreen(
                                appPreferences = appPreferences,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                appPreferences = appPreferences,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Reminders.route) {
                            RemindersScreen(
                                appPreferences = appPreferences,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAddReminder = {
                                    navController.navigate(Screen.AddReminder.route)
                                }
                            )
                        }

                        composable(Screen.AddReminder.route) {
                            AddReminderScreen(
                                appPreferences = appPreferences,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}