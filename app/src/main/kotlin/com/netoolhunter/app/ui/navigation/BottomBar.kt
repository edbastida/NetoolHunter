package com.netoolhunter.app.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.netoolhunter.app.R
import com.netoolhunter.app.ui.theme.BackgroundDark
import com.netoolhunter.app.ui.theme.KaliBlue
import com.netoolhunter.app.ui.theme.TextSecondary

private data class TabItem(
    val route: String,
    val emoji: String,
    val labelRes: Int
)

private val Tabs = listOf(
    TabItem("home",      "🏠", R.string.tab_home),
    TabItem("tools",     "🧰", R.string.tab_tools),
    TabItem("repos",     "📚", R.string.tab_repos),
    TabItem("terminal",  "⌨️", R.string.tab_terminal),
    TabItem("installed", "✅", R.string.tab_installed)
)

@Composable
fun BottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route?.substringBefore('?')

    NavigationBar(
        containerColor = BackgroundDark,
        tonalElevation = 0.dp
    ) {
        Tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Text(tab.emoji) },
                label = { Text(stringResource(tab.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = KaliBlue,
                    selectedTextColor = KaliBlue,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = BackgroundDark
                )
            )
        }
    }
}
