package com.example.zillahairballmonitor.data

import java.util.Date

/**
 * Data class representing all metrics from the Zilla Hairball 2 interface
 */
data class ZillaMetrics(
    val timestamp: Date = Date(),
    val batteryVoltage: Float = 0f,
    val batteryCurrent: Float = 0f,
    val motorVoltage: Float = 0f,
    val motorCurrent: Float = 0f,
    val motorSpeed: Int = 0,
    val controllerStatus: ControllerStatus = ControllerStatus.UNKNOWN,
    val acceleratorPosition: Float = 0f, // 0.0 to 1.0
    val prechargeStatus: Boolean = false,
    val reverseMode: Boolean = false,
    val valetMode: Boolean = false,
    val errorCodes: List<ErrorCode> = emptyList(),
    val batteryLow: Boolean = false,
    val mainContactorDrop: Float = 0f,
) {
    /**
     * Calculate power output in kilowatts
     */
    val powerOutput: Float
        get() = motorVoltage * motorCurrent / 1000

    /**
     * Calculate motor efficiency (if available)
     */
    val motorEfficiency: Float?
        get() {
            if (batteryCurrent <= 0 || motorCurrent <= 0) return null
            return (motorVoltage * motorCurrent) / (batteryVoltage * batteryCurrent) * 100
        }
}

/**
 * Enum representing the controller status
 */
enum class ControllerStatus {
    STANDBY,
    PRECHARGE,
    RUNNING,
    FAULT,
    UNKNOWN;
    
    override fun toString(): String {
        return when (this) {
            STANDBY -> "Standby"
            PRECHARGE -> "Precharge"
            RUNNING -> "Running"
            FAULT -> "Fault"
            UNKNOWN -> "Unknown"
        }
    }
}

/**
 * Enum representing error codes from the controller
 */
enum class ErrorCode(val code: Int, val description: String) {
    NONE(0, "No Error"),
    OVERCURRENT(1, "Overcurrent Fault"),
    OVERVOLTAGE(2, "Overvoltage Fault"),
    UNDERVOLTAGE(3, "Undervoltage Fault"),
    OVERTEMP(4, "Overtemperature Fault"),
    STALL(5, "Stalled Rotor"),
    PRECHARGE_FAILURE(6, "Precharge Failure"),
    CONTACTOR_FAILURE(7, "Contactor Failure"),
    HALL_THROTTLE_FAILURE(8, "Hall Throttle Failure"),
    WATCHDOG_RESET(9, "Watchdog Reset"),
    UNKNOWN_ERROR(99, "Unknown Error");
    
    companion object {
        fun fromCode(code: Int): ErrorCode {
            return values().find { it.code == code } ?: UNKNOWN_ERROR
        }
    }
}
