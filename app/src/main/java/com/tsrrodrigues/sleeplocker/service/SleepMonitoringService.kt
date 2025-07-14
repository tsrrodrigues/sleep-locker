package com.tsrrodrigues.sleeplocker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tsrrodrigues.sleeplocker.R
import com.tsrrodrigues.sleeplocker.domain.lock.LockController
import com.tsrrodrigues.sleeplocker.domain.model.SleepState
import com.tsrrodrigues.sleeplocker.domain.sleep.SleepDetector
import com.tsrrodrigues.sleeplocker.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SleepMonitoringService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "sleep_monitoring"
        private const val LOCK_DELAY_MINUTES = 2L // Delay configurável

        fun startService(context: Context) {
            val intent = Intent(context, SleepMonitoringService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SleepMonitoringService::class.java)
            context.stopService(intent)
        }
    }

    @Inject
    lateinit var sleepDetector: SleepDetector

    @Inject
    lateinit var lockController: LockController

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var monitoringJob: Job? = null
    private var lockJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification(SleepState.AWAKE))
        startMonitoring()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        monitoringJob?.cancel()
        lockJob?.cancel()
    }

    private fun startMonitoring() {
        monitoringJob = serviceScope.launch {
            sleepDetector.sleepState.collectLatest { sleepState ->
                updateNotification(sleepState)

                if (lockController.shouldLock(sleepState)) {
                    scheduleLock()
                } else {
                    cancelLock()
                }
            }
        }
    }

    private fun scheduleLock() {
        lockJob?.cancel()
        lockJob = serviceScope.launch {
            delay(LOCK_DELAY_MINUTES * 60 * 1000) // Converter minutos para milissegundos
            lockController.lockScreen()
        }
    }

    private fun cancelLock() {
        lockJob?.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.service_notification_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitoramento de sono"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(sleepState: SleepState): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, SleepMonitoringService::class.java).apply {
            action = "PAUSE_MONITORING"
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 1, pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_monitoring, getSleepStateString(sleepState)))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_pause,
                getString(R.string.service_notification_pause),
                pausePendingIntent
            )
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(sleepState: SleepState) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(sleepState))
    }

    private fun getSleepStateString(sleepState: SleepState): String {
        return when (sleepState) {
            SleepState.AWAKE -> getString(R.string.sleep_state_awake)
            SleepState.LIGHT_SLEEP -> getString(R.string.sleep_state_light_sleep)
            SleepState.ASLEEP -> getString(R.string.sleep_state_asleep)
        }
    }
}