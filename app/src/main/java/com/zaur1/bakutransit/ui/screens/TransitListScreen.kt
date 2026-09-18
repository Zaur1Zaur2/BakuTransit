package com.zaur1.bakutransit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zaur1.bakutransit.data.local.TransitStopEntity
import com.zaur1.bakutransit.data.model.LatLng
import com.zaur1.bakutransit.data.model.TransportType
import com.zaur1.bakutransit.ui.util.MetroStationDetailPopup
import com.zaur1.bakutransit.ui.viewmodel.TransitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitListScreen(viewModel: TransitViewModel) {
    val stops by viewModel.allStops.collectAsState()
    val routes by viewModel.allBusRoutes.collectAsState()
    var tabIndex by remember { mutableStateOf(0) }
    var selectedS by remember { mutableStateOf<TransitStopEntity?>(null) }
    
    val fInfo by viewModel.footInfo.collectAsState()
    val cInfo by viewModel.carInfo.collectAsState()

    val metros = remember(stops) { stops.filter { it.type == TransportType.METRO } }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        ScrollableTabRow(selectedTabIndex = tabIndex, edgePadding = 16.dp) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Avtobuslar") })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Metro") })
            Tab(selected = tabIndex == 2, onClick = { tabIndex = 2 }, text = { Text("Qaydalar") })
        }

        when (tabIndex) {
            0 -> {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    items(routes) { r ->
                        Card(modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("№${r.number}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("0.60 AZN", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Text(r.name, style = MaterialTheme.typography.bodyMedium); Spacer(Modifier.height(4.dp))
                                Text("Model: ${r.vehicleModel.displayName}", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            }
                        }
                    }
                }
            }
            1 -> {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    items(metros) { s ->
                        Card(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().clickable { 
                            selectedS = s; viewModel.setDestination(LatLng(s.latitude, s.longitude))
                        }, shape = MaterialTheme.shapes.medium) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Default.Place, null, tint = Color.Red)
                                Spacer(Modifier.width(16.dp))
                                Column { Text(s.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Bakı Metropoliteni", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary) }
                            }
                        }
                    }
                }
            }
            2 -> GeneralGuidelines()
        }
    }

    selectedS?.let { s -> MetroStationDetailPopup(stop = s, onDismiss = { selectedS = null }, footInfo = fInfo, carInfo = cInfo) }
}

@Composable
fun GeneralGuidelines() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { 
            Text("Bakı Nəqliyyat Qaydaları", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Sərnişin daşınması və BakıKart haqqında ümumi məlumatlar.", style = MaterialTheme.typography.bodyMedium) 
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🚇 Metropoliten Qaydaları", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text("1. Ödəniş yalnız BakıKart (plastik və ya QR) ilədir.", style = MaterialTheme.typography.bodySmall)
                    Text("2. Gediş haqqı: 0.60 AZN.", style = MaterialTheme.typography.bodySmall)
                    Text("3. İş saatları: 06:00 - 00:00.", style = MaterialTheme.typography.bodySmall)
                    Text("4. Stansiyalarda siqaret çəkmək qadağandır.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🚌 Avtobus Qaydaları", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text("1. Avtobusa ön qapıdan minib, orta/arxa qapıdan düşmək lazımdır.", style = MaterialTheme.typography.bodySmall)
                    Text("2. Ödəniş sürücüyə yox, BakıKart oxuyucuya edilir.", style = MaterialTheme.typography.bodySmall)
                    Text("3. Yaşlılara və ehtiyacı olanlara yer vermək tövsiyə olunur.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
