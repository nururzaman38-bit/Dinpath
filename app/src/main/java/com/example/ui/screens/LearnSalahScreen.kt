package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DeenViewModel
import com.example.ui.theme.*

data class PostureStep(
    val id: Int,
    val actName: String,
    val emoji: String,
    val arabic: String,
    val pronunciation: String,
    val meaning: String,
    val guidance: String
)

object SalahSteps {
    val items = listOf(
        PostureStep(
            1, "নামাজের নিয়ত", "🧍",
            "نَوَيْتُ أَنْ أُصَلِّيَ لِلَّهِ تَعَالَى...",
            "নাওয়াইতু আন উসাল্লিয়া লিল্লাহি তায়ালা...",
            "আমি কেবল আল্লাহর সন্তুষ্টির উদ্দেশ্যে সালাতের নিয়ত করছি...",
            "ক্বিবলামুখী হয়ে সোজা হয়ে দাঁড়ান, দৃষ্টি সেজদার স্থানে রাখুন।"
        ),
        PostureStep(
            2, "তাকবীরে তাহরীমা", "🤲",
            "اللَّهُ أَكْبَرُ",
            "আল্লাহু আকবার",
            "আল্লাহ সবচেয়ে মহান (বড়)।",
            "উভয় হাত কাঁধ বরাবর বা কান পর্যন্ত তুলুন এবং বুকের উপর অথবা নাভির নিচে আস্থার সাথে হাত বাঁধুন।"
        ),
        PostureStep(
            3, "ছানা পাঠ", "🧍",
            "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ...",
            "সুবহানাকা আল্লাহুম্মা ওয়া বিহামদিকা ওয়াতাবারাকাসমুকা...",
            "হে আল্লাহ! প্রশংসার সাথে আপনার পবিত্রতা ঘোষণা করছি। আপনার নাম বরকতময়...",
            "বুকের ওপর হাত বাঁধা অবস্থায় মনে মনে খুশু-খুজুর সাথে ছানা পাঠ করুন।"
        ),
        PostureStep(
            4, "রুকু (অবনত হওয়া)", "🧎",
            "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
            "সুবহানা রাব্বিয়াল আজিম (৩ বার)",
            "আমার মহান প্রতিপালক অতি পবিত্র।",
            "মাথা ও পিঠ সমান্তরাল করে সোজা রেখে ঝুঁকে পড়ুন এবং দুই হাত দিয়ে হাঁটু শক্ত করে ধরুন।"
        ),
        PostureStep(
            5, "কওমা (রুকু থেকে উঠা)", "🧍",
            "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ • رَبَّنَا لَكَ الْحَمْدُ",
            "সামিয়াল্লাহু লিমান হামিদাহ • রাব্বানা লাকাল হামদ",
            "আল্লাহ শুনলেন তার কথা যে তার প্রশংসা করল • হে আমাদের প্রতিপালক! সমস্ত প্রশংসা আপনারই জন্য।",
            "রুকু হতে পুনরায় একদম সোজা হয়ে দাঁড়ান যাকে কওমা বলা হয়ে থাকে।"
        ),
        PostureStep(
            6, "সিজদাহ (ভূমিষ্ঠ অবনতি)", "🙇",
            "سُبْحَانَ رَبِّيَ الْأَعْلَى",
            "সুবহানা রাব্বিয়াল আলা (৩ বার)",
            "আমার সর্বশ্রেষ্ঠ প্রতিপালক অতি পবিত্র।",
            "হাঁটু, হাত, কপাল এবং নাক মাটি স্পর্শ করিয়ে সর্বোচ্চ বিনম্রতায় সেজদায় যান।"
        )
    )
}

@Composable
fun LearnSalahScreen(
    viewModel: DeenViewModel
) {
    val context = LocalContext.current
    var selectedTopic by remember { mutableStateOf<String?>(null) }
    var activeStepIdx by remember { mutableIntStateOf(0) }

    if (selectedTopic == null) {
        // ① Main Topics Menu
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎓 সহজ সালাত ও ওযূ প্রশিক্ষণ",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val topics = listOf(
                Triple("🚿 ওযূ শেখার সহজ পদ্ধতি", "ওযূর ফরজসমূহ এবং পবিত্রতার পূর্ণাঙ্গ ৭টি নিয়ম কানুন ছবিসহ", "🚿"),
                Triple("🕌 ৫ ওয়াক্ত সালাত শিক্ষা", "তাকবীর হতে সালাম পর্যন্ত পূর্ণাঙ্গ নামাজ আদায়ের নিয়মাবলী", "🕌"),
                Triple("📿 সালাত পরবর্তী তাসবিহসমূহ", "ফরজ নামাজ সমাপনান্তে পাঠ্য দোয়া ও মাসনুন জিকির সমূহ", "📿"),
                Triple("🧎 জানাজার নামাজ প্রশিক্ষণ", "চার তাকবীরে মৃত ব্যক্তির মাগফিরাত দোয়ার প্রশিক্ষণ", "🧎"),
                Triple("🕋 পবিত্র জুমার নামাজ নিয়ম", "জুমুআর দিনের বিশেষ ফজিলত ও নামাজের আবশ্যক রূপরেখা", "🕋"),
                Triple("🌙 তারাবির নামাজ প্রশিক্ষণ", "পবিত্র রমজানের তারাবীহ এবং বিতর সালাত পড়ার নিয়ম", "🌙")
            )

            topics.forEach { (title, subtitle, emoji) ->
                Card(
                    onClick = { selectedTopic = title },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(emoji, fontSize = 38.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                        }
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go", tint = Gold)
                    }
                }
            }
        }
    } else {
        // ② Step by Step Visual Guide page
        val steps = SalahSteps.items
        val currentStep = steps[activeStepIdx]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedTopic = null }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Gold)
                }

                Text(
                    text = "সালাত শিক্ষা — ধাপ ${activeStepIdx + 1}/${steps.size}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            // Silhouette / Emoji Visual card display
            Card(
                modifier = Modifier.size(160.dp).padding(bottom = 16.dp),
                shape = RoundedCornerShape(80.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.2f)),
                border = BorderStroke(2.dp, Gold)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = currentStep.emoji,
                        fontSize = 72.sp
                    )
                }
            }

            // Action focus state
            Text(
                text = "এখন আমল করুন: " + currentStep.actName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Gold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Text contents card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(18.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Arabic recitation
                    Text(
                        text = currentStep.arabic,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pronunciation
                    Text("উচ্চারণঃ", fontSize = 11.sp, color = Gold, fontWeight = FontWeight.Bold)
                    Text(
                        text = currentStep.pronunciation,
                        fontSize = 14.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Meaning
                    Text("অর্থঃ", fontSize = 11.sp, color = Gold, fontWeight = FontWeight.Bold)
                    Text(
                        text = "“" + currentStep.meaning + "”",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Guidance details
                    Text("নির্দেশনাঃ", fontSize = 11.sp, color = Gold, fontWeight = FontWeight.Bold)
                    Text(
                        text = currentStep.guidance,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Audio listening button trigger
                    Button(
                        onClick = {
                            Toast.makeText(context, "${currentStep.actName} তিলাওয়াত অডিও বাজছে...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Play voice")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("তিলাওয়াত শুনুন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation selector triggers
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (activeStepIdx > 0) activeStepIdx--
                    },
                    enabled = activeStepIdx > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("← আগের ধাপ")
                }

                Button(
                    onClick = {
                        if (activeStepIdx < steps.size - 1) activeStepIdx++
                    },
                    enabled = activeStepIdx < steps.size - 1,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("পরের ধাপ →")
                }
            }
        }
    }
}
