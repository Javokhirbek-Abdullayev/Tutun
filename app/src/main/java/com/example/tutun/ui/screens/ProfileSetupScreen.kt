package com.example.tutun.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun ProfileSetupScreen(
    selectedLanguage: String,
    initialFirstName: String = "",
    initialLastName: String = "",
    initialAge: String = "",
    initialStatus: String = "",
    onBack: () -> Unit,
    onNext: (firstName: String, lastName: String, age: String, status: String) -> Unit
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

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var firstName by remember { mutableStateOf(initialFirstName) }
    var lastName by remember { mutableStateOf(initialLastName) }
    var selectedAge by remember {
        mutableStateOf(initialAge.ifEmpty { "1991-08-31" })
    }
    var selectedStatus by remember { mutableStateOf(initialStatus) }

    val isFormValid = firstName.isNotBlank() && selectedAge.isNotBlank() && selectedStatus.isNotBlank()

    var contentVisible by remember { mutableStateOf(false) }
    var showValidation by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(300)
        contentVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .background(profileSkyGradient())
    ) {
        ProfileFloatingParticles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(durationMillis = 350, easing = FastOutSlowInEasing)) +
                    slideInVertically(tween(durationMillis = 350, easing = FastOutSlowInEasing)) { it / 4 },
                exit = fadeOut(tween(durationMillis = 200, easing = FastOutSlowInEasing)) +
                    slideOutVertically(tween(durationMillis = 200, easing = FastOutSlowInEasing)) { it / 4 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val title = when (selectedLanguage) {
                        "ky" -> "Өзүңүз жөнүндө"
                        "ru" -> "О себе"
                        "uz" -> "O'zingiz haqida"
                        "en" -> "About yourself"
                        else -> "About yourself"
                    }
                    Text(
                        text = title,
                        color = Color(0xFF1A3A5C),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OnboardingDots(currentStep = 2)
                    Spacer(modifier = Modifier.height(12.dp))
                    NameCard(
                        label = localizedLabel("first", selectedLanguage),
                        value = firstName,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isLetter() || it.isWhitespace() }
                            firstName = filtered.take(30)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NameCard(
                        label = localizedLabel("last", selectedLanguage),
                        value = lastName,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isLetter() || it.isWhitespace() }
                            lastName = filtered.take(30)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AgeCard(
                        selectedAge = selectedAge,
                        onAgeSelected = { range ->
                            selectedAge = range
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                        selectedLanguage = selectedLanguage
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StatusCard(
                        selectedStatus = selectedStatus,
                        onStatusSelected = { code ->
                            selectedStatus = code
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                        selectedLanguage = selectedLanguage
                    )
                }
            }

            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(durationMillis = 350, easing = FastOutSlowInEasing)) +
                    slideInVertically(tween(durationMillis = 350, easing = FastOutSlowInEasing)) { it / 4 },
                exit = fadeOut(tween(durationMillis = 200, easing = FastOutSlowInEasing)) +
                    slideOutVertically(tween(durationMillis = 200, easing = FastOutSlowInEasing)) { it / 4 }
            ) {
                BottomProfileButtons(
                    selectedLanguage = selectedLanguage,
                    isFormValid = isFormValid,
                    onBack = onBack,
                    onNext = {
                        if (!isFormValid) {
                            val msg = when {
                                firstName.isBlank() -> when (selectedLanguage) {
                                    "ky" -> "👆 Атыңызды жазыңыз"
                                    "ru" -> "👆 Введите ваше имя"
                                    "uz" -> "👆 Ismingizni kiriting"
                                    "en" -> "👆 Please enter your first name"
                                    else -> "👆 Please enter your first name"
                                }

                                selectedAge.isBlank() -> when (selectedLanguage) {
                                    "ky" -> "👆 Туулган күнүңүздү тандаңыз"
                                    "ru" -> "👆 Выберите дату рождения"
                                    "uz" -> "👆 Tug'ilgan kuningizni tanlang"
                                    "en" -> "👆 Please select your birthday"
                                    else -> "👆 Please select your birthday"
                                }

                                selectedStatus.isBlank() -> when (selectedLanguage) {
                                    "ky" -> "👆 Абалыңызды тандаңыз"
                                    "ru" -> "👆 Выберите статус"
                                    "uz" -> "👆 Holatingizni tanlang"
                                    "en" -> "👆 Please select your status"
                                    else -> "👆 Please select your status"
                                }

                                else -> ""
                            }
                            if (msg.isNotEmpty()) {
                                validationMessage = msg
                                showValidation = true
                                scope.launch {
                                    delay(2500)
                                    showValidation = false
                                }
                            }
                        } else {
                            onNext(firstName, lastName, selectedAge, selectedStatus)
                        }
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = showValidation,
            enter = fadeIn(tween(200)) +
                slideInVertically(tween(200)) { it / 4 },
            exit = fadeOut(tween(200)) +
                slideOutVertically(tween(200)) { it / 4 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A3A5C).copy(alpha = 0.92f))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = validationMessage,
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun profileSkyGradient(): Brush {
    val transition = rememberInfiniteTransition(label = "profileSkyGradient")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "profileSkyPhase"
    )

    val segment = floor(phase * 3f).toInt().coerceIn(0, 2)
    val t = (phase * 3f) - segment
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

    fun lerpColor(a: Color, b: Color, f: Float): Color =
        androidx.compose.ui.graphics.lerp(a, b, f)

    val top = lerpColor(topColors[segment], topColors[nextSegment], t)
    val middle = lerpColor(middleColors[segment], middleColors[nextSegment], t)
    val bottom = lerpColor(bottomColors[segment], bottomColors[nextSegment], t)

    return Brush.verticalGradient(listOf(top, middle, bottom))
}

@Composable
private fun ProfileFloatingParticles(particleCount: Int = 12) {
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

    val transition = rememberInfiniteTransition(label = "profileParticles")
    val baseProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "profileParticleBase"
    )

    val density = LocalDensity.current

    Canvas(modifier = Modifier.fillMaxSize()) {
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
                label = "profileDotWidth_$step"
            )
            val height by animateDpAsState(
                targetValue = targetHeight,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                label = "profileDotHeight_$step"
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
private fun NameCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor = if (focused) Color(0xFF1A6BAA) else Color(0xFF1A6BAA).copy(alpha = 0.35f)
    val borderWidth = if (focused) 2.dp else 1.5.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.55f))
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFF1A3A5C),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        val hintColor = Color(0xFF1A3A5C).copy(alpha = 0.4f)

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = Color(0xFF1A3A5C),
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(Color(0xFF1A6BAA)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .onFocusChanged { focused = it.isFocused },
            singleLine = true,
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = label,
                        color = hintColor,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun AgeCard(
    selectedAge: String,
    onAgeSelected: (String) -> Unit,
    selectedLanguage: String
) {
    val label = when (selectedLanguage) {
        "ky" -> "Туулган күнүңүз"
        "ru" -> "Дата рождения"
        "uz" -> "Tug'ilgan kuningiz"
        "en" -> "Birthday"
        else -> "Birthday"
    }

    val parts = remember(selectedAge) {
        if (selectedAge.isNotBlank() && selectedAge.contains("-")) {
            selectedAge.split("-")
        } else emptyList()
    }
    val defaultMonthIndex = (parts.getOrNull(1)?.toIntOrNull()?.minus(1))?.coerceIn(0, 11) ?: 7 // Aug = 8 -> index 7
    var year by remember(selectedAge) { mutableStateOf(parts.getOrNull(0) ?: "1991") }
    var monthIndex by remember(selectedAge) { mutableStateOf(defaultMonthIndex) }
    var day by remember(selectedAge) { mutableStateOf(parts.getOrNull(2) ?: "31") }
    val hasSelection = selectedAge.isNotBlank()

    val monthShortNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val borderColor = if (hasSelection) Color(0xFF1A6BAA) else Color(0xFF1A6BAA).copy(alpha = 0.35f)
    val borderWidth = if (hasSelection) 2.dp else 1.5.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.55f))
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFF1A3A5C),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        val currentYear = remember {
            java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        }
    // 150-year range ending in the current year.
    val years = (currentYear - 149..currentYear).map { it.toString() }

    val daysAll = (1..31).map { it.toString().padStart(2, '0') }

    val yearInt = year.toIntOrNull() ?: currentYear
    val monthNumber = (monthIndex + 1).coerceIn(1, 12)
    val maxDay = daysInMonth(monthNumber, yearInt)

    // Invalid days become "-" so the wheel can’t save them.
    val dayItems = daysAll.map { dd ->
        val d = dd.toIntOrNull() ?: 1
        if (d <= maxDay) dd else "-"
    }

    fun updateSelection(
        newYear: String = year,
        newMonthIndex: Int = monthIndex,
        newDay: String = day
    ) {
        val parsedYear = newYear.toIntOrNull() ?: return
        val parsedMonthIndex = newMonthIndex.coerceIn(0, 11)
        val monthNum = parsedMonthIndex + 1
        val maxForMonth = daysInMonth(monthNum, parsedYear)

        val chosenDayInt = newDay.toIntOrNull()
        val finalDayInt = chosenDayInt?.coerceIn(1, maxForMonth) ?: maxForMonth
        val finalDayStr = finalDayInt.toString().padStart(2, '0')
        val monthStr = (parsedMonthIndex + 1).toString().padStart(2, '0')

        year = newYear
        monthIndex = parsedMonthIndex
        day = finalDayStr

        onAgeSelected("$parsedYear-$monthStr-$finalDayStr")
    }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BirthdayPickerColumn(
                    modifier = Modifier.weight(1f),
                    items = dayItems,
                    selected = day,
                    label = when (selectedLanguage) {
                        "ky" -> "Күн"
                        "ru" -> "День"
                        "uz" -> "Kun"
                        "en" -> "Day"
                        else -> "Day"
                    },
                    onSelected = { d -> updateSelection(newDay = d) }
                )

                BirthdayPickerColumn(
                    modifier = Modifier.weight(1f),
                    items = monthShortNames,
                    selected = monthShortNames[monthIndex],
                    label = when (selectedLanguage) {
                        "ky" -> "Ай"
                        "ru" -> "Месяц"
                        "uz" -> "Oy"
                        "en" -> "Month"
                        else -> "Month"
                    },
                    onSelected = { name ->
                        val idx = monthShortNames.indexOf(name).coerceAtLeast(0)
                        updateSelection(newMonthIndex = idx)
                    }
                )

                BirthdayPickerColumn(
                    modifier = Modifier.weight(1.2f),
                    items = years,
                    selected = year,
                    label = when (selectedLanguage) {
                        "ky" -> "Жыл"
                        "ru" -> "Год"
                        "uz" -> "Yil"
                        "en" -> "Year"
                        else -> "Year"
                    },
                    onSelected = { y -> updateSelection(newYear = y) }
                )
            }
        }
    }
}

private val WHEEL_ITEM_HEIGHT = 28.dp
private val WHEEL_VISIBLE_HEIGHT = 80.dp
private val WHEEL_VERTICAL_PADDING = 26.dp

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

private fun daysInMonth(month: Int, year: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 31
    }
}

@Composable
private fun BirthdayPickerColumn(
    modifier: Modifier,
    items: List<String>,
    selected: String,
    label: String,
    onSelected: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    // Add "-" sentinels to allow centering the first/last real value,
    // but never allow "-" to be saved/selected.
    val paddedItems = remember(items) { listOf("-") + items + listOf("-") }

    val initialRealIndex = remember(selected, items) {
        items.indexOf(selected).takeIf { it >= 0 } ?: items.indexOfFirst { it != "-" }.coerceAtLeast(0)
    }
    val initialPaddedIndex = initialRealIndex + 1

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialPaddedIndex)
    var centeredPaddedIndex by remember(selected, items) { mutableStateOf(initialPaddedIndex) }

    fun nearestSelectableRealIndex(startRealIndex: Int): Int {
        if (items.isEmpty()) return 0
        val clamped = startRealIndex.coerceIn(0, items.lastIndex)
        if (items[clamped] != "-") return clamped

        for (d in 1..items.lastIndex) {
            val left = clamped - d
            if (left >= 0 && items[left] != "-") return left
            val right = clamped + d
            if (right <= items.lastIndex && items[right] != "-") return right
        }
        return clamped
    }

    LaunchedEffect(listState.isScrollInProgress, items) {
        if (listState.isScrollInProgress) return@LaunchedEffect
        if (items.isEmpty()) return@LaunchedEffect
        val layoutInfo = listState.layoutInfo
        if (layoutInfo.visibleItemsInfo.isEmpty()) return@LaunchedEffect

        val viewportCenterY =
            layoutInfo.viewportStartOffset.toFloat() + (layoutInfo.viewportSize.height.toFloat() / 2f)

        val bestVisible = layoutInfo.visibleItemsInfo.minByOrNull { item ->
            val h = item.size
            val itemCenterY = item.offset.toFloat() + h.toFloat() / 2f
            abs(itemCenterY - viewportCenterY)
        } ?: return@LaunchedEffect

        val bestPaddedIndex = bestVisible.index
        val bestRealIndex = (bestPaddedIndex - 1).coerceIn(0, items.lastIndex)
        val nearestRealIndex = nearestSelectableRealIndex(bestRealIndex)
        val nearestPaddedIndex = nearestRealIndex + 1

        if (nearestPaddedIndex != centeredPaddedIndex) {
            centeredPaddedIndex = nearestPaddedIndex
            val valueToSave = items[nearestRealIndex]
            if (valueToSave != "-") onSelected(valueToSave)
            scope.launch {
                listState.animateScrollToItem(nearestPaddedIndex)
            }
        }
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color(0xFF1A3A5C),
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))

        val baseColor = Color(0xFF1A3A5C)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(WHEEL_VISIBLE_HEIGHT),
            contentPadding = PaddingValues(vertical = WHEEL_VERTICAL_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(paddedItems.size) { index ->
                val value = paddedItems[index]
                val distance = abs(index - centeredPaddedIndex)
                val alpha = when (distance) {
                    0 -> 1f
                    1 -> 0.6f
                    else -> 0.3f
                }
                val fontSize = when (distance) {
                    0 -> 15.sp
                    1 -> 13.sp
                    else -> 11.sp
                }
                val isSelected = index == centeredPaddedIndex
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "birthday_${label}_$value"
                )

                Box(
                    modifier = Modifier
                        .height(WHEEL_ITEM_HEIGHT)
                        .fillMaxWidth()
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .clickable(
                            enabled = value != "-"
                        ) {
                            centeredPaddedIndex = index
                            if (value != "-") onSelected(value)
                            scope.launch {
                                listState.animateScrollToItem(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value,
                        color = baseColor.copy(alpha = alpha),
                        fontSize = fontSize,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    selectedLanguage: String
) {
    val label = localizedLabel("status", selectedLanguage)
    val options = localizedStatusOptions(selectedLanguage)
    val hasSelection = selectedStatus.isNotBlank()
    val borderColor = if (hasSelection) Color(0xFF1A6BAA) else Color(0xFF1A6BAA).copy(alpha = 0.35f)
    val borderWidth = if (hasSelection) 2.dp else 1.5.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.55f))
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFF1A3A5C),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (emoji, text) ->
                val selected = selectedStatus == text
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selected) Color.White.copy(alpha = 0.85f)
                            else Color.Transparent
                        )
                        .clickable { onStatusSelected(text) }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = text,
                        color = Color(0xFF1A3A5C),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = Color(0xFF1A6BAA),
                                shape = CircleShape
                            )
                            .background(
                                if (selected) Color(0xFF1A6BAA) else Color.Transparent
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomProfileButtons(
    selectedLanguage: String,
    isFormValid: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val backText = when (selectedLanguage) {
        "ky" -> "Артка"
        "ru" -> "Назад"
        "uz" -> "Orqaga"
        "en" -> "Back"
        else -> "Back"
    }
    val nextText = when (selectedLanguage) {
        "ky" -> "Бүтөрүү"
        "ru" -> "Завершить"
        "uz" -> "Tugatish"
        "en" -> "Finish"
        else -> "Finish"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.55f))
                .border(
                    width = 1.5.dp,
                    color = Color(0xFF1A6BAA).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(28.dp)
                )
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = backText,
                color = Color(0xFF1A3A5C),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    if (isFormValid) Color(0xFF1A6BAA)
                    else Color(0xFF1A6BAA).copy(alpha = 0.3f)
                )
                .clickable { onNext() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nextText,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun localizedLabel(field: String, lang: String): String =
    when (field) {
        "first" -> when (lang) {
            "ky" -> "Атыңыз"
            "ru" -> "Имя"
            "uz" -> "Ismingiz"
            "en" -> "First name"
            else -> "First name"
        }

        "last" -> when (lang) {
            "ky" -> "Фамилияңыз"
            "ru" -> "Фамилия"
            "uz" -> "Familiyangiz"
            "en" -> "Last name"
            else -> "Last name"
        }

        "age" -> when (lang) {
            "ky" -> "Жашыңыз"
            "ru" -> "Возраст"
            "uz" -> "Yoshingiz"
            "en" -> "Age"
            else -> "Age"
        }

        "status" -> when (lang) {
            "ky" -> "Абалыңыз"
            "ru" -> "Статус"
            "uz" -> "Holatingiz"
            "en" -> "Status"
            else -> "Status"
        }

        else -> ""
    }

private fun localizedStatusOptions(lang: String): List<Pair<String, String>> =
    when (lang) {
        "ky" -> listOf(
            "🎓" to "Студент / Окуучу",
            "💼" to "Иштеген",
            "✨" to "Башка"
        )

        "ru" -> listOf(
            "🎓" to "Студент / Школьник",
            "💼" to "Работающий",
            "✨" to "Другое"
        )

        "uz" -> listOf(
            "🎓" to "Talaba / O'quvchi",
            "💼" to "Ishlovchi",
            "✨" to "Boshqa"
        )

        "en" -> listOf(
            "🎓" to "Student / Pupil",
            "💼" to "Employed",
            "✨" to "Other"
        )

        else -> localizedStatusOptions("en")
    }

