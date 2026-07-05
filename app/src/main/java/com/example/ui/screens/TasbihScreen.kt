package com.example.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DeenViewModel
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TasbihScreen(
    viewModel: DeenViewModel
) {
    val context = LocalContext.current
    val view = LocalView.current

    val count by viewModel.tasbihCount.collectAsState()
    val target by viewModel.tasbihTarget.collectAsState()
    val zikir by viewModel.selectedZikir.collectAsState()
    val history by viewModel.tasbihHistory.collectAsState()

    var showTargetDialog by remember { mutableStateOf(false) }
    var customTargetInput by remember { mutableStateOf("33") }
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: তাসবিহ, 1: ইতিহাস

    // Total counts today
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val totalCountToday = remember(history, todayDateStr) {
        history.filter { it.date == todayDateStr }.sumOf { it.count } + count
    }

    // Bounce tap scale animation
    var tapTrigger by remember { mutableStateOf(false) }
    val tapScale by animateFloatAsState(
        targetValue = if (tapTrigger) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        finishedListener = { tapTrigger = false },
        label = "click_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tab Headers: [ তাসবিহ ] [ ইতিহাস ]
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = Color.Transparent,
            contentColor = Gold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                    color = Gold
                )
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Tab(selected = activeSubTab == 0, onClick = { activeSubTab = 0 }) {
                Text("📿 তাসবিহ কাউন্টার", fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
            }
            Tab(selected = activeSubTab == 1, onClick = { activeSubTab = 1 }) {
                Text("📊 আমার জিকির ইতিহাস", fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
            }
        }

        if (activeSubTab == 0) {
            // Zikir Preset Horizontal Row Selection
            val presets = listOf("সুবহানাল্লাহ", "আলহামদুলিল্লাহ", "আল্লাহু আকবার", "লা ইলাহা ইল্লাল্লাহ", "সবুজ দুরুদ শরিফ")
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { pr ->
                    val isSel = zikir == pr
                    Card(
                        onClick = { viewModel.changeZikir(pr) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) PrimaryGreen else SurfaceDark.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, if (isSel) Gold else Color.Transparent)
                    ) {
                        Text(
                            text = pr,
                            fontSize = 13.sp,
                            color = if (isSel) Gold else Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Active Zikir Calligraphy card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val arabicCalligraphy = when (zikir) {
                        "সুবহানাল্লাহ" -> "سُبْحَانَ اللَّهِ"
                        "আলহামদুলিল্লাহ" -> "الْحَمْدُ لِلَّهِ"
                        "আল্লাহু আকবার" -> "اللَّهُ أَكْبَرُ"
                        "লা ইলাহা ইল্লাল্লাহ" -> "لَا إِلَٰهَ إِلَّا اللَّهُ"
                        else -> "صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ"
                    }
                    Text(
                        text = arabicCalligraphy,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = zikir,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Giant interactive click circular button register
            Box(
                modifier = Modifier
                    .size(242.dp)
                    .scale(tapScale)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // prevent default rectangular ripple
                    ) {
                        tapTrigger = true
                        // Interactive Haptic feedback vibration trigger
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        viewModel.incrementTasbih()

                        // Target chime trigger simulation
                        if (count + 1 == target) {
                            Toast.makeText(context, "🎉 অভিনন্দন! লক্ষ্য ($target) সফলভাবে পূর্ণ হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PrimaryGreen, DarkGreen)
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .border(
                        width = 4.dp,
                        brush = Brush.linearGradient(colors = listOf(Gold, LightGold)),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Background ripple guide circles
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .border(1.dp, Gold.copy(alpha = 0.15f), androidx.compose.foundation.shape.CircleShape)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "মোট গণনা",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = count.toString(),
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "লক্ষ্য: $target বার",
                        fontSize = 12.sp,
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Real-time linear progress indicator bar towards target goal
            val rawProgress = if (target > 0) count.toFloat() / target else 0f
            val animProgress by animateFloatAsState(
                targetValue = rawProgress.coerceIn(0f, 1f),
                animationSpec = tween(300),
                label = "progress_growth"
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("আজকের মোট জিকির: $totalCountToday বার", fontSize = 12.sp, color = TextSecondary)
                    Text("${(rawProgress * 100).toInt()}% সম্পন্ন", fontSize = 12.sp, color = Gold, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { animProgress },
                    color = Gold,
                    trackColor = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ট্যাপ করুন স্ক্রিনের যেকোনো জায়গায়",
                    fontSize = 11.sp,
                    color = TextSecondary.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action row buttons: RESET, UPDATE TARGET
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.resetTasbih() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Zikir")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("রিসেট / সেভ", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        customTargetInput = target.toString()
                        showTargetDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Configure Target")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("লক্ষ্য সেট", fontSize = 13.sp)
                }
            }
        } else {
            // ② History Tab: Displays Saved logs list from DB
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 সংরক্ষিত জিকির খতিয়ান",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(onClick = { viewModel.clearSavedTasbihHistory() }) {
                    Text("ইতিহাস মুছুন 🗑️", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("কোনো জিকির রেকর্ড সম্পন্ন করা হয়নি", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(history) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = log.zikirText, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "তারিখ: ${log.date}", color = TextSecondary, fontSize = 11.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${log.count} বার",
                                        fontWeight = FontWeight.Bold,
                                        color = Gold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Target Settings popup trigger
    if (showTargetDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text("🎯 জিকির লক্ষ্য নিরূপণ") },
            text = {
                Column {
                    Text("প্রতি সেশনে আপনার কাঙ্ক্ষিত জিকির লক্ষ্য লিখুন:", fontSize = 13.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customTargetInput,
                        onValueChange = { customTargetInput = it },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold),
                        singleLine = true,
                        placeholder = { Text("যেমন: ৩৩, ১০০ বা ৫০০") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedVal = customTargetInput.toIntOrNull() ?: 33
                        viewModel.updateTasbihTarget(parsedVal)
                        showTargetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DarkGreen)
                ) {
                    Text("সেট করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) {
                    Text("বাতিল", color = Color.White)
                }
            }
        )
    }
}
