package com.tsrrodrigues.sleeplocker.domain.sleep

import com.tsrrodrigues.sleeplocker.domain.model.SensorData
import com.tsrrodrigues.sleeplocker.domain.model.SleepState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepDetector @Inject constructor() {

    private val _sleepState = MutableStateFlow(SleepState.AWAKE)
    val sleepState: Flow<SleepState> = _sleepState.asStateFlow()

    private val sensorDataBuffer = mutableListOf<SensorData>()
    private val bufferSize = 300 // 5 minutos com dados a cada segundo

    fun processSensorData(data: SensorData) {
        sensorDataBuffer.add(data)

        // Manter apenas os últimos 5 minutos de dados
        if (sensorDataBuffer.size > bufferSize) {
            sensorDataBuffer.removeAt(0)
        }

        // Só analisar se temos dados suficientes
        if (sensorDataBuffer.size >= 60) { // Pelo menos 1 minuto de dados
            val newState = analyzeSleepState()
            if (newState != _sleepState.value) {
                _sleepState.value = newState
            }
        }
    }

    private fun analyzeSleepState(): SleepState {
        val recentData = sensorDataBuffer.takeLast(60) // Último minuto

        val avgHeartRate = recentData.map { it.heartRate }.average()
        val heartRateVariation = recentData.map { it.heartRate }.let { rates ->
            if (rates.size > 1) rates.maxOrNull()!! - rates.minOrNull()!! else 0
        }
        val avgAcceleration = recentData.map { it.accelerationRMS }.average()

        // Algoritmo Cole-Kripke simplificado
        return when {
            // Critérios para sono profundo
            avgHeartRate < 60 &&
            heartRateVariation < 5 &&
            avgAcceleration < 0.05 -> SleepState.ASLEEP

            // Critérios para sono leve
            avgHeartRate < 70 &&
            heartRateVariation < 10 &&
            avgAcceleration < 0.1 -> SleepState.LIGHT_SLEEP

            // Caso contrário, acordado
            else -> SleepState.AWAKE
        }
    }

    fun reset() {
        sensorDataBuffer.clear()
        _sleepState.value = SleepState.AWAKE
    }
}