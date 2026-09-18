package com.zaur1.bakutransit.ui.util

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.zaur1.bakutransit.R
import com.zaur1.bakutransit.data.local.TransitStopEntity
import com.zaur1.bakutransit.data.model.TransportType

@Composable
fun MetroStationDetailPopup(
    stop: TransitStopEntity, 
    onDismiss: () -> Unit,
    onShowRoute: () -> Unit = {},
    footInfo: String? = null,
    carInfo: String? = null
) {
    val context = LocalContext.current
    
    // Görməli yerlər bazası
    val landmarks = remember(stop.name) {
        when {
            stop.name.contains("İçərişəhər") -> "Qız Qalası, Şirvanşahlar Sarayı, Filarmoniya"
            stop.name.contains("Sahil") -> "Bulvar, Tarqovı, Milli Kitabxana"
            stop.name.contains("28 May") -> "Dəmir Yolu Vağzalı, 28 Mall, ADNSU"
            stop.name.contains("Elmlər") -> "Hüseyn Cavid Parkı, BDU, Texniki Universitet"
            stop.name.contains("Koroğlu") -> "Milli Stadion, Heydər Əliyev Mərkəzi (yaxınlıqda)"
            stop.name.contains("Nizami") -> "Qış Parkı, Dram Teatrı"
            else -> "Yaxınlıqda parklar və mağazalar mövcuddur."
        }
    }

    // Qiymət müqayisəsi məntiqi (Bolt təxmini: 1.50 açılış + 0.60/km)
    val taxiFare = remember(footInfo) {
        val km = footInfo?.split(" ")?.get(0)?.replace(",", ".")?.toDoubleOrNull() ?: 1.0
        val price = 1.50 + (km * 0.60)
        "~%.2f AZN".format(price)
    }

    val imgId = getStationImage(stop.name)
    val finalPhoto = if (imgId != 0) imgId else R.drawable.projected_metro

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7))
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(stop.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Spacer(Modifier.height(12.dp))
                
                AsyncImage(
                    model = finalPhoto,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.projected_metro)
                )
                
                Spacer(Modifier.height(16.dp))

                // 3. Görməli yerlər (Landmarks)
                Text("🏛️ Görməli yerlər:", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                Text(landmarks, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))

                // 5. Qiymət Müqayisəsi
                Text("💰 Qiymət Müqayisəsi:", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🚇 Metro/Bus", fontSize = 11.sp, color = Color.Gray)
                        Text("0.60 AZN", fontWeight = FontWeight.Bold, color = Color(0xFF00639B))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🚕 Bolt/Uber", fontSize = 11.sp, color = Color.Gray)
                        Text(taxiFare, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🚶 Piyada", fontSize = 11.sp, color = Color.Gray)
                        Text("0.00 AZN", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    }
                }

                Spacer(Modifier.height(16.dp))
                
                // Məsafə datası
                Row { Text("🚶 Piyada: ", fontWeight = FontWeight.Bold, color = Color(0xFF00639B)); Text(footInfo ?: "...") }

                Spacer(Modifier.height(24.dp))
                Button(onClick = onShowRoute, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(26.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00639B))) {
                    Text("Marşrutu göstər", fontWeight = FontWeight.Bold)
                }
                
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Bağla", color = Color(0xFF00639B)) }
            }
        }
    }
}

private fun getStationImage(name: String): Int {
    val n = name.trim().lowercase()
        .replace("i̇", "i").replace("ə", "e").replace("ı", "i")
        .replace("ö", "o").replace("ü", "u").replace("ş", "s")
        .replace("ç", "c").replace("ğ", "g").replace(" ", "")
    return when {
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

fun getNormalizedResourceName(name: String): String {
    val b = name.trim().lowercase().replace("i̇", "i").replace("ə", "e").replace("ı", "i").replace(" ", "")
    return "st_$b"
}
