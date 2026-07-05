package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
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

        // ⑦ Daily Hadith Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        text = "📚 আজকের হাদিস",
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "সহীহ বুখারী — ৬০১৮",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "“যে ব্যক্তি আল্লাহ ও শেষ দিনে (পরকালে) বিশ্বাস রাখে, সে যেন সর্বদা উত্তম কথা বলে অথবা চুপ থাকে।”",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 21.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "আজকের হাদিস:\n“যে ব্যক্তি আল্লাহ ও শেষ দিনে বিশ্বাস রাখে, সে যেন উত্তম কথা বলে অথবা চুপ থাকে।”\n(সহীহ বুখারী — ৬০১৮) - দ্বীনপথ অ্যাপ")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Gold)
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
