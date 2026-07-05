package com.example.ui.screens

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DeenViewModel
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class IslamicEvent(
    val hijriDate: String,
    val gregorianDate: String,
    val banglaDate: String,
    val name: String,
    val description: String,
    val type: String, // "Holiday" or "Ayam"
    val recommendedAmals: List<String>
)

object IslamicCalendarData {
    val items = listOf(
        IslamicEvent(
            hijriDate = "১৫ শাবান",
            gregorianDate = "২১ ফেব্রুয়ারি ২০২৬",
            banglaDate = "৮ ফাল্গুন ১৪৩২",
            name = "শবে বরাত (Shab-e-Barat)",
            description = "ভাগ্য নির্ধারণ ও ক্ষমা প্রার্থনার বিশেষ মহিমান্বিত রজনী। এই রাতে আল্লাহ তায়ালা সৃষ্টির আগামী এক বছরের রিযিক ও ভাগ্য বন্টন করেন এবং অসংখ্য বান্দাকে ক্ষমা করেন।",
            type = "Holiday",
            recommendedAmals = listOf(
                "এশার ও মাগরিবের পর নফল নামাজ আদায় করা ও দীর্ঘ সিজদা দেওয়া",
                "কুরআন তেলাওয়াত, জিকির ও ইস্তিগফার (ক্ষমা প্রার্থনা) করা",
                "পরবর্তী দিন (১৫ শাবান) নফল রোজা রাখা",
                "মৃত পিতা-মাতা ও আত্মীয়দের জন্য কবর জিয়ারত ও দোয়া করা"
            )
        ),
        IslamicEvent(
            hijriDate = "১ রমজান",
            gregorianDate = "২ মার্চ ২০২৬",
            banglaDate = "১৭ ফাল্গুন ১৪৩২",
            name = "রমজান মাস শুরু",
            description = "পূণ্যময় সংযম ও সিয়াম সাধনার পবিত্র মাসের সূচনা। এই মাসে একটি ফরজ কাজের সওয়াব ৭০ গুণ এবং নফলের সওয়াব ফরজের সমান দেওয়া হয়।",
            type = "Holiday",
            recommendedAmals = listOf(
                "প্রতিদিন সঠিক সময়ে সাহরি ও ইফতার করা",
                "এশার নামাজের পর ২০ রাকাত তারাবীহ নামাজ আদায় করা",
                "দৈনিক অন্তত ১ পারা বা তার বেশি কুরআন তেলাওয়াত করা",
                "মিথ্যা কথা, গিবত ও গুনাহের কাজ থেকে সম্পূর্ণ বিরত থাকা"
            )
        ),
        IslamicEvent(
            hijriDate = "২৭ রমজান",
            gregorianDate = "২৮ মার্চ ২০২৬",
            banglaDate = "১৪ চৈত্র ১৪৩২",
            name = "লাইলাতুল কদর (Shab-e-Qadr)",
            description = "হাজার মাস অপেক্ষা শ্রেষ্ঠ বরকতময় পবিত্র কোরআন নাজিলের রাত। এই এক রাতের ইবাদত ৮৩ বছর ৪ মাসের ইবাদতের চেয়েও উত্তম।",
            type = "Holiday",
            recommendedAmals = listOf(
                "বিশেষ দোয়া পড়া: 'আল্লাহুম্মা ইন্নাকা আফুওন তুহিব্বুল আফওয়া ফা'ফু আন্নী'",
                "সারারাত জেগে নফল নামাজ, তসবিহ-তাহলিল ও দরুদ পাঠ করা",
                "নিজের ও সারা মুসলিম উম্মাহর গুনাহ মাফের জন্য অশ্রুসিক্ত চোখে দোয়া করা",
                "দান-সদকা ও ইতিকাফের মাধ্যমে আল্লাহর নৈকট্য অর্জন করা"
            )
        ),
        IslamicEvent(
            hijriDate = "১ শাওয়াল",
            gregorianDate = "১ এপ্রিল ২০২৬",
            banglaDate = "১৮ চৈত্র ১৪৩২",
            name = "ঈদুল ফিতর (Eid-ul-Fitr)",
            description = "পবিত্র সুস্থ সিয়াম সাধনা সমাপন আনতে মুমিনদের পরম আনন্দের উৎসব। এই দিনে রোজা রাখা হারাম এবং ঈদের আনন্দ ভাগ করে নেওয়া সুন্নত।",
            type = "Holiday",
            recommendedAmals = listOf(
                "ঈদের নামাজের পূর্বে অবশ্যই ফিতরা (সদকাতুল ফিতর) আদায় করা",
                "সকালে মিষ্টি বা খেজুর খেয়ে ঈদের ময়দানে রওনা হওয়া",
                "উচ্চস্বরে তাকবীর বলা: 'আল্লাহু আকবার আল্লাহু আকবার লা ইলাহা ইল্লাল্লাহু...'",
                "আত্মীয়-স্বজন ও পাড়া-প্রতিবেশীর খোঁজখবর নেওয়া ও সালাম বিনিময়"
            )
        ),
        IslamicEvent(
            hijriDate = "৯ জিলহজ",
            gregorianDate = "২৭ মে ২০২৬",
            banglaDate = "১৩ জ্যৈষ্ঠ ১৪৩৩",
            name = "আরাফাহর দিন",
            description = "পবিত্র হজ পালনের আরাফাত ময়দানের মহাসমাবেশ ও রোজার ফজিলত। রাসুল (সাঃ) বলেছেন, আরাফাহর দিনের রোজা বিগত ও আগামী এক বছরের গুনাহ মাফ করে দেয়।",
            type = "Holiday",
            recommendedAmals = listOf(
                "৯ জিলহজ ফজর থেকে তাকবীরে তাশরীফ পাঠ শুরু করা",
                "হজে না থাকলে এই দিনে বিশেষ সওয়াবের আশায় নফল রোজা রাখা",
                "বেশি বেশি দরুদ শরীফ, লা ইলাহা ইল্লাল্লাহ ও ইস্তিগফার পড়া",
                "কোরবানির পশু ক্রয় ও ত্যাগের প্রস্তুতি সম্পন্ন করা"
            )
        ),
        IslamicEvent(
            hijriDate = "১০ জিলহজ",
            gregorianDate = "২৮ মে ২০২৬",
            banglaDate = "১৪ জ্যৈষ্ঠ ১৪৩৩",
            name = "ঈদুল আজহা (Eid-ul-Adha)",
            description = "ঐতিহাসিক কোরবানি ও সর্বোচ্চ ত্যাগ উৎসর্গের মহিমান্বিত মুসলিম উৎসব। হযরত ইব্রাহিম (আঃ) ও ইসমাইল (আঃ) এর ত্যাগের স্মৃতিচারণ।",
            type = "Holiday",
            recommendedAmals = listOf(
                "সকালে কিছু না খেয়ে ঈদের নামাজ আদায় করা এবং কোরবানির গোশত দিয়ে প্রথম খাবার খাওয়া",
                "সামর্থ্যবান ব্যক্তিদের জন্য আল্লাহর সন্তুষ্টির উদ্দেশ্যে হালাল পশু কোরবানি করা",
                "কোরবানির গোশত তিন ভাগে ভাগ করে গরিব-দুঃখী ও আত্মীয়দের মাঝে বন্টন করা",
                "আইয়ামে তাশরীফ (১৩ জিলহজ আসর পর্যন্ত) প্রতি ফরজ নামাজের পর তাকবীর বলা"
            )
        ),
        IslamicEvent(
            hijriDate = "১০ মহররম",
            gregorianDate = "২৭ জুন ২০২৬",
            banglaDate = "১৩ আষাঢ় ১৪৩৩",
            name = "আশুরা রজনী (Ashura)",
            description = "সত্যের পক্ষে কারবালার ঐতিহাসিক বিপ্লব ও ঐতিহাসিক রোজা রাখার দিন। হযরত মুসা (আঃ) এর ফেরাউনের হাত থেকে মুক্তি পাওয়ার স্মরণীয় দিন।",
            type = "Holiday",
            recommendedAmals = listOf(
                "৯ ও ১০ মহররম অথবা ১০ ও ১১ মহররম মোট ২টি রোজা রাখা",
                "নিজের পরিবারের জন্য এই দিনে ভালো খাবারের ব্যবস্থা করা (রিযিকে বরকত হয়)",
                "কারবালার শহীদদের ত্যাগের শিক্ষা জীবনে ধারণ করা",
                "বেশি বেশি ইস্তিগফার ও নফল ইবাদতে সময় কাটানো"
            )
        ),
        IslamicEvent(
            hijriDate = "১২ রবিউল আউয়াল",
            gregorianDate = "৪ সেপ্টেম্বর ২০২৬",
            banglaDate = "২০ ভাদ্র ১৪৩৩",
            name = "ঈদে মিলাদুন্নবী (সাঃ)",
            description = "বিশ্বনবী হযরত মুহাম্মদ (সাঃ) এর পবিত্র মিলাদ ও আগমন দিবস স্মরণ। সমগ্র মানবজাতির জন্য রহমত হিসেবে রাসুল (সাঃ) এর পৃথিবীতে আগমন।",
            type = "Holiday",
            recommendedAmals = listOf(
                "রাসুলুল্লাহ (সাঃ) এর প্রতি বেশি বেশি দরুদ ও সালাম পাঠ করা",
                "সিরাতুন্নবী (সাঃ) বা নবীর জীবনী আলোচনা ও অধ্যয়ন করা",
                "নবীর সুন্নাতসমূহ নিজের বাস্তব জীবনে পুরোপুরি প্রয়োগ করার শপথ নেওয়া",
                "গরিব-দুঃখীদের মাঝে খাবার বিতরণ ও দান সদকা করা"
            )
        )
    )
}

@Composable
fun CalendarScreen(
    viewModel: DeenViewModel
) {
    val calendar by viewModel.currentCalendar.collectAsState()
    val hijri = remember(calendar) { PrayerTimeHelper.getHijriDate(calendar) }
    val banglaDateObj = remember(calendar) { BanglaCalendarHelper.getBanglaDate(calendar) }
    val banglaDateStr = "${banglaDateObj.day} ${banglaDateObj.monthName} ${banglaDateObj.year} বঙ্গাব্দ"
    val banglaSeason = banglaDateObj.seasonName
    
    val isSyncing by viewModel.isSyncingCalendar.collectAsState()
    val syncMessage by viewModel.calendarSyncMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: হিজরি, 1: বাংলা, 2: ইংরেজি
    var selectedEvent by remember { mutableStateOf<IslamicEvent?>(null) }

    val gregDateText = remember(calendar) {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("bn", "BD"))
        sdf.format(calendar.time)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar with Online Sync Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📅 সমন্বিত ত্রি-ক্যালেন্ডার",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Gold
                )
                Text(
                    text = "আরবি, বাংলা ও ইংরেজি ক্যালেন্ডার (২০২৬)",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Button(
                onClick = { viewModel.syncCalendarOnline() },
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DarkGreen),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DarkGreen, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সিঙ্ক হচ্ছে...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("অনলাইন সিঙ্ক", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sync Status Message Card
        syncMessage?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    IconButton(onClick = { viewModel.dismissCalendarSyncMessage() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Gold, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Three Calendar Selector Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark.copy(alpha = 0.6f),
            contentColor = Gold,
            modifier = Modifier.clip(RoundedCornerShape(14.dp)),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Gold,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("🌙 আরবি হিজরি", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("🇧🇩 বাংলা বঙ্গাব্দ", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("📅 ইংরেজি সন", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        // Active Calendar Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (selectedTab) {
                    0 -> {
                        // Arabic Hijri View
                        Text("🌙", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${hijri.day} ${hijri.monthName}, ${hijri.year} হিজরি",
                            color = Gold,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "চাঁদের অবস্থান অনুযায়ী নির্ভুল হিজরি সময়সূচী",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("চলতি হিজরি মাস", fontSize = 11.sp, color = TextSecondary)
                                Text(hijri.monthName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("মাসের দৈর্ঘ্য", fontSize = 11.sp, color = TextSecondary)
                                Text("২৯/৩০ দিন", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    1 -> {
                        // Bangla Calendar View
                        Text("🇧🇩", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = banglaDateStr,
                            color = Gold,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "বর্তমান ঋতু: $banglaSeason",
                            fontSize = 14.sp,
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "ℹ️ বাংলাদেশ সরকারের বাংলা একাডেমির সংশোধিত নতুন বর্ষপঞ্জি অনুযায়ী নির্ভুল বাংলা তারিখ।",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                    2 -> {
                        // English Calendar View
                        Text("📅", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = gregDateText,
                            color = Gold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "গ্রেগরিয়ান আন্তর্জাতিক বর্ষপঞ্জি ২০২৬",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("চলতি বছর", fontSize = 11.sp, color = TextSecondary)
                                Text("২০২৬ খ্রিস্টাব্দ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("লিপ ইয়ার (অধিবর্ষ)", fontSize = 11.sp, color = TextSecondary)
                                Text("না (৩৬৫ দিন)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Full Month Calendar Grid View
        var viewMonthOffset by remember { mutableStateOf(0) }
        val gridCal = remember(viewMonthOffset) {
            Calendar.getInstance().apply {
                add(Calendar.MONTH, viewMonthOffset)
                set(Calendar.DAY_OF_MONTH, 1)
            }
        }
        val maxDays = gridCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = gridCal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 7=Sat
        val monthYearStr = SimpleDateFormat("MMMM yyyy", Locale("bn", "BD")).format(gridCal.time)
        val selectedMonthHijri = PrayerTimeHelper.getHijriDate(gridCal)
        val context = LocalContext.current

        fun toBn(num: Any): String {
            val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
            return num.toString().map { if (it in '0'..'9') bnDigits[it - '0'] else it }.joinToString("")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewMonthOffset-- }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = Gold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(monthYearStr, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Gold)
                        Text("${selectedMonthHijri.monthName} ${selectedMonthHijri.year} হিজরি", fontSize = 12.sp, color = TextSecondary)
                    }
                    IconButton(onClick = { viewMonthOffset++ }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = Gold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(10.dp))

                // Days of week header
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহ", "শুক্র", "শনি").forEach { dow ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(dow, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (dow == "শুক্র") AccentGreen else Gold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Calendar days grid (rows of 7)
                val totalCells = (firstDayOfWeek - 1) + maxDays
                val rows = (totalCells + 6) / 7
                val todayCal = Calendar.getInstance()
                val isCurrentMonth = viewMonthOffset == 0

                for (r in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        for (c in 0 until 7) {
                            val cellIdx = r * 7 + c
                            val dayNum = cellIdx - (firstDayOfWeek - 1) + 1
                            if (dayNum in 1..maxDays) {
                                val isToday = isCurrentMonth && dayNum == todayCal.get(Calendar.DAY_OF_MONTH)
                                val cellCal = gridCal.clone() as Calendar
                                cellCal.set(Calendar.DAY_OF_MONTH, dayNum)
                                val cellHijri = PrayerTimeHelper.getHijriDate(cellCal)
                                val cellBangla = BanglaCalendarHelper.getBanglaDate(cellCal)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .background(
                                            color = if (isToday) Gold else if (c == 5) DarkGreen.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            Toast.makeText(context, "📅 $dayNum $monthYearStr\n🇧🇩 ${cellBangla.day} ${cellBangla.monthName}\n🌙 ${cellHijri.day} ${cellHijri.monthName}", Toast.LENGTH_LONG).show()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = toBn(dayNum),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isToday) DarkGreen else if (c == 5) AccentGreen else Color.White
                                        )
                                        Text(
                                            text = toBn(cellHijri.day),
                                            fontSize = 9.sp,
                                            color = if (isToday) DarkGreen.copy(alpha = 0.8f) else Gold.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("💡 যেকোনো তারিখে ট্যাপ করে সেই দিনের বাংলা ও হিজরি তারিখ দেখুন। শুক্রবার সবুজ চিহ্নিত।", fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Section header for events list
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🕌 গুরুত্বপূর্ণ ইসলামিক দিবস ও আমলসমূহ",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
            Text(
                text = "(ট্যাপ করুন)",
                fontSize = 11.sp,
                color = Gold
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            IslamicCalendarData.items.forEach { ev ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedEvent = ev },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Event Badge
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val parts = ev.hijriDate.split(" ")
                                Text(text = parts[0], fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Gold)
                                Text(text = parts.getOrNull(1) ?: "", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ev.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = ev.description,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 2,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📅 " + ev.gregorianDate, color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("🇧🇩 " + ev.banglaDate.split(" ").take(2).joinToString(" "), color = AccentGreen, fontSize = 11.sp)
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Details",
                            tint = Gold
                        )
                    }
                }
            }
        }
    }

    // Event Details Popup Modal
    selectedEvent?.let { ev ->
        AlertDialog(
            onDismissRequest = { selectedEvent = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✨ ${ev.name}", color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dates block
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                            Text("🌙 হিজরি তারিখ: ${ev.hijriDate} ১৪৪৭ হিজরি", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("📅 ইংরেজি তারিখ: ${ev.gregorianDate}", color = Gold, fontSize = 12.sp)
                            Text("🇧🇩 বাংলা তারিখ: ${ev.banglaDate}", color = AccentGreen, fontSize = 12.sp)
                        }
                    }

                    Text("📖 দিবসের তাৎপর্য ও পটভূমি:", color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = ev.description,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    Text("🤲 এই দিনে করণীয় বিশেষ আমলসমূহ:", color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    ev.recommendedAmals.forEachIndexed { index, amal ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Text("${index + 1}.", color = AccentGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp))
                            Text(amal, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, lineHeight = 17.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedEvent = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                ) {
                    Text("সুবহানাল্লাহ / বন্ধ করুন", color = DarkGreen, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkGreen,
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 8.dp
        )
    }
}
