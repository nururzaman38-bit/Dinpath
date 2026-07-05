package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DeenViewModel
import com.example.data.*
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun PrayerTimesScreen(
    viewModel: DeenViewModel
) {
    val location by viewModel.settingsManager.location.collectAsState()
    val madhab by viewModel.settingsManager.madhab.collectAsState()
    val method by viewModel.settingsManager.calculationMethod.collectAsState()
    
    val times by viewModel.prayerTimes.collectAsState()
    val activeTracker by viewModel.activePrayerTracker.collectAsState()
    val calendar by viewModel.currentCalendar.collectAsState()

    // Persistent notifications states from settingsManager SharedPreferences
    val fNotif by viewModel.settingsManager.fajrNotification.collectAsState()
    val zNotif by viewModel.settingsManager.zuhrNotification.collectAsState()
    val aNotif by viewModel.settingsManager.asrNotification.collectAsState()
    val mNotif by viewModel.settingsManager.maghribNotification.collectAsState()
    val iNotif by viewModel.settingsManager.ishaNotification.collectAsState()
    val azanType by viewModel.settingsManager.azanType.collectAsState()

    val (currentPrayer, _) = remember(times, calendar) {
        val (nextName, minutes) = PrayerTimeHelper.getNextPrayerCountdown(times, calendar)
        // Basic deduction of active prayer
        val cur = when (nextName) {
            "যোহর" -> "ফজর"
            "আসর" -> "যোহর"
            "মাগরিব" -> "আসর"
            "এশা" -> "মাগরিব"
            "ফজর" -> "এশা"
            else -> "ফজর"
        }
        Pair(cur, nextName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        // Upper location details display
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 আপনার বর্তমান স্থান",
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = location,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("মাজহাব: $madhab", fontSize = 12.sp, color = TextSecondary)
                    Text("পদ্ধতি: ${method.split(" ")[0]}...", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        // Detailed prayer times list widget
        Text("🕒 আজকের ওয়াক্তসমূহ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Gold, modifier = Modifier.padding(bottom = 8.dp))

        val prayerRows = listOf(
            PrayerRowData("🌅 ফজর", times.fajr, "fajr", fNotif, activeTracker.fajr),
            PrayerRowData("☀️ সূর্যোদয়", times.sunrise, "sunrise", false, false, isSunrise = true),
            PrayerRowData("☀️ যোহর", times.zuhr, "zuhr", zNotif, activeTracker.zuhr),
            PrayerRowData("🌤️ আসর", times.asr, "asr", aNotif, activeTracker.asr),
            PrayerRowData("🌇 মাগরিব", times.maghrib, "maghrib", mNotif, activeTracker.maghrib),
            PrayerRowData("🌙 এশা", times.isha, "isha", iNotif, activeTracker.isha)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            prayerRows.forEach { row ->
                val isCurrent = currentPrayer in row.name
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (!row.isSunrise) viewModel.togglePrayerStatus(row.name.substring(3)) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) PrimaryGreen else SurfaceDark.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isCurrent) Gold else Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = row.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Gold else Color.White
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Gold, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("চলতি ওয়াক্ত", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ওয়াক্ত শুরু: ${row.time}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!row.isSunrise) {
                                // Notification bell icon toggle
                                IconButton(onClick = {
                                    viewModel.settingsManager.setNotificationEnabled(row.key, !row.notifEnabled)
                                }) {
                                    Icon(
                                        imageVector = if (row.notifEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                        contentDescription = "Notif Tracker",
                                        tint = if (row.notifEnabled) Gold else Color.LightGray.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // Check circle status
                                Icon(
                                    imageVector = if (row.checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Completed flag",
                                    tint = if (row.checked) AccentGreen else Color.LightGray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = "Sun",
                                    tint = Gold.copy(alpha = 0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Notification sound configuration
        Text("🔔 আজান নোটিফিকেশন সেটিংস", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Gold, modifier = Modifier.padding(bottom = 12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("আজানের কণ্ঠস্বর সিলেক্ট করুন:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                val sounds = listOf("Makkah", "Madinah", "Mishary Rashid", "শুধু নোটিফিকেশন")
                
                Column(modifier = Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sounds.forEach { sound ->
                        val isSel = azanType == sound
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.settingsManager.setAzanType(sound) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSel,
                                onClick = { viewModel.settingsManager.setAzanType(sound) },
                                colors = RadioButtonDefaults.colors(selectedColor = Gold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = sound, fontSize = 14.sp, color = if (isSel) Gold else TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

data class PrayerRowData(
    val name: String,
    val time: String,
    val key: String,
    val notifEnabled: Boolean,
    val checked: Boolean,
    val isSunrise: Boolean = false
)
