package com.breathe.calm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing box breathing exercise logic
 * Box breathing: 4 seconds in, 4 seconds hold, 4 seconds out, 4 seconds hold
 */
class BreathingViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(BreathingState())
    val state: StateFlow<BreathingState> = _state.asStateFlow()
    
    private var breathingJob: Job? = null
    
    companion object {
        private const val UPDATE_INTERVAL_MS = 16L // ~60 FPS for smooth animation
        private const val MIN_DURATION_SECONDS = 3
        private const val MAX_DURATION_SECONDS = 5
        private const val MIN_SESSION_LENGTH = 10
        private const val MAX_SESSION_LENGTH = 300 // 5 minutes max
        private const val SESSION_LENGTH_INCREMENT = 10
    }
    
    /**
     * Start the breathing exercise
     */
    fun startBreathing() {
        if (_state.value.isActive) return
        
        _state.update { it.copy(isActive = true, currentPhase = BreathingPhase.INHALE, totalElapsedSeconds = 0) }
        
        breathingJob = viewModelScope.launch {
            var elapsedTime = 0L
            var currentPhase = BreathingPhase.INHALE
            var cycleCount = 0
            val sessionLengthMs = _state.value.sessionLengthSeconds * 1000L
            
            while (_state.value.isActive && elapsedTime < sessionLengthMs) {
                val phaseDurationMs = _state.value.phaseDurationSeconds * 1000L
                val phaseProgress = (elapsedTime % phaseDurationMs).toFloat() / phaseDurationMs
                val currentSecond = (elapsedTime % phaseDurationMs / 1000).toInt()
                val totalElapsedSeconds = (elapsedTime / 1000).toInt()
                
                _state.update { 
                    it.copy(
                        currentPhase = currentPhase,
                        progress = phaseProgress,
                        cycleCount = cycleCount,
                        currentSecond = currentSecond,
                        totalElapsedSeconds = totalElapsedSeconds
                    )
                }
                
                delay(UPDATE_INTERVAL_MS)
                elapsedTime += UPDATE_INTERVAL_MS
                
                // Transition to next phase
                if (elapsedTime % phaseDurationMs < UPDATE_INTERVAL_MS) {
                    currentPhase = when (currentPhase) {
                        BreathingPhase.INHALE -> BreathingPhase.HOLD_IN
                        BreathingPhase.HOLD_IN -> BreathingPhase.EXHALE
                        BreathingPhase.EXHALE -> BreathingPhase.HOLD_OUT
                        BreathingPhase.HOLD_OUT -> {
                            cycleCount++
                            BreathingPhase.INHALE
                        }
                        BreathingPhase.IDLE -> BreathingPhase.INHALE
                    }
                }
            }
            
            // Session completed
            if (elapsedTime >= sessionLengthMs) {
                stopBreathing()
            }
        }
    }
    
    /**
     * Stop the breathing exercise
     */
    fun stopBreathing() {
        breathingJob?.cancel()
        breathingJob = null
        _state.update { it.copy(isActive = false) }
    }
    
    /**
     * Reset the breathing exercise to initial state
     */
    fun resetBreathing() {
        stopBreathing()
        _state.update { 
            BreathingState(
                phaseDurationSeconds = it.phaseDurationSeconds,
                sessionLengthSeconds = it.sessionLengthSeconds
            ) 
        }
    }
    
    /**
     * Increase phase duration by 1 second
     */
    fun increaseDuration() {
        if (!_state.value.isActive && _state.value.phaseDurationSeconds < MAX_DURATION_SECONDS) {
            _state.update { it.copy(phaseDurationSeconds = it.phaseDurationSeconds + 1) }
        }
    }
    
    /**
     * Decrease phase duration by 1 second
     */
    fun decreaseDuration() {
        if (!_state.value.isActive && _state.value.phaseDurationSeconds > MIN_DURATION_SECONDS) {
            _state.update { it.copy(phaseDurationSeconds = it.phaseDurationSeconds - 1) }
        }
    }
    
    /**
     * Increase session length by 10 seconds
     */
    fun increaseSessionLength() {
        if (!_state.value.isActive && _state.value.sessionLengthSeconds < MAX_SESSION_LENGTH) {
            _state.update { it.copy(sessionLengthSeconds = it.sessionLengthSeconds + SESSION_LENGTH_INCREMENT) }
        }
    }
    
    /**
     * Decrease session length by 10 seconds
     */
    fun decreaseSessionLength() {
        if (!_state.value.isActive && _state.value.sessionLengthSeconds > MIN_SESSION_LENGTH) {
            _state.update { it.copy(sessionLengthSeconds = it.sessionLengthSeconds - SESSION_LENGTH_INCREMENT) }
        }
    }
    
    /**
     * Get the instruction text for the current phase
     */
    fun getPhaseInstruction(phase: BreathingPhase): String {
        return when (phase) {
            BreathingPhase.IDLE -> "Ready to begin"
            BreathingPhase.INHALE -> "Breathe In"
            BreathingPhase.HOLD_IN -> "Hold"
            BreathingPhase.EXHALE -> "Breathe Out"
            BreathingPhase.HOLD_OUT -> "Hold"
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopBreathing()
    }
}
