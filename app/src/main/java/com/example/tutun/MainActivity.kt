package com.example.tutun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.tutun.ui.screens.LanguageSelectionScreen
import com.example.tutun.ui.screens.ProfileSetupScreen
import com.tutun.bishkek.ui.screens.HomeScreen
import com.tutun.bishkek.ui.theme.TutunTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("tutun_prefs", MODE_PRIVATE)
        setContent {
            val onboardingCompleted = prefs.getBoolean("onboarding_completed", false)
            var currentScreen by remember {
                mutableStateOf(if (onboardingCompleted) "main" else "language")
            }
            var selectedLanguage by remember {
                mutableStateOf(prefs.getString("selected_language", "ky") ?: "ky")
            }
            var userFirstName by remember {
                mutableStateOf(prefs.getString("user_first_name", "") ?: "")
            }
            var userLastName by remember {
                mutableStateOf(prefs.getString("user_last_name", "") ?: "")
            }
            var userAge by remember {
                mutableStateOf(prefs.getString("user_age", "") ?: "")
            }
            var userStatus by remember {
                mutableStateOf(prefs.getString("user_status", "") ?: "")
            }

            TutunTheme {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        val slideIn = slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (targetState == "profile") fullWidth else -fullWidth },
                            animationSpec = tween(durationMillis = 350)
                        ) + fadeIn(tween(350))
                        val slideOut = slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (targetState == "profile") -fullWidth else fullWidth },
                            animationSpec = tween(durationMillis = 350)
                        ) + fadeOut(tween(350))
                        slideIn togetherWith slideOut
                    },
                    label = "onboarding_nav"
                ) { screen ->
                    when (screen) {
                        "language" -> LanguageSelectionScreen(
                            selectedLanguage = selectedLanguage,
                            onContinue = { lang ->
                                selectedLanguage = lang
                                currentScreen = "profile"
                            }
                        )

                        "profile" -> ProfileSetupScreen(
                            selectedLanguage = selectedLanguage,
                            initialFirstName = userFirstName,
                            initialLastName = userLastName,
                            initialAge = userAge,
                            initialStatus = userStatus,
                            onBack = { currentScreen = "language" },
                            onNext = { firstName, lastName, age, status ->
                                userFirstName = firstName
                                userLastName = lastName
                                userAge = age
                                userStatus = status
                                prefs.edit()
                                    .putBoolean("onboarding_completed", true)
                                    .putString("selected_language", selectedLanguage)
                                    .putString("user_first_name", firstName)
                                    .putString("user_last_name", lastName)
                                    .putString("user_age", age)
                                    .putString("user_status", status)
                                    .apply()
                                currentScreen = "main"
                            }
                        )

                        "main" -> HomeScreen(
                            language = selectedLanguage,
                            userFirstName = userFirstName,
                            userStatus = userStatus,
                            viewModel = viewModel()
                        )

                        else -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = "Tutun is alive",
                                color = Color(0xFF1A3A5C),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

