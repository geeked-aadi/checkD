package com.example.eventattendance.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.eventattendance.presentation.admin.AdminDashboardScreen
import com.example.eventattendance.presentation.authentication.AdminAuthScreen
import com.example.eventattendance.presentation.eventdetails.EventDetailScreen
import com.example.eventattendance.presentation.home.HomeScreen
import com.example.eventattendance.presentation.scanner.ScannerScreen
import com.example.eventattendance.presentation.settings.SettingsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // 1. Home Screen (Single primary home page with event cards)
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) }
        ) {
            HomeScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // 2. Event Detail Screen (Stats, search bar, list of scanned attendees, scan button)
        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
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
                    navController.navigate(Screen.Scanner.createRoute(eventId))
                }
            )
        }

        // 3. Full-Screen QR Scanner
        composable(
            route = Screen.Scanner.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
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

        // 4. Settings Screen (Feedback options, app info, discreet Admin entry)
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
                onAdminAccessClick = {
                    navController.navigate(Screen.AdminAuth.route)
                }
            )
        }

        // 5. Admin Authentication Screen
        composable(
            route = Screen.AdminAuth.route,
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
            AdminAuthScreen(
                onBackClick = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Settings.route)
                    }
                }
            )
        }

        // 6. Admin Management Dashboard
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
            AdminDashboardScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
