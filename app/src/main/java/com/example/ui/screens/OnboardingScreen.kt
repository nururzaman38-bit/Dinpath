package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DeenViewModel
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    viewModel: DeenViewModel,
    onOnboardingFinished: () -> Unit
) {
    var currentPage by remember { mutableStateOf(1) }
    
    val language by viewModel.settingsManager.language.collectAsState()
    val calMethod by viewModel.settingsManager.calculationMethod.collectAsState()
    val madhab by viewModel.settingsManager.madhab.collectAsState()
    val location by viewModel.settingsManager.location.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        DarkGreen
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Identifier
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(
                    text = "🌙 দ্বীনপথ",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = 1.sp
                )
            }

            // Cross-fading center box to animate slide changes nicely
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                when (currentPage) {
                    1 -> PageWelcome()
                    2 -> PageLocation(location) { viewModel.settingsManager.setLocation(it) }
                    3 -> PageNotification()
                    4 -> PageSettings(
                        language = language,
                        calMethod = calMethod,
                        madhab = madhab,
                        onLangChanged = { viewModel.settingsManager.setLanguage(it) },
                        onMethodChanged = { viewModel.settingsManager.setCalculationMethod(it) },
                        onMadhabChanged = { viewModel.settingsManager.setMadhab(it) }
                    )
                }
            }

            // Bottom Buttons and indicators
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Progress indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    for (i in 1..4) {
                        val active = currentPage == i
                        Box(
                            modifier = Modifier
                                .size(if (active) 12.dp else 8.dp)
                                .background(
                                    color = if (active) Gold else Color.Gray.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                // Control Button
                if (currentPage < 4) {
                    Button(
                        onClick = { currentPage++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (currentPage == 1) "পরবর্তী →" else "ধাপ এগিয়ে যান",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.settingsManager.setOnboardingCompleted(true)
                            onOnboardingFinished()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gold,
                            contentColor = DarkGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "শুরু করি বিসমিল্লাহ 🕌",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PageWelcome() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.6f)),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "🕌",
                    fontSize = 80.sp
                )
            }
        }

        Text(
            text = "আস-সালামু আলাইকুম! 🌙",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Gold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "দ্বীনপথ অ্যাপে আপনাকে স্বাগতম",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "আপনার প্রতিদিনের বিশ্বস্ত ইসলামিক সঙ্গী — নামাজ, কুরআন, প্রয়োজনীয় দোয়া, হাদিস, তাসবিহ এবং আরো অনেক চমৎকার ফিচার নিয়ে সজ্জিত।",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun PageLocation(
    selectedCity: String,
    onCityChosen: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = Gold,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "সঠিক নামাজের সময়",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "আপনার জেলা অনুযায়ী সঠিক নামাজের ওয়াক্ত ও আজানের অ্যালার্ট পেতে আপনার কাছাকাছি অবস্থান সিলেক্ট করুন:",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Grid selection for divisions
        val citiesList = listOf("ঢাকা", "চট্টগ্রাম", "সিলেট", "খুলনা", "রাজশাহী", "বরিশাল", "রংপুর", "ময়মনসিংহ", "কলকাতা")
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp).verticalScroll(rememberScrollState())
        ) {
            citiesList.chunked(3).forEach { rowList ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowList.forEach { city ->
                        val isSelected = selectedCity == city
                        Button(
                            onClick = { onCityChosen(city) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Gold else SurfaceDark.copy(alpha = 0.5f),
                                contentColor = if (isSelected) DarkGreen else TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text(city, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PageNotification() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Notifications",
            tint = Gold,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "আজানের সময় মনে করিয়ে দেবে",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "প্রতিটি ফরজ ওয়াক্ত সালাতের সময় আজান এবং নামাজের ১০ মিনিট আগে প্রাক-সালাত রিমাইন্ডার পেতে অনুগ্রহ করে নোটিফিকেশন সচল রাখুন।",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        var enabled by remember { mutableStateOf(true) }
        Button(
            onClick = { enabled = !enabled },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled) AccentGreen else SurfaceDark,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                text = if (enabled) "✓ নোটিফিকেশন সচল করা হয়েছে" else "🔔 নোটিফিকেশন অনুমতি দিন",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PageSettings(
    language: String,
    calMethod: String,
    madhab: String,
    onLangChanged: (String) -> Unit,
    onMethodChanged: (String) -> Unit,
    onMadhabChanged: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Text(
            text = "পছন্দ অনুযায়ী সাজান",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Gold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Section 1: language
        Text("১. ভাষা নির্বাচন (Language)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            val bnSelected = language == "BN"
            Button(
                onClick = { onLangChanged("BN") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (bnSelected) Gold else SurfaceDark,
                    contentColor = if (bnSelected) DarkGreen else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("বাংলা (BN)", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { onLangChanged("EN") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!bnSelected) Gold else SurfaceDark,
                    contentColor = if (!bnSelected) DarkGreen else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("English (EN)", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section 2: Method
        Text("২. হিসাব পদ্ধতি (Calculation Method)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        val methods = listOf(
            "University of Islamic Sciences, Karachi",
            "Muslim World League",
            "Egyptian General Authority"
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            methods.forEach { method ->
                val isSelected = calMethod == method
                Card(
                    onClick = { onMethodChanged(method) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PrimaryGreen else SurfaceDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = method,
                        fontSize = 12.sp,
                        color = if (isSelected) Gold else TextPrimary,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section 3: Madhab
        Text("৩. মাযহাব (Asr Shadow Calculation)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            val isHanafi = madhab == "হানাফী"
            Button(
                onClick = { onMadhabChanged("হানাফী") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHanafi) Gold else SurfaceDark,
                    contentColor = if (isHanafi) DarkGreen else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("হানাফী (Hanafi)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Button(
                onClick = { onMadhabChanged("শাফেয়ী") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isHanafi) Gold else SurfaceDark,
                    contentColor = if (!isHanafi) DarkGreen else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("শাফেয়ী / অন্যান্য", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
