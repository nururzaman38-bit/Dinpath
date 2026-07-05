package com.example.ui.screens

import android.widget.Toast
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
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: DeenViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val language by viewModel.settingsManager.language.collectAsState()
    val themeName by viewModel.settingsManager.themeName.collectAsState()
    val fontSize by viewModel.settingsManager.fontSize.collectAsState()
    val location by viewModel.settingsManager.location.collectAsState()
    val method by viewModel.settingsManager.calculationMethod.collectAsState()
    val madhab by viewModel.settingsManager.madhab.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "⚙️ সেটিংস ও কনফিগারেশন",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Gold,
            modifier = Modifier.fillMaxWidth()
        )

        // ① Language Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = "Lang", tint = Gold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("অ্যাপের ভাষা নির্বাচন (Language)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val bn = language == "BN"
                    Button(
                        onClick = { viewModel.settingsManager.setLanguage("BN") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (bn) Gold else SurfaceDark,
                            contentColor = if (bn) DarkGreen else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("বাংলা (BN)", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.settingsManager.setLanguage("EN") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!bn) Gold else SurfaceDark,
                            contentColor = if (!bn) DarkGreen else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("English (EN)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ② Theme Selector Card (Dark, Light, Green)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = "Theme", tint = Gold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("থিম মোড রূপান্তর (Theme System)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                val themesList = listOf(
                    Triple("DARK", "অন্ধকার রাত", "🕶️"),
                    Triple("LIGHT", "উজ্জ্বল দিন", "☀️"),
                    Triple("GREEN", "সবুজ মেরুন", "🕌")
                )

                themesList.forEach { (tKey, tDesc, emoji) ->
                    val isSel = themeName == tKey
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.settingsManager.setThemeName(tKey) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = tDesc, color = if (isSel) Gold else Color.White, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                        }
                        RadioButton(
                            selected = isSel,
                            onClick = { viewModel.settingsManager.setThemeName(tKey) },
                            colors = RadioButtonDefaults.colors(selectedColor = Gold)
                        )
                    }
                }
            }
        }

        // ③ City Location Calibration
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationCity, contentDescription = "City", tint = Gold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("অবস্থান ক্যালিব্রেশন (City)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("বর্তমানে নির্বাচিতঃ $location", fontSize = 12.sp, color = Gold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                val cities = listOf("ঢাকা", "চট্টগ্রাম", "সিলেট", "খুলনা", "রাজশাহী", "বরিশাল", "রংপুর", "ময়মনসিংহ", "কলকাতা")
                
                var expandedCity by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { expandedCity = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("অন্য জেলা সিলেক্ট করুন ▾", color = Color.White)
                    }

                    DropdownMenu(
                        expanded = expandedCity,
                        onDismissRequest = { expandedCity = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        cities.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c, color = Color.White) },
                                onClick = {
                                    viewModel.settingsManager.setLocation(c)
                                    expandedCity = false
                                    Toast.makeText(context, "$c স্থান হিসেবে লক হয়েছে!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // ④ Font Sizes Configurator
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.FormatSize, contentDescription = "Text Size", tint = Gold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("পবিত্র কুরআনের ফন্ট সাইজ", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                val sizes = listOf("ছোট", "মাঝারি", "বড়")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    sizes.forEach { s ->
                        val isSel = fontSize == s
                        Button(
                            onClick = { viewModel.settingsManager.setFontSize(s) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) Gold else SurfaceDark,
                                contentColor = if (isSel) DarkGreen else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(s, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // ⑤ Asr Calculation shadow (Hanafi/Shafi)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.HourglassEmpty, contentDescription = "Asr rule", tint = Gold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("আসর নামাজ ওয়াক্ত নির্ধারণ পদ্ধতি", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val isHanafi = madhab == "হানাфী" || madhab == "হানাফী"
                    Button(
                        onClick = { viewModel.settingsManager.setMadhab("হানাফী") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHanafi) Gold else SurfaceDark,
                            contentColor = if (isHanafi) DarkGreen else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("হানাফী (দ্বিগুণ ছায়া)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { viewModel.settingsManager.setMadhab("শাফেয়ী") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isHanafi) Gold else SurfaceDark,
                            contentColor = if (!isHanafi) DarkGreen else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("শাফেয়ী / অন্যান্য", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // ⑥ Dev Info Credit & Purge data logs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "দ্বীনপথ — ভলিউম ১.০.০\nসংস্করণ: v1.0.0 (Build 126)\nআল্লাহর সন্তুষ্টি অর্জনে উৎসর্গীকৃত",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        viewModel.clearSavedTasbihHistory()
                        viewModel.settingsManager.setOnboardingCompleted(false)
                        viewModel.settingsManager.setCompletedRoshasCount(0)
                        Toast.makeText(context, "সকল হিস্ট্রি ও ডাটা সম্পূর্ণ পরিষ্কার করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🗑️ সকল ডাটা ও ইতিহাস পরিষ্কার করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
