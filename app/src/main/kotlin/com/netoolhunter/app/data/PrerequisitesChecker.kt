package com.netoolhunter.app.data

import android.util.Log
import com.netoolhunter.app.shell.KaliEntryPoint
import com.netoolhunter.app.shell.RootChecker
import com.netoolhunter.app.shell.ShellExecutor

data class PrerequisiteStatus(
    val rootAvailable: Boolean,
    val kaliEntrypoint: Boolean,
    val chrootPresent: Boolean,
    val basePackages: Boolean
) {
    val allOk: Boolean = rootAvailable && kaliEntrypoint && chrootPresent && basePackages
}

class PrerequisitesChecker(
    private val shell: ShellExecutor,
    private val rootChecker: RootChecker
) {
    suspend fun check(): PrerequisiteStatus {
        val root = rootChecker.isRootAvailable()
        if (!root) {
            return PrerequisiteStatus(
                rootAvailable = false,
                kaliEntrypoint = false,
                chrootPresent = false,
                basePackages = false
            )
        }

        val chroot = shell.execBlocking("test -d $CHROOT_PATH") == 0
        Log.i(TAG, "chroot dir present=$chroot path=$CHROOT_PATH")

        // Probe the actual wrapper end-to-end (nh -r 'echo ok' or bootkali_bash -c 'echo ok').
        // This validates that *some* entrypoint resolves AND the chroot responds.
        val probe = if (chroot) shell.execCapture(KaliEntryPoint.probeCommand)
                    else ShellExecutor.CaptureResult(-1, "", "skipped: chroot dir missing")
        val entrypoint = probe.exit == 0 && probe.stdout.trim() == "ok"
        Log.i(TAG, "kali entrypoint ok=$entrypoint (chroot=$chroot) exit=${probe.exit} stdout=[${probe.stdout}] stderr=[${probe.stderr}]")

        val pkgs = entrypoint && shell.execInKaliBlocking(
            "command -v git >/dev/null 2>&1 && command -v go >/dev/null 2>&1 && " +
                "command -v pip >/dev/null 2>&1 && command -v pipx >/dev/null 2>&1 && " +
                "command -v docker >/dev/null 2>&1"
        ) == 0
        Log.i(TAG, "base packages ok=$pkgs (entrypoint=$entrypoint)")

        return PrerequisiteStatus(
            rootAvailable = root,
            kaliEntrypoint = entrypoint,
            chrootPresent = chroot,
            basePackages = pkgs
        )
    }

    fun installBasePackagesCommand(): String =
        "apt-get update && apt-get install -y git golang python3-pip pipx docker.io && pipx ensurepath"

    companion object {
        const val CHROOT_PATH = "/data/local/nhsystem/kali-arm64"
        private const val TAG = "NetoolHunter"
    }
}
