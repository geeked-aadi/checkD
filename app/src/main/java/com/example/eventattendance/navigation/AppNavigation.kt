package com.example.eventattendance.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.eventattendance.MainViewModel
import com.example.eventattendance.domain.model.UserRole
import com.example.eventattendance.presentation.admin.AdminDashboardScreen
import com.example.eventattendance.presentation.admin.ParticipantDetailScreen
import com.example.eventattendance.presentation.admin.SpocDetailScreen
import com.example.eventattendance.presentation.authentication.StaffAuthScreen
import com.example.eventattendance.presentation.eventdetails.EventDetailScreen
import com.example.eventattendance.presentation.home.HomeScreen
import com.example.eventattendance.presentation.scanner.ScannerScreen
import com.example.eventattendance.presentation.settings.SettingsScreen
import com.example.eventattendance.presentation.spoc.SpocDashboardScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by mainViewModel.currentRole.collectAsStateWithLifecycle()

    // Determine start destination: if DataStore holds a staff role, start at that dashboard instead of Home (Correction 6)
    val startDestination = remember {
        when (mainViewModel.currentRole.value) {
            is UserRole.Spoc -> Screen.SpocDashboard.route
            is UserRole.Admin -> Screen.AdminDashboard.route
            is UserRole.Volunteer -> Screen.Home.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 1. Home Screen (Volunteer event list)
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) }
        ) {
            HomeScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId, scoped = false))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // 2. Event Detail Screen (Scoped or Unscoped)
        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("scoped") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            EventDetailScreen(
                onBackClick = { navController.popBackStack() },
                onScanClick = { eventId ->
                    navController.navigate(Screen.Scanner.createRoute(eventId, isBulk = false))
                },
                onBulkScanClick = { eventId ->
                    navController.navigate(Screen.Scanner.createRoute(eventId, isBulk = true))
                },
                onParticipantClick = { pId ->
                    if (currentRole is UserRole.Admin || currentRole is UserRole.Spoc) {
                        navController.navigate(Screen.ParticipantDetail.createRoute(pId))
                    }
                }
            )
        }

        // 3. Scanner Screen (Supports single scan and bulk scan modes)
        composable(
            route = Screen.Scanner.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("bulk") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(300)
                )
            }
        ) {
            ScannerScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // 4. Settings Screen (Preferences, role access, cache sync)
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onStaffLoginClick = {
                    navController.navigate(Screen.StaffLogin.route)
                },
                onSpocDashboardClick = {
                    navController.navigate(Screen.SpocDashboard.route)
                },
                onAdminDashboardClick = {
                    navController.navigate(Screen.AdminDashboard.route)
                }
            )
        }

        // 5. Staff Authentication Screen (SPOC & Admin login)
        composable(
            route = Screen.StaffLogin.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            StaffAuthScreen(
                onBackClick = { navController.popBackStack() },
                onLoginSuccess = { role ->
                    val destination = when (role) {
                        is UserRole.Spoc -> Screen.SpocDashboard.route
                        is UserRole.Admin -> Screen.AdminDashboard.route
                        is UserRole.Volunteer -> Screen.Home.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.StaffLogin.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // 6. SPOC Dashboard (Guarded: only accessible by UserRole.Spoc)
        composable(
            route = Screen.SpocDashboard.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            // Role Guard: Pop to Home if role is not Spoc (Correction 6)
            LaunchedEffect(currentRole) {
                if (currentRole !is UserRole.Spoc) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            if (currentRole is UserRole.Spoc) {
                SpocDashboardScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId.toString(), scoped = true))
                    },
                    onParticipantClick = { pId ->
                        navController.navigate(Screen.ParticipantDetail.createRoute(pId))
                    },
                    onLogout = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(Screen.Home.route)
                        }
                    }
                )
            }
        }

        // 7. Admin Dashboard (Guarded: only accessible by UserRole.Admin)
        composable(
            route = Screen.AdminDashboard.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            // Role Guard: Pop to Home if role is not Admin (Correction 6)
            LaunchedEffect(currentRole) {
                if (currentRole !is UserRole.Admin) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            if (currentRole is UserRole.Admin) {
                AdminDashboardScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId.toString(), scoped = false))
                    },
                    onSpocClick = { spocId ->
                        navController.navigate(Screen.SpocDetail.createRoute(spocId))
                    },
                    onParticipantClick = { pId ->
                        navController.navigate(Screen.ParticipantDetail.createRoute(pId))
                    },
                    onLogout = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(Screen.Home.route)
                        }
                    }
                )
            }
        }

        // 8. Admin Spoc Detail Screen
        composable(
            route = Screen.SpocDetail.route,
            arguments = listOf(
                navArgument("spocId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            LaunchedEffect(currentRole) {
                if (currentRole !is UserRole.Admin) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            if (currentRole is UserRole.Admin) {
                SpocDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onParticipantClick = { pId ->
                        navController.navigate(Screen.ParticipantDetail.createRoute(pId))
                    }
                )
            }
        }

        // 9. Participant Detail / Entire Journey Screen
        composable(
            route = Screen.ParticipantDetail.route,
            arguments = listOf(
                navArgument("pId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            LaunchedEffect(currentRole) {
                if (currentRole !is UserRole.Admin && currentRole !is UserRole.Spoc) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            if (currentRole is UserRole.Admin || currentRole is UserRole.Spoc) {
                ParticipantDetailScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
