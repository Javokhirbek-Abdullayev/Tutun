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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tutun.bishkek.data.model.AirBillCalculator
import com.tutun.bishkek.data.model.BISHKEK_DISTRICTS
import com.tutun.bishkek.data.model.CarouselCard
import com.tutun.bishkek.data.model.District
import com.tutun.bishkek.data.model.HomeData
import com.tutun.bishkek.data.model.OwmHourly
import com.tutun.bishkek.viewmodel.HomeViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private fun tr(language: String, ky: String, ru: String, uz: String, en: String): String {
    return when (language) {
        "ky" -> ky
        "ru" -> ru
        "uz" -> uz
        else -> en
    }
}

private data class RemoteCarouselItem(
    val id: String,
    val badge: String,
    val title: Map<String, String>,
    val subtitle: Map<String, String>,
    val image: String,
    val link: String,
)

private fun pickLang(map: Map<String, String>, language: String): String {
    return map[language] ?: map["en"] ?: map.values.firstOrNull().orEmpty()
}

private fun localizedWeatherDescription(language: String, rawDescription: String): String {
    val v = rawDescription.trim().lowercase(Locale.ROOT)
    return when (language) {
        "ky" -> when {
            v.contains("overcast") -> "Толук булут"
            v.contains("clear") -> "Ачык асман"
            v.contains("few clouds") || v.contains("few") -> "Аз булуттуу"
            v.contains("scattered") -> "Чачыраган булут"
            v.contains("broken") -> "Сынган булут"
            v.contains("shower") || v.contains("rain") -> "Жаан"
            v.contains("thunderstorm") -> "Найзағай"
            v.contains("snow") -> "Кар"
            v.contains("mist") || v.contains("fog") -> "Туман"
            v.contains("haze") -> "Түтүнчө"
            else -> rawDescription
        }
        "ru" -> when {
            v.contains("overcast") -> "Пасмурно"
            v.contains("clear") -> "Ясно"
            v.contains("few clouds") || v.contains("few") -> "Небольшая облачность"
            v.contains("scattered") -> "Рассеянные облака"
            v.contains("broken") -> "Облачно"
            v.contains("shower") || v.contains("rain") -> "Дождь"
            v.contains("thunderstorm") -> "Гроза"
            v.contains("snow") -> "Снег"
            v.contains("mist") || v.contains("fog") -> "Туман"
            v.contains("haze") -> "Дымка"
            else -> rawDescription
        }
        "uz" -> when {
            v.contains("overcast") -> "Bulutli"
            v.contains("clear") -> "Ochiq osmon"
            v.contains("few clouds") || v.contains("few") -> "Kam bulut"
            v.contains("scattered") -> "Tarqoq bulut"
            v.contains("broken") -> "Ayrim bulutlar"
            v.contains("shower") || v.contains("rain") -> "Yomg'ir"
            v.contains("thunderstorm") -> "Momaqaldiroq"
            v.contains("snow") -> "Qor"
            v.contains("mist") || v.contains("fog") -> "Tuman"
            v.contains("haze") -> "Tuman"
            else -> rawDescription
        }
        else -> rawDescription
    }
}

private fun dayShort(language: String, dayOfWeek: java.time.DayOfWeek): String {
    return when (language) {
        "ky" -> when (dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "Дш"
            java.time.DayOfWeek.TUESDAY -> "Сш"
            java.time.DayOfWeek.WEDNESDAY -> "Шш"
            java.time.DayOfWeek.THURSDAY -> "Бш"
            java.time.DayOfWeek.FRIDAY -> "Жу"
            java.time.DayOfWeek.SATURDAY -> "Ша"
            java.time.DayOfWeek.SUNDAY -> "Же"
        }
        "ru" -> when (dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "Пн"
            java.time.DayOfWeek.TUESDAY -> "Вт"
            java.time.DayOfWeek.WEDNESDAY -> "Ср"
            java.time.DayOfWeek.THURSDAY -> "Чт"
            java.time.DayOfWeek.FRIDAY -> "Пт"
            java.time.DayOfWeek.SATURDAY -> "Сб"
            java.time.DayOfWeek.SUNDAY -> "Вс"
        }
        "uz" -> when (dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "Dsh"
            java.time.DayOfWeek.TUESDAY -> "Ses"
            java.time.DayOfWeek.WEDNESDAY -> "Cho"
            java.time.DayOfWeek.THURSDAY -> "Pay"
            java.time.DayOfWeek.FRIDAY -> "Jum"
            java.time.DayOfWeek.SATURDAY -> "Sha"
            java.time.DayOfWeek.SUNDAY -> "Yak"
        }
        else -> when (dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "MON"
            java.time.DayOfWeek.TUESDAY -> "TUE"
            java.time.DayOfWeek.WEDNESDAY -> "WED"
            java.time.DayOfWeek.THURSDAY -> "THU"
            java.time.DayOfWeek.FRIDAY -> "FRI"
            java.time.DayOfWeek.SATURDAY -> "SAT"
            java.time.DayOfWeek.SUNDAY -> "SUN"
        }
    }
}

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
                        language = language,
                        error = homeData.error ?: "",
                        onRetry = { viewModel.fetchAllData() },
                    )
                }
            } else {
                item {
                    CarouselSection(language = language, context = context)
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
                        language = language,
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
                    EightDayForecastSection(language = language, dailyForecast = dailyForecast)
                }
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    SectionHeading(
                        title = tr(
                            language,
                            ky = "Аба булганычтары",
                            ru = "Загрязнители воздуха",
                            uz = "Havo ifloslantiruvchilar",
                            en = "Air Pollutants",
                        ),
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
                    SectionHeading(
                        title = tr(
                            language,
                            ky = "Бишкектин райондору",
                            ru = "Районы Бишкека",
                            uz = "Bishkek tumanlari",
                            en = "Bishkek Districts",
                        )
                    )
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
                        AiSummaryCard(language = language, aiSummary = aiSummary)
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
                item {
                    PollutionSourcesCard(language = language)
                }
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    AirBillTeaserCard(
                        language = language,
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
    language: String,
) {
    val desc = localizedWeatherDescription(language = language, rawDescription = weatherDescription)
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
                        text = tr(
                            language,
                            ky = "Аба сапатынын индекси",
                            ru = "Индекс качества воздуха",
                            uz = "Havo sifati indeksi",
                            en = "Air Quality Index",
                        ),
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
                        text = tr(
                            language,
                            ky = "сезилет ${feelsLike.toInt()}°C",
                            ru = "ощущается ${feelsLike.toInt()}°C",
                            uz = "seziladi ${feelsLike.toInt()}°C",
                            en = "feels ${feelsLike.toInt()}°C",
                        ),
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
                    text = tr(
                        language,
                        ky = "🚬 = $cigarettes бүгүн сыртта дем алган гана үчүн",
                        ru = "🚬 = $cigarettes сигарет сегодня только от дыхания на улице",
                        uz = "🚬 = $cigarettes bugun faqat tashqarida nafas olishdan",
                        en = "🚬 = $cigarettes cigarettes today just from breathing outside",
                    ),
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
    language: String,
    dailyForecast: List<com.tutun.bishkek.data.model.OwmDaily>,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeading(
            title = tr(
                language,
                ky = "8 күндүк болжол",
                ru = "Прогноз на 8 дней",
                uz = "8 kunlik prognoz",
                en = "8-Day Forecast",
            )
        )
        Spacer(Modifier.height(8.dp))
        ForecastStrip(language = language, dailyForecast = dailyForecast)
    }
}

@Composable
private fun AiSummaryCard(language: String, aiSummary: String) {
    FrosteredSectionCard {
        Text(
            text = tr(
                language,
                ky = "🤖 Аба ырайынын жыйынтыгы",
                ru = "🤖 Сводка погоды",
                uz = "🤖 Ob-havo xulosasi",
                en = "🤖 Weather Summary",
            ),
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
private fun ErrorStateCard(language: String, error: String, onRetry: () -> Unit) {
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
                Text(
                    text = tr(
                        language,
                        ky = "Кайра аракет кылуу",
                        ru = "Повторить",
                        uz = "Qayta urinib ko‘ring",
                        en = "Try again",
                    ),
                    fontWeight = FontWeight.Bold
                )
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
    language: String,
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
            val dayName = formatUnixSecondsToDayName(unixSeconds = daily.dt, language = language)

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

private fun formatUnixSecondsToDayName(unixSeconds: Long, language: String): String {
    val date = Instant.ofEpochSecond(unixSeconds)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return dayShort(language = language, dayOfWeek = date.dayOfWeek)
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
private fun PollutionSourcesCard(language: String) {
    val sources = listOf(
        SourceBar(
            tr(
                language,
                ky = "🏠 Үй жылытуу (көмүр)",
                ru = "🏠 Отопление домов (уголь)",
                uz = "🏠 Uy isitish (ko‘mir)",
                en = "🏠 Residential heating (coal)",
            ),
            0.60,
            Color(0xFFEF5350)
        ),
        SourceBar(
            tr(
                language,
                ky = "🚗 Унаалардын чыгындылары",
                ru = "🚗 Выбросы транспорта",
                uz = "🚗 Avtomobil chiqindilari",
                en = "🚗 Vehicle emissions",
            ),
            0.20,
            Color(0xFFFF7043)
        ),
        SourceBar(
            tr(
                language,
                ky = "🏭 Өндүрүш жана ЖЭБ",
                ru = "🏭 Промышленность и ТЭЦ",
                uz = "🏭 Sanoat va IES",
                en = "🏭 Industry & power plant",
            ),
            0.10,
            Color(0xFFFFCA28)
        ),
        SourceBar(
            tr(
                language,
                ky = "🌬️ Башка булактар",
                ru = "🌬️ Прочие булактар",
                uz = "🌬️ Boshqa manbalar",
                en = "🌬️ Other sources",
            ),
            0.10,
            Color(0xFF66BB6A)
        ),
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
                text = tr(
                    language,
                    ky = "Бишкекти эмне булгайт?",
                    ru = "Что загрязняет Бишкек?",
                    uz = "Bishkekni nima ifloslantiradi?",
                    en = "What's polluting Bishkek?",
                ),
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
                text = tr(
                    language,
                    ky = "Булак: Дүйнөлүк банк, Бишкек аба сапаты отчету 2024",
                    ru = "Источник: Всемирный банк, отчет по качеству воздуха Бишкека 2024",
                    uz = "Manba: Jahon banki, Bishkek havo sifati hisoboti 2024",
                    en = "Source: World Bank Bishkek Air Quality Report 2024",
                ),
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
    language: String,
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
                    text = tr(
                        language,
                        ky = "💸 Бүгүнкү аба чыгымы",
                        ru = "💸 Стоимость воздуха сегодня",
                        uz = "💸 Bugungi havo xarajati",
                        en = "💸 Today's Air Cost",
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706),
                )
                Text(
                    text = tr(
                        language,
                        ky = "Деталдар →",
                        ru = "Детали →",
                        uz = "Tafsilotlar →",
                        en = "Details →",
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706),
                    modifier = Modifier.clickable { onDetails() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
private fun CarouselSection(
    language: String,
    context: android.content.Context
) {
    val jsonUrl = "https://raw.githubusercontent.com/Javokhirbek-Abdullayev/tutun_content/main/carousel/v1.json"
    val rawBase = "https://raw.githubusercontent.com/Javokhirbek-Abdullayev/tutun_content/main/"

    var remoteItems by remember { mutableStateOf<List<RemoteCarouselItem>?>(null) }

    LaunchedEffect(jsonUrl) {
        try {
            val body = withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val req = Request.Builder().url(jsonUrl).build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) throw IllegalStateException("HTTP ${resp.code}")
                    resp.body?.string().orEmpty()
                }
            }
            val type = object : TypeToken<List<RemoteCarouselItem>>() {}.type
            remoteItems = Gson().fromJson<List<RemoteCarouselItem>>(body, type)
        } catch (_: Exception) {
            remoteItems = null
        }
    }

    val items = remoteItems ?: emptyList()
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(5_000)
            val next = (pagerState.currentPage + 1) % items.size
            pagerState.animateScrollToPage(next)
        }
    }

    val title = tr(
        language,
        ky = "Бишкек аба жана чөйрө",
        ru = "Воздух и среда Бишкека",
        uz = "Bishkek havo va muhit",
        en = "Bishkek Air & Environment",
    )

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
            CarouselCardItem(
                badge = items[page].badge,
                title = pickLang(items[page].title, language),
                subtitle = pickLang(items[page].subtitle, language),
                imageUrl = rawBase + items[page].image,
                linkUrl = items[page].link,
                context = context,
            )
        }

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEachIndexed { index, _ ->
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
private fun CarouselCardItem(
    badge: String,
    title: String,
    subtitle: String,
    imageUrl: String,
    linkUrl: String,
    context: android.content.Context,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A5C)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.05f),
                                Color.Black.copy(alpha = 0.55f),
                            )
                        )
                    )
            )
            Text(
                text = badge,
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
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl))
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
                text = tr(
                    language,
                    ky = "Деталдар",
                    ru = "Детали",
                    uz = "Tafsilotlar",
                    en = "Details",
                ),
                color = Color(0xFF1A3A5C),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onClose) {
                Text(
                    text = tr(
                        language,
                        ky = "Жабуу",
                        ru = "Закрыть",
                        uz = "Yopish",
                        en = "Close",
                    ),
                    fontWeight = FontWeight.Bold
                )
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
                val sourceSentence = pollutantSourceSentence(language, key)

                Text(
                    text = fullName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A5C),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = tr(
                        language,
                        ky = "Азыркы көрсөткүч: ${String.format("%.1f", value)} ${pollutantUnit(key)}",
                        ru = "Текущее значение: ${String.format("%.1f", value)} ${pollutantUnit(key)}",
                        uz = "Joriy qiymat: ${String.format("%.1f", value)} ${pollutantUnit(key)}",
                        en = "Current value: ${String.format("%.1f", value)} ${pollutantUnit(key)}",
                    ),
                    fontSize = 14.sp,
                    color = Color(0xFF1A3A5C).copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = tr(
                        language,
                        ky = "Деңгээли: ${info.levelLabel}",
                        ru = "Уровень: ${info.levelLabel}",
                        uz = "Daraja: ${info.levelLabel}",
                        en = "Level: ${info.levelLabel}",
                    ),
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
                    text = tr(
                        language,
                        ky = "Болжолдуу AQI: $estimatedAqi",
                        ru = "Оценочный AQI: $estimatedAqi",
                        uz = "Taxminiy AQI: $estimatedAqi",
                        en = "Estimated AQI: $estimatedAqi",
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A5C),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = districtExplanationSentence(language, district),
                    fontSize = 13.sp,
                    color = Color(0xFF1A3A5C).copy(alpha = 0.75f),
                    fontStyle = FontStyle.Italic,
                )
            }

            BottomSheetContent.AirBillSheet -> {
                Text(
                    text = tr(
                        language,
                        ky = "Бүгүнкү аба чыгымынын бөлүштүрүлүшү",
                        ru = "Разбивка стоимости воздуха за сегодня",
                        uz = "Bugungi havo xarajatlari tafsiloti",
                        en = "Today's Air Cost Breakdown",
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A5C),
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = tr(
                        language,
                        ky = "Бүгүн: ~${airBillResult.totalDaily.toInt()} KGS   •   Ай: ~${airBillResult.totalMonthly.toInt()} KGS   •   Жыл: ~${airBillResult.totalYearly.toInt()} KGS",
                        ru = "Сегодня: ~${airBillResult.totalDaily.toInt()} KGS   •   Месяц: ~${airBillResult.totalMonthly.toInt()} KGS   •   Год: ~${airBillResult.totalYearly.toInt()} KGS",
                        uz = "Bugun: ~${airBillResult.totalDaily.toInt()} KGS   •   Oy: ~${airBillResult.totalMonthly.toInt()} KGS   •   Yil: ~${airBillResult.totalYearly.toInt()} KGS",
                        en = "Today: ~${airBillResult.totalDaily.toInt()} KGS   •   Month: ~${airBillResult.totalMonthly.toInt()} KGS   •   Year: ~${airBillResult.totalYearly.toInt()} KGS",
                    ),
                    fontSize = 13.sp,
                    color = Color(0xFF1A3A5C).copy(alpha = 0.85f),
                )

                Spacer(Modifier.height(14.dp))

                var expanded by remember { mutableStateOf(setOf<String>()) }

                receiptLine(
                    language = language,
                    key = "Healthcare",
                    title = tr(language, ky = "🏥 Ден соолук", ru = "🏥 Здоровье", uz = "🏥 Sog‘liq", en = "🏥 Healthcare"),
                    value = airBillResult.healthcareCost.toInt().toString(),
                    description = tr(
                        language,
                        ky = "PM2.5 жогору болгондо дарыгерге баруу жана дары-дармек чыгымдарын эсептейт.",
                        ru = "Покрывает ожидаемые визиты к врачу и лекарства при повышенном воздействии PM2.5.",
                        uz = "PM2.5 yuqori bo‘lganda shifokor qabuliga borish va dori xarajatlarini baholaydi.",
                        en = "Covers expected doctor visits and medication needs related to elevated PM2.5 exposure.",
                    ),
                    expandedKeys = expanded,
                    onToggle = { newSet -> expanded = newSet },
                )

                receiptLine(
                    language = language,
                    key = "Productivity",
                    title = tr(language, ky = "⚡ Өндүрүмдүүлүк", ru = "⚡ Продуктивность", uz = "⚡ Samaradorlik", en = "⚡ Productivity"),
                    value = airBillResult.productivityCost.toInt().toString(),
                    description = tr(
                        language,
                        ky = "Бөлүкчөлөр көбөйгөндө ден соолукка таасир этип, күнүмдүк өндүрүмдүүлүк азайышын эсептейт.",
                        ru = "Оценивает потерю дневной продуктивности из‑за ухудшения самочувствия при высоких частицах.",
                        uz = "Zarralar ko‘payganda sog‘liq pasayishi sababli kunlik samaradorlik yo‘qotilishini baholaydi.",
                        en = "Estimates lost daily productivity due to reduced health performance at higher particulate levels.",
                    ),
                    expandedKeys = expanded,
                    onToggle = { newSet -> expanded = newSet },
                )

                receiptLine(
                    language = language,
                    key = "Prevention",
                    title = tr(language, ky = "🛡️ Алдын алуу", ru = "🛡️ Профилактика", uz = "🛡️ Oldini olish", en = "🛡️ Prevention"),
                    value = airBillResult.preventiveCost.toInt().toString(),
                    description = tr(
                        language,
                        ky = "Коргонуу чараларын (маска) жана көмүр зонасындагы кошумча жүктү камтыйт.",
                        ru = "Включает защитные меры (маски) и дополнительную нагрузку в зонах высокого риска отопления.",
                        uz = "Himoya choralari (niqob) va isitish xavfi yuqori hududlarda qo‘shimcha yukni hisobga oladi.",
                        en = "Represents protective actions (masks) plus extra burden in higher heating-risk zones.",
                    ),
                    expandedKeys = expanded,
                    onToggle = { newSet -> expanded = newSet },
                )

                Spacer(Modifier.height(14.dp))
                Text(
                    text = tr(
                        language,
                        ky = "Бул эмнени билдирет",
                        ru = "Что это значит",
                        uz = "Bu nimani anglatadi",
                        en = "What this means",
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A5C),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = tr(
                        language,
                        ky = "Аба начарлаганда, ден соолукка таасир жана күнүмдүк чыгымдар кошо өсөт. Бул бөлүштүрүү Бишкекте булгануу кандайча чыныгы күнүмдүк чыгымга айланарын көрсөтөт.",
                        ru = "Когда воздух становится хуже, растут и риски для здоровья, и повседневные расходы. Эта разбивка помогает понять, во что превращается загрязнение в Бишкеке каждый день.",
                        uz = "Havo yomonlashsa, sog‘liqka ta’sir ham, kundalik xarajatlar ham oshadi. Bu tafsilot Bishkekda ifloslanish qanday real kundalik xarajatlarga aylanishini ko‘rsatadi.",
                        en = "When the air gets worse, health impacts and everyday costs rise together. This breakdown helps compare how pollution translates into real, daily trade-offs in Bishkek.",
                    ),
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
    language: String,
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
                text = if (isExpanded) {
                    tr(language, ky = "Жашыруу үчүн тапта", ru = "Нажмите, чтобы скрыть", uz = "Yashirish uchun bosing", en = "Tap to hide")
                } else {
                    tr(language, ky = "Көрүү үчүн тапта", ru = "Нажмите, чтобы раскрыть", uz = "Ko‘rish uchun bosing", en = "Tap to expand")
                },
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

private fun pollutantSourceSentence(language: String, key: String): String {
    return when (key) {
        "pm25" -> tr(
            language,
            ky = "Негизинен турак жайлардагы көмүр мештеринен чыгат.",
            ru = "В основном из-за угольного отопления в жилых районах.",
            uz = "Asosan turar-joylarda ko‘mir yoqishdan kelib chiqadi.",
            en = "Mainly from coal heating stoves in residential areas.",
        )
        "pm10" -> tr(
            language,
            ky = "Жолдордон жана курулуштардан чыккан чаң.",
            ru = "Пыль с дорог и строительных площадок.",
            uz = "Yo‘llar va qurilishlardan chiqqan chang.",
            en = "Dust from roads and construction sites.",
        )
        "no2" -> tr(
            language,
            ky = "Көбүнчө Бишкектеги унаа түтүндөрүнөн.",
            ru = "В основном из выхлопов транспорта в Бишкеке.",
            uz = "Asosan Bishkekdagi avtomobil chiqindilaridan.",
            en = "Primarily from vehicle exhaust in Bishkek traffic.",
        )
        "so2" -> tr(
            language,
            ky = "Көмүр жагуудан жана Бишкек ЖЭБинен.",
            ru = "От сжигания угля и Бишкекской ТЭЦ.",
            uz = "Ko‘mir yoqish va Bishkek IESdan.",
            en = "From coal burning and the Bishkek thermal power plant.",
        )
        "co" -> tr(
            language,
            ky = "Унаалар жана мештердеги толук эмес күйүүдөн.",
            ru = "Из-за неполного сгорания в транспорте и печах.",
            uz = "Avtomobil va pechlarda to‘liq yonmaslikdan.",
            en = "From incomplete combustion in vehicles and stoves.",
        )
        "o3" -> tr(
            language,
            ky = "Күндүн нурунда унаа жана өнөр жай чыгындыларынан пайда болот.",
            ru = "Образуется на солнце из выбросов транспорта и промышленности.",
            uz = "Quyoshda transport va sanoat chiqindilaridan hosil bo‘ladi.",
            en = "Forms in sunlight from vehicle and industrial emissions.",
        )
        else -> tr(
            language,
            ky = "Бишкекте ар кандай булактардан чыгат.",
            ru = "Из нескольких источников по всему городу.",
            uz = "Shahardagi turli manbalardan.",
            en = "From multiple sources across Bishkek.",
        )
    }
}

private fun districtExplanationSentence(language: String, district: District): String {
    return when (district.heatingZoneLabel) {
        "Coal zone" -> tr(
            language,
            ky = "Жылытуу маалында көмүр жагуудан жана абанын токтоп калышынан аба сапаты бул жерде начарлайт.",
            ru = "В отопительный сезон качество воздуха здесь хуже из‑за угольных выбросов и застойных условий.",
            uz = "Isitish mavsumida ko‘mir yoqish va turg‘un havo sabab bu yerda havo sifati yomonroq bo‘ladi.",
            en = "Air quality is worse here during heating season due to coal combustion emissions and colder stagnant conditions.",
        )
        "Average" -> tr(
            language,
            ky = "Бул жерде булгануу орточо: жол кыймылы жана үй жылытуудан таасир этет.",
            ru = "Здесь умеренное загрязнение: влияет и транспорт, и отопление домов.",
            uz = "Bu yerda ifloslanish o‘rtacha: transport va uy isitishi ta’sir qiladi.",
            en = "Pollution levels are moderate, influenced by both traffic and dispersed residential heating sources.",
        )
        else -> tr(
            language,
            ky = "Жылытуу тобокелдиги төмөн болгондуктан чыгындылар азайып, AQI коопсузураакка жакын болот.",
            ru = "Из‑за более низкого риска отопления выбросов меньше, поэтому AQI ближе к безопасному уровню.",
            uz = "Isitish xavfi past bo‘lgani uchun chiqindilar kamroq, AQI xavfsizroq darajaga yaqin bo‘ladi.",
            en = "Lower heating-risk conditions help reduce emissions, so AQI stays closer to the safer range.",
        )
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
