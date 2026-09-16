package com.zaur1.bakutransit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zaur1.bakutransit.R
import com.zaur1.bakutransit.data.local.BusRouteEntity
import com.zaur1.bakutransit.ui.viewmodel.TransitViewModel

@Composable
fun BusRoutesScreen(viewModel: TransitViewModel) {
    val routes by viewModel.allBusRoutes.collectAsState()
    var selectedRouteForPopup by remember { mutableStateOf<BusRouteEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredRoutes = remember(routes, searchQuery) {
        val normalizedQuery = viewModel.normalizeAze(searchQuery)
        if (searchQuery.isBlank()) {
            routes
        } else {
            routes.filter { 
                viewModel.normalizeAze(it.number).contains(normalizedQuery, ignoreCase = true) || 
                viewModel.normalizeAze(it.name).contains(normalizedQuery, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
        )

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(filteredRoutes) { route ->
                ListItem(
                    headlineContent = { Text("Marşrut №${route.number}", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(route.name) },
                    trailingContent = { Text("0.60 AZN", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.clickable { selectedRouteForPopup = route }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }

    selectedRouteForPopup?.let { route ->
        AlertDialog(
            onDismissRequest = { selectedRouteForPopup = null },
            title = { Text(text = "Marşrut №${route.number}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "🚩 ${route.startPoint}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "🏁 ${route.endPoint}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "🚌 Model: ${route.vehicleModel.displayName}", color = MaterialTheme.colorScheme.secondary)
                    Text(text = "⏱️ İnterval: ${route.interval} dəqiqə")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedRouteForPopup = null }) {
                    Text(stringResource(R.string.close_button))
                }
            }
        )
    }
}
