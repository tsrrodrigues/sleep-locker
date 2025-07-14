package com.tsrrodrigues.sleeplocker.domain.lock

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.media.AudioManager
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.tsrrodrigues.sleeplocker.domain.model.SleepState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockController @Inject constructor(
    private val context: Context
) {

    private val _lastLockTime = MutableStateFlow<Long?>(null)
    val lastLockTime: Flow<Long?> = _lastLockTime.asStateFlow()

    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun lockScreen() {
        try {
            // Pausar mídia primeiro
            pauseMedia()

            // Bloquear tela
            devicePolicyManager.lockNow()

            // Vibrar (opcional)
            vibrate()

            // Registrar tempo do bloqueio
            _lastLockTime.value = System.currentTimeMillis()

        } catch (e: Exception) {
            // Log do erro
            e.printStackTrace()
        }
    }

    private fun pauseMedia() {
        try {
            // Tentar pausar via MediaSession primeiro
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val activeSessions = mediaSessionManager.getActiveSessions(null)
                for (session in activeSessions) {
                    session.controller.transportControls.pause()
                }
            }
        } catch (e: Exception) {
            // Fallback: usar AudioManager
            try {
                audioManager.dispatchMediaKeyEvent(
                    android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_DOWN,
                        android.view.KeyEvent.KEYCODE_MEDIA_PAUSE
                    )
                )
                audioManager.dispatchMediaKeyEvent(
                    android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_UP,
                        android.view.KeyEvent.KEYCODE_MEDIA_PAUSE
                    )
                )
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shouldLock(sleepState: SleepState): Boolean {
        return sleepState == SleepState.ASLEEP
    }
}