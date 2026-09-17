package com.zaur1.bakutransit.data.repository

import com.zaur1.bakutransit.data.local.BusRouteEntity
import com.zaur1.bakutransit.data.local.TransitDao
import com.zaur1.bakutransit.data.local.TransitStopEntity
import com.zaur1.bakutransit.data.model.TransportType
import com.zaur1.bakutransit.data.model.VehicleModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TransitRepository(private val transitDao: TransitDao) {

    val allBusRoutes: Flow<List<BusRouteEntity>> = transitDao.getAllBusRoutes()
    val allStops: Flow<List<TransitStopEntity>> = transitDao.getAllStops()

    suspend fun prePopulateData() {
        if (allStops.first().isNotEmpty()) return

        val stops = listOf(
            // Metro Stops Red Line
            TransitStopEntity("m_r1", "İçərişəhər", TransportType.METRO, 40.3661, 49.8335, listOf("metro_red")),
            TransitStopEntity("m_r2", "Sahil", TransportType.METRO, 40.3702, 49.8471, listOf("metro_red")),
            TransitStopEntity("m_r3", "28 May", TransportType.METRO, 40.3797, 49.8488, listOf("metro_red", "metro_green")),
            TransitStopEntity("m_r4", "Gənclik", TransportType.METRO, 40.3996, 49.8517, listOf("metro_red")),
            TransitStopEntity("m_r5", "Nəriman Nərimanov", TransportType.METRO, 40.4101, 49.8712, listOf("metro_red")),
            TransitStopEntity("m_r_bakmil", "Bakmil", TransportType.METRO, 40.4100, 49.8750, listOf("metro_red")),
            TransitStopEntity("m_r6", "Ulduz", TransportType.METRO, 40.4151, 49.8911, listOf("metro_red")),
            TransitStopEntity("m_r7", "Koroğlu", TransportType.METRO, 40.4202, 49.9172, listOf("metro_red")),
            TransitStopEntity("m_r8", "Qara Qarayev", TransportType.METRO, 40.4173, 49.9351, listOf("metro_red")),
            TransitStopEntity("m_r9", "Neftçilər", TransportType.METRO, 40.4111, 49.9421, listOf("metro_red")),
            TransitStopEntity("m_r10", "Xalqlar Dostluğu", TransportType.METRO, 40.3971, 49.9521, listOf("metro_red")),
            TransitStopEntity("m_r11", "Əhmədli", TransportType.METRO, 40.3852, 49.9542, listOf("metro_red")),
            TransitStopEntity("m_r12", "Həzi Aslanov", TransportType.METRO, 40.3731, 49.9531, listOf("metro_red", "metro_green")),

            // Metro Stops Green Line
            TransitStopEntity("m_g1", "Dərnəgül", TransportType.METRO, 40.4261, 49.8512, listOf("metro_green")),
            TransitStopEntity("m_g2", "Azadlıq Prospekti", TransportType.METRO, 40.4261, 49.8412, listOf("metro_green")),
            TransitStopEntity("m_g3", "Nəsimi", TransportType.METRO, 40.4201, 49.8312, listOf("metro_green")),
            TransitStopEntity("m_g4", "Memar Əcəmi", TransportType.METRO, 40.4111, 49.8112, listOf("metro_green", "metro_purple")),
            TransitStopEntity("m_g5", "20 Yanvar", TransportType.METRO, 40.4001, 49.8152, listOf("metro_green")),
            TransitStopEntity("m_g6", "İnşaatçılar", TransportType.METRO, 40.3901, 49.8112, listOf("metro_green")),
            TransitStopEntity("m_g7", "Elmlər Akademiyası", TransportType.METRO, 40.3751, 49.8152, listOf("metro_green")),
            TransitStopEntity("m_g8", "Nizami", TransportType.METRO, 40.3791, 49.8302, listOf("metro_green")),
            TransitStopEntity("m_g9", "Cəfər Cabbarlı", TransportType.METRO, 40.3797, 49.8488, listOf("metro_green")),
            TransitStopEntity("m_g10", "Xətai", TransportType.METRO, 40.3831, 49.8732, listOf("metro_green")),

            // Metro Stops Purple Line
            TransitStopEntity("m_p1", "Xocəsən", TransportType.METRO, 40.4181, 49.7712, listOf("metro_purple")),
            TransitStopEntity("m_p2", "Avtovağzal", TransportType.METRO, 40.4191, 49.7912, listOf("metro_purple")),
            TransitStopEntity("m_p3", "8 Noyabr", TransportType.METRO, 40.4051, 49.8212, listOf("metro_purple")),

            // Bus Stops
            TransitStopEntity("bus_s1", "Bakı Bulvarı", TransportType.BUS, 40.3700, 49.8500, emptyList()),
            TransitStopEntity("bus_s2", "Fəvvarələr Meydanı", TransportType.BUS, 40.3708, 49.8369, emptyList()),
            TransitStopEntity("bus_s3", "Port Baku", TransportType.BUS, 40.3758, 49.8650, emptyList())
        )

        val busRoutes = listOf(
            BusRouteEntity("bus_1", "1", "28 May m/st - Neftçilər m/st", "28 May m/st", "Neftçilər m/st", "06:00 - 23:59", 8, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r3", "m_r9")),
            BusRouteEntity("bus_2", "2", "B.Avtovağzal - Dərnəgül m/st", "B.Avtovağzal", "Dərnəgül m/st", "05:48 - 23:30", 10, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_p2", "m_g1")),
            BusRouteEntity("bus_3", "3", "Badamdar qəs. - Dərnəgül m/st", "Badamdar qəs.", "Dərnəgül m/st", "06:00 - 23:30", 12, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_g1")),
            BusRouteEntity("bus_4", "4", "Ağ Şəhər - Montin", "Ağ Şəhər", "Montin", "06:30 - 22:05", 15, 0.60, VehicleModel.KARSAN_ATAK, emptyList()),
            BusRouteEntity("bus_5", "5", "Nərimanov m/st - 20-ci Sahə", "Nərimanov m/st", "20-ci Sahə", "05:55 - 23:59", 7, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r5")),
            BusRouteEntity("bus_6", "6", "20-ci Sahə - Azadlıq m/st", "20-ci Sahə", "Azadlıq m/st", "05:50 - 23:30", 8, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_g2")),
            BusRouteEntity("bus_7a", "7A", "B.Avtovağzal - Gəncə Pr", "B.Avtovağzal", "Gəncə Pr", "06:00 - 23:30", 9, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_p2", "m_r11")),
            BusRouteEntity("bus_7b", "7B", "Yeni Yasamal - Koroğlu m/st", "Yeni Yasamal", "Koroğlu m/st", "06:00 - 23:30", 10, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_g6", "m_r7")),
            BusRouteEntity("bus_10", "10", "Mida - Ağ Şəhər", "Mida", "Ağ Şəhər", "06:00 - 23:00", 15, 0.60, VehicleModel.KARSAN_ATAK, emptyList()),
            BusRouteEntity("bus_11", "11", "S.Rəhimov küç - Sadıqcan küç", "S.Rəhimov küç", "Sadıqcan küç", "06:00 - 23:30", 12, 0.60, VehicleModel.BMC_PROCITY_12M, emptyList()),
            BusRouteEntity("bus_13", "13", "B.Avtovağzal - Koroğlu m/st", "B.Avtovağzal", "Koroğlu m/st", "06:00 - 23:30", 10, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_p2", "m_r7")),
            BusRouteEntity("bus_14", "14", "B.Avtovağzal - 28 May m/st", "B.Avtovağzal", "28 May m/st", "06:00 - 23:30", 8, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_p2", "m_r3")),
            BusRouteEntity("bus_17", "17", "Yeni Yasamal - Kobia", "Yeni Yasamal", "Kobia", "06:00 - 23:30", 12, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_g6")),
            BusRouteEntity("bus_21", "21", "Əhmədli m/st - 28 May m/st", "Əhmədli m/st", "28 May m/st", "06:00 - 23:30", 8, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r11", "m_r3")),
            BusRouteEntity("bus_32", "32", "C.Naxçıvanski küç - 28 May m/st", "C.Naxçıvanski küç", "28 May m/st", "05:45 - 23:30", 12, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r3")),
            BusRouteEntity("bus_88", "88", "Dərnəgül m/st - 20-ci Sahə", "Dərnəgül m/st", "20-ci Sahə", "06:00 - 23:30", 5, 0.60, VehicleModel.BMC_PROCITY_18M, listOf("m_g1")),
            BusRouteEntity("bus_h1", "H1", "Hava Limanı - 28 May m/st", "Hava Limanı", "28 May m/st", "06:00 - 01:00", 30, 1.50, VehicleModel.NEOPLAN_TOURLINER, listOf("m_r3"))
        )

        transitDao.insertStops(stops)
        transitDao.insertBusRoutes(busRoutes)
    }
}
