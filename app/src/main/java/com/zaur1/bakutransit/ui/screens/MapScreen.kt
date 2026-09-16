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

/**
 * HIGH-PERFORMANCE COORDINATE-BASED MAP SCREEN
 * Optimized to prevent lag during panning and provide 100% reliable pop-ups.
 */
@Composable
fun MapScreen(viewModel: TransitViewModel, onMapReady: () -> Unit = {}) {
    val context = LocalContext.current
    
    // VIEWMODEL STATE
    val stops by viewModel.allStops.collectAsState()
    val userLoc by viewModel.userLocation.collectAsState()
    val nearestS by viewModel.nearestStop.collectAsState()
    val rPoints by viewModel.routePoints.collectAsState()
    val walkInfo by viewModel.footInfo.collectAsState()
    val driveInfo by viewModel.carInfo.collectAsState()

    // UI STATE
    var query by remember { mutableStateOf("") }
    var searchOpen by remember { mutableStateOf(false) }
    var activePopupStop by remember { mutableStateOf<TransitStopEntity?>(null) }
    var mapInit by remember { mutableStateOf(false) }
    var fallbackMode by remember { mutableStateOf(false) }

    // MAP SETUP
    val mapOptions = remember {
        MTMapOptions(
            center = LngLat(49.8671, 40.4093), // Baku center
            zoom = 11.2 
        )
    }
    val controller = remember { MTMapViewController(context) }
    val markerStore = remember { mutableMapOf<String, MTMarker>() }
    var meMarker by remember { mutableStateOf<MTMarker?>(null) }
    var linesDrawn by remember { mutableStateOf(false) }

    // Atomic references for delegate
    val latestNearest = rememberUpdatedState(nearestS)

    val mapDelegate = remember {
        object : MTMapViewDelegate {
            override fun onMapViewInitialized() {
                mapInit = true
                onMapReady()
            }
            override fun onEventTriggered(event: MTEvent, data: MTData?) {
                if (event == MTEvent.ON_TAP) {
                    val lat = data?.coordinate?.lat
                    val lng = data?.coordinate?.lng
                    
                    if (lat != null && lng != null) {
                        val id = data.id
                        if (id == "user_location") {
                            latestNearest.value?.let { s ->
                                activePopupStop = s
                                viewModel.setDestination(LatLng(s.latitude, s.longitude))
                            }
                        } else {
                            val found = viewModel.findStopAt(lat, lng, 0.25)
                            if (found != null) {
                                activePopupStop = found
                                viewModel.setDestination(LatLng(found.latitude, found.longitude))
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(controller) {
        controller.delegate = mapDelegate
    }

    // --- RENDERING LOGIC ---

    // 1. Markers (Batch update logic moved to separate check to reduce re-runs)
    LaunchedEffect(stops, mapInit, fallbackMode) {
        if (mapInit && !fallbackMode) {
            controller.style?.let { style ->
                val activeIds = stops.map { it.id }.toSet()
                // Fast remove
                markerStore.keys.filter { it !in activeIds }.forEach { id ->
                    markerStore[id]?.let { style.removeMarker(it) }
                    markerStore.remove(id)
                }
                // Fast add
                stops.forEach { s ->
                    if (s.id !in markerStore) {
                        val marker = MTMarker(identifier = s.id, LngLat(s.longitude, s.latitude))
                        marker.color = when (s.type) {
                            TransportType.METRO -> Color.RED
                            TransportType.BUS -> Color.BLUE
                            else -> Color.GRAY
                        }
                        style.addMarker(marker)
                        markerStore[s.id] = marker
                    }
                }
                
                if (!linesDrawn && stops.isNotEmpty()) {
                    drawBakuMetroLines(controller, stops)
                    linesDrawn = true
                }
            }
        }
    }

    // 2. Routing Path (Yellow)
    LaunchedEffect(rPoints, mapInit) {
        if (mapInit && rPoints.isNotEmpty()) {
            controller.style?.let { style ->
                val helper = style.polylineHelper()
                val coords = rPoints.map { listOf(it.longitude, it.latitude) }
                helper.addPolyline(
                    MTPolylineLayerOptions(
                        data = buildGeoJson(coords),
                        lineColor = "#FFD700", 
                        lineWidth = 6.0
                    )
                )
            }
        }
    }

    // 3. User Marker
    LaunchedEffect(userLoc, mapInit) {
        if (mapInit && userLoc != null) {
            controller.style?.let { style ->
                meMarker?.let { style.removeMarker(it) }
                val marker = MTMarker("user_location", LngLat(userLoc!!.longitude, userLoc!!.latitude))
                marker.color = Color.CYAN
                style.addMarker(marker)
                meMarker = marker
            }
        }
    }

    // PERMISSIONS
    val pLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { p ->
        if (p.values.all { it }) {
            viewModel.startLocationUpdates(LocationServices.getFusedLocationProviderClient(context))
        }
    }

    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine) {
            viewModel.startLocationUpdates(LocationServices.getFusedLocationProviderClient(context))
        } else {
            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!fallbackMode) {
            MTMapView(
                referenceStyle = MTMapReferenceStyle.STREETS,
                options = mapOptions,
                controller = controller,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = StopImageProvider.getStaticMapUrl(40.4093, 49.8671, zoom = 11),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // SEARCH
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .width(if (searchOpen) 300.dp else 56.dp)
        ) {
            if (searchOpen) {
                Column {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), MaterialTheme.shapes.medium),
                        placeholder = { Text(stringResource(R.string.search_hint)) },
                        trailingIcon = {
                            IconButton(onClick = { searchOpen = false; query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                    
                    if (query.isNotEmpty()) {
                        val norm = viewModel.normalizeAze(query)
                        val filtered = stops.filter { 
                            viewModel.normalizeAze(it.name).contains(norm, ignoreCase = true) 
                        }.take(5)
                        
                        if (filtered.isNotEmpty()) {
                            Card(
                                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                                    items(filtered) { s ->
                                        ListItem(
                                            headlineContent = { Text(s.name) },
                                            supportingContent = { Text(if(s.type == TransportType.METRO) "Metro" else "Avtobus") },
                                            modifier = Modifier.clickable {
                                                controller.setCenter(LngLat(s.longitude, s.latitude))
                                                controller.setZoom(15.0)
                                                activePopupStop = s
                                                viewModel.setDestination(LatLng(s.latitude, s.longitude))
                                                searchOpen = false
                                                query = ""
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                FloatingActionButton(
                    onClick = { searchOpen = true },
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        }

        // TOOLBAR
        Column(
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = { fallbackMode = !fallbackMode },
                containerColor = if (fallbackMode) ComposeColor.Red else MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Safe Mode")
            }

            FloatingActionButton(
                onClick = { 
                    userLoc?.let { 
                        controller.setCenter(LngLat(it.longitude, it.latitude))
                        controller.setZoom(15.5)
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Focus User")
            }
        }

        // BOTTOM STATUS
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (fallbackMode) "Şəhər Xəritəsi (Statik)" else "Canlı Nəqliyyat Rejimi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendItem(ComposeColor.Red, "Metro")
                    LegendItem(ComposeColor(0xFFFFD700), "Piyada yolu")
                }
                
                nearestS?.let { s ->
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ən yaxın: ${s.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            walkInfo?.let {
                                Text(
                                    text = "🚶 $it",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        IconButton(onClick = { 
                            activePopupStop = s 
                            viewModel.setDestination(LatLng(s.latitude, s.longitude))
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Open popup")
                        }
                    }
                }
            }
        }
    }

    // POP-UP
    activePopupStop?.let { s ->
        MetroStationDetailPopup(
            stop = s, 
            onDismiss = { activePopupStop = null },
            onShowRoute = { 
                viewModel.setDestination(LatLng(s.latitude, s.longitude))
                activePopupStop = null
            },
            footInfo = walkInfo,
            carInfo = driveInfo
        )
    }
}

private fun drawBakuMetroLines(controller: MTMapViewController, stops: List<TransitStopEntity>) {
    val style = controller.style ?: return
    val helper = style.polylineHelper()
    
    // RED LINE
    val redIds = listOf("m_r1", "m_r2", "m_r3", "m_r4", "m_r5", "m_r6", "m_r7", "m_r8", "m_r9", "m_r10", "m_r11", "m_r12")
    drawSegment(helper, redIds, stops, "#E53935")
    drawSegment(helper, listOf("m_r5", "m_r_bakmil"), stops, "#E53935")

    // GREEN LINE
    val greenIds = listOf("m_g1", "m_g2", "m_g3", "m_g4", "m_g5", "m_g6", "m_g7", "m_g8", "m_r3", "m_g10")
    drawSegment(helper, greenIds, stops, "#4CAF50")

    // PURPLE LINE
    val purpleIds = listOf("m_p1", "m_p2", "m_g4", "m_p3")
    drawSegment(helper, purpleIds, stops, "#8E24AA")
}

private fun drawSegment(h: MTPolylineLayerHelper, ids: List<String>, s: List<TransitStopEntity>, c: String) {
    val pts = ids.mapNotNull { id -> s.find { it.id == id } }.map { listOf(it.longitude, it.latitude) }
    if (pts.size >= 2) {
        h.addPolyline(MTPolylineLayerOptions(data = buildGeoJson(pts), lineColor = c, lineWidth = 4.5))
    }
}

private fun buildGeoJson(c: List<List<Double>>): String {
    val s = c.joinToString(",") { "[${it[0]},${it[1]}]" }
    return "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[$s]}}]}"
}

@Composable
fun LegendItem(color: ComposeColor, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
