package com.lazybuzz.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lazybuzz.features.dashboard.DashboardScreen
import com.lazybuzz.features.onboarding.SplashScreen
import com.lazybuzz.ui.components.PlaceholderScreen

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    NavHost(nav, startDestination = Dest.Splash.route) {
        composable(Dest.Splash.route) {
            SplashScreen(onDone = {
                nav.navigate(Dest.Dashboard.route) { popUpTo(Dest.Splash.route) { inclusive = true } }
            })
        }
        composable(Dest.Dashboard.route) { DashboardScreen(onOpen = { nav.navigate(it.route) }) }
        Dest.entries.filter { it.milestone.isNotEmpty() }.forEach { d ->
            composable(d.route) { PlaceholderScreen(d.title, d.blurb, d.milestone, onBack = { nav.popBackStack() }) }
        }
    }
}
