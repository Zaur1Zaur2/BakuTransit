package com.zaur1.bakutransit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.zaur1.bakutransit.R
import com.zaur1.bakutransit.data.model.JourneyPlan
import com.zaur1.bakutransit.ui.viewmodel.TransitViewModel

@Composable
fun TripPlannerScreen(viewModel: TransitViewModel) {
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    
    val stops by viewModel.allStops.collectAsState()
    
    var originSuggestions by remember { mutableStateOf(false) }
    var destSuggestions by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.trip_planner_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Box {
            OutlinedTextField(
                value = origin,
                onValueChange = { 
                    origin = it 
                    originSuggestions = it.isNotBlank()
                },
                label = { Text(stringResource(R.string.origin_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            if (originSuggestions) {
                val normalizedQuery = viewModel.normalizeAze(origin)
                val filtered = stops.filter { 
                    viewModel.normalizeAze(it.name).contains(normalizedQuery, ignoreCase = true) 
                }.take(5)
                if (filtered.isNotEmpty()) {
                    Card(modifier = Modifier.padding(top = 64.dp).fillMaxWidth().zIndex(1f)) {
                        Column {
                            filtered.forEach { stop ->
                                Text(
                                    text = stop.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            origin = stop.name
                                            originSuggestions = false
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box {
            OutlinedTextField(
                value = destination,
                onValueChange = { 
                    destination = it 
                    destSuggestions = it.isNotBlank()
                },
                label = { Text(stringResource(R.string.destination_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            if (destSuggestions) {
                val normalizedQuery = viewModel.normalizeAze(destination)
                val filtered = stops.filter { 
                    viewModel.normalizeAze(it.name).contains(normalizedQuery, ignoreCase = true) 
                }.take(5)
                if (filtered.isNotEmpty()) {
                    Card(modifier = Modifier.padding(top = 64.dp).fillMaxWidth().zIndex(1f)) {
                        Column {
                            filtered.forEach { stop ->
                                Text(
                                    text = stop.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            destination = stop.name
                                            destSuggestions = false
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { 
                // Simulated planning logic
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.plan_button))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(stringResource(R.string.suggested_routes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                RouteResultCard(stringResource(R.string.st_red_line), "12 min", "0.60 AZN", "Direct")
            }
            item {
                RouteResultCard(stringResource(R.string.buses_tab) + " 1 + " + stringResource(R.string.metro_tab), "18 min", "1.20 AZN", "1 Transfer")
            }
        }
    }
}

@Composable
fun RouteResultCard(title: String, time: String, fare: String, type: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(time)
                Text(fare, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Text(type, style = MaterialTheme.typography.labelSmall)
        }
    }
}
