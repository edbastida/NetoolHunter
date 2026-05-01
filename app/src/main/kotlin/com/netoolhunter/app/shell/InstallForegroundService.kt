package com.netoolhunter.app.shell

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.netoolhunter.app.NetoolHunterApp
import com.netoolhunter.app.R
import com.netoolhunter.app.domain.InstallEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Foreground service that runs a shell command and pumps every event into [TerminalBus].
 * Required for installs that survive lock-screen — without it, Android may kill the
 * process mid `apt install metasploit-framework` and leave the chroot in a half-broken state.
 */
class InstallForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val command = intent?.getStringExtra(EXTRA_COMMAND) ?: run {
            stopSelf()
            return START_NOT_STICKY
        }
        val toolName = intent.getStringExtra(EXTRA_TOOL_NAME) ?: getString(R.string.app_name)
        val inKali = intent.getBooleanExtra(EXTRA_IN_KALI, true)

        startForegroundCompat(toolName)

        currentJob?.cancel()
        currentJob = scope.launch {
            val shell = (application as NetoolHunterApp).shell
            val flow = if (inKali) shell.execInKali(command) else shell.exec(command)
            flow.collect { event ->
                TerminalBus.emit(event)
                if (event is InstallEvent.Exit || event is InstallEvent.Error) {
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun startForegroundCompat(toolName: String) {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_install_title, toolName))
            .setContentText(getString(R.string.notif_install_body))
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    companion object {
        const val EXTRA_COMMAND = "extra_command"
        const val EXTRA_TOOL_NAME = "extra_tool_name"
        const val EXTRA_IN_KALI = "extra_in_kali"
        private const val CHANNEL_ID = "install_channel"
        private const val NOTIF_ID = 0xC0DE

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.notif_channel_install),
                        NotificationManager.IMPORTANCE_LOW
                    )
                    mgr.createNotificationChannel(channel)
                }
            }
        }

        fun start(context: Context, command: String, toolName: String, inKali: Boolean = true) {
            val intent = Intent(context, InstallForegroundService::class.java).apply {
                putExtra(EXTRA_COMMAND, command)
                putExtra(EXTRA_TOOL_NAME, toolName)
                putExtra(EXTRA_IN_KALI, inKali)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
