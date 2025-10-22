package com.breathe.calm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.breathe.calm.ui.theme.BreatheTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            BreatheTheme {
                var showWelcome by remember { mutableStateOf(true) }
                
                if (showWelcome) {
                    WelcomeScreen(
                        onContinue = { showWelcome = false },
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    )
                } else {
                    val viewModel: BreathingViewModel = viewModel()
                    BreathingScreen(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    )
                }
            }
        }
    }
}
