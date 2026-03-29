package com.tutun.bishkek.data.model

object AirBillCalculator {

    // Bishkek cost constants in KGS
    private const val DOCTOR_VISIT_KGS = 280.0
    private const val MEDICATION_KGS = 180.0
    private const val MASK_DAILY_KGS = 120.0
    private const val COAL_PENALTY_KGS = 15.0

    // Daily income values in KGS
    private const val STUDENT_DAILY = 833.0
    private const val LOW_INCOME_DAILY = 500.0
    private const val MEDIUM_INCOME_DAILY = 1333.0
    private const val HIGH_INCOME_DAILY = 3000.0

    data class AirBillResult(
        val healthcareCost: Double,
        val productivityCost: Double,
        val preventiveCost: Double,
        val totalDaily: Double,
        val totalMonthly: Double,
        val totalYearly: Double,
    )

    private fun isStudent(status: String): Boolean =
        status.contains("Student", ignoreCase = true) ||
            status.contains("Студент", ignoreCase = true) ||
            status.contains("Окуучу", ignoreCase = true) ||
            status.contains("Talaba", ignoreCase = true) ||
            status.contains("O'quvchi", ignoreCase = true)

    private fun isEmployed(status: String): Boolean =
        status.contains("Employed", ignoreCase = true) ||
            status.contains("Иштеген", ignoreCase = true) ||
            status.contains("Работающий", ignoreCase = true) ||
            status.contains("Working", ignoreCase = true) ||
            status.contains("Ishlovchi", ignoreCase = true)

    private fun isRetired(status: String): Boolean =
        status.contains("Retired", ignoreCase = true) ||
            status.contains("Пенсион", ignoreCase = true)

    fun calculate(pm25: Double, status: String, districtMultiplier: Double): AirBillResult {

        // Healthcare cost
        val visitProbability = (pm25 / 3650.0) * when {
            isStudent(status) -> 1.0
            isRetired(status) -> 2.5
            else -> 1.3
        }
        val healthcareCost = (visitProbability * DOCTOR_VISIT_KGS) +
            (visitProbability * 0.6 * MEDICATION_KGS)

        // Productivity cost
        val dailyValue = when {
            isStudent(status) -> STUDENT_DAILY
            isEmployed(status) -> MEDIUM_INCOME_DAILY
            isRetired(status) -> LOW_INCOME_DAILY
            else -> HIGH_INCOME_DAILY
        }
        val productivityReduction = (pm25 / 10.0) * 0.0035
        val productivityCost = dailyValue * productivityReduction

        // Preventive cost
        val preventiveCost = MASK_DAILY_KGS +
            if (districtMultiplier > 1.2) COAL_PENALTY_KGS else 0.0

        val total = healthcareCost + productivityCost + preventiveCost

        return AirBillResult(
            healthcareCost = healthcareCost,
            productivityCost = productivityCost,
            preventiveCost = preventiveCost,
            totalDaily = total,
            totalMonthly = total * 30,
            totalYearly = total * 365,
        )
    }

    // Cigarette equivalence: 22 µg/m³ PM2.5 = 1 cigarette (Berkeley Earth)
    fun cigaretteEquivalent(pm25: Double): Double = pm25 / 22.0
}

