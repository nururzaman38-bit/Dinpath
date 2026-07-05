package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DeenViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: DeenViewModel by viewModels()
            
            // Collect theme configuration from preferences StateFlow
            val themeName by viewModel.settingsManager.themeName.collectAsState()
            val onboardingCompleted by viewModel.settingsManager.onboardingCompleted.collectAsState()
            val splashFinished by viewModel.splashFinished.collectAsState()

            MyApplicationTheme(themeName = themeName) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        !splashFinished -> {
                            SplashScreen(
                                themeName = themeName,
                                onSplashFinished = { viewModel.finishSplash() }
                            )
                        }
                        !onboardingCompleted -> {
                            OnboardingScreen(
                                viewModel = viewModel,
                                onOnboardingFinished = {
                                    // Finished onboarding, home page will auto-load
                                }
                            )
                        }
                        else -> {
                            MainAppScaffold(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: DeenViewModel) {
    // Current primary tab index (0: হোমি, 1: কুরআন, 2: দোয়া, 3: তাসবিহ, 4: আরও)
    var currentTab by remember { mutableStateOf(0) }
    
    // Sub-modules inside "More" tab (settings, calendar, hadiths, salah, ramadan)
    var activeMoreSection by remember { mutableStateOf("menu") }

    val location by viewModel.settingsManager.location.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🕌 দ্বীনপথ",
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                            fontSize = 20.sp
                        )
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "📍 $location",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary
            ) {
                val items = listOf(
                    Triple("হোম", Icons.Default.Home, 0),
                    Triple("কুরআন", Icons.Default.Book, 1),
                    Triple("দোয়া", Icons.Default.Favorite, 2),
                    Triple("তাসবিহ", Icons.Default.CompassCalibration, 3),
                    Triple("আরও", Icons.Default.Menu, 4)
                )

                items.forEach { (label, icon, idx) ->
                    val isSelected = currentTab == idx
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentTab = idx
                            if (idx == 4) {
                                activeMoreSection = "menu" // reset more menu list when tab clicked
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) Gold else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Gold else Color.Gray,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = PrimaryGreen.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    if (viewModel.settingsManager.themeName.value == "LIGHT") BackgroundLight else BackgroundDark
                )
        ) {
            when (currentTab) {
                0 -> HomeScreen(viewModel = viewModel) { sectionName ->
                    // Navigation routing support from Quick Actions
                    when (sectionName) {
                        "quran" -> currentTab = 1
                        "dua" -> currentTab = 2
                        "tasbih" -> currentTab = 3
                        "qibla" -> {
                            currentTab = 4
                            activeMoreSection = "qibla"
                        }
                        "ramadan" -> {
                            currentTab = 4
                            activeMoreSection = "ramadan"
                        }
                        "ai_chat" -> {
                            currentTab = 4
                            activeMoreSection = "ai_chat"
                        }
                    }
                }
                1 -> QuranScreen(viewModel = viewModel)
                2 -> DuaScreen(viewModel = viewModel)
                3 -> TasbihScreen(viewModel = viewModel)
                4 -> {
                    // Sub-navigation handling for the "More" section
                    when (activeMoreSection) {
                        "menu" -> MoreMenuSection(
                            onNavigate = { target -> activeMoreSection = target }
                        )
                        "ai_chat" -> AiChatScreen(viewModel = viewModel)
                        "qibla" -> QiblaScreen(viewModel = viewModel)
                        "hadith" -> HadithScreen(viewModel = viewModel)
                        "salah" -> LearnSalahScreen(viewModel = viewModel)
                        "calendar" -> CalendarScreen(viewModel = viewModel)
                        "ramadan" -> RamadanScreen(viewModel = viewModel)
                        "settings" -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MoreMenuSection(
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "📂 আরও প্রয়োজনীয় ইসলামিক বিষয়াবলী",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Gold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val menuItems = listOf(
            Triple("🤖 ইসলামিক AI সহকারী (Chatbot)", "যেকোনো ইসলামিক প্রশ্ন ও ফতোয়া জানুন", "ai_chat"),
            Triple("🧭 কিবলা কম্পাস", "সঠিক ক্বিবলার মুখ নিরূপণ", "qibla"),
            Triple("📚 হাদিস শরীফ", "সহীহ বুখারী, মুসলিম কুতুবে সিত্তা", "hadith"),
            Triple("🎓 নামাজ শিক্ষা ও ওযু", "পূর্ণাঙ্গ ওয়াক্তভিত্তিক নামাজ শিক্ষা", "salah"),
            Triple("📅 হিজরি ক্যালেন্ডার", "গুরুত্বপূর্ণ ইসলামিক দিনসমূহ", "calendar"),
            Triple("🌙 রমজান মোবারক", "সাহরি ও ইফতার সময়সূচী", "ramadan"),
            Triple("⚙️ সেটিংস ও রুপান্তর", "ভাষা, থিম এবং হিস্ট্রি পরিষ্কার", "settings")
        )

        menuItems.forEach { (title, desc, destination) ->
            Card(
                onClick = { onNavigate(destination) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Navigate",
                        tint = Gold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
