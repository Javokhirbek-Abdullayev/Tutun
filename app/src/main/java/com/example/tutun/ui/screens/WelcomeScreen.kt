package com.example.tutun.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.random.Random

@Composable
fun WelcomeScreen(
    firstName: String,
    language: String
) {
    val context = LocalContext.current
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val activity = context as? Activity
            val window = activity?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.statusBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller?.isAppearanceLightStatusBars = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(welcomeSkyGradient())
    ) {
        WelcomeFloatingParticles()

        val circleVisible by remember { mutableStateOf(true) }
        val circleScale by animateFloatAsState(
            targetValue = if (circleVisible) 1f else 0f,
            animationSpec = tween(durationMillis = 700, easing = EaseOutBack),
            label = "welcomeCircleScale"
        )

        val pulseScale = remember { Animatable(0f) }
        val pulseAlpha = remember { Animatable(0f) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            // start pulse slightly after circle appears
            delay(500)
            repeat(2) {
                pulseScale.snapTo(1f)
                pulseAlpha.snapTo(0.4f)
                pulseScale.animateTo(
                    targetValue = 1.6f,
                    animationSpec = tween(durationMillis = 700, easing = LinearEasing)
                )
                pulseAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 700, easing = LinearEasing)
                )
                pulseScale.snapTo(0f)
                pulseAlpha.snapTo(0f)
            }
        }

        var showGreeting by remember { mutableStateOf(false) }
        var showLine2 by remember { mutableStateOf(false) }
        var showTagline by remember { mutableStateOf(false) }
        var showButton by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(600)
            showGreeting = true
            delay(200)
            showLine2 = true
            delay(200)
            showTagline = true
            delay(200)
            showButton = true
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                if (pulseScale.value > 0f) {
                    Box(
                        modifier = Modifier
                            .size(100.dp * pulseScale.value)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = Color.White.copy(alpha = pulseAlpha.value),
                                shape = CircleShape
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer(
                            scaleX = circleScale,
                            scaleY = circleScale
                        )
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .border(
                            width = 2.dp,
                            color = Color.White.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val greeting = when (language) {
                "ky" -> "Кош келдиңиз, $firstName! 👋"
                "ru" -> "Добро пожаловать, $firstName! 👋"
                "uz" -> "Xush kelibsiz, $firstName! 👋"
                "en" -> "Welcome, $firstName! 👋"
                else -> "Welcome, $firstName! 👋"
            }

            val subtitle = when (language) {
                "ky" -> "Tutun даярдалды"
                "ru" -> "Tutun готов"
                "uz" -> "Tutun tayyor"
                "en" -> "Tutun is ready"
                else -> "Tutun is ready"
            }

            val tagline = when (language) {
                "ky" -> "Бишкектин абасын бирге байкайлы"
                "ru" -> "Вместе следим за воздухом Бишкека"
                "uz" -> "Bishkek havosini birga kuzataylik"
                "en" -> "Let's watch Bishkek's air together"
                else -> "Let's watch Bishkek's air together"
            }

            AnimatedVisibility(
                visible = showGreeting,
                enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(animationSpec = tween(300)) { it / 6 },
                exit = fadeOut()
            ) {
                Text(
                    text = greeting,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = showLine2,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut()
            ) {
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = showTagline,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut()
            ) {
                Text(
                    text = tagline,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }
        }

        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 0.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(fraction = 1f)
                )

                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxSize(fraction = 0.0f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                        .align(Alignment.BottomCenter)
                        .clickable {
                            Log.d("Tutun", "Onboarding complete")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (language) {
                            "ky" -> "Баштоо"
                            "ru" -> "Начать"
                            "uz" -> "Boshlash"
                            "en" -> "Get Started"
                            else -> "Get Started"
                        },
                        color = Color(0xFF1A6BAA),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun welcomeSkyGradient(): Brush {
    val transition = rememberInfiniteTransition(label = "welcomeSkyGradient")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "welcomeSkyPhase"
    )

    val segment = floor(phase).toInt() % 3
    val t = phase - floor(phase)
    val nextSegment = (segment + 1) % 3

    val topColors = listOf(
        Color(0xFF87CEEB),
        Color(0xFF90CAF9),
        Color(0xFF81D4FA)
    )
    val middleColors = listOf(
        Color(0xFFE0F7FA),
        Color(0xFFE1F5FE),
        Color(0xFFB3E5FC)
    )
    val bottomColors = listOf(
        Color(0xFFB3E5FC),
        Color(0xFFE8F5E9),
        Color(0xFFE0F7FA)
    )

    fun lerpColor(a: Color, b: Color, fraction: Float): Color {
        return androidx.compose.ui.graphics.lerp(a, b, fraction)
    }

    val top = lerpColor(topColors[segment], topColors[nextSegment], t)
    val middle = lerpColor(middleColors[segment], middleColors[nextSegment], t)
    val bottom = lerpColor(bottomColors[segment], bottomColors[nextSegment], t)

    return Brush.verticalGradient(
        colors = listOf(top, middle, bottom)
    )
}

@Composable
private fun WelcomeFloatingParticles(particleCount: Int = 12) {
    val random = remember { Random(0x5475756E) }
    data class P(
        val xFraction: Float,
        val size: Float,
        val alpha: Float,
        val durationMillis: Int,
        val initialOffsetFraction: Float
    )
    val particles = remember {
        List(particleCount) {
            P(
                xFraction = random.nextFloat(),
                size = (6 + random.nextInt(13)).toFloat(),
                alpha = 0.15f + random.nextFloat() * 0.2f,
                durationMillis = 6_000 + random.nextInt(6_001),
                initialOffsetFraction = random.nextFloat()
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "welcomeParticleTransition")
    val progresses = particles.mapIndexed { index, particle ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = particle.durationMillis,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "welcomeParticleProgress_$index"
        )
    }

    val density = LocalDensity.current

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        particles.forEachIndexed { index, particle ->
            val progress = (progresses[index].value + particle.initialOffsetFraction) % 1f
            val x = size.width * particle.xFraction
            val y = size.height * (1f - progress)
            val radius = with(density) { particle.size.dp.toPx() } / 2f

            drawCircle(
                color = Color.White.copy(alpha = particle.alpha),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

