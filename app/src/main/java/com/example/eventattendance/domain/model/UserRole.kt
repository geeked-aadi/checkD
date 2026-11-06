package com.example.eventattendance.domain.model

sealed interface UserRole {
    data object Volunteer : UserRole
    data class Spoc(val spocId: String) : UserRole
    data object Admin : UserRole
}
