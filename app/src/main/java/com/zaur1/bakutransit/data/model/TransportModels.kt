package com.zaur1.bakutransit.data.model

import androidx.compose.runtime.Immutable

enum class TransportType {
    METRO, BUS, TRAIN, BICYCLE
}

enum class VehicleModel(val displayName: String) {
    BMC_PROCITY_12M("BMC Procity 12m"),
    BMC_PROCITY_18M("BMC Procity 18m"),
    KARSAN_ATAK("Karsan Atak"),
    ISUZU_NP_CITY("Isuzu NP City"),
    NEOPLAN_TOURLINER("Neoplan Tourliner"),
    METRO_81_760("Metro 81-760/761"),
    METRO_81_717("Metro 81-717/714")
}

@Immutable
data class LatLng(val latitude: Double, val longitude: Double)

data class TransitStop(
    val id: String,
    val name: String,
    val type: TransportType,
    val location: LatLng,
    val lineIds: List<String>,
    val imageUrl: String? = null
)

data class TransitRoute(
    val id: String,
    val number: String,
    val name: String,
    val type: TransportType,
    val startPoint: String,
    val endPoint: String,
    val operatingHours: String,
    val interval: Int,
    val tariff: Double,
    val vehicleModel: VehicleModel,
    val stops: List<TransitStop>
)

data class JourneyStep(
    val instruction: String,
    val fromStop: String,
    val toStop: String,
    val routeId: String,
    val vehicleModel: VehicleModel,
    val estimatedMinutes: Int
)

data class JourneyPlan(
    val origin: String,
    val destination: String,
    val steps: List<JourneyStep>,
    val totalTimeMinutes: Int,
    val totalFare: Double
)
