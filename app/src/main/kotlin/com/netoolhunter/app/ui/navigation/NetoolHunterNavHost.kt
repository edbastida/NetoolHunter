package com.netoolhunter.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.netoolhunter.app.ui.screens.home.HomeScreen
import com.netoolhunter.app.ui.screens.installed.InstalledScreen
import com.netoolhunter.app.ui.screens.repos.ReposScreen
import com.netoolhunter.app.ui.screens.terminal.TerminalScreen
import com.netoolhunter.app.ui.screens.tools.ToolsScreen

@Composable
fun NetoolHunterNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onCategoryClick = { categoryId ->
                    navController.navigate(Route.Tools.build(categoryId))
                }
            )
        }
        composable(
            route = Route.Tools.path,
            arguments = listOf(
                navArgument(Route.Tools.ARG_CAT) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val categoryArg = entry.arguments?.getString(Route.Tools.ARG_CAT)
            ToolsScreen(
                initialCategoryId = categoryArg,
                onNavigateToTerminal = { navController.navigate(Route.Terminal.path) }
            )
        }
        composable(Route.Repos.path) {
            ReposScreen(
                onNavigateToTerminal = { navController.navigate(Route.Terminal.path) }
            )
        }
        composable(Route.Terminal.path) {
            TerminalScreen()
        }
        composable(Route.Installed.path) {
            InstalledScreen(
                onNavigateToTerminal = { navController.navigate(Route.Terminal.path) }
            )
        }
    }
}
