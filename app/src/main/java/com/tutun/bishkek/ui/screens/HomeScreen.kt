@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.tutun.bishkek.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tutun.bishkek.data.model.AirBillCalculator
import com.tutun.bishkek.data.model.BISHKEK_DISTRICTS
import com.tutun.bishkek.data.model.CarouselCard
import com.tutun.bishkek.data.model.District
import com.tutun.bishkek.data.model.HomeData
import com.tutun.bishkek.data.model.OwmHourly
import com.tutun.bishkek.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    language: String,
    userFirstName: String,
    userStatus: String,
    viewModel: HomeViewModel,
) {
    val homeData by viewModel.homeData.collectAsState()
    val context = LocalContext.current

    val aqi = homeData.aqiData?.aqi ?: 0
    val pm25 = homeData.aqiData?.iaqi?.pm25?.v ?: 0.0
    val temp = homeData.owmData?.current?.temp ?: 0.0
    val feelsLike = homeData.owmData?.current?.feels_like ?: 0.0
    val humidity = homeData.owmData?.current?.humidity ?: 0
    val windSpeed = homeData.owmData?.current?.wind_speed ?: 0.0
    val sunrise = homeData.owmData?.current?.sunrise ?: 0L
    val sunset = homeData.owmData?.current?.sunset ?: 0L
    val weatherDesc = homeData.owmData?.current?.weather?.firstOrNull()
    val aiSummary = homeData.owmData?.current?.summary
    val dailyForecast = homeData.owmData?.daily ?: emptyList()
    val hourlyForecast = homeData.owmData?.hourly ?: emptyList()

    val (cStart, cEnd) = viewModel.getAqiColors(aqi)
    val gradientStart by animateColorAsState(targetValue = cStart, label = "aqiGradientStart")
    val gradientEnd by animateColorAsState(targetValue = cEnd, label = "aqiGradientEnd")
    val heroGradient = remember(gradientStart, gradientEnd) { Brush.linearGradient(listOf(gradientStart, gradientEnd)) }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    var currentTimeText by remember { mutableStateOf(DateTimeFormatter.ofPattern("HH:mm").format(java.time.LocalTime.now())) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            currentTimeText = timeFormatter.format(java.time.LocalTime.now())
        }
    }

    val refreshInfiniteTransition = rememberInfiniteTransition(label = "homeRefreshRotation")
    val refreshRotation by refreshInfiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
        ),
        label = "homeRefreshRotationAnim",
    )

    var bottomSheetContent by remember { mutableStateOf<BottomSheetContent?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val airBillResult = remember(pm25, userStatus) {
        AirBillCalculator.calculate(
            pm25 = pm25,
            status = userStatus,
            districtMultiplier = 1.0,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                HomeTopBar(
                    currentTimeText = currentTimeText,
                    isLoading = homeData.isLoading,
                    refreshRotation = refreshRotation,
                    onRefresh = { viewModel.fetchAllData() },
                )
            }

            item { Spacer(Modifier.height(4.dp)) }

            if (homeData.isLoading) {
                item { ShimmerCarousel() }
                item { Spacer(Modifier.height(12.dp)) }
                item { ShimmerHero() }
                item { Spacer(Modifier.height(12.dp)) }
                item { ShimmerHourlyStrip() }
                item { Spacer(Modifier.height(12.dp)) }
                item { ShimmerCardRow() }
                item { Spacer(Modifier.height(12.dp)) }
                item { ShimmerSectionTitle() }
                item { ShimmerPollutantChips() }
            } else if (homeData.error != null) {
                item {
                    ErrorStateCard(
                        error = homeData.error ?: "",
                        onRetry = { viewModel.fetchAllData() },
                    )
                }
            } else {
                item {
                    CarouselSection(context = context)
                }
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    HeroCard(
                        aqi = aqi,
                        aqiLabel = viewModel.getAqiLabel(aqi, language),
                        dominantPollutant = dominantPollutantLabel(homeData.aqiData?.dominentpol),
                        heroGradient = heroGradient,
                        temp = temp,
                        feelsLike = feelsLike,
                        weatherDescription = weatherDesc?.description.orEmpty(),
                        cigarettesRaw = AirBillCalculator.cigaretteEquivalent(pm25),
                        humidity = humidity,
                        windSpeed = windSpeed,
                        sunrise = sunrise,
                        sunset = sunset,
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    HourlyForecastStrip(
                        language = language,
                        hourly = hourlyForecast,
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    EightDayForecastSection(dailyForecast = dailyForecast)
                }
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    SectionHeading(
                        title = "Air Pollutants",
                        showInfo = true,
                    )
                }
                item {
                    PollutantChipsRow(
                        language = language,
                        homeData = homeData,
                        onPollutantClick = { sheet ->
                            bottomSheetContent = sheet
                        },
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    SectionHeading(title = "Bishkek Districts")
                }
                item {
                    DistrictsStrip(
                        language = language,
                        cityAqi = aqi,
                        onDistrictClick = { district ->
                            bottomSheetContent = BottomSheetContent.DistrictSheet(district = district)
                        },
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
                if (!aiSummary.isNullOrBlank()) {
                    item {
                        AiSummaryCard(aiSummary = aiSummary)
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
                item {
                    PollutionSourcesCard()
                }
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    AirBillTeaserCard(
                        dailyTotal = airBillResult.totalDaily,
                        healthcareCost = airBillResult.healthcareCost,
                        productivityCost = airBillResult.productivityCost,
                        preventiveCost = airBillResult.preventiveCost,
                        onDetails = {
                            bottomSheetContent = BottomSheetContent.AirBillSheet
                        },
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }

        bottomSheetContent?.let { content ->
            ModalBottomSheet(
                onDismissRequest = { bottomSheetContent = null },
                sheetState = sheetState,
                containerColor = Color(0xFFF8FAFC),
                dragHandle = { },
            ) {
                BottomSheetBody(
                    language = language,
                    homeData = homeData,
                    viewModel = viewModel,
                    userStatus = userStatus,
                    airBillResult = airBillResult,
                    content = content,
                    onClose = { bottomSheetContent = null },
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    currentTimeText: String,
    isLoading: Boolean,
    refreshRotation: Float,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC).copy(alpha = 0.9f))
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Bishkek 🇰🇬",
                color = Color(0xFF1A3A5C),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.wrapContentWidth(),
            )
            Text(
                text = currentTimeText,
                color = Color(0xFF1A3A5C),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.wrapContentWidth(),
            )
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .size(40.dp)
                    .alpha(if (isLoading) 1f else 0.9f),
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color(0xFF1A3A5C),
                    modifier = Modifier.graphicsLayer {
                        rotationZ = if (isLoading) refreshRotation else 0f
                    },
                )
            }
        }
    }
}

private fun formatCigarettesDisplay(cigs: Double): String {
    if (cigs < 0.1) return "< 0.1"
    val s = String.format("%.1f", cigs)
    if (s == "0.0") return "< 0.1"
    return s
}

@Composable
private fun PollutantBadge(label: String) {
    AqiBadge(text = label, backgroundAlpha = 0.30f)
}

@Composable
private fun StatChip(emoji: String, value: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$emoji $value",
            fontSize = 11.sp,
            color = Color(0xFF1A3A5C),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun HeroCard(
    aqi: Int,
    aqiLabel: String,
    dominantPollutant: String,
    heroGradient: Brush,
    temp: Double,
    feelsLike: Double,
    weatherDescription: String,
    cigarettesRaw: Double,
    humidity: Int,
    windSpeed: Double,
    sunrise: Long,
    sunset: Long,
) {
    val desc = weatherDescription.trim().replaceFirstChar { ch ->
        if (ch.isLowerCase()) ch.titlecase() else ch.toString()
    }
    val cigarettes = formatCigarettesDisplay(cigarettesRaw)
    val cigaretteScroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(heroGradient)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AqiBadge(text = aqiLabel, backgroundAlpha = 0.30f)
                PollutantBadge(dominantPollutant)
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = aqi.toString(),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A3A5C),
                    )
                    Text(
                        text = "Air Quality Index",
                        fontSize = 11.sp,
                        color = Color(0xFF1A3A5C).copy(alpha = 0.6f),
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${temp.toInt()}°C",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A3A5C),
                    )
                    Text(
                        text = "feels ${feelsLike.toInt()}°C",
                        fontSize = 13.sp,
                        color = Color(0xFF1A3A5C).copy(alpha = 0.7f),
                    )
                    Text(
                        text = desc,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF1A3A5C).copy(alpha = 0.6f),
                        textAlign = TextAlign.End,
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🚬 = $cigarettes cigarettes today just from breathing outside",
                    fontSize = 12.sp,
                    color = Color(0xFF1A3A5C),
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    softWrap = false,
                    modifier = Modifier.horizontalScroll(cigaretteScroll),
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatChip("💧", "$humidity%", Modifier.weight(1f))
                StatChip("💨", "${windSpeed.toInt()} km/h", Modifier.weight(1f))
                StatChip("🌅", formatTime(sunrise), Modifier.weight(1f))
                StatChip("🌇", formatTime(sunset), Modifier.weight(1f))
            }
        }
    }
}

private fun formatTime(unixSeconds: Long): String = formatUnixSecondsToHHmm(unixSeconds)

private fun hourlyForecastTitle(language: String): String {
    return when (language) {
        "ky" -> "Бүгүнкү болжол"
        "ru" -> "Прогноз на сегодня"
        "uz" -> "Bugungi ob-havo"
        else -> "Today's Forecast"
    }
}

@Composable
private fun HourlyForecastStrip(
    language: String,
    hourly: List<OwmHourly>,
) {
    val hourlyItems = hourly.take(24)
    if (hourlyItems.isEmpty()) return

    var timeTick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            timeTick++
        }
    }

    val bishkekTz = remember { TimeZone.getTimeZone("Asia/Bishkek") }
    val sdf = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
            timeZone = bishkekTz
        }
    }

    val currentHour = remember(timeTick) {
        Calendar.getInstance(bishkekTz).get(Calendar.HOUR_OF_DAY)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = hourlyForecastTitle(language),
            color = Color(0xFF1A3A5C),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(hourlyItems) { _, hour ->
                val iconCode = hour.weather.firstOrNull()?.icon
                val emoji = hourlyWeatherEmoji(iconCode)
                val rainText = if (hour.pop > 0.1) "💧${(hour.pop * 100).toInt()}%" else ""
                val timeStr = sdf.format(Date(hour.dt * 1000L))
                val slotCal = Calendar.getInstance(bishkekTz).apply {
                    timeInMillis = hour.dt * 1000L
                }
                val slotHour = slotCal.get(Calendar.HOUR_OF_DAY)
                val isHighlight = slotHour == currentHour
                Column(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = if (isHighlight) 1.05f else 1f
                            scaleY = if (isHighlight) 1.05f else 1f
                        }
                        .width(60.dp)
                        .height(90.dp)
                        .background(
                            color = Color.White.copy(if (isHighlight) 0.75f else 0.55f),
                            shape = RoundedCornerShape(16.dp),
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF1A6BAA).copy(alpha = 0.25f),
                            shape = RoundedCornerShape(16.dp),
                        )
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = timeStr,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A3A5C),
                    )
                    Text(text = emoji, fontSize = 20.sp)
                    Text(
                        text = "${hour.temp.toInt()}°",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A3A5C),
                    )
                    Text(
                        text = rainText,
                        fontSize = 10.sp,
                        color = Color(0xFF1A3A5C).copy(alpha = 0.85f),
                        modifier = Modifier.height(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EightDayForecastSection(
    dailyForecast: List<com.tutun.bishkek.data.model.OwmDaily>,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeading(title = "8-Day Forecast")
        Spacer(Modifier.height(8.dp))
        ForecastStrip(dailyForecast = dailyForecast)
    }
}

@Composable
private fun AiSummaryCard(aiSummary: String) {
    FrosteredSectionCard {
        Text(
            text = "🤖 Weather Summary",
            color = Color(0xFF1A3A5C).copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = aiSummary,
            color = Color(0xFF1A3A5C),
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic,
        )
    }
}

private sealed class BottomSheetContent {
    data class PollutantSheet(val pollutantKey: String) : BottomSheetContent()
    data class DistrictSheet(val district: District) : BottomSheetContent()
    data object AirBillSheet : BottomSheetContent()
}

@Composable
private fun ErrorStateCard(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "😔", fontSize = 38.sp)
            Spacer(Modifier.height(10.dp))
            Text(text = error, fontSize = 14.sp, color = Color(0xFF1A3A5C))
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onRetry) {
                Text(text = "Try again", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    showInfo: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A3A5C),
            modifier = Modifier.weight(1f),
        )
        if (showInfo) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF1A3A5C).copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun AqiBadge(text: String?, backgroundAlpha: Float) {
    val safe = text ?: "-"
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = backgroundAlpha), shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            text = safe,
            color = Color(0xFF1A3A5C),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun dominantPollutantLabel(dominant: String?): String {
    return when (dominant) {
        "pm25" -> "PM2.5"
        "pm10" -> "PM10"
        "so2" -> "SO2"
        "no2" -> "NO2"
        "co" -> "CO"
        "o3" -> "O3"
        else -> dominant?.uppercase() ?: "-"
    }
}

@Composable
private fun FrosteredSectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.55f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun PollutantChipsRow(
    language: String,
    homeData: HomeData,
    onPollutantClick: (BottomSheetContent) -> Unit,
) {
    val iaqi = homeData.aqiData?.iaqi
    val pollutants = listOf(
        PollutantChip("pm25", "PM2.5", iaqi?.pm25?.v),
        PollutantChip("pm10", "PM10", iaqi?.pm10?.v),
        PollutantChip("no2", "NO2", iaqi?.no2?.v),
        PollutantChip("so2", "SO2", iaqi?.so2?.v),
        PollutantChip("co", "CO", iaqi?.co?.v),
        PollutantChip("o3", "O3", iaqi?.o3?.v),
    ).filter { it.value != null }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        pollutants.forEach { pollutant ->
            val colorInfo = pollutantColorAndLevel(pollutant.key, pollutant.value!!)
            Card(
                modifier = Modifier
                    .width(80.dp)
                    .height(72.dp)
                    .clickable { onPollutantClick(BottomSheetContent.PollutantSheet(pollutantKey = pollutant.key)) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.50f)),
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = pollutant.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A5C))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = String.format("%.1f", pollutant.value!!),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorInfo.color,
                    )
                    Text(
                        text = pollutantUnit(pollutant.key),
                        fontSize = 10.sp,
                        color = Color(0xFF1A3A5C).copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

data class PollutantChip(
    val key: String,
    val label: String,
    val value: Double?,
)

private fun pollutantUnit(key: String): String = when (key) {
    "co" -> "ppm"
    else -> "µg/m³"
}

private data class PollutantColorInfo(val color: Color, val levelLabel: String)

private fun pollutantColorAndLevel(key: String, value: Double): PollutantColorInfo {
    return when (key) {
        "pm25" -> when {
            value < 12 -> PollutantColorInfo(Color(0xFF16A34A), "Good")
            value <= 35 -> PollutantColorInfo(Color(0xFFD97706), "Moderate")
            value <= 55 -> PollutantColorInfo(Color(0xFFF59E0B), "Unhealthy")
            else -> PollutantColorInfo(Color(0xFFDC2626), "Hazardous")
        }

        "pm10" -> when {
            value < 54 -> PollutantColorInfo(Color(0xFF16A34A), "Good")
            value <= 154 -> PollutantColorInfo(Color(0xFFD97706), "Moderate")
            value <= 254 -> PollutantColorInfo(Color(0xFFF59E0B), "Unhealthy")
            else -> PollutantColorInfo(Color(0xFFDC2626), "Hazardous")
        }

        "no2" -> when {
            value < 53 -> PollutantColorInfo(Color(0xFF16A34A), "Good")
            value <= 100 -> PollutantColorInfo(Color(0xFFD97706), "Moderate")
            value <= 360 -> PollutantColorInfo(Color(0xFFF59E0B), "Unhealthy")
            else -> PollutantColorInfo(Color(0xFFDC2626), "Hazardous")
        }

        else -> when {
            value < 50 -> PollutantColorInfo(Color(0xFF16A34A), "Good")
            value < 100 -> PollutantColorInfo(Color(0xFFD97706), "Moderate")
            value < 200 -> PollutantColorInfo(Color(0xFFF59E0B), "Unhealthy")
            else -> PollutantColorInfo(Color(0xFFDC2626), "Hazardous")
        }
    }
}

@Composable
private fun ForecastStrip(
    dailyForecast: List<com.tutun.bishkek.data.model.OwmDaily>,
) {
    val days = dailyForecast.take(8)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        days.forEachIndexed { index, daily ->
            val bgAlpha = if (index == 0) 0.70f else 0.55f
            val iconCode = daily.weather.firstOrNull()?.icon
            val emoji = iconEmoji(iconCode)
            val rainPopText = if (daily.pop > 0.2) "💧${(daily.pop * 100).toInt()}%" else ""
            val dayName = formatUnixSecondsToDayName(daily.dt)

            Card(
                modifier = Modifier
                    .width(72.dp)
                    .height(104.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = bgAlpha)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = dayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A5C))
                        Spacer(Modifier.height(4.dp))
                        Text(text = emoji, fontSize = 24.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${daily.temp.max.toInt()}°",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A3A5C),
                        )
                        Text(
                            text = "${daily.temp.min.toInt()}°",
                            fontSize = 12.sp,
                            color = Color(0xFF1A3A5C).copy(alpha = 0.55f),
                        )
                    }

                    if (rainPopText.isNotEmpty()) {
                        Text(text = rainPopText, fontSize = 11.sp, color = Color(0xFF1A3A5C).copy(alpha = 0.75f))
                    } else {
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

private fun hourlyWeatherEmoji(iconCode: String?): String {
    return when (iconCode) {
        "01d" -> "☀️"
        "01n" -> "🌙"
        "02d" -> "⛅"
        "02n" -> "☁️"
        "03d" -> "🌤️"
        "03n" -> "☁️"
        "04d" -> "☁️"
        "04n" -> "☁️"
        "09d" -> "🌧️"
        "09n" -> "🌧️"
        "10d" -> "🌦️"
        "10n" -> "🌧️"
        "11d" -> "⛈️"
        "11n" -> "⛈️"
        "13d" -> "❄️"
        "13n" -> "❄️"
        "50d" -> "🌫️"
        "50n" -> "🌫️"
        else -> "🌡️"
    }
}

private fun iconEmoji(iconCode: String?): String = hourlyWeatherEmoji(iconCode)

private fun formatUnixSecondsToDayName(unixSeconds: Long): String {
    val date = Instant.ofEpochSecond(unixSeconds)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return date.dayOfWeek.name.take(3)
}

@Composable
private fun DistrictsStrip(
    language: String,
    cityAqi: Int,
    onDistrictClick: (District) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BISHKEK_DISTRICTS.forEach { district ->
            val districtName = when (language) {
                "ky" -> district.nameKy
                "ru" -> district.nameRu
                "uz" -> district.nameUz
                else -> district.nameEn
            }
            val estimatedAqi = (cityAqi * district.aqiMultiplier).toInt()
            val dotColor = aqiColor(estimatedAqi)
            Card(
                modifier = Modifier
                    .height(44.dp)
                    .wrapContentWidth()
                    .clickable { onDistrictClick(district) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.55f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(dotColor, shape = RoundedCornerShape(5.dp)),
                    )
                    Text(
                        text = districtName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A3A5C),
                    )
                }
            }
        }
    }
}

private fun aqiColor(aqi: Int): Color {
    return when {
        aqi <= 50 -> Color(0xFF16A34A)
        aqi <= 100 -> Color(0xFFD97706)
        aqi <= 150 -> Color(0xFFF59E0B)
        else -> Color(0xFFDC2626)
    }
}

private data class SourceBar(val name: String, val fraction: Double, val color: Color)

@Composable
private fun PollutionSourcesCard() {
    val sources = listOf(
        SourceBar("🏠 Residential heating (coal)", 0.60, Color(0xFFEF5350)),
        SourceBar("🚗 Vehicle emissions", 0.20, Color(0xFFFF7043)),
        SourceBar("🏭 Industry & power plant", 0.10, Color(0xFFFFCA28)),
        SourceBar("🌬️ Other sources", 0.10, Color(0xFF66BB6A)),
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.55f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "What's polluting Bishkek?",
                color = Color(0xFF1A3A5C),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            sources.forEach { source ->
                AnimatedSourceBarRow(source = source)
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = "Source: World Bank Bishkek Air Quality Report 2024",
                color = Color(0xFF1A3A5C).copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
            )
        }
    }
}

@Composable
private fun AnimatedSourceBarRow(source: SourceBar) {
    val target = (source.fraction * 100).toFloat()
    val anim = remember { Animatable(0f) }
    LaunchedEffect(source.name) {
        anim.animateTo(target, tween(durationMillis = 1200, easing = LinearEasing))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = source.name,
            fontSize = 13.sp,
            color = Color(0xFF1A3A5C),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Box(
            modifier = Modifier
                .weight(1.2f)
                .height(12.dp)
                .background(Color(0xFF1A3A5C).copy(alpha = 0.08f), shape = RoundedCornerShape(6.dp)),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val fillW = w * (anim.value / 100f)
                drawRoundRect(
                    color = source.color,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(fillW, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                )
            }
        }

        Text(
            text = "${target.toInt()}%",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A3A5C),
            modifier = Modifier.wrapContentWidth(),
        )
    }
}

private fun formatKgsShort(value: Double): String {
    val n = kotlin.math.round(value).toInt()
    return when {
        n >= 1000 -> "${n / 1000}k"
        else -> "$n"
    }
}

@Composable
private fun AirBillTeaserCard(
    dailyTotal: Double,
    healthcareCost: Double,
    productivityCost: Double,
    preventiveCost: Double,
    onDetails: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "💸 Today's Air Cost",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706),
                )
                Text(
                    text = "Details →",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706),
                    modifier = Modifier.clickable { onDetails() },
                )
            }

            Text(
                text = "~${dailyTotal.toInt()} KGS",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD97706),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "🏥 ${formatKgsShort(healthcareCost)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A5C),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                Text(
                    text = "⚡ ${formatKgsShort(productivityCost)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A5C),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                Text(
                    text = "🛡️ ${formatKgsShort(preventiveCost)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A5C),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun CarouselSection(context: android.content.Context) {
    val cards = remember {
        listOf(
            CarouselCard(
                id = 1,
                type = "youtube",
                title = "Why is Bishkek so polluted in winter?",
                description = "A doctor explains what PM2.5 does to your lungs",
                sourceBadge = "YouTube",
                linkUrl = "https://youtube.com/results?search_query=bishkek+air+pollution",
                bgColorHex = "#1A3A5C",
            ),
            CarouselCard(
                id = 2,
                type = "news",
                title = "World Bank commits $50M to clean Bishkek air",
                description = "New project targets coal-heated homes",
                sourceBadge = "News",
                linkUrl = "https://www.worldbank.org/en/country/kyrgyzrepublic",
                bgColorHex = "#0D9488",
            ),
            CarouselCard(
                id = 3,
                type = "instagram",
                title = "Bishkek smog captured by locals",
                description = "A photo series showing visibility on bad air days",
                sourceBadge = "Instagram",
                linkUrl = "https://www.instagram.com/explore/tags/bishkeksmog/",
                bgColorHex = "#6366F1",
            ),
            CarouselCard(
                id = 4,
                type = "app",
                title = "IQAir — Track air worldwide",
                description = "Compare Bishkek to any city in the world",
                sourceBadge = "App",
                linkUrl = "https://play.google.com/store/apps/details?id=air.visual.airvisual",
                bgColorHex = "#16A34A",
            ),
            CarouselCard(
                id = 5,
                type = "news",
                title = "UNICEF: Bishkek children face highest air risk",
                description = "Children in north Bishkek exposed to 4x safe limits",
                sourceBadge = "Research",
                linkUrl = "https://www.unicef.org/kyrgyzstan/",
                bgColorHex = "#D97706",
            ),
        )
    }

    val pagerState = rememberPagerState(pageCount = { cards.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(5_000)
            val next = (pagerState.currentPage + 1) % cards.size
            pagerState.animateScrollToPage(next)
        }
    }

    val title = "Bishkek Air & Environment"

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = Color(0xFF1A3A5C),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(10.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
        ) { page ->
            CarouselCardItem(card = cards[page], context = context)
        }

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                cards.forEachIndexed { index, _ ->
                    val isActive = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (isActive) {
                                    Color(0xFF1A6BAA)
                                } else {
                                    Color(0xFF1A6BAA).copy(alpha = 0.3f)
                                },
                                shape = RoundedCornerShape(4.dp),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun CarouselCardItem(card: CarouselCard, context: android.content.Context) {
    val bg = Color(android.graphics.Color.parseColor(card.bgColorHex))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = card.sourceBadge,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A3A5C),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = card.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = card.description,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(card.linkUrl))
                        context.startActivity(intent)
                    },
            )
        }
    }
}

@Composable
private fun BottomSheetBody(
    language: String,
    homeData: HomeData,
    viewModel: HomeViewModel,
    userStatus: String,
    airBillResult: AirBillCalculator.AirBillResult,
    content: BottomSheetContent,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Details",
                color = Color(0xFF1A3A5C),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onClose) {
                Text(text = "Close", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(10.dp))

        when (content) {
            is BottomSheetContent.PollutantSheet -> {
                val key = content.pollutantKey
                val iaqi = homeData.aqiData?.iaqi
                val value = when (key) {
                    "pm25" -> iaqi?.pm25?.v
                    "pm10" -> iaqi?.pm10?.v
                    "no2" -> iaqi?.no2?.v
                    "so2" -> iaqi?.so2?.v
                    "co" -> iaqi?.co?.v
                    "o3" -> iaqi?.o3?.v
                    else -> null
                } ?: 0.0

                val info = pollutantColorAndLevel(key, value)
                val fullName = dominantPollutantLabel(key)
                val sourceSentence = pollutantSourceSentence(key)

                Text(
                    text = fullName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A5C),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Current value: ${String.format("%.1f", value)} ${pollutantUnit(key)}",
                    fontSize = 14.sp,
                    color = Color(0xFF1A3A5C).copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Level: ${info.levelLabel}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = info.color,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = sourceSentence,
                    fontSize = 13.sp,
                    color = Color(0xFF1A3A5C).copy(alpha = 0.75f),
                    fontStyle = FontStyle.Italic,
                )
            }

            is BottomSheetContent.DistrictSheet -> {
                val cityAqi = homeData.aqiData?.aqi ?: 0
                val district = content.district
                val estimatedAqi = (cityAqi * district.aqiMultiplier).toInt()
                val heatingColor = when (district.heatingZoneLabel) {
                    "Coal zone" -> Color(0xFFD97706)
                    "Average" -> Color(0xFFF59E0B)
                    else -> Color(0xFF16A34A)
                }

                val districtName = when (language) {
                    "ky" -> district.nameKy
                    "ru" -> district.nameRu
                    "uz" -> district.nameUz
                    else -> district.nameEn
                }

                Text(
                    text = districtName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A5C),
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.75f), shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = district.heatingZoneLabel,
                        color = heatingColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Estimated AQI: $estimatedAqi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A5C),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = districtExplanationSentence(district),
                    fontSize = 13.sp,
                    color = Color(0xFF1A3A5C).copy(alpha = 0.75f),
                    fontStyle = FontStyle.Italic,
                )
            }

            BottomSheetContent.AirBillSheet -> {
                Text(
                    text = "Today's Air Cost Breakdown",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A5C),
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Today: ~${airBillResult.totalDaily.toInt()} KGS   •   Month: ~${airBillResult.totalMonthly.toInt()} KGS   •   Year: ~${airBillResult.totalYearly.toInt()} KGS",
                    fontSize = 13.sp,
                    color = Color(0xFF1A3A5C).copy(alpha = 0.85f),
                )

                Spacer(Modifier.height(14.dp))

                var expanded by remember { mutableStateOf(setOf<String>()) }

                receiptLine(
                    key = "Healthcare",
                    title = "🏥 Healthcare",
                    value = airBillResult.healthcareCost.toInt().toString(),
                    description = "Covers expected doctor visits and medication needs related to elevated PM2.5 exposure.",
                    expandedKeys = expanded,
                    onToggle = { newSet -> expanded = newSet },
                )

                receiptLine(
                    key = "Productivity",
                    title = "⚡ Productivity",
                    value = airBillResult.productivityCost.toInt().toString(),
                    description = "Estimates lost daily productivity due to reduced health performance at higher particulate levels.",
                    expandedKeys = expanded,
                    onToggle = { newSet -> expanded = newSet },
                )

                receiptLine(
                    key = "Prevention",
                    title = "🛡️ Prevention",
                    value = airBillResult.preventiveCost.toInt().toString(),
                    description = "Represents protective actions (masks) plus extra burden in higher heating-risk zones.",
                    expandedKeys = expanded,
                    onToggle = { newSet -> expanded = newSet },
                )

                Spacer(Modifier.height(14.dp))
                Text(
                    text = "What this means",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A5C),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "When the air gets worse, health impacts and everyday costs rise together. This breakdown helps compare how pollution translates into real, daily trade-offs in Bishkek.",
                    fontSize = 13.sp,
                    color = Color(0xFF1A3A5C).copy(alpha = 0.75f),
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}

@Composable
private fun receiptLine(
    key: String,
    title: String,
    value: String,
    description: String,
    expandedKeys: Set<String>,
    onToggle: (Set<String>) -> Unit,
) {
    val isExpanded = expandedKeys.contains(key)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A5C))
                Text(text = "${value} KGS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A5C))
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF1A3A5C).copy(alpha = 0.75f),
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (isExpanded) "Tap to hide" else "Tap to expand",
                fontSize = 12.sp,
                color = Color(0xFF1A3A5C).copy(alpha = 0.55f),
                modifier = Modifier.clickable {
                    val newSet = if (isExpanded) expandedKeys - key else expandedKeys + key
                    onToggle(newSet)
                },
            )
        }
    }
}

private fun pollutantSourceSentence(key: String): String {
    return when (key) {
        "pm25" -> "Mainly from coal heating stoves in residential areas."
        "pm10" -> "Dust from roads and construction sites."
        "no2" -> "Primarily from vehicle exhaust in Bishkek traffic."
        "so2" -> "From coal burning and the Bishkek thermal power plant."
        "co" -> "From incomplete combustion in vehicles and stoves."
        "o3" -> "Forms in sunlight from vehicle and industrial emissions."
        else -> "From multiple sources across Bishkek."
    }
}

private fun districtExplanationSentence(district: District): String {
    return when (district.heatingZoneLabel) {
        "Coal zone" -> "Air quality is worse here during heating season due to coal combustion emissions and colder stagnant conditions."
        "Average" -> "Pollution levels are moderate, influenced by both traffic and dispersed residential heating sources."
        else -> "Lower heating-risk conditions help reduce emissions, so AQI stays closer to the safer range."
    }
}

private fun formatUnixSecondsToHHmm(unixSeconds: Long): String {
    if (unixSeconds <= 0L) return "-"
    val time = Instant.ofEpochSecond(unixSeconds)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return time.format(DateTimeFormatter.ofPattern("HH:mm"))
}

@Composable
private fun ShimmerCarousel() {
    val shimmerAlpha = rememberShimmerAlpha()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(160.dp)
            .background(Color(0xFF1A3A5C).copy(alpha = shimmerAlpha), shape = RoundedCornerShape(20.dp)),
    )
}

@Composable
private fun ShimmerHero() {
    val shimmerAlpha = rememberShimmerAlpha()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(220.dp)
            .background(Color(0xFF1A3A5C).copy(alpha = shimmerAlpha), shape = RoundedCornerShape(24.dp)),
    )
}

@Composable
private fun ShimmerHourlyStrip() {
    val shimmerAlpha = rememberShimmerAlpha()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(90.dp)
                    .background(Color(0xFF1A3A5C).copy(alpha = shimmerAlpha), RoundedCornerShape(16.dp)),
            )
        }
    }
}

@Composable
private fun ShimmerCardRow() {
    val shimmerAlpha = rememberShimmerAlpha()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.weight(1f).height(104.dp).background(Color(0xFF1A3A5C).copy(alpha = shimmerAlpha), RoundedCornerShape(12.dp)))
        Box(modifier = Modifier.weight(1f).height(104.dp).background(Color(0xFF1A3A5C).copy(alpha = shimmerAlpha), RoundedCornerShape(12.dp)))
    }
}

@Composable
private fun ShimmerSectionTitle() {
    val shimmerAlpha = rememberShimmerAlpha()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(20.dp)
            .background(Color(0xFF1A3A5C).copy(alpha = shimmerAlpha), RoundedCornerShape(10.dp)),
    )
}

@Composable
private fun ShimmerPollutantChips() {
    val shimmerAlpha = rememberShimmerAlpha()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(6) {
            Box(modifier = Modifier.width(80.dp).height(72.dp).background(Color(0xFF1A3A5C).copy(alpha = shimmerAlpha), RoundedCornerShape(20.dp)))
        }
    }
}

@Composable
private fun rememberShimmerAlpha(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmerAlpha")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
        ),
        label = "shimmerAlphaAnim",
    )
    return alpha
}
