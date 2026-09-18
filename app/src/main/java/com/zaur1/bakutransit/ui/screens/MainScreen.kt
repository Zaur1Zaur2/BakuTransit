package com.zaur1.bakutransit.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.zaur1.bakutransit.R
import com.zaur1.bakutransit.ui.viewmodel.ChatViewModel
import com.zaur1.bakutransit.ui.viewmodel.SettingsViewModel
import com.zaur1.bakutransit.ui.viewmodel.TransitViewModel
import com.zaur1.bakutransit.ui.screens.ExtraTransportScreen
import com.zaur1.bakutransit.ui.screens.CreditsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    transitViewModel: TransitViewModel, 
    settingsViewModel: SettingsViewModel,
    onMapReady: () -> Unit = {}
) {
    val navController = rememberNavController()
    val chatViewModel: ChatViewModel = viewModel()
    val sheetState = rememberModalBottomSheetState()
    var showChat by remember { mutableStateOf(false) }
    
    val newVersion by transitViewModel.newVersionAvailable.collectAsState()
    val isDownloading by transitViewModel.isDownloading.collectAsState()
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showChat = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Face, contentDescription = stringResource(R.string.ai_assistant))
                }
            },
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Place, null) },
                        label = { Text(stringResource(R.string.map_tab)) },
                        selected = currentDestination?.route == "map",
                        onClick = { navController.navigate("map") }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, null) },
                        label = { Text(stringResource(R.string.buses_tab)) },
                        selected = currentDestination?.route == "bus_list",
                        onClick = { navController.navigate("bus_list") }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Star, null) },
                        label = { Text(stringResource(R.string.tools_tab)) },
                        selected = currentDestination?.route == "extra",
                        onClick = { navController.navigate("extra") }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Info, null) },
                        label = { Text("Müəllif") },
                        selected = currentDestination?.route == "credits",
                        onClick = { navController.navigate("credits") }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text(stringResource(R.string.settings_tab)) },
                        selected = currentDestination?.route == "settings",
                        onClick = { navController.navigate("settings") }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = "map", modifier = Modifier.padding(innerPadding)) {
                composable("map") { MapScreen(transitViewModel, onMapReady) }
                composable("bus_list") { TransitListScreen(transitViewModel) }
                composable("extra") { ExtraTransportScreen(
                    onNavigateToMetro = { navController.navigate("metro_scheme") },
                    onNavigateToPlanner = { navController.navigate("trip_planner") }
                ) }
                composable("fare") { FareHubScreen() }
                composable("settings") { SettingsAndInfoScreen(settingsViewModel, transitViewModel, onNavigateToCredits = { navController.navigate("credits") }) }
                composable("metro_scheme") { MetroSchemeScreen(onBack = { navController.popBackStack() }) }
                composable("trip_planner") { TripPlannerScreen(transitViewModel) }
                composable("credits") { CreditsScreen() }
            }

            // OTA Dialog
            newVersion?.let { version ->
                AlertDialog(
                    onDismissRequest = { /* Keep it visible */ },
                    title = { Text("Yeni Yenilənmə Mövcuddur!") },
                    text = { Text("Tətbiqin yeni versiyası ($version) hazırdır. İndi yükləyib quraşdıraq?") },
                    confirmButton = {
                        if (isDownloading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Button(onClick = {
                                transitViewModel.downloadAndInstallApk(context)
                            }) {
                                Text("Yüklə və Quraşdır")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { transitViewModel.dismissUpdateDialog() }) {
                            Text("Bağla")
                        }
                    }
                )
            }

            if (showChat) {
                ModalBottomSheet(
                    onDismissRequest = { showChat = false },
                    sheetState = sheetState,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    ChatScreen(chatViewModel)
                }
            }
        }
    }
}

@Composable
fun SettingsAndInfoScreen(settingsViewModel: SettingsViewModel, transitViewModel: TransitViewModel, onNavigateToCredits: () -> Unit = {}) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val currentLang by settingsViewModel.language.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.appearance_lang), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.dark_mode), style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = isDarkMode, onCheckedChange = { settingsViewModel.toggleDarkMode() })
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("${stringResource(R.string.current_lang)}: $currentLang", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { settingsViewModel.setLanguage("AZE") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(currentLang == "AZE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)) { Text("AZ") }
                        Button(onClick = { settingsViewModel.setLanguage("ENG") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(currentLang == "ENG") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)) { Text("EN") }
                        Button(onClick = { settingsViewModel.setLanguage("RUS") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(currentLang == "RUS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)) { Text("RU") }
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.legal_info), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${stringResource(R.string.version_label)}: 1.1.3", style = MaterialTheme.typography.bodyMedium)
                    Text("${stringResource(R.string.support_label)}: support@bakutransit.live", style = MaterialTheme.typography.bodyMedium)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { transitViewModel.checkForUpdates() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Yenilənmələri yoxla")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateToCredits,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Credits (Müəlliflər)")
                    }
                }
            }
        }
    }
}
