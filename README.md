# Breathe - Box Breathing App

A polished Android application for guided box breathing exercises to help reduce stress and anxiety.

## Features

- **Box Breathing Technique**: 4-4-4-4 pattern (Inhale 4s → Hold 4s → Exhale 4s → Hold 4s)
- **Material Design 3**: Modern, clean UI with beautiful animations
- **Visual Feedback**: Animated breathing circle with smooth transitions
- **Haptic Feedback**: Gentle vibrations at each phase transition
- **Progress Tracking**: Cycle counter to track your practice
- **Accessibility**: Full support for screen readers and keyboard navigation
- **Dark Theme**: Easy on the eyes with a calming color palette

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with ViewModel and StateFlow
- **Material Design**: Material 3 components
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## Project Structure

```
app/
├── src/main/
│   ├── java/com/breathe/calm/
│   │   ├── BreathingState.kt       # State data classes
│   │   ├── BreathingViewModel.kt   # Business logic
│   │   ├── BreathingScreen.kt      # UI components
│   │   ├── MainActivity.kt         # App entry point
│   │   └── ui/theme/               # Material theme
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   └── res/
│       ├── values/
│       │   ├── strings.xml
│       │   └── themes.xml
│       └── xml/
│           ├── backup_rules.xml
│           └── data_extraction_rules.xml
├── build.gradle.kts
└── proguard-rules.pro
```

## Building the App

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Click "Run" or press `Shift + F10`

## Gradle Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Clean build
./gradlew clean
```

## Key Dependencies

- `androidx.compose:compose-bom:2023.10.01` - Compose Bill of Materials
- `androidx.compose.material3:material3:1.1.2` - Material Design 3
- `androidx.lifecycle:lifecycle-viewmodel-compose` - ViewModel for Compose
- `androidx.lifecycle:lifecycle-runtime-compose` - Lifecycle utilities

## Design Principles

- **Minimalist**: Clean, uncluttered interface focused on the breathing exercise
- **Accessible**: WCAG 2.1 AA compliant with proper contrast ratios
- **Performant**: Smooth 60fps animations using Compose
- **Responsive**: Adapts to different screen sizes and orientations
- **Intuitive**: Simple controls with clear visual feedback

## Color Palette

- **Primary**: Deep blue (#0A2540) - Calming and professional
- **Secondary**: Teal (#00C49A) - Energizing accent
- **Background**: Dark (#0F1419) - Reduces eye strain
- **Breathing States**: 
  - Inhale: Teal gradient
  - Hold: Warm orange
  - Exhale: Cool blue gradient

## License

This is a production-ready application built for anxiety and panic attack management.

## Version

**1.0.0** - Initial release
