package com.tsrrodrigues.sleeplocker.domain.model

data class SensorData(
    val heartRate: Int,
    val accelerationX: Float,
    val accelerationY: Float,
    val accelerationZ: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    val accelerationRMS: Float
        get() = kotlin.math.sqrt(accelerationX * accelerationX + accelerationY * accelerationY + accelerationZ * accelerationZ)
}