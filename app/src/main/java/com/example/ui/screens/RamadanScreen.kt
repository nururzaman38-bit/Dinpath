package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
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

@Composable
fun RamadanScreen(
    viewModel: DeenViewModel
) {
    val context = LocalContext.current
    val times by viewModel.prayerTimes.collectAsState()
    val completedRoshahs by viewModel.settingsManager.completedRoshasCount.collectAsState()

    // Estimate Sehri and Iftar limits
    val sehriTime = times.fajr // Fajr starts represents stop limits of Sehri
    val iftarTime = times.maghrib // Maghrib starts represents beginning of Iftar

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🌙 মাহে রমজান মোবারক ১৪৪৭",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Gold,
            modifier = Modifier.fillMaxWidth()
        )

        // Iftar Sehri Card with dual progress timers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Row(
                modifier = Modifier
                    .background(Brush.linearGradient(colors = listOf(PrimaryGreen, DarkGreen)))
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Iftar Time
                Column(horizontalAlignment = Alignment.Start) {
                    Text("🌇 ইফতারের সময়সীমা:", fontSize = 12.sp, color = Gold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(iftarTime, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("সূর্যাস্ত সমাপনান্তে", fontSize = 11.sp, color = TextPrimary.copy(alpha = 0.7f))
                }

                Box(modifier = Modifier.width(1.dp).height(50.dp).background(Gold.copy(alpha = 0.3f)))

                // Right Sehri Time
                Column(horizontalAlignment = Alignment.End) {
                    Text("🌅 সাহরির শেষ সময়:", fontSize = 12.sp, color = Gold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(sehriTime, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("ফজর ওয়াক্তের পূর্বে", fontSize = 11.sp, color = TextPrimary.copy(alpha = 0.7f))
                }
            }
        }

        // Ramadan Supplications Duas Card
        Text(text = "🤲 রমজানের প্রতিদিনের অতি জরুরি দোয়া", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White, modifier = Modifier.fillMaxWidth())

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Sehri Dua
                Text("১. নিয়ত করার দোয়া (সাহরি)", fontWeight = FontWeight.Bold, color = Gold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "نَوَيْتُ أَنْ أَصُومَ غَدًا مِنْ شَهْرِ رَمَضَانَ الْمُبَارَكِ...",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "উচ্চারণ: নাওয়াইতু আন আসুমা গাদাম মিন শাহরি রামাদ্বানাল মুবারাকি...",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Text(
                    text = "অনুবাদ: “আমি আগামীকাল পবিত্র রমজান মাসের রোজা রাখার দৃঢ় নিয়ত করছি।”",
                    fontSize = 13.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                // Iftar Dua
                Text("২. রোজা ভাঙ্গার দোয়া (ইফতার)", fontWeight = FontWeight.Bold, color = Gold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "اللَّهُمَّ لَكَ صُمْتُ وَعَلَى رِزْقِكَ أَفْطَرْتُ...",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "উচ্চারণ: আল্লাহুম্মা লাকা ছুমতু ওয়া আলা রিযক্বিকা আফত্বারতু...",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Text(
                    text = "অনুবাদ: “হে আল্লাহ! আমি আপনার সন্তুষ্টির উদ্দেশ্যে রোজা রেখেছি এবং আপনারই রিজিক দিয়ে ইফতার করছি।”",
                    fontSize = 13.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Ramadan Dua", "সাহরি ও ইফতারের দোয়া:\n\nসাহরির নিয়ত:\n“আমি আগামীকাল পবিত্র রমজান মাসের রোজা রাখার নিয়ত করছি।”\n\nইফতারের দোয়া:\n“হে আল্লাহ! আমি আপনার উদ্দেশ্যে রোজা রেখেছি এবং আপনার রিজিক দিয়ে ইফতার করছি।”\n- দ্বীনপথ অ্যাপ")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "দোয়া কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Dua", tint = Gold)
                    }
                }
            }
        }

        // Interactive 30-Day fasting checklist logs grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 ৩০ দিনের সিয়াম (রোজা) ট্র্যাকার",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )

            Text(
                text = "মোট সম্পন্ন: $completedRoshahs টি",
                color = Gold,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        // Horizontal scrolling or custom grid mapping 30 days
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "পবিত্র রমজানের দিনে সিয়াম সফলভাবে পূর্ণ করলে টিক দিন। এটি আপনার জমার খতিয়ান হিসেবে ডাটাবেসে চিরকাল সংরক্ষিত থাকবে।",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Render 30 buttons structured as chunks
                val totalDays = 30
                val columns = 5
                val chunkedList = (1..totalDays).chunked(columns)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    chunkedList.forEach { rowDays ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowDays.forEach { d ->
                                // If day is less than or equal to current completed roshahs, we consider it checked!
                                val isChecked = d <= completedRoshahs
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChecked) PrimaryGreen else SurfaceDark)
                                        .border(1.dp, if (isChecked) Gold else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            // Handle fast toggling increment or resetting
                                            if (isChecked) {
                                                // untick
                                                viewModel.settingsManager.setCompletedRoshasCount(d - 1)
                                            } else {
                                                // tick
                                                viewModel.settingsManager.setCompletedRoshasCount(d)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "দিন $d",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isChecked) Gold else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
