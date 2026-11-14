package com.example.eventattendance.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object EventDetail : Screen("event_detail/{eventId}?scoped={scoped}") {
        fun createRoute(eventId: String, scoped: Boolean = false) = "event_detail/$eventId?scoped=$scoped"
    }
    data object Scanner : Screen("scanner/{eventId}?bulk={bulk}") {
        fun createRoute(eventId: String, isBulk: Boolean = false) = "scanner/$eventId?bulk=$isBulk"
    }
    data object Settings : Screen("settings")
    data object StaffLogin : Screen("staff_login")
    data object SpocDashboard : Screen("spoc_dashboard")
    data object AdminDashboard : Screen("admin_dashboard")
    data object SpocDetail : Screen("spoc_detail/{spocId}") {
        fun createRoute(spocId: String) = "spoc_detail/$spocId"
    }
    data object ParticipantDetail : Screen("participant_detail/{pId}") {
        fun createRoute(pId: String) = "participant_detail/$pId"
    }
    data object QrVerifier : Screen("qr_verifier")
}

