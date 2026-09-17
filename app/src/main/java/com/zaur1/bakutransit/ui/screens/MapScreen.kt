package com.zaur1.bakutransit.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.zaur1.bakutransit.R
import com.zaur1.bakutransit.data.model.TransportType
import com.zaur1.bakutransit.ui.util.StopImageProvider
import com.zaur1.bakutransit.ui.viewmodel.TransitViewModel
import com.maptiler.maptilersdk.annotations.MTMarker
import com.maptiler.maptilersdk.helpers.MTPolylineLayerOptions
import com.maptiler.maptilersdk.map.LngLat
import com.maptiler.maptilersdk.map.MTMapOptions
import com.maptiler.maptilersdk.map.MTMapView
import com.maptiler.maptilersdk.map.MTMapViewController
import com.maptiler.maptilersdk.map.style.MTMapReferenceStyle
import com.zaur1.bakutransit.data.local.TransitStopEntity
import com.maptiler.maptilersdk.helpers.MTPolylineLayerHelper
import com.google.android.gms.location.LocationServices
import com.zaur1.bakutransit.data.model.LatLng
import com.zaur1.bakutransit.ui.util.MetroStationDetailPopup
import com.maptiler.maptilersdk.events.MTEvent
import com.maptiler.maptilersdk.map.MTMapViewDelegate
import com.maptiler.maptilersdk.map.types.MTData

@Composable
fun MapScreen(viewModel: TransitViewModel, onMapReady: () -> Unit = {}) {
    val context = LocalContext.current
    val stops by viewModel.allStops.collectAsState()
    val userLoc by viewModel.userLocation.collectAsState()
    val nearestS by viewModel.nearestStop.collectAsState()
    val rPoints by viewModel.routePoints.collectAsState()
    val fInfo by viewModel.footInfo.collectAsState()
    val cInfo by viewModel.carInfo.collectAsState()

    var query by remember { mutableStateOf("") }
    var sOpen by remember { mutableStateOf(false) }
    var activePopup by remember { mutableStateOf<TransitStopEntity?>(null) }
    var mInit by remember { mutableStateOf(false) }
    var sMode by remember { mutableStateOf(false) }

    val controller = remember { MTMapViewController(context) }
    val mStore = remember { mutableMapOf<String, MTMarker>() }
    var meM by remember { mutableStateOf<MTMarker?>(null) }

    val curNear = rememberUpdatedState(nearestS)
    val curStops = rememberUpdatedState(stops)

    val delegate = remember {
        object : MTMapViewDelegate {
            override fun onMapViewInitialized() { mInit = true; onMapReady() }
            override fun onEventTriggered(event: MTEvent, data: MTData?) {
                if (event == MTEvent.ON_TAP) {
                    val lat = data?.coordinate?.lat
                    val lng = data?.coordinate?.lng
                    
                    if (lat != null && lng != null) {
                        val id = data.id
                        if (id == "user_location") {
                            curNear.value?.let { activePopup = it; viewModel.setDestination(LatLng(it.latitude, it.longitude)) }
                        } else {
                            val found = viewModel.findStopAt(lat, lng, 0.4)
                            if (found != null) {
                                activePopup = found
                                viewModel.setDestination(LatLng(found.latitude, found.longitude))
                            } else if (id != null) {
                                curStops.value.find { it.id == id }?.let { activePopup = it; viewModel.setDestination(LatLng(it.latitude, it.longitude)) }
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(controller) { controller.delegate = delegate }

    LaunchedEffect(stops, mInit, sMode) {
        if (mInit && !sMode) {
            controller.style?.let { style ->
                val ids = stops.map { it.id }.toSet()
                mStore.keys.filter { it !in ids }.forEach { mStore[it]?.let { m -> style.removeMarker(m) }; mStore.remove(it) }
                stops.forEach { s ->
                    if (s.id !in mStore) {
                        val m = MTMarker(s.id, LngLat(s.longitude, s.latitude))
                        m.color = if(s.type == TransportType.METRO) Color.RED else Color.BLUE
                        style.addMarker(m); mStore[s.id] = m
                    }
                }
                drawBakuLines(controller, stops)
            }
        }
    }

    LaunchedEffect(rPoints, mInit) {
        if (mInit && rPoints.isNotEmpty()) {
            controller.style?.let { style ->
                val h = style.polylineHelper()
                h.addPolyline(MTPolylineLayerOptions(data = geoJson(rPoints), lineColor = "#FFD700", lineWidth = 6.0))
            }
        }
    }

    LaunchedEffect(userLoc, mInit) {
        if (mInit && userLoc != null) {
            controller.style?.let { style ->
                meM?.let { style.removeMarker(it) }
                val m = MTMarker("user_location", LngLat(userLoc!!.longitude, userLoc!!.latitude))
                m.color = Color.CYAN; style.addMarker(m); meM = m
            }
        }
    }

    val pL = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { p ->
        if (p.values.all { it }) viewModel.startLocationUpdates(LocationServices.getFusedLocationProviderClient(context))
    }
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) viewModel.startLocationUpdates(LocationServices.getFusedLocationProviderClient(context))
        else pL.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    Box(Modifier.fillMaxSize()) {
        if (!sMode) MTMapView(referenceStyle = MTMapReferenceStyle.STREETS, options = remember { MTMapOptions(center = LngLat(49.8671, 40.4093), zoom = 11.0) }, controller = controller, modifier = Modifier.fillMaxSize())
        else AsyncImage(model = StopImageProvider.getStaticMapUrl(40.4093, 49.8671), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        
        Box(Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(16.dp).width(if (sOpen) 300.dp else 56.dp)) {
            if (sOpen) {
                Column {
                    OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(0.95f), MaterialTheme.shapes.medium), placeholder = { Text("Axtarış...") }, trailingIcon = { IconButton(onClick = { sOpen = false; query = "" }) { Icon(Icons.Default.Close, null) } }, singleLine = true, shape = MaterialTheme.shapes.medium)
                    if (query.isNotEmpty()) {
                        val n = viewModel.normalizeAze(query)
                        val f = stops.filter { viewModel.normalizeAze(it.name).contains(n, true) }.take(5)
                        Card(Modifier.padding(top = 8.dp).fillMaxWidth()) {
                            LazyColumn(Modifier.heightIn(max = 280.dp)) {
                                items(f) { s ->
                                    ListItem(headlineContent = { Text(s.name) }, modifier = Modifier.clickable {
                                        controller.setCenter(LngLat(s.longitude, s.latitude)); controller.setZoom(15.0)
                                        activePopup = s; viewModel.setDestination(LatLng(s.latitude, s.longitude))
                                        sOpen = false; query = ""
                                    })
                                }
                            }
                        }
                    }
                }
            } else FloatingActionButton(onClick = { sOpen = true }, containerColor = MaterialTheme.colorScheme.surface) { Icon(Icons.Default.Search, null) }
        }

        Column(Modifier.align(Alignment.TopStart).statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FloatingActionButton(onClick = { sMode = !sMode }, containerColor = if (sMode) ComposeColor.Red else MaterialTheme.colorScheme.tertiaryContainer) { Icon(Icons.Default.Refresh, null) }
            FloatingActionButton(onClick = { userLoc?.let { controller.setCenter(LngLat(it.longitude, it.latitude)); controller.setZoom(15.5) } }, containerColor = MaterialTheme.colorScheme.secondaryContainer) { Icon(Icons.Default.LocationOn, null) }
        }

        Card(Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(0.95f))) {
            Column(Modifier.padding(16.dp)) {
                Row { LegendItem(ComposeColor.Red, "Metro"); Spacer(Modifier.width(16.dp)); LegendItem(ComposeColor(0xFFFFD700), "Marşrut") }
                nearestS?.let { s ->
                    Spacer(Modifier.height(12.dp)); HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(s.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            fInfo?.let { Text("🚶 $it", style = MaterialTheme.typography.labelSmall) }
                        }
                        IconButton(onClick = { activePopup = s; viewModel.setDestination(LatLng(s.latitude, s.longitude)) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
                    }
                }
            }
        }
    }

    activePopup?.let { s -> MetroStationDetailPopup(stop = s, onDismiss = { activePopup = null }, onShowRoute = { viewModel.setDestination(LatLng(s.latitude, s.longitude)); activePopup = null }, footInfo = fInfo, carInfo = cInfo) }
}

private fun drawBakuLines(c: MTMapViewController, s: List<TransitStopEntity>) {
    val h = c.style?.polylineHelper() ?: return
    drawSegment(h, listOf("m_r1", "m_r2", "m_r3", "m_r4", "m_r5", "m_r6", "m_r7", "m_r8", "m_r9", "m_r10", "m_r11", "m_r12"), s, "#E53935")
    drawSegment(h, listOf("m_r5", "m_r_bakmil"), s, "#E53935")
    drawSegment(h, listOf("m_g1", "m_g2", "m_g3", "m_g4", "m_g5", "m_g6", "m_g7", "m_g8", "m_r3", "m_g10"), s, "#4CAF50")
    drawSegment(h, listOf("m_p1", "m_p2", "m_g4", "m_p3"), s, "#8E24AA")
}

private fun drawSegment(h: MTPolylineLayerHelper, ids: List<String>, s: List<TransitStopEntity>, c: String) {
    val pts = ids.mapNotNull { id -> s.find { it.id == id } }.map { LngLat(it.longitude, it.latitude) }
    if (pts.size >= 2) h.addPolyline(MTPolylineLayerOptions(data = buildGeoJsonSimple(pts), lineColor = c, lineWidth = 4.5))
}

private fun geoJson(pts: List<LatLng>): String {
    val s = pts.joinToString(",") { "[${it.longitude},${it.latitude}]" }
    return "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[$s]}}]}"
}

private fun buildGeoJsonSimple(pts: List<LngLat>): String {
    val s = pts.joinToString(",") { "[${it.lng},${it.lat}]" }
    return "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[$s]}}]}"
}

@Composable
fun LegendItem(c: ComposeColor, l: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(c, CircleShape)); Spacer(Modifier.width(6.dp)); Text(l, style = MaterialTheme.typography.labelSmall)
    }
}
