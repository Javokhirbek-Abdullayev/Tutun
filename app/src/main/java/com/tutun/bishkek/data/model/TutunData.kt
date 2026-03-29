package com.tutun.bishkek.data.model

// From AQICN API
data class AqiResponse(
    val status: String,
    val data: AqiData
)

data class AqiData(
    val aqi: Int,
    val idx: Int,
    val time: AqiTime,
    val city: AqiCity,
    val iaqi: AqiIaqi,
    val dominentpol: String? = null
)

data class AqiTime(
    val s: String, // timestamp string
    val v: Long, // unix timestamp
)

data class AqiCity(
    val name: String,
    val geo: List<Double>,
)

data class AqiIaqi(
    val pm25: AqiValue? = null,
    val pm10: AqiValue? = null,
    val no2: AqiValue? = null,
    val so2: AqiValue? = null,
    val co: AqiValue? = null,
    val o3: AqiValue? = null,
    val t: AqiValue? = null, // temperature
    val h: AqiValue? = null, // humidity
    val w: AqiValue? = null, // wind
    val p: AqiValue? = null, // pressure
)

data class AqiValue(val v: Double)

// From OpenWeatherMap One Call 3.0
data class OwmResponse(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val current: OwmCurrent,
    val hourly: List<OwmHourly>,
    val daily: List<OwmDaily>,
    val alerts: List<OwmAlert>? = null,
)

data class OwmCurrent(
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val temp: Double,
    val feels_like: Double,
    val pressure: Int,
    val humidity: Int,
    val dew_point: Double,
    val uvi: Double,
    val clouds: Int,
    val visibility: Int,
    val wind_speed: Double,
    val wind_deg: Int,
    val wind_gust: Double? = null,
    val weather: List<OwmWeatherDesc>,
    val summary: String? = null,
)

data class OwmHourly(
    val dt: Long,
    val temp: Double,
    val feels_like: Double,
    val humidity: Int,
    val wind_speed: Double,
    val wind_deg: Int,
    val weather: List<OwmWeatherDesc>,
    val pop: Double, // probability of precipitation
    val uvi: Double,
)

data class OwmDaily(
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val moonrise: Long,
    val moonset: Long,
    val moon_phase: Double,
    val summary: String? = null,
    val temp: OwmDailyTemp,
    val feels_like: OwmDailyFeelsLike,
    val pressure: Int,
    val humidity: Int,
    val wind_speed: Double,
    val wind_deg: Int,
    val weather: List<OwmWeatherDesc>,
    val clouds: Int,
    val pop: Double,
    val rain: Double? = null,
    val snow: Double? = null,
    val uvi: Double,
)

data class OwmDailyTemp(
    val morn: Double,
    val day: Double,
    val eve: Double,
    val night: Double,
    val min: Double,
    val max: Double,
)

data class OwmDailyFeelsLike(
    val morn: Double,
    val day: Double,
    val eve: Double,
    val night: Double,
)

data class OwmWeatherDesc(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String,
)

data class OwmAlert(
    val sender_name: String,
    val event: String,
    val start: Long,
    val end: Long,
    val description: String,
)

// Combined state for the Home screen
data class HomeData(
    val aqiData: AqiData? = null,
    val owmData: OwmResponse? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val lastUpdated: Long = 0L,
)

// Carousel card model
data class CarouselCard(
    val id: Int,
    val type: String, // "youtube", "instagram", "news", "app"
    val title: String,
    val description: String,
    val sourceBadge: String,
    val linkUrl: String,
    val bgColorHex: String,
)

