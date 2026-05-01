package com.netoolhunter.app.shell

import com.netoolhunter.app.domain.InstallEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class ShellExecutor {

    /** Run [command] as root. Emits Started → Stdout/Stderr lines → Exit (or Error). */
    fun exec(command: String): Flow<InstallEvent> = callbackFlow {
        trySend(InstallEvent.Started(command))
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(arrayOf("su", "-c", withRootPath(command)))

            val stdoutThread = Thread {
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    reader.lineSequence().forEach { trySend(InstallEvent.Stdout(it)) }
                }
            }.also { it.start() }

            val stderrThread = Thread {
                BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                    reader.lineSequence().forEach { trySend(InstallEvent.Stderr(it)) }
                }
            }.also { it.start() }

            val code = process.waitFor()
            stdoutThread.join()
            stderrThread.join()
            trySend(InstallEvent.Exit(code))
        } catch (t: Throwable) {
            trySend(InstallEvent.Error(t.message ?: "unknown error"))
        }

        awaitClose {
            try {
                process?.destroy()
            } catch (_: Throwable) {
            }
        }
    }.flowOn(Dispatchers.IO)

    /** Wrap [command] for execution inside the Kali chroot. See [KaliEntryPoint]. */
    fun execInKali(command: String): Flow<InstallEvent> =
        exec(KaliEntryPoint.wrap(command))

    /**
     * Synchronous helper for fire-and-forget checks (e.g. detection probes).
     * Returns the exit code, or -1 if the process couldn't be started.
     */
    suspend fun execBlocking(command: String): Int = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", withRootPath(command)))
            // Drain streams so the process can exit without blocking.
            Thread { process.inputStream.bufferedReader().forEachLine { } }.start()
            Thread { process.errorStream.bufferedReader().forEachLine { } }.start()
            process.waitFor()
        } catch (_: Throwable) {
            -1
        }
    }

    suspend fun execInKaliBlocking(command: String): Int =
        execBlocking(KaliEntryPoint.wrap(command))

    data class CaptureResult(val exit: Int, val stdout: String, val stderr: String)

    /** Like [execBlocking] but captures stdout/stderr. Use only for short probes. */
    suspend fun execCapture(command: String): CaptureResult = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", withRootPath(command)))
            val out = StringBuilder()
            val err = StringBuilder()
            val tOut = Thread { process.inputStream.bufferedReader().forEachLine { out.appendLine(it) } }.also { it.start() }
            val tErr = Thread { process.errorStream.bufferedReader().forEachLine { err.appendLine(it) } }.also { it.start() }
            val code = process.waitFor()
            tOut.join()
            tErr.join()
            CaptureResult(code, out.toString().trim(), err.toString().trim())
        } catch (t: Throwable) {
            CaptureResult(-1, "", t.message ?: "exec error")
        }
    }

    private fun withRootPath(command: String): String =
        "export PATH=$ROOT_PATH:\$PATH; $command"

    companion object {
        private const val ROOT_PATH =
            "/sbin:/system/sbin:/system/bin:/system/xbin:/vendor/bin:/vendor/xbin:" +
                "/product/bin:/apex/com.android.runtime/bin:/data/adb/magisk:" +
                "/data/adb/ksu/bin:/data/adb/ap/bin:/su/bin:/magisk/.core/bin"
    }
}
