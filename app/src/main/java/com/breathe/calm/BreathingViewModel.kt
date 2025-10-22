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
        private const val PHASE_DURATION_MS = 4000L // 4 seconds per phase
        private const val UPDATE_INTERVAL_MS = 16L // ~60 FPS for smooth animation
    }
    
    /**
     * Start the breathing exercise
     */
    fun startBreathing() {
        if (_state.value.isActive) return
        
        _state.update { it.copy(isActive = true, currentPhase = BreathingPhase.INHALE) }
        
        breathingJob = viewModelScope.launch {
            var elapsedTime = 0L
            var currentPhase = BreathingPhase.INHALE
            var cycleCount = 0
            
            while (_state.value.isActive) {
                val phaseProgress = (elapsedTime % PHASE_DURATION_MS).toFloat() / PHASE_DURATION_MS
                val currentSecond = (elapsedTime % PHASE_DURATION_MS / 1000).toInt()
                
                _state.update { 
                    it.copy(
                        currentPhase = currentPhase,
                        progress = phaseProgress,
                        cycleCount = cycleCount,
                        currentSecond = currentSecond
                    )
                }
                
                delay(UPDATE_INTERVAL_MS)
                elapsedTime += UPDATE_INTERVAL_MS
                
                // Transition to next phase
                if (elapsedTime % PHASE_DURATION_MS < UPDATE_INTERVAL_MS) {
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
        _state.value = BreathingState()
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
