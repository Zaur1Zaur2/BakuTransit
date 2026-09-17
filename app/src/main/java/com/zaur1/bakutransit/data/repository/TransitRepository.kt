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
        // ALWAYS update bus routes to ensure all are present
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
            BusRouteEntity("bus_20", "20", "Neftçilər parkı - Tibb Universiteti", "Neftçilər parkı", "Tibb Universiteti", "06:00 - 23:30", 10, 0.60, VehicleModel.KARSAN_ATAK, emptyList()),
            BusRouteEntity("bus_21", "21", "Əhmədli m/st - 28 May m/st", "Əhmədli m/st", "28 May m/st", "06:00 - 23:30", 8, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r11", "m_r3")),
            BusRouteEntity("bus_24", "24", "Qaraçuxur - Nərimanov m/st", "Qaraçuxur", "Nərimanov m/st", "05:40 - 23:30", 10, 0.60, VehicleModel.KARSAN_ATAK, listOf("m_r5")),
            BusRouteEntity("bus_30", "30", "C.Naxçıvanski küç - S.Vurğun bağı", "C.Naxçıvanski küç", "S.Vurğun bağı", "05:30 - 23:30", 12, 0.60, VehicleModel.BMC_PROCITY_12M, emptyList()),
            BusRouteEntity("bus_32", "32", "C.Naxçıvanski küç - 28 May m/st", "C.Naxçıvanski küç", "28 May m/st", "05:45 - 23:30", 12, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r3")),
            BusRouteEntity("bus_33", "33", "28 Mall t/m - Dərnəgül NMM", "28 Mall t/m", "Dərnəgül NMM", "06:00 - 23:30", 10, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_g1")),
            BusRouteEntity("bus_35", "35", "Suraxanı - R.Quliyev küç.", "Suraxanı", "R.Quliyev küç.", "05:45 - 23:30", 15, 0.60, VehicleModel.KARSAN_ATAK, emptyList()),
            BusRouteEntity("bus_44", "44", "Suraxanı - Neftçilər m/st", "Suraxanı", "Neftçilər m/st", "05:40 - 23:30", 12, 0.60, VehicleModel.KARSAN_ATAK, listOf("m_r9")),
            BusRouteEntity("bus_46", "46", "256 məktəb - 28 May m/st", "256 məktəb", "28 May m/st", "06:00 - 23:30", 10, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r3")),
            BusRouteEntity("bus_57", "57", "Bakıxanov - Asiman", "Bakıxanov", "Asiman", "06:00 - 23:30", 12, 0.60, VehicleModel.KARSAN_ATAK, emptyList()),
            BusRouteEntity("bus_60", "60", "221 məktəb - Koroğlu NMM", "221 məktəb", "Koroğlu NMM", "06:00 - 23:00", 15, 0.60, VehicleModel.KARSAN_ATAK, listOf("m_r7")),
            BusRouteEntity("bus_62", "62", "N.Tusi küç - Sabunçu d/st", "N.Tusi küç", "Sabunçu d/st", "05:40 - 23:30", 10, 0.60, VehicleModel.KARSAN_ATAK, emptyList()),
            BusRouteEntity("bus_72", "72", "Günəşli qəs - Sədərək t/m", "Günəşli qəs", "Sədərək t/m", "06:00 - 20:00", 15, 0.60, VehicleModel.BMC_PROCITY_12M, emptyList()),
            BusRouteEntity("bus_88", "88", "Dərnəgül m/st - 20-ci Sahə", "Dərnəgül m/st", "20-ci Sahə", "06:00 - 23:30", 5, 0.60, VehicleModel.BMC_PROCITY_18M, listOf("m_g1")),
            BusRouteEntity("bus_88a", "88A", "28 May m/st - Azadlıq m/st", "28 May m/st", "Azadlıq m/st", "06:00 - 23:30", 8, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r3", "m_g2")),
            BusRouteEntity("bus_108a", "108A", "Fatmayi q. - Azadlıq m/st", "Fatmayi q.", "Azadlıq m/st", "05:50 - 22:00", 15, 0.60, VehicleModel.KARSAN_ATAK, listOf("m_g2")),
            BusRouteEntity("bus_120", "120", "Hövsan y/k - 28 May m/st", "Hövsan y/k", "28 May m/st", "05:45 - 23:30", 12, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r3")),
            BusRouteEntity("bus_120e", "120E", "Hövsan qəs - 28 May m/st", "Hövsan qəs", "28 May m/st", "06:30 - 21:00", 10, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r3")),
            BusRouteEntity("bus_121e", "121E", "Hövsan Mida - Əhmədli m/st", "Hövsan Mida", "Əhmədli m/st", "06:30 - 21:00", 10, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r11")),
            BusRouteEntity("bus_125", "125", "28 May m/st - Lökbatan qəs", "28 May m/st", "Lökbatan qəs", "06:00 - 23:30", 10, 0.60, VehicleModel.BMC_PROCITY_18M, listOf("m_r3")),
            BusRouteEntity("bus_130", "130", "Lökbatan qəs - 20 Yanvar m/st", "Lökbatan qəs", "20 Yanvar m/st", "05:50 - 23:30", 10, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_g5")),
            BusRouteEntity("bus_139", "139", "Koroğlu m/st - Savalan", "Koroğlu m/st", "Savalan", "05:40 - 23:00", 12, 0.60, VehicleModel.KARSAN_ATAK, listOf("m_r7")),
            BusRouteEntity("bus_140", "140", "Koroğlu m/st - Şüvəlan qəs", "Koroğlu m/st", "Şüvəlan qəs", "05:30 - 23:30", 15, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r7")),
            BusRouteEntity("bus_140e", "140E", "Koroğlu m/st - Mərdəkan qəs", "Koroğlu m/st", "Mərdəkan qəs", "06:30 - 21:00", 12, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r7")),
            BusRouteEntity("bus_141e", "141E", "Koroğlu m/st - Qala qəs", "Koroğlu m/st", "Qala qəs", "06:00 - 22:30", 15, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r7")),
            BusRouteEntity("bus_149", "149", "Sahil qəs - Sahil m/st", "Sahil qəs", "Sahil m/st", "06:10 - 22:00", 15, 0.60, VehicleModel.KARSAN_ATAK, listOf("m_r2")),
            BusRouteEntity("bus_150", "150", "Bilgəh qəs - Koroğlu m/st", "Bilgəh qəs", "Koroğlu m/st", "05:30 - 23:30", 15, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r7")),
            BusRouteEntity("bus_163", "163", "Ramana qəs - Koroğlu m/st", "Ramana qəs", "Koroğlu m/st", "05:40 - 23:15", 12, 0.60, VehicleModel.KARSAN_ATAK, listOf("m_r7")),
            BusRouteEntity("bus_172", "172", "Bilgəh qəs - Koroğlu m/st", "Bilgəh qəs", "Koroğlu m/st", "05:20 - 23:00", 15, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r7")),
            BusRouteEntity("bus_189", "189", "Nardaran qəs - Koroğlu m/st", "Nardaran qəs", "Koroğlu m/st", "05:40 - 23:00", 15, 0.60, VehicleModel.KARSAN_ATAK, listOf("m_r7")),
            BusRouteEntity("bus_205", "205", "Abşeron t/m - Nəsimi m/st", "Abşeron t/m", "Nəsimi m/st", "06:15 - 19:40", 12, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_g3")),
            BusRouteEntity("bus_211", "211", "Kobia - Abşeron t/m", "Kobia", "Abşeron t/m", "05:30 - 20:00", 12, 0.60, VehicleModel.BMC_PROCITY_12M, emptyList()),
            BusRouteEntity("bus_217", "217", "Məmmədli qəs - Koroğlu m/st", "Məmmədli qəs", "Koroğlu m/st", "06:00 - 23:00", 12, 0.60, VehicleModel.KARSAN_ATAK, listOf("m_r7")),
            BusRouteEntity("bus_508", "508", "Saray qəs - 20 Yanvar m/st", "Saray qəs", "20 Yanvar m/st", "06:00 - 23:00", 15, 0.60, VehicleModel.KARSAN_ATAK, listOf("m_g5")),
            BusRouteEntity("bus_594", "594", "Sumqayıt - 20 Yanvar m/st", "Sumqayıt", "20 Yanvar m/st", "05:50 - 23:00", 10, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_g5")),
            BusRouteEntity("bus_595", "595", "S.Avtovağzal - B.Avtovağzal", "S.Avtovağzal", "B.Avtovağzal", "06:00 - 22:30", 12, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_p2")),
            BusRouteEntity("bus_e1", "E1", "Xalqlar m/st - Abşeron t/m", "Xalqlar m/st", "Abşeron t/m", "06:00 - 19:00", 10, 0.60, VehicleModel.BMC_PROCITY_18M, listOf("m_r10")),
            BusRouteEntity("bus_h1", "H1", "Hava Limanı - 28 May m/st", "Hava Limanı", "28 May m/st", "06:00 - 01:00", 30, 1.50, VehicleModel.NEOPLAN_TOURLINER, listOf("m_r3")),
            BusRouteEntity("bus_m8", "M8", "Maştağa qəs - Gənclik m/st", "Maştağa qəs", "Gənclik m/st", "05:40 - 22:15", 20, 0.60, VehicleModel.BMC_PROCITY_12M, listOf("m_r4"))
        )
        
        transitDao.insertBusRoutes(busRoutes)

        if (allStops.first().isEmpty()) {
            val stops = listOf(
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
                TransitStopEntity("m_p1", "Xocəsən", TransportType.METRO, 40.4181, 49.7712, listOf("metro_purple")),
                TransitStopEntity("m_p2", "Avtovağzal", TransportType.METRO, 40.4191, 49.7912, listOf("metro_purple")),
                TransitStopEntity("m_p3", "8 Noyabr", TransportType.METRO, 40.4051, 49.8212, listOf("metro_purple")),
                TransitStopEntity("bus_s1", "Bakı Bulvarı", TransportType.BUS, 40.3700, 49.8500, emptyList()),
                TransitStopEntity("bus_s2", "Fəvvarələr Meydanı", TransportType.BUS, 40.3708, 49.8369, emptyList()),
                TransitStopEntity("bus_s3", "Port Baku", TransportType.BUS, 40.3758, 49.8650, emptyList())
            )
            transitDao.insertStops(stops)
        }
    }
}
