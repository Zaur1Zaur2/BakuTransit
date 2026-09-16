package com.zaur1.bakutransit.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.ui.res.stringResource
import com.zaur1.bakutransit.R

@Composable
fun ExtraTransportScreen(onNavigateToMetro: () -> Unit, onNavigateToPlanner: () -> Unit) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.extra_tab),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            TransportCard(
                title = stringResource(R.string.metro_title),
                description = stringResource(R.string.scheme_map),
                icon = Icons.Default.Info,
                buttonText = stringResource(R.string.scheme_map),
                onClick = onNavigateToMetro
            )
        }

        item {
            TransportCard(
                title = stringResource(R.string.trip_planner_title),
                description = stringResource(R.string.suggested_routes),
                icon = Icons.Default.Star,
                buttonText = stringResource(R.string.trip_planner_title),
                onClick = onNavigateToPlanner
            )
        }

        item {
            TransportCard(
                title = "Bike (APAR)",
                description = "Eco-friendly micromobility in Baku. Rent a bike or scooter easily.",
                icon = Icons.Default.Info,
                buttonText = "Open APAR",
                onClick = {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.apar.app")
                    if (intent != null) {
                        context.startActivity(intent)
                    } else {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://apar.az"))
                        context.startActivity(webIntent)
                    }
                }
            )
        }

        item {
            TransportCard(
                title = "Taxi (Bolt)",
                description = "Fast and affordable rides. The most popular taxi service in Azerbaijan.",
                icon = Icons.Default.Star,
                buttonText = "Open Bolt",
                onClick = {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.bolt.client")
                    if (intent != null) {
                        context.startActivity(intent)
                    } else {
                        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.bolt.client"))
                        context.startActivity(playStoreIntent)
                    }
                }
            )
        }
    }
}

@Composable
fun TransportCard(
    title: String,
    description: String,
    icon: ImageVector,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }
        }
    }
}
