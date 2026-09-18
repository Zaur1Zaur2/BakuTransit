package com.zaur1.bakutransit.ui.viewmodel

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Looper
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.zaur1.bakutransit.BuildConfig
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
import java.io.File
import java.util.Locale
import kotlin.math.*

class TransitViewModel(private val repository: TransitRepository) : ViewModel() {

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

    private val _newVersion = MutableStateFlow<String?>(null)
    val newVersionAvailable = _newVersion.asStateFlow()

    private val _updateCheckMessage = MutableStateFlow<String?>(null)
    val updateCheckMessage = _updateCheckMessage.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    val allBusRoutes = repository.allBusRoutes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allStops = repository.allStops.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val nearestStop: StateFlow<TransitStopEntity?> = combine(userLocation, allStops) { loc, stops ->
        loc?.let { l ->
            stops.filter { it.type == TransportType.METRO }.minByOrNull { s ->
                calculateDistance(l, LatLng(s.latitude, s.longitude))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch { 
            repository.prePopulateData()
            checkForUpdates()
        }
        
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

    fun checkForUpdates(manual: Boolean = false) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val url = "https://api.github.com/repos/Zaur1zaur2/BakuTransit/releases/latest"
                    val body = OkHttpClient().newCall(Request.Builder().url(url).build()).execute().body?.string()
                    if (body != null) {
                        val json = Gson().fromJson(body, JsonObject::class.java)
                        val latestTag = json.get("tag_name").asString
                        if (latestTag != "v1.1.4") {
                            _newVersion.value = latestTag
                        } else if (manual) {
                            _updateCheckMessage.value = "Tətbiq artıq ən son versiyadadır."
                        }
                    }
                } catch (e: Exception) { 
                    if (manual) _updateCheckMessage.value = "Yenilənmə yoxlanarkən xəta baş verdi."
                }
            }
        }
    }

    fun clearUpdateMessage() {
        _updateCheckMessage.value = null
    }

    fun dismissUpdateDialog() {
        _newVersion.value = null
    }

    fun downloadAndInstallApk(context: Context) {
        _isDownloading.value = true
        val destinationFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "v.1.1.4.apk")
        if (destinationFile.exists()) destinationFile.delete()

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse("https://github.com/Zaur1zaur2/BakuTransit/releases/latest/download/v.1.1.4.apk"))
            .setTitle("Baku Transit")
            .setDescription("Yenilənmə yüklənir...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(destinationFile))

        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    _isDownloading.value = false
                    promptInstall(ctxt, destinationFile)
                    context.unregisterReceiver(this)
                }
            }
        }
        
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(onComplete, filter)
        }
    }

    private fun promptInstall(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(installIntent)
    }

    // GPS & ROUTING
    private var fusedClient: FusedLocationProviderClient? = null
    private var locCallback: LocationCallback? = null

    fun startLocationUpdates(client: FusedLocationProviderClient) {
        // ALWAYS update the client and restart if necessary to ensure it's not "broken"
        fusedClient?.removeLocationUpdates(locCallback ?: object : LocationCallback() {})
        
        fusedClient = client
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500)
            .build()
            
        locCallback = object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                res.lastLocation?.let { 
                    _userLocation.value = LatLng(it.latitude, it.longitude) 
                }
            }
        }
        
        try { 
            fusedClient?.requestLocationUpdates(request, locCallback!!, Looper.getMainLooper()) 
        } catch (e: SecurityException) { 
            e.printStackTrace() 
        }
    }

    fun setDestination(latLng: LatLng?) { _destination.value = latLng }

    fun findStopAt(lat: Double, lng: Double, radiusKm: Double = 0.5): TransitStopEntity? {
        val clickLoc = LatLng(lat, lng)
        val candidates = allStops.value.filter { calculateDistance(clickLoc, LatLng(it.latitude, it.longitude)) <= radiusKm }
        if (candidates.isEmpty()) return null
        return candidates.filter { it.type == TransportType.METRO }.minByOrNull { calculateDistance(clickLoc, LatLng(it.latitude, it.longitude)) }
            ?: candidates.minByOrNull { calculateDistance(clickLoc, LatLng(it.latitude, it.longitude)) }
    }

    private fun executeRouting(o: LatLng, d: LatLng) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // profile=foot for walking
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
            ptsToPair(path) to "%.2f km | %d dəq".format(dist, time)
        } catch (e: Exception) { emptyList<LatLng>() to null }
    }

    private fun ptsToPair(path: JsonObject): List<LatLng> {
        return path.getAsJsonObject("points").getAsJsonArray("coordinates").map { LatLng(it.asJsonArray[1].asDouble, it.asJsonArray[0].asDouble) }
    }

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
        val callback = locCallback
        if (callback != null) {
            fusedClient?.removeLocationUpdates(callback)
        }
    }
}
