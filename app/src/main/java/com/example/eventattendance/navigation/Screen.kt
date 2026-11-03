package com.example.eventattendance.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    data object Scanner : Screen("scanner/{eventId}") {
        fun createRoute(eventId: String) = "scanner/$eventId"
    }
    data object Settings : Screen("settings")
    data object AdminAuth : Screen("admin_auth")
    data object AdminDashboard : Screen("admin_dashboard")
}
