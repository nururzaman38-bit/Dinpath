package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DeenViewModel
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: DeenViewModel,
    onNavigateToSection: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val location by viewModel.settingsManager.location.collectAsState()
    val calendar by viewModel.currentCalendar.collectAsState()
    val times by viewModel.prayerTimes.collectAsState()
    val activeTracker by viewModel.activePrayerTracker.collectAsState()
    
    val completedRoshahs by viewModel.settingsManager.completedRoshasCount.collectAsState()

    // Sound alert triggers bound to real MediaPlayer in ViewModel
    val azanPlaying by viewModel.isPlayingAzan.collectAsState()
    val seasonEvent by viewModel.currentSeasonEvent.collectAsState()
    val locationAutoDetected by viewModel.locationAutoDetected.collectAsState()
    val locationDetecting by viewModel.locationDetecting.collectAsState()
    val locationStatusMessage by viewModel.locationStatusMessage.collectAsState()

    val updatePrompt by viewModel.updateAvailablePrompt.collectAsState()
    val downloadingApk by viewModel.isDownloadingApk.collectAsState()
    val apkProgress by viewModel.apkDownloadProgress.collectAsState()
    val config by viewModel.cloudConfig.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.fetchAndUpdateCurrentLocation(context)
    }

    // Hijri components
    val hijri = remember(calendar) { PrayerTimeHelper.getHijriDate(calendar) }

    // Display formatted Gregorian date
    val gregDateText = remember(calendar) {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("bn", "BD"))
        sdf.format(calendar.time)
    }

    // Tick countdown calculated dynamically
    val countdownInfo = remember(times, calendar) {
        val (nextName, minutes) = PrayerTimeHelper.getNextPrayerCountdown(times, calendar)
        val h = (minutes / 60).toInt()
        val m = (minutes % 60).toInt()
        val displayTime = if (h > 0) "$h ঘণ্টা $m মিনিট" else "$m মিনিট"
        Triple(nextName, displayTime, minutes)
    }

    updatePrompt?.let { prompt ->
        AlertDialog(
            onDismissRequest = { if (!prompt.forceUpdate) viewModel.dismissUpdatePrompt() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚀 ${prompt.updateTitle}", color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "নতুন সংস্করণ: ${prompt.latestVersionName}\n\n${prompt.updateNotes}",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ℹ️ আপডেট করলে নতুন সকল ফিচার ও সমাধান পাবেন এবং আপনার কোনো ডেটা হারাবে না।",
                        color = AccentGreen,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.startApkDownloadAndInstall(context, prompt.apkDownloadUrl) },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                ) {
                    Text("আপডেট করুন", color = DarkGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    OutlinedButton(
                        onClick = { viewModel.openApkInBrowser(context, prompt.apkDownloadUrl) },
                        border = BorderStroke(1.dp, Gold)
                    ) {
                        Text("লিংক খুলুন", color = Gold, fontSize = 12.sp)
                    }
                    if (!prompt.forceUpdate) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { viewModel.dismissUpdatePrompt() }) {
                            Text("পরে করুন", color = Color.LightGray)
                        }
                    }
                }
            },
            containerColor = DarkGreen,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        )
    }

    if (downloadingApk) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("⏳ আপডেট ডাউনলোড হচ্ছে...", color = Gold, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("দয়া করে অপেক্ষা করুন, ফাইলটি ডাউনলোড হয়ে গেলে স্বয়ংক্রিয়ভাবে ইনস্টল হবে...", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { apkProgress },
                        color = Gold,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(apkProgress * 100).toInt()}%", color = Gold, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {},
            containerColor = DarkGreen,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp) // Avoid overlap with bottom nav bar
    ) {
        config?.let { cfg ->
            if (cfg.announcementText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.9f)),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📢", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(cfg.announcementText, color = Color.White, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        locationStatusMessage?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkGreen),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(msg, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.dismissLocationStatusMessage() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Gold, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        if (!locationAutoDetected) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📍", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("স্বয়ংক্রিয় স্থান নির্ধারণ", fontWeight = FontWeight.Bold, color = Gold, fontSize = 13.sp)
                        Text("আপনার সঠিক জেলা ও নামাজের সময়সূচী পেতে জিপিএস চালু করুন", color = TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DarkGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(if (locationDetecting) "খোঁজা হচ্ছে..." else "শনাক্ত করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ① Gregorian + Hijri Date Block (Immersive borderless top info bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = gregDateText,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${hijri.day} ${hijri.monthName}, ${hijri.year} হিজরি",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = location,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // ② Next Prayer Card (Super premium visual indicator)
        val timeVal = when (countdownInfo.first) {
            "ফজর" -> times.fajr
            "যোহর" -> times.zuhr
            "আসর" -> times.asr
            "মাগরিব" -> times.maghrib
            "এশা" -> times.isha
            else -> times.fajr
        }

        val progressFloat = remember(countdownInfo.third) {
            val minutesLeft = countdownInfo.third
            val percent = ((240.0 - minutesLeft.coerceAtMost(240.0)) / 240.0).toFloat()
            percent.coerceIn(0f, 1f)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryGreen, DarkGreen)
                        )
                    )
                    .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Mosque,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "পরবর্তী নামাজ",
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.offset(y = (-0.5).dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = countdownInfo.first,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = timeVal.replace(" AM", "").replace(" PM", ""),
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold,
                                    lineHeight = 44.sp
                                )
                                if (timeVal.contains("AM") || timeVal.contains("PM")) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (timeVal.contains("AM")) "AM" else "PM",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .border(2.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "বাকি আছে",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = countdownInfo.second,
                                fontSize = 15.sp,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ওয়াক্ত প্রস্তুতি:",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "${(progressFloat * 100).toInt()}% সম্পন্ন",
                                fontSize = 11.sp,
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progressFloat },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Gold,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }

        // ③ 5 Prayer Times Row (Horizontal Checklist representation)
        Text(
            text = "🕌 নামাজ ট্র্যাকার ও সময়সূচী",
            fontWeight = FontWeight.Bold,
            color = Gold,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val prayerDetails = listOf(
                Triple("ফজর", times.fajr, activeTracker.fajr),
                Triple("যোহর", times.zuhr, activeTracker.zuhr),
                Triple("আসর", times.asr, activeTracker.asr),
                Triple("মাগরিব", times.maghrib, activeTracker.maghrib),
                Triple("এশা", times.isha, activeTracker.isha)
            )

            prayerDetails.forEach { (name, timeStr, checked) ->
                val isNextOne = countdownInfo.first == name
                Card(
                    modifier = Modifier
                        .width(96.dp)
                        .clickable { viewModel.togglePrayerStatus(name) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNextOne) PrimaryGreen else SurfaceDark.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        width = if (isNextOne) 2.dp else 1.dp,
                        color = if (isNextOne) Gold else Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNextOne) Color.White else TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timeStr.replace(" AM", "").replace(" PM", ""),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isNextOne) Gold else Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Icon(
                            imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Check",
                            tint = if (checked) {
                                if (isNextOne) Gold else AccentGreen
                            } else {
                                Color.White.copy(alpha = 0.2f)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ④ Azan Card (Quick audio tool)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text("📢", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "আজান শুনুন",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (azanPlaying) "চলছে মধুর আজান..." else "মক্কা ও মদীনার মধুর আওয়াজে",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                Button(
                    onClick = { 
                        if (azanPlaying) viewModel.stopAzan() else viewModel.playAzan()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (azanPlaying) AccentGreen else Gold,
                        contentColor = DarkGreen
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (azanPlaying) "⏹️ থামান" else "▶ শুনুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // ⑤ AI Assistant Card
        Card(
            onClick = { onNavigateToSection("ai_chat") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.85f)),
            border = BorderStroke(1.dp, Gold)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Gold.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ইসলামিক AI সহকারী",
                                fontWeight = FontWeight.Bold,
                                color = Gold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Gold),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("NEW", color = DarkGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Text(
                            text = "যেকোনো মাসয়ালা, দোয়া ও ফতোয়া সম্পর্কে চ্যাট করুন",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Chat",
                    tint = Gold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // ⑥ 2x2 Quick Action Grid
        Text(
            text = "⚡ দ্রুত অ্যাকশন",
            fontWeight = FontWeight.Bold,
            color = Gold,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        
        Column(modifier = Modifier.padding(16.dp)) {
            val rowCards = listOf(
                Pair("📖 কুরআন", "quran"),
                Pair("🤲 দোয়া", "dua"),
                Pair("📿 তাসবিহ", "tasbih"),
                Pair("🧭 কিবলা", "qibla")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickGridCard(title = rowCards[0].first, emoji = "📖", subtitle = "পবিত্র কালাম", modifier = Modifier.weight(1f)) {
                    onNavigateToSection(rowCards[0].second)
                }
                QuickGridCard(title = rowCards[1].first, emoji = "🤲", subtitle = "প্রার্থনা সংকলন", modifier = Modifier.weight(1f)) {
                    onNavigateToSection(rowCards[1].second)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickGridCard(title = rowCards[2].first, emoji = "📿", subtitle = "ডিজিটাল জিকির", modifier = Modifier.weight(1f)) {
                    onNavigateToSection(rowCards[2].second)
                }
                QuickGridCard(title = rowCards[3].first, emoji = "🧭", subtitle = "দিক নির্দেশনা", modifier = Modifier.weight(1f)) {
                    onNavigateToSection(rowCards[3].second)
                }
            }
        }

        // ⑥ Daily Ayah Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
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
                        text = "📖 আজকের আয়াত",
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "সূরা আল-ইনশিরাহ : ৬",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
                    fontSize = 22.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "“নিশ্চয়ই কষ্টের সাথে স্বস্তি রয়েছে।”",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "আজকের আয়াত:\nإِنَّ مَعَ الْعُسْرِ يُسْرًا\n“নিশ্চয়ই কষ্টের সাথে স্বস্তি রয়েছে।”\n(সূরা আল-ইনশিরাহ : ৬) - দ্বীনপথ অ্যাপ")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Gold)
                    }
                }
            }
        }

        // ⑦ Daily Islamic Quiz Section (দৈনিক ইসলামিক কুইজ ও জ্ঞান পরীক্ষা)
        val customQuizJson by viewModel.settingsManager.customQuizJson.collectAsState()
        val allQuizzes = remember(customQuizJson) { IslamicFeedAndQuizEngine.getCombinedQuizzes(customQuizJson) }
        var currentQuizIndex by remember { mutableStateOf(0) }
        val currentQuiz = remember(allQuizzes, currentQuizIndex) {
            allQuizzes.getOrNull(currentQuizIndex % allQuizzes.size) ?: allQuizzes.first()
        }
        var selectedOption by remember(currentQuiz) { mutableStateOf<Int?>(null) }
        var showQuizResult by remember(currentQuiz) { mutableStateOf(false) }
        val quizScore by viewModel.settingsManager.quizScore.collectAsState()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = "Quiz", tint = Gold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🧠 দৈনিক ইসলামিক কুইজ",
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                            fontSize = 15.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(DarkGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "পয়েন্ট: $quizScore 🏆", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = currentQuiz.question,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                currentQuiz.options.forEachIndexed { idx, optionText ->
                    val isSelected = selectedOption == idx
                    val isCorrect = idx == currentQuiz.correctIndex
                    val bgColor = when {
                        !showQuizResult -> if (isSelected) Gold.copy(alpha = 0.2f) else SurfaceDark
                        isCorrect -> PrimaryGreen.copy(alpha = 0.8f)
                        isSelected && !isCorrect -> ErrorRed.copy(alpha = 0.7f)
                        else -> SurfaceDark.copy(alpha = 0.4f)
                    }
                    val borderColor = when {
                        !showQuizResult -> if (isSelected) Gold else Color.White.copy(alpha = 0.1f)
                        isCorrect -> AccentGreen
                        isSelected && !isCorrect -> ErrorRed
                        else -> Color.Transparent
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = !showQuizResult) {
                                selectedOption = idx
                                showQuizResult = true
                                if (idx == currentQuiz.correctIndex) {
                                    viewModel.settingsManager.addQuizScore(10)
                                    Toast.makeText(context, "মাশাআল্লাহ! সঠিক উত্তর (+১০ পয়েন্ট) 🌟", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "উত্তর সঠিক হয়নি, আবার চেষ্টা করুন!", Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}. $optionText",
                                fontSize = 13.sp,
                                color = if (showQuizResult && isCorrect) Color.White else if (showQuizResult && isSelected && !isCorrect) Color.White else TextPrimary,
                                fontWeight = if (isSelected || (showQuizResult && isCorrect)) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (showQuizResult && isCorrect) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = AccentGreen, modifier = Modifier.size(18.dp))
                            } else if (showQuizResult && isSelected && !isCorrect) {
                                Icon(Icons.Default.Cancel, contentDescription = "Wrong", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                if (showQuizResult) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkGreen.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "💡 ব্যাখ্যা: ${currentQuiz.explanation}",
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            currentQuizIndex = (currentQuizIndex + 1) % allQuizzes.size
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DarkGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("পরবর্তী কুইজ দেখুন ➡️", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ⑦.২ Islamic Feed Section (ইসলামিক ফিড: নসিহত ও স্ট্যাটাস)
        val customFeedJson by viewModel.settingsManager.customFeedJson.collectAsState()
        val allFeeds = remember(customFeedJson) { IslamicFeedAndQuizEngine.getCombinedFeeds(customFeedJson) }
        var selectedFeedCategory by remember { mutableStateOf("সব") }
        val filteredFeeds = remember(allFeeds, selectedFeedCategory) {
            if (selectedFeedCategory == "সব") allFeeds else allFeeds.filter { it.category == selectedFeedCategory }
        }
        val coroutineScope = rememberCoroutineScope()
        var isScrapingOnline by remember { mutableStateOf(false) }
        var visibleFeedLimit by remember { mutableStateOf(4) }
        val likedStatusIds = remember { mutableStateMapOf<String, Boolean>() }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Article, contentDescription = "Feed", tint = AccentGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📜 ইসলামিক ফিড (নসিহত ও স্ট্যাটাস)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = "${filteredFeeds.size}টি পোস্ট",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("সব", "নসিহত", "স্ট্যাটাস", "কুরআনের বাণী", "হাদিস").forEach { cat ->
                        val isSel = selectedFeedCategory == cat
                        Button(
                            onClick = {
                                selectedFeedCategory = cat
                                visibleFeedLimit = 4
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) Gold else DarkGreen.copy(alpha = 0.7f),
                                contentColor = if (isSel) DarkGreen else Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(text = cat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Feeds List (Paginated / Smooth scroll)
                filteredFeeds.take(visibleFeedLimit).forEach { feed ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkGreen.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("#${feed.category}", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(feed.source, color = TextSecondary, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = feed.text,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.95f),
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isLiked = likedStatusIds[feed.id] == true
                                val displayLikes = feed.likes + (if (isLiked) 1 else 0)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        likedStatusIds[feed.id] = !isLiked
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isLiked) ErrorRed else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("$displayLikes", color = TextSecondary, fontSize = 12.sp)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Islamic Status", "${feed.text}\n(${feed.source}) — দ্বীনপথ অ্যাপ")
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "স্ট্যাটাসটি কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Gold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("কপি", color = Gold, fontSize = 12.sp)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            val shareIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, "${feed.text}\n— ${feed.source}\n(দ্বীনপথ অ্যাপ থেকে সংগ্রহ)")
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                                        }
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = AccentGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("শেয়ার", color = AccentGreen, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (visibleFeedLimit < filteredFeeds.size) {
                        Button(
                            onClick = { visibleFeedLimit += 5 },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = Gold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⬇️ আরও স্ট্যাটাস দেখুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            if (!isScrapingOnline) {
                                isScrapingOnline = true
                                Toast.makeText(context, "🌐 অনলাইন থেকে পাবলিক ইসলামিক ডাটা সিঙ্ক হচ্ছে...", Toast.LENGTH_SHORT).show()
                                coroutineScope.launch {
                                    val resMsg = IslamicFeedAndQuizEngine.scrapeOnlineFeedsAndQuizzes(viewModel.settingsManager)
                                    isScrapingOnline = false
                                    Toast.makeText(context, resMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isScrapingOnline) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সিঙ্ক হচ্ছে...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = "Scrape", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🌐 অনলাইন সিঙ্ক", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ⑧ Dynamic Seasonal Event & Amal Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onNavigateToSection(seasonEvent.targetScreen) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(DarkGreen, SurfaceDark)
                        )
                    )
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(seasonEvent.emoji, fontSize = 36.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = seasonEvent.title,
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = seasonEvent.subtitle,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go",
                        tint = Gold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = seasonEvent.description,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                if (seasonEvent.specialAmalList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("✨ আজকের বিশেষ আমল ও করণীয়:", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    seasonEvent.specialAmalList.take(2).forEach { amal ->
                        Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.Top) {
                            Text("•", color = AccentGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp))
                            Text(amal, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickGridCard(
    title: String,
    emoji: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DarkGreen, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title.replace(Regex("^[^\\s]+\\s+"), ""),
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
