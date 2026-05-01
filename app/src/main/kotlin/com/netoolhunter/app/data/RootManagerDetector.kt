package com.netoolhunter.app.data

import android.content.Context
import android.content.pm.PackageManager
import com.netoolhunter.app.domain.RootManager

class RootManagerDetector(private val context: Context) {

    fun detect(): RootManager {
        val pm = context.packageManager
        val candidates = listOf(
            "com.rifsxd.ksunext" to RootManager.KernelSUNext,
            "me.weishu.kernelsu" to RootManager.KernelSU,
            "me.bmax.apatch" to RootManager.APatch,
            "com.topjohnwu.magisk" to RootManager.MagiskOfficial,
            "io.github.huskydg.magisk" to RootManager.MagiskOfficial
        )
        for ((pkg, mgr) in candidates) {
            if (isInstalled(pm, pkg)) return mgr
        }
        return RootManager.Unknown
    }

    private fun isInstalled(pm: PackageManager, packageName: String): Boolean = try {
        pm.getPackageInfo(packageName, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }
}
