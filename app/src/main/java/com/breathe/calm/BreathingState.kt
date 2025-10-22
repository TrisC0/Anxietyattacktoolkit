package com.breathe.calm

/**
 * Represents the current phase of box breathing
 */
enum class BreathingPhase {
    IDLE,
    INHALE,    // Breathe in for 4 seconds
    HOLD_IN,   // Hold for 4 seconds after inhaling
    EXHALE,    // Breathe out for 4 seconds
    HOLD_OUT   // Hold for 4 seconds after exhaling
}

/**
 * State data for the breathing exercise
 */
data class BreathingState(
    val isActive: Boolean = false,
    val currentPhase: BreathingPhase = BreathingPhase.IDLE,
    val progress: Float = 0f,  // 0.0 to 1.0 for current phase
    val cycleCount: Int = 0,
    val currentSecond: Int = 0, // Current second in the phase (0-3)
    val phaseDurationSeconds: Int = 4, // Duration of each phase in seconds (adjustable)
    val sessionLengthSeconds: Int = 30, // Total session length in seconds (adjustable)
    val totalElapsedSeconds: Int = 0 // Total elapsed time in current session
)
