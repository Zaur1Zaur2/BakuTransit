package com.zaur1.bakutransit.ui.viewmodel

import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.zaur1.bakutransit.data.local.BusRouteEntity
import com.zaur1.bakutransit.data.local.TransitStopEntity
import com.zaur1.bakutransit.data.model.LatLng
import com.zaur1.bakutransit.data.model.TransportType
import com.zaur1.bakutransit.data.repository.TransitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.math.*

/**
 * HIGH-PERFORMANCE VIEWMODEL
 * Focused on fast coordinate matching and efficient state updates to reduce lag.
 */
class TransitViewModel(private val repository: TransitRepository) : ViewModel() {

    // --- SHARED STATE ---
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation = _userLocation.asStateFlow()

    private val _destination = MutableStateFlow<LatLng?>(null)
    val destination = _destination.asStateFlow()

    private val _footInfo = MutableStateFlow<String?>(null)
    val footInfo = _footInfo.asStateFlow()

    private val _carInfo = MutableStateFlow<String?>(null)
    val carInfo = _carInfo.asStateFlow()

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints = _routePoints.asStateFlow()

    // --- DATA ---
    val allBusRoutes = repository.allBusRoutes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allStops = repository.allStops.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Derived: Closest metro to user
    val nearestStop: StateFlow<TransitStopEntity?> = combine(userLocation, allStops) { loc, stops ->
        loc?.let { l ->
            stops.filter { it.type == TransportType.METRO }.minByOrNull { s ->
                calculateDistance(l, LatLng(s.latitude, s.longitude))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch { repository.prePopulateData() }
        
        // Auto-routing observer
        viewModelScope.launch {
            combine(userLocation, destination, allStops) { loc, dest, stops ->
                val target = dest ?: loc?.let { l ->
                    stops.filter { it.type == TransportType.METRO }
                        .minByOrNull { s -> calculateDistance(l, LatLng(s.latitude, s.longitude)) }
                        ?.let { LatLng(it.latitude, it.longitude) }
                }
                loc to target
            }.distinctUntilChanged().collect { (loc, target) ->
                if (loc != null && target != null) executeRouting(loc, target)
            }
        }
    }

    // --- GPS ---
    private var fusedClient: FusedLocationProviderClient? = null
    private var locCallback: LocationCallback? = null

    fun startLocationUpdates(client: FusedLocationProviderClient) {
        if (locCallback != null) return
        fusedClient = client
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build()
        locCallback = object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                res.lastLocation?.let { _userLocation.value = LatLng(it.latitude, it.longitude) }
            }
        }
        try { fusedClient?.requestLocationUpdates(request, locCallback!!, Looper.getMainLooper()) }
        catch (e: SecurityException) { e.printStackTrace() }
    }

    // --- INTERACTION ---
    fun setDestination(latLng: LatLng?) { _destination.value = latLng }

    /**
     * Finds the closest stop to a given coordinate.
     * Priority is given to Metro stations.
     */
    fun findStopAt(lat: Double, lng: Double, radiusKm: Double = 0.4): TransitStopEntity? {
        val clickLoc = LatLng(lat, lng)
        val candidates = allStops.value.filter { 
            calculateDistance(clickLoc, LatLng(it.latitude, it.longitude)) <= radiusKm 
        }
        
        if (candidates.isEmpty()) return null
        
        // Priority 1: Nearest Metro
        val nearestMetro = candidates.filter { it.type == TransportType.METRO }
            .minByOrNull { calculateDistance(clickLoc, LatLng(it.latitude, it.longitude)) }
        if (nearestMetro != null) return nearestMetro
        
        // Priority 2: Nearest Bus
        return candidates.minByOrNull { calculateDistance(clickLoc, LatLng(it.latitude, it.longitude)) }
    }

    // --- ROUTING ---
    private fun executeRouting(o: LatLng, d: LatLng) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val foot = fetchGH(o, d, "foot")
                _routePoints.value = foot.first
                _footInfo.value = foot.second
                val car = fetchGH(o, d, "car")
                _carInfo.value = car.second
            }
        }
    }

    private fun fetchGH(o: LatLng, d: LatLng, p: String): Pair<List<LatLng>, String?> {
        return try {
            val key = "e82b32df-fcd5-4dc6-88e9-536ed3e2e4db"
            val url = "https://graphhopper.com/api/1/route?point=${o.latitude},${o.longitude}&point=${d.latitude},${d.longitude}&profile=$p&points_encoded=false&key=$key"
            val body = OkHttpClient().newCall(Request.Builder().url(url).build()).execute().body?.string() ?: return emptyList<LatLng>() to null
            val json = Gson().fromJson(body, JsonObject::class.java)
            val path = json.getAsJsonArray("paths")?.get(0)?.asJsonObject ?: return emptyList<LatLng>() to null
            val dist = path.get("distance").asDouble / 1000.0
            val time = path.get("time").asLong / 60000
            val info = "%.2f km | %d dəq".format(dist, time)
            val pts = path.getAsJsonObject("points").getAsJsonArray("coordinates").map { LatLng(it.asJsonArray[1].asDouble, it.asJsonArray[0].asDouble) }
            pts to info
        } catch (e: Exception) { emptyList<LatLng>() to null }
    }

    // --- UTILS ---
    fun normalizeAze(t: String) = t.lowercase().replace("i̇", "i").replace("ə", "e").replace("ı", "i").replace("ö", "o").replace("ü", "u").replace("ş", "s").replace("ç", "c").replace("ğ", "g").replace(" ", "")

    fun calculateDistance(l1: LatLng, l2: LatLng): Double {
        val r = 6371.0
        val dLat = Math.toRadians(l2.latitude - l1.latitude)
        val dLon = Math.toRadians(l2.longitude - l1.longitude)
        val a = sin(dLat/2) * sin(dLat/2) + cos(Math.toRadians(l1.latitude)) * cos(Math.toRadians(l2.latitude)) * sin(dLon/2) * sin(dLon/2)
        return r * 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
    }

    override fun onCleared() {
        super.onCleared()
        locCallback?.let { fusedClient?.removeLocationUpdates(it) }
    }
}
