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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aion.mobile.data.prefs.AppPreferences
import kotlinx.coroutines.launch
import com.aion.mobile.navigation.Screen
import com.aion.mobile.ui.screen.AddReminderScreen
import com.aion.mobile.ui.screen.AddServerScreen
import com.aion.mobile.ui.screen.ConnectScreen
import com.aion.mobile.ui.screen.HomeScreen
import com.aion.mobile.ui.screen.RemindersScreen
import com.aion.mobile.ui.screen.ServersScreen
import com.aion.mobile.ui.screen.SettingsScreen
import com.aion.mobile.ui.screen.SplashScreen
import com.aion.mobile.ui.screen.WelcomeScreen
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
                            val hasSeenWelcome by appPreferences.hasSeenWelcome.collectAsState(initial = false)
                            SplashScreen(
                                onNavigateToHome = {
                                    if (!hasSeenWelcome) {
                                        navController.navigate(Screen.Welcome.route) {
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                        }
                                    } else {
                                        val dest = if (servers.isEmpty()) {
                                            Screen.Connect.route
                                        } else {
                                            Screen.Home.route
                                        }
                                        navController.navigate(dest) {
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable(Screen.Welcome.route) {
                            WelcomeScreen(
                                appPreferences = appPreferences,
                                onContinue = {
                                    val dest = if (servers.isEmpty()) {
                                        Screen.Connect.route
                                    } else {
                                        Screen.Home.route
                                    }
                                    navController.navigate(dest) {
                                        popUpTo(Screen.Welcome.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Connect.route) {
                            ConnectScreen(
                                appPreferences = appPreferences,
                                onConnected = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Connect.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Home.route) {
                            val s by appPreferences.servers.collectAsState(initial = emptyList())
                            val activeId by appPreferences.activeServerId.collectAsState(initial = null)
                            val currentActive = s.find { it.id == activeId } ?: s.firstOrNull()
                            val homeScope = rememberCoroutineScope()

                            HomeScreen(
                                serverUrl = currentActive?.url,
                                serverName = currentActive?.name ?: "",
                                appPreferences = appPreferences,
                                onNavigateToSplash = {
                                    navController.navigate(Screen.Connect.route) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                },
                                onNavigateToAddServer = {
                                    navController.navigate(Screen.AddServer.route)
                                },
                                onNavigateToSettings = {
                                    navController.navigate(Screen.Settings.route)
                                },
                                onNavigateToServers = {
                                    navController.navigate(Screen.Servers.route)
                                },
                                onNavigateToReminders = {
                                    navController.navigate(Screen.Reminders.route)
                                },
                                onViewTutorial = {
                                    homeScope.launch { appPreferences.setHasSeenWelcome(false) }
                                    navController.navigate(Screen.Welcome.route) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                },
                                onResetApp = {
                                    homeScope.launch { appPreferences.resetToWelcome() }
                                    navController.navigate(Screen.Welcome.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
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
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
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
