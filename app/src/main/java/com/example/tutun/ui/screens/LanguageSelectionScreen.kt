package com.example.tutun.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.random.Random

@Composable
fun LanguageSelectionScreen(
    selectedLanguage: String = "ky",
    onContinue: (String) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    if (!view.isInEditMode) {
        val activity = context as? Activity
        SideEffect {
            val window = activity?.window ?: return@SideEffect
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view)?.isAppearanceLightStatusBars = false
        }
    }

    var selectedCode by remember(selectedLanguage) { mutableStateOf(selectedLanguage) }
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        contentVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .background(languageSkyGradient())
    ) {
        LanguageFloatingParticles()

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopLanguageSection(selectedLanguageCode = selectedCode)

            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(durationMillis = 350, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        tween(durationMillis = 350, easing = FastOutSlowInEasing)
                    ) { it / 3 },
                exit = fadeOut(tween(durationMillis = 200, easing = FastOutSlowInEasing)) +
                    slideOutVertically(
                        tween(durationMillis = 200, easing = FastOutSlowInEasing)
                    ) { it / 3 }
            ) {
                LanguageListSection(
                    selectedLanguageCode = selectedCode,
                    onLanguageSelected = { selectedCode = it }
                )
            }

            LanguageContinueButton(
                selectedLanguageCode = selectedCode,
                onClick = { onContinue(selectedCode) }
            )
        }
    }
}

@Composable
private fun TopLanguageSection(selectedLanguageCode: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.55f))
                .border(
                    width = 1.5.dp,
                    color = Color(0xFF1A6BAA).copy(alpha = 0.35f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🌫️", fontSize = 48.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tutun",
            color = Color(0xFF1A3A5C),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.15.em
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "таза абa · чистый воздух · toza havo · clean air",
            color = Color(0xFF1A3A5C),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OnboardingDots(currentStep = 1)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

private data class LanguageOption(
    val code: String,
    val flag: String,
    val nativeName: String,
    val englishName: String
)

@Composable
private fun LanguageListSection(
    selectedLanguageCode: String,
    onLanguageSelected: (String) -> Unit
) {
    val options = listOf(
        LanguageOption("ky", "🇰🇬", "Кыргызча", "Kyrgyz"),
        LanguageOption("ru", "🇷🇺", "Русский", "Russian"),
        LanguageOption("uz", "🇺🇿", "O'zbek", "Uzbek"),
        LanguageOption("en", "🇬🇧", "English", "English")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(options) { option ->
            val isSelected = option.code == selectedLanguageCode
            LanguageCard(
                option = option,
                isSelected = isSelected,
                onClick = { onLanguageSelected(option.code) }
            )
        }
    }
}

@Composable
private fun LanguageCard(
    option: LanguageOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = Color.White.copy(alpha = 0.55f)
    val borderColor = if (isSelected) {
        Color(0xFF1A6BAA)
    } else {
        Color(0xFF1A6BAA).copy(alpha = 0.35f)
    }
    val borderWidth = if (isSelected) 2.dp else 1.5.dp

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "languageCardScale_${option.code}"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(72.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A6BAA).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = option.flag, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = option.nativeName,
                    color = Color(0xFF1A3A5C),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = option.englishName,
                    color = Color(0xFF1A3A5C),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF1A6BAA),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun LanguageContinueButton(
    selectedLanguageCode: String,
    onClick: () -> Unit
) {
    val label = when (selectedLanguageCode) {
        "ky" -> "Улантуу"
        "ru" -> "Продолжить"
        "uz" -> "Davom etish"
        "en" -> "Continue"
        else -> "Continue"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color(0xFF1A6BAA),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OnboardingDots(currentStep: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..2).forEach { step ->
            val isActive = step == currentStep
            val targetWidth = if (isActive) 16.dp else 8.dp
            val targetHeight = if (isActive) 10.dp else 8.dp
            val width by animateDpAsState(
                targetValue = targetWidth,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                label = "langDotWidth_$step"
            )
            val height by animateDpAsState(
                targetValue = targetHeight,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                label = "langDotHeight_$step"
            )

            Box(
                modifier = Modifier
                    .size(width, height)
                    .clip(if (isActive) RoundedCornerShape(50) else CircleShape)
                    .background(
                        if (isActive) Color(0xFF1A6BAA)
                        else Color(0xFF1A6BAA).copy(alpha = 0.3f)
                    )
            )
        }
    }
}

@Composable
private fun languageSkyGradient(): Brush {
    val transition = rememberInfiniteTransition(label = "languageSkyGradient")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "languageSkyPhase"
    )

    val segment = floor(phase * 3f).toInt().coerceIn(0, 2)
    val localT = (phase * 3f) - segment
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

    fun lerpColor(a: Color, b: Color, fraction: Float): Color =
        androidx.compose.ui.graphics.lerp(a, b, fraction)

    val top = lerpColor(topColors[segment], topColors[nextSegment], localT)
    val middle = lerpColor(middleColors[segment], middleColors[nextSegment], localT)
    val bottom = lerpColor(bottomColors[segment], bottomColors[nextSegment], localT)

    return Brush.verticalGradient(colors = listOf(top, middle, bottom))
}

@Composable
private fun LanguageFloatingParticles(
    particleCount: Int = 12
) {
    val random = remember { Random(0x5475756E) }

    data class Particle(
        val xFraction: Float,
        val size: Dp,
        val alpha: Float,
        val phaseOffset: Float
    )

    val particles = remember {
        List(particleCount) { index ->
            Particle(
                xFraction = random.nextFloat(),
                size = (6 + random.nextInt(13)).dp,
                alpha = 0.15f + random.nextFloat() * 0.2f,
                phaseOffset = index.toFloat() / particleCount
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "languageParticles")
    val baseProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "languageParticleBase"
    )

    val density = LocalDensity.current

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        particles.forEach { particle ->
            val progress = (baseProgress + particle.phaseOffset) % 1f
            val x = size.width * particle.xFraction
            val y = size.height * (1f - progress)
            val radius = with(density) { particle.size.toPx() } / 2f

            drawCircle(
                color = Color.White.copy(alpha = particle.alpha),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}
