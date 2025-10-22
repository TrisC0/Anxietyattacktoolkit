package com.breathe.calm

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.breathe.calm.ui.theme.*

@Composable
fun BreathingScreen(
    viewModel: BreathingViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Haptic feedback on phase change
    LaunchedEffect(state.currentPhase) {
        if (state.isActive && state.currentPhase != BreathingPhase.IDLE) {
            provideHapticFeedback(context)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            HeaderSection()
            
            // Main breathing visualization with duration controls
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Up arrow to increase duration
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TriangleButton(
                        pointingUp = true,
                        enabled = !state.isActive && state.phaseDurationSeconds < 5,
                        onClick = { viewModel.increaseDuration() },
                        contentDescription = "Increase duration"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+1",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (!state.isActive && state.phaseDurationSeconds < 5)
                            MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Breathing visualization
                BreathingVisualization(
                    state = state,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Down arrow to decrease duration
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TriangleButton(
                        pointingUp = false,
                        enabled = !state.isActive && state.phaseDurationSeconds > 3,
                        onClick = { viewModel.decreaseDuration() },
                        contentDescription = "Decrease duration"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "-1",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (!state.isActive && state.phaseDurationSeconds > 3)
                            MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Phase instruction and session length
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                PhaseInstructionText(
                    phase = state.currentPhase
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Session length display and controls
                SessionLengthControl(
                    sessionLengthSeconds = state.sessionLengthSeconds,
                    totalElapsedSeconds = state.totalElapsedSeconds,
                    isActive = state.isActive,
                    onIncrease = { viewModel.increaseSessionLength() },
                    onDecrease = { viewModel.decreaseSessionLength() }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Control buttons
            ControlButtons(
                isActive = state.isActive,
                cycleCount = state.cycleCount,
                onStartStop = {
                    if (state.isActive) {
                        viewModel.stopBreathing()
                    } else {
                        viewModel.startBreathing()
                    }
                },
                onReset = { viewModel.resetBreathing() }
            )
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.box_breathing),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.find_your_calm),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BreathingVisualization(
    state: BreathingState,
    modifier: Modifier = Modifier
) {
    val breathingColors = getBreathingColors(state.currentPhase)
    
    // Animate circle scale based on breathing phase
    val targetScale = when (state.currentPhase) {
        BreathingPhase.INHALE -> 1.0f
        BreathingPhase.HOLD_IN -> 1.0f
        BreathingPhase.EXHALE -> 0.5f
        BreathingPhase.HOLD_OUT -> 0.5f
        BreathingPhase.IDLE -> 0.7f
    }
    
    val animatedScale by animateFloatAsState(
        targetValue = if (state.isActive) targetScale else 0.7f,
        animationSpec = tween(
            durationMillis = if (state.currentPhase == BreathingPhase.HOLD_IN || 
                               state.currentPhase == BreathingPhase.HOLD_OUT) 0 
                            else state.phaseDurationSeconds * 1000,
            easing = LinearEasing
        ),
        label = "breathingScale"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Box(
            modifier = Modifier
                .fillMaxSize(0.75f)
                .scale(animatedScale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            breathingColors.first.copy(alpha = 0.3f),
                            breathingColors.second.copy(alpha = 0.1f)
                        )
                    )
                )
                .semantics {
                    contentDescription = "Breathing visualization circle"
                }
        )
        
        // Progress ring
        ProgressRing(
            progress = state.progress,
            color = breathingColors.first,
            modifier = Modifier.fillMaxSize(0.8f)
        )
        
        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display countdown or duration in center of circle
            if (state.currentPhase != BreathingPhase.IDLE) {
                val secondsRemaining = state.phaseDurationSeconds - state.currentSecond
                Text(
                    text = "$secondsRemaining",
                    style = MaterialTheme.typography.displayLarge,
                    color = breathingColors.first,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "${state.phaseDurationSeconds}",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "seconds",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (state.cycleCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cycle ${state.cycleCount}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProgressRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 8.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        
        // Background ring
        drawCircle(
            color = Border,
            radius = radius,
            style = Stroke(width = strokeWidth)
        )
        
        // Progress ring
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth),
            topLeft = Offset(
                (size.width - radius * 2) / 2,
                (size.height - radius * 2) / 2
            ),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
    }
}

@Composable
private fun TriangleButton(
    pointingUp: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    contentDescription: String
) {
    val color = if (enabled) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(32.dp)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            
            if (pointingUp) {
                // Hollow chevron pointing up - draw two lines forming a ^
                // Left line (bottom-left to top-center)
                drawLine(
                    color = color,
                    start = Offset(0f, size.height),
                    end = Offset(size.width / 2f, 0f),
                    strokeWidth = strokeWidth
                )
                // Right line (top-center to bottom-right)
                drawLine(
                    color = color,
                    start = Offset(size.width / 2f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            } else {
                // Hollow chevron pointing down - draw two lines forming a v
                // Left line (top-left to bottom-center)
                drawLine(
                    color = color,
                    start = Offset(0f, 0f),
                    end = Offset(size.width / 2f, size.height),
                    strokeWidth = strokeWidth
                )
                // Right line (bottom-center to top-right)
                drawLine(
                    color = color,
                    start = Offset(size.width / 2f, size.height),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

@Composable
private fun PhaseInstructionText(
    phase: BreathingPhase
) {
    val instruction = when (phase) {
        BreathingPhase.IDLE -> "Ready to begin"
        BreathingPhase.INHALE -> "Breathe In"
        BreathingPhase.HOLD_IN -> "Hold"
        BreathingPhase.EXHALE -> "Breathe Out"
        BreathingPhase.HOLD_OUT -> "Hold"
    }
    
    Text(
        text = instruction,
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun SessionLengthControl(
    sessionLengthSeconds: Int,
    totalElapsedSeconds: Int,
    isActive: Boolean,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        // Decrease button
        IconButton(
            onClick = onDecrease,
            enabled = !isActive && sessionLengthSeconds > 10,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease session length",
                tint = if (!isActive && sessionLengthSeconds > 10)
                    MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Session length display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isActive) {
                Text(
                    text = "${sessionLengthSeconds - totalElapsedSeconds}s remaining",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "Session: ${sessionLengthSeconds}s",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Increase button
        IconButton(
            onClick = onIncrease,
            enabled = !isActive && sessionLengthSeconds < 300,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase session length",
                tint = if (!isActive && sessionLengthSeconds < 300)
                    MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ControlButtons(
    isActive: Boolean,
    cycleCount: Int,
    onStartStop: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Start/Stop button
        Button(
            onClick = onStartStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isActive) Error else Secondary,
                contentColor = if (isActive) OnPrimary else OnSecondary
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isActive) stringResource(R.string.stop_breathing) 
                       else stringResource(R.string.start_breathing),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Reset button (only show if there are cycles)
        if (cycleCount > 0 && !isActive) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Border, Border)
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.reset_breathing),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun getBreathingColors(phase: BreathingPhase): Pair<Color, Color> {
    return when (phase) {
        BreathingPhase.INHALE -> BreathInStart to BreathInEnd
        BreathingPhase.HOLD_IN -> HoldColor to HoldColor
        BreathingPhase.EXHALE -> BreathOutStart to BreathOutEnd
        BreathingPhase.HOLD_OUT -> HoldColor to HoldColor
        BreathingPhase.IDLE -> Secondary to Secondary
    }
}

private fun provideHapticFeedback(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    vibrator?.let {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            it.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            it.vibrate(50)
        }
    }
}
