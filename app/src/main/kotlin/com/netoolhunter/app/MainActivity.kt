package com.netoolhunter.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.netoolhunter.app.ui.navigation.BottomBar
import com.netoolhunter.app.ui.navigation.NetoolHunterNavHost
import com.netoolhunter.app.ui.screens.prerequisites.PrerequisitesScreen
import com.netoolhunter.app.ui.theme.NetoolHunterTheme
import com.netoolhunter.app.util.DataStoreKeys
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetoolHunterTheme { Root() }
        }
    }
}

@Composable
private fun Root() {
    val app = LocalContext.current.applicationContext as NetoolHunterApp
    var prereqsPassed by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        val saved = app.dataStore.data.first()[DataStoreKeys.PREREQS_COMPLETED] ?: false
        // Saved=true alone isn't enough — re-verify root at every cold start.
        prereqsPassed = saved && app.rootChecker.isRootAvailable()
    }

    when (prereqsPassed) {
        null -> Unit
        false -> PrerequisitesScreen(onCompleted = { prereqsPassed = true })
        true -> MainScaffold()
    }
}

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            NetoolHunterNavHost(navController = navController)
        }
    }
}
