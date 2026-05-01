package com.netoolhunter.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.netoolhunter.app.data.CatalogRepository
import com.netoolhunter.app.data.InstalledRepository
import com.netoolhunter.app.data.PrerequisitesChecker
import com.netoolhunter.app.data.ReposRepository
import com.netoolhunter.app.data.RootManagerDetector
import com.netoolhunter.app.shell.InstallForegroundService
import com.netoolhunter.app.shell.RootChecker
import com.netoolhunter.app.shell.ShellExecutor
import com.netoolhunter.app.util.DataStoreKeys

private val Application.netoolDataStore: DataStore<Preferences> by preferencesDataStore(
    name = DataStoreKeys.NAME
)

class NetoolHunterApp : Application() {

    // Manual constructor injection — no Hilt/Koin.
    val shell: ShellExecutor by lazy { ShellExecutor() }
    val rootChecker: RootChecker by lazy { RootChecker() }
    val dataStore: DataStore<Preferences> by lazy { netoolDataStore }
    val repos: ReposRepository by lazy { ReposRepository(dataStore, shell) }
    val catalog: CatalogRepository by lazy { CatalogRepository(this) }
    val installed: InstalledRepository by lazy { InstalledRepository(shell, catalog) }
    val prereqs: PrerequisitesChecker by lazy { PrerequisitesChecker(shell, rootChecker) }
    val rootManagerDetector: RootManagerDetector by lazy { RootManagerDetector(this) }

    override fun onCreate() {
        super.onCreate()
        InstallForegroundService.ensureChannel(this)
    }
}
