package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DeenViewModel
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun QuranScreen(
    viewModel: DeenViewModel
) {
    val context = LocalContext.current
    var selectedSurah by remember { mutableStateOf<Surah?>(null) }
    var activeTab by remember { mutableIntStateOf(0) } // 0: সূচী, 1: বুকমার্ক
    var searchQuery by remember { mutableStateOf("") }

    val bookmarks by viewModel.allBookmarks.collectAsState()
    val playingSurah by viewModel.playingSurahName.collectAsState()
    val isPlaying by viewModel.isPlayingAudio.collectAsState()
    val qari by viewModel.playingQari.collectAsState()
    val downloadPrompt by viewModel.surahDownloadPrompt.collectAsState()
    val downloadingSurah by viewModel.downloadingSurahName.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val audioProgress by viewModel.audioProgress.collectAsState()

    // Filtered surah list
    val filteredSurahs = remember(searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            QuranData.surahs
        } else {
            QuranData.surahs.filter {
                it.banglaName.contains(searchQuery, ignoreCase = true) ||
                it.englishName.contains(searchQuery, ignoreCase = true) ||
                it.id.toString() == searchQuery.trim()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedSurah == null) {
            // Surah index explorer
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(bottom = 80.dp)) {
                // Tab Selection Layout
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = Gold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = Gold
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("📖 সূরা তালিকা", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("🔖 বুকমার্কসমূহ", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (activeTab == 0) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        placeholder = { Text("সূরা খুঁজুন (যেমন: ফাতিহা বা ১৮)...", color = TextSecondary.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Gold) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = Gold,
                            unfocusedLabelColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Scroll list of Surahs
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredSurahs) { surah ->
                            Card(
                                onClick = { selectedSurah = surah },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                               ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Number badge circle
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(PrimaryGreen.copy(alpha = 0.6f), RoundedCornerShape(19.dp))
                                                .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(19.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = surah.id.toString(),
                                                color = Gold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column {
                                            Text(
                                                text = surah.banglaName,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${surah.type} • ${surah.versesCount} আয়াত",
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = surah.arabicName,
                                        fontSize = 20.sp,
                                        color = Gold,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Bookmarks list from Room database
                    val quranBookmarks = bookmarks.filter { it.type == "quran" }
                    if (quranBookmarks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔖", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("কোনো কুরআন বুকমার্ক নেই", color = TextSecondary, fontSize = 14.sp)
                                Text("আয়াতের দীর্ঘ ক্লিকে বুকমার্ক অপশন পাবেন", color = TextSecondary.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                            items(quranBookmarks) { bookmark ->
                                Card(
                                    onClick = {
                                        // Load the surah corresponding to bookmark.itemId format "surahId:ayahIdx"
                                        val surahId = bookmark.itemId.split(":")[0].toIntOrNull() ?: 1
                                        selectedSurah = QuranData.surahs.find { it.id == surahId }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(bookmark.title, fontWeight = FontWeight.Bold, color = Gold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(bookmark.subtitle, fontSize = 12.sp, color = TextPrimary)
                                        }
                                        Icon(imageVector = Icons.Default.BookmarkAdded, contentDescription = "Saved", tint = Gold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Surah detailed reading view screen (8B)
            val surah = selectedSurah!!
            SurahReaderView(
                surah = surah,
                viewModel = viewModel,
                onBack = { selectedSurah = null }
            )
        }

        downloadPrompt?.let { surah ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissSurahDownloadPrompt() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📥 অফলাইন অডিও ডাউনলোড", color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                text = {
                    Text(
                        text = "আপনি কি সূরা '${surah.banglaName}' এর অডিও (ক্বারী: $qari) অফলাইনে শোনার জন্য ডাউনলোড করতে চান? ডাউনলোড সম্পন্ন হলে ইন্টারনেট ছাড়াই শুনতে পারবেন।",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.downloadSurahAudio(surah) },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold)
                    ) {
                        Text("ডাউনলোড করুন", color = DarkGreen, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Row {
                        OutlinedButton(
                            onClick = { viewModel.startStreamingAudio(surah.banglaName) },
                            border = BorderStroke(1.dp, Gold)
                        ) {
                            Text("সরাসরি শুনুন", color = Gold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { viewModel.dismissSurahDownloadPrompt() }) {
                            Text("বাতিল", color = Color.LightGray)
                        }
                    }
                },
                containerColor = DarkGreen,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 8.dp
            )
        }

        if (downloadingSurah.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("⏳ অডিও ডাউনলোড হচ্ছে...", color = Gold, fontWeight = FontWeight.Bold) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("সূরা: $downloadingSurah\nদয়া করে অপেক্ষা করুন...", color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            color = Gold,
                            trackColor = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${(downloadProgress * 100).toInt()}%", color = Gold, fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {},
                containerColor = DarkGreen,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // 8C: Sticky Audio Player bottom card if alive
        if (playingSurah.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp) // offset above bottom bar
                    .padding(horizontal = 8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkGreen),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text("🎵", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "চলছে তিরওয়াত: $playingSurah",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "ক্বারী: $qari",
                                        fontSize = 11.sp,
                                        color = Gold
                                    )
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    if (isPlaying) viewModel.pauseAudioPlayback()
                                    else viewModel.startAudioPlayback(playingSurah)
                                }) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                        contentDescription = "Trigger",
                                        tint = Gold,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                IconButton(onClick = { viewModel.stopAudio() }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                                }
                            }
                        }
                        
                        // Real progress line
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { audioProgress },
                            color = Gold,
                            trackColor = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SurahReaderView(
    surah: Surah,
    viewModel: DeenViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val ayahs = remember(surah) { QuranData.getAyahsForSurah(surah.id) }
    val bookmarks by viewModel.allBookmarks.collectAsState()

    // Configurable display options from settings manager flows
    val showTranslation by viewModel.settingsManager.showTranslation.collectAsState()
    val showPronunciation by viewModel.settingsManager.showPronunciation.collectAsState()
    val fontSizeStr by viewModel.settingsManager.fontSize.collectAsState()

    // Dynamic state multipliers based on selected size config
    val scaleMultiplier = when (fontSizeStr) {
        "ছোট" -> 0.8f
        "বড়" -> 1.3f
        else -> 1.0f // "মাঝারি"
    }

    var showQariDialog by remember { mutableStateOf(false) }

    // Alert showing popup Tafsir values
    var selectedTafsirText by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)) {
        // Control Row Top Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(14.dp).statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Gold)
                    }
                    Text(
                        text = surah.banglaName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    IconButton(onClick = { showQariDialog = true }) {
                        Icon(imageVector = Icons.Default.Audiotrack, contentDescription = "Choose Reciter", tint = Gold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom control toggles bar
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.startAudioPlayback(surah.banglaName) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("▶ শুনুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val currentSize = when (fontSizeStr) {
                                "ছোট" -> "মাঝারি"
                                "মাঝারি" -> "বড়"
                                else -> "ছোট"
                            }
                            viewModel.settingsManager.setFontSize(currentSize)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ফন্ট: $fontSizeStr", fontSize = 11.sp, color = Gold)
                    }

                    Button(
                        onClick = { viewModel.settingsManager.setShowTranslation(!showTranslation) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showTranslation) Gold else SurfaceDark.copy(alpha = 0.5f),
                            contentColor = if (showTranslation) DarkGreen else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("অর্থ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.settingsManager.setShowPronunciation(!showPronunciation) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showPronunciation) Gold else SurfaceDark.copy(alpha = 0.5f),
                            contentColor = if (showPronunciation) DarkGreen else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("উচ্চারণ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Verses Lazy list content
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Include Bismillah Header for applicable Surahs
            if (surah.id != 1 && surah.id != 9) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            fontSize = (26 * scaleMultiplier).sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "পরম করুণাময়, অতি দয়ালু আল্লাহর নামে শুরু করছি।",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Render all verses
            items(ayahs) { ayah ->
                val ayahKey = "${surah.id}:${ayah.id}"
                val isBookmarked = bookmarks.any { it.type == "quran" && it.itemId == ayahKey }

                Card(
                    modifier = Modifier.fillMaxWidth().combinedClickable(
                        onClick = {
                            // Simple Tafsir simulation overlay trigger
                            selectedTafsirText = "তাফসীর ইবনে কাসীর (${surah.banglaName} : ${ayah.id}):\n\nএটি একটি ঐতিহাসিক পবিত্র আয়াত। আল্লাহর মহিমা এবং হুকুম বর্ণনা করা হয়েছে। মুমিনদের জন্য অত্যন্ত গুরুত্বপূর্ণ শিক্ষা এবং অনুপ্রেরণা রয়েছে এই আয়াতে। সর্বশক্তিমান আল্লাহর প্রতি অটল বিশ্বাসের মাধ্যমে ইহকাল ও পরকালে সাফল্য অর্জন সম্ভব।"
                        },
                        onLongClick = {
                            viewModel.toggleBookmark(
                                type = "quran",
                                itemId = ayahKey,
                                title = "${surah.banglaName} : আয়াত ${ayah.id}",
                                subtitle = ayah.banglaMeaning.take(60) + "..."
                            )
                            Toast.makeText(context, if (isBookmarked) "বুকমার্ক মুছে ফেলা হয়েছে" else "বুকমার্ক যোগ করা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // Right aligned Arabic calligraphy
                        Text(
                            text = ayah.arabic,
                            fontSize = (24 * scaleMultiplier).sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            lineHeight = (36 * scaleMultiplier).sp
                        )

                        // Bangla phonetic Pronunciation if enabled
                        if (showPronunciation) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = ayah.banglaPronunciation,
                                fontSize = (14 * scaleMultiplier).sp,
                                fontStyle = FontStyle.Italic,
                                color = TextSecondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Bangla translation translated meaning if enabled
                        if (showTranslation) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${ayah.id}. " + ayah.banglaMeaning,
                                fontSize = (15 * scaleMultiplier).sp,
                                fontWeight = FontWeight.Normal,
                                color = TextPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interaction controls bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Badge indicating verses reference
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("[${ayah.id}]", fontSize = (10 * scaleMultiplier).sp, color = Gold, fontWeight = FontWeight.Bold)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Bookmark trigger
                                IconButton(onClick = {
                                    viewModel.toggleBookmark(
                                        type = "quran",
                                        itemId = ayahKey,
                                        title = "${surah.banglaName} : আয়াত ${ayah.id}",
                                        subtitle = ayah.banglaMeaning.take(60) + "..."
                                    )
                                }) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = Gold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                
                                // Copy trigger
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Ayah copy", "${ayah.arabic}\n${ayah.banglaMeaning}\n(সূরা ${surah.banglaName} : ${ayah.id})")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "আয়াত কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                }

                                // Social share
                                IconButton(onClick = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "${ayah.arabic}\n\nঅর্থ: ${ayah.banglaMeaning}\n(সূরা ${surah.banglaName} : ${ayah.id}) - দ্বীনপথ অ্যাপ")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                                }) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                }
            }
        }
    }

    // 8C: Qari Selection Dialog
    if (showQariDialog) {
        AlertDialog(
            onDismissRequest = { showQariDialog = false },
            title = { Text("🎤 ক্বারী নির্বাচন করুন") },
            text = {
                val qaris = listOf(
                    "Mishary Rashid Al-Afasy",
                    "Abdul Rahman Al-Sudais",
                    "Saad Al-Ghamdi",
                    "Maher Al-Muaiqly",
                    "Abu Bakr Al-Shatri"
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    qaris.forEach { q ->
                        Button(
                            onClick = {
                                viewModel.selectQari(q)
                                showQariDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.playingQari.value == q) Gold else SurfaceDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(q, color = if (viewModel.playingQari.value == q) DarkGreen else Color.White)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQariDialog = false }) {
                    Text("বন্ধ করুন", color = Gold)
                }
            }
        )
    }

    // Interactive Tafsir simulation Dialog
    if (selectedTafsirText != null) {
        AlertDialog(
            onDismissRequest = { selectedTafsirText = null },
            title = { Text("📖 আয়াত তাফসীর", color = Gold, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = selectedTafsirText!!,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedTafsirText = null }) {
                    Text("ঠিক আছে", color = Gold, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
