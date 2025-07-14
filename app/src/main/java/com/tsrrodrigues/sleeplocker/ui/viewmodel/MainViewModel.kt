package com.tsrrodrigues.sleeplocker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsrrodrigues.sleeplocker.domain.lock.LockController
import com.tsrrodrigues.sleeplocker.domain.model.SleepState
import com.tsrrodrigues.sleeplocker.domain.sleep.SleepDetector
import com.tsrrodrigues.sleeplocker.service.SleepMonitoringService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sleepDetector: SleepDetector,
    private val lockController: LockController
) : ViewModel() {

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _sleepState = MutableStateFlow(SleepState.AWAKE)
    val sleepState: StateFlow<SleepState> = _sleepState.asStateFlow()

    private val _lastLockTime = MutableStateFlow<Long?>(null)
    val lastLockTime: StateFlow<Long?> = _lastLockTime.asStateFlow()

    private val _isProUser = MutableStateFlow(false)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    init {
        viewModelScope.launch {
            sleepDetector.sleepState.collect { state ->
                _sleepState.value = state
            }
        }

        viewModelScope.launch {
            lockController.lastLockTime.collect { time ->
                _lastLockTime.value = time
            }
        }
    }

    fun toggleMonitoring() {
        _isMonitoring.value = !_isMonitoring.value

        if (_isMonitoring.value) {
            startMonitoring()
        } else {
            stopMonitoring()
        }
    }

    private fun startMonitoring() {
        // O serviço será iniciado pela UI
    }

    private fun stopMonitoring() {
        // O serviço será parado pela UI
    }

    fun testLock() {
        lockController.lockScreen()
    }

    fun formatLastLockTime(): String {
        return _lastLockTime.value?.let { time ->
            val date = Date(time)
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(date)
        } ?: "Nunca"
    }

    fun getSleepStateString(): String {
        return when (_sleepState.value) {
            SleepState.AWAKE -> "ACORDO"
            SleepState.LIGHT_SLEEP -> "SONO LEVE"
            SleepState.ASLEEP -> "DORMINDO"
        }
    }
}