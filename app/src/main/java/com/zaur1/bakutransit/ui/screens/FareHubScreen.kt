package com.zaur1.bakutransit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zaur1.bakutransit.R

@Composable
fun FareHubScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(stringResource(R.string.bakikart_calc), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        var rideCount by remember { mutableStateOf(1) }
        
        Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.ride_count, rideCount))
                Slider(
                    value = rideCount.toFloat(),
                    onValueChange = { rideCount = it.toInt() },
                    valueRange = 1f..50f
                )
                Text(stringResource(R.string.total_standard, rideCount * 0.60), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.total_aero, rideCount * 1.50), style = MaterialTheme.typography.titleLarge)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.guidelines), style = MaterialTheme.typography.titleLarge)
        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text(stringResource(R.string.guideline_1))
            Text(stringResource(R.string.guideline_2))
            Text(stringResource(R.string.guideline_3))
            Text(stringResource(R.string.guideline_4))
        }
    }
}
