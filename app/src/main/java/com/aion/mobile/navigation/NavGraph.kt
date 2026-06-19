package com.aion.mobile.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Connect : Screen("connect")
    data object Home : Screen("home")
    data object Servers : Screen("servers")
    data object AddServer : Screen("add_server")
    data object Settings : Screen("settings")
    data object Reminders : Screen("reminders")
    data object AddReminder : Screen("add_reminder")
}
