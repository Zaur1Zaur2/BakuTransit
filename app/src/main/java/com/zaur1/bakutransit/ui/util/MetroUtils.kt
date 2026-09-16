package com.zaur1.bakutransit.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.zaur1.bakutransit.R
import com.zaur1.bakutransit.data.local.TransitStopEntity
import com.zaur1.bakutransit.data.model.TransportType

/**
 * Professional Detail Popup for stations.
 * Features strict coordinate-based hit detection support.
 */
@Composable
fun MetroStationDetailPopup(
    stop: TransitStopEntity, 
    onDismiss: () -> Unit,
    onShowRoute: () -> Unit = {},
    footInfo: String? = null,
    carInfo: String? = null
) {
    // IMAGE RESOLUTION
    val imgId = remember(stop.name) {
        val n = stop.name.trim().lowercase()
            .replace("i̇", "i").replace("ə", "e").replace("ı", "i")
            .replace("ö", "o").replace("ü", "u").replace("ş", "s")
            .replace("ç", "c").replace("ğ", "g").replace(" ", "")
            
        when {
            n.contains("iceriseher") -> R.drawable.st_iceriseher
            n.contains("sahil") -> R.drawable.st_sahil
            n.contains("28may") -> R.drawable.st_28may
            n.contains("genclik") -> R.drawable.st_genclik
            n.contains("nermanov") -> R.drawable.st_nermanov
            n.contains("bakmil") -> R.drawable.st_bakmil
            n.contains("ulduz") -> R.drawable.st_ulduz
            n.contains("koroglu") -> R.drawable.st_koroglu
            n.contains("qarayev") -> R.drawable.st_qaraqarayev
            n.contains("neftciler") -> R.drawable.st_neftciler
            n.contains("xalqlar") -> R.drawable.st_xalqalardoslugu
            n.contains("ehmedli") -> R.drawable.st_ehmedli
            n.contains("aslanov") -> R.drawable.st_heziaslanov
            n.contains("dernegul") -> R.drawable.st_dernegul
            n.contains("azadliq") -> R.drawable.st_azadliqprospekti
            n.contains("nesimi") -> R.drawable.st_nesimi
            n.contains("ecemi") -> R.drawable.st_memarecemi
            n.contains("20yanvar") -> R.drawable.st_20yanvar
            n.contains("insaatcilar") -> R.drawable.st_insaatcilar
            n.contains("akademiyasi") -> R.drawable.st_elmlerakademiyasi
            n.contains("nizami") -> R.drawable.st_nizami
            n.contains("cabbarli") -> R.drawable.st_cefercabbarli
            n.contains("xetai") -> R.drawable.st_xetai
            n.contains("xocesen") -> R.drawable.st_xocesen
            n.contains("avtovagzal") -> R.drawable.st_avtovagzal
            n.contains("8noyabr") -> R.drawable.st_8noyabr
            else -> 0
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7))
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(stop.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Spacer(Modifier.height(16.dp))
                
                AsyncImage(
                    model = if (imgId != 0) imgId else R.drawable.projected_metro,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.projected_metro)
                )
                Spacer(Modifier.height(16.dp))

                // ROUTING DATA
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row { Text("🚶 Piyada: ", fontWeight = FontWeight.Bold, color = Color(0xFF00639B)); Text(footInfo ?: "...") }
                    Row { Text("🚗 Maşınla: ", fontWeight = FontWeight.Bold, color = Color(0xFF00639B)); Text(carInfo ?: "...") }
                }
                
                Spacer(Modifier.height(12.dp))
                Text("💳 Gediş haqqı: 0.60 AZN", fontWeight = FontWeight.Bold, color = Color(0xFF00639B))
                
                if (stop.type == TransportType.METRO) {
                    Spacer(Modifier.height(12.dp)); Text("Xətlər:", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        stop.lineIds.forEach { id ->
                            val c = when { id.contains("red") -> Color(0xFFE53935); id.contains("green") -> Color(0xFF4CAF50); else -> Color(0xFF8E24AA) }
                            Box(modifier = Modifier.background(c, CircleShape).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text(if(id.contains("red")) "Qırmızı" else if(id.contains("green")) "Yaşıl" else "Bənövşəyi", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                Button(onClick = onShowRoute, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(26.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00639B))) {
                    Text("Marşrutu göstər", fontWeight = FontWeight.Bold)
                }
                
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Bağla", color = Color(0xFF00639B)) }
            }
        }
    }
}
