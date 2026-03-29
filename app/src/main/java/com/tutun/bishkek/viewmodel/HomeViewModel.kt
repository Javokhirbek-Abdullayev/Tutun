package com.tutun.bishkek.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutun.bishkek.data.api.ApiClient
import com.tutun.bishkek.data.model.HomeData
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _homeData = MutableStateFlow(HomeData())
    val homeData: StateFlow<HomeData> = _homeData.asStateFlow()

    // Auto-refresh every 10 minutes
    private val REFRESH_INTERVAL_MS = 10 * 60 * 1000L

    init {
        fetchAllData()
        // Schedule periodic refresh
        viewModelScope.launch {
            while (true) {
                delay(REFRESH_INTERVAL_MS)
                fetchAllData()
            }
        }
    }

    fun fetchAllData() {
        viewModelScope.launch {
            _homeData.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch both APIs in parallel
                val aqiDeferred = async {
                    ApiClient.aqicnService.getBishkekAqi(ApiClient.AQICN_TOKEN)
                }
                val owmDeferred = async {
                    ApiClient.owmService.getWeather(
                        lat = 42.8746,
                        lon = 74.5698,
                        apiKey = ApiClient.OWM_KEY,
                    )
                }

                val aqiResult = aqiDeferred.await()
                val owmResult = owmDeferred.await()

                _homeData.update {
                    it.copy(
                        aqiData = if (aqiResult.status == "ok") aqiResult.data else null,
                        owmData = owmResult,
                        isLoading = false,
                        error = null,
                        lastUpdated = System.currentTimeMillis(),
                    )
                }
            } catch (_: Exception) {
                _homeData.update {
                    it.copy(
                        isLoading = false,
                        error = "Could not load data. Check your connection.",
                    )
                }
            }
        }
    }

    // Helper: get AQI level label
    fun getAqiLabel(aqi: Int, language: String): String {
        return when {
            aqi <= 50 -> when (language) {
                "ky" -> "Жакшы"
                "ru" -> "Хорошо"
                "uz" -> "Yaxshi"
                else -> "Good"
            }

            aqi <= 100 -> when (language) {
                "ky" -> "Орточо"
                "ru" -> "Умеренно"
                "uz" -> "O'rtacha"
                else -> "Moderate"
            }

            aqi <= 150 -> when (language) {
                "ky" -> "Начар"
                "ru" -> "Нездоровый"
                "uz" -> "Yomon"
                else -> "Unhealthy"
            }

            aqi <= 200 -> when (language) {
                "ky" -> "Абдан начар"
                "ru" -> "Очень нездоровый"
                "uz" -> "Juda yomon"
                else -> "Very Unhealthy"
            }

            else -> when (language) {
                "ky" -> "Коркунучтуу"
                "ru" -> "Опасно"
                "uz" -> "Xavfli"
                else -> "Hazardous"
            }
        }
    }

    // Helper: get hero card colors based on AQI
    fun getAqiColors(aqi: Int): Pair<Color, Color> {
        return when {
            aqi <= 50 -> Pair(Color(0xFF87CEEB), Color(0xFFE0F7FA))
            aqi <= 100 -> Pair(Color(0xFFFFF9C4), Color(0xFFFFF3E0))
            aqi <= 150 -> Pair(Color(0xFFFFE0B2), Color(0xFFFFCCBC))
            aqi <= 200 -> Pair(Color(0xFFFFCDD2), Color(0xFFEF9A9A))
            else -> Pair(Color(0xFFB0BEC5), Color(0xFF78909C))
        }
    }

    // Helper: format "last updated X minutes ago"
    fun getLastUpdatedText(lastUpdated: Long): String {
        if (lastUpdated == 0L) return ""
        val diffMinutes = (System.currentTimeMillis() - lastUpdated) / 1000 / 60
        return if (diffMinutes < 1) "Just updated" else "Updated ${diffMinutes}m ago"
    }
}


