package com.example.bookhive.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Theme Colors
val PrimaryBlue = Color(0xFF2196F3)
val LightBackground = Color(0xFFF5F5F5)
val WhiteBackground = Color.White

// Navigation Routes
sealed class AppScreen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : AppScreen("dashboard", "Dashboard", Icons.Filled.Home)
    object ReadingLog : AppScreen("reading_log", "Reading Log", Icons.Filled.Menu)
    object Library : AppScreen("library", "My Library", Icons.Filled.Menu)
    object Profile : AppScreen("profile", "Profile", Icons.Filled.Person)
}

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookHiveApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookHiveApp() {
    val navController = rememberNavController()
    val screens = listOf(AppScreen.Dashboard, AppScreen.ReadingLog, AppScreen.Library, AppScreen.Profile)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentScreen = screens.find { it.route == currentDestination?.route } ?: AppScreen.Dashboard

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentScreen.title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryBlue
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = WhiteBackground,
                contentColor = PrimaryBlue
            ) {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.1f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppScreen.Dashboard.route) { DashboardScreen(navController) }
            composable(AppScreen.ReadingLog.route) { ReadingLogScreen(navController) }
            composable(AppScreen.Library.route) { BooksLibraryScreen(navController) }
            composable(AppScreen.Profile.route) { ProfileScreen() }
        }
    }
}
