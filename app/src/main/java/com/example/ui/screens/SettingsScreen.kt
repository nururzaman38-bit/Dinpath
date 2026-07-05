package com.example.ui.screens

import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
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

    // Admin secret access state
    val isAdminUnlocked by viewModel.settingsManager.isAdminUnlocked.collectAsState()
    val adminSecretPin by viewModel.settingsManager.adminSecretPin.collectAsState()
    var secretTapCount by remember { mutableStateOf(0) }
    var showSecretPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }

    if (showSecretPinDialog) {
        AlertDialog(
            onDismissRequest = { showSecretPinDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Gold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🔐 গোপন এডমিন এক্সেস", color = Gold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text("এডমিন প্যানেল আনলক করতে আপনার গোপন পিন কোডটি লিখুন (ডিফল্ট: 050126 বা 7860):", color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { enteredPin = it },
                        label = { Text("গোপন পিন কোড") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredPin == adminSecretPin || enteredPin == "7860" || enteredPin == "050126") {
                            viewModel.settingsManager.setAdminUnlocked(true)
                            showSecretPinDialog = false
                            enteredPin = ""
                            Toast.makeText(context, "✅ এডমিন এক্সেস সফল হয়েছে! এডমিন প্যানেল দৃশ্যমান হয়েছে।", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "❌ ভুল পিন কোড! আবার চেষ্টা করুন।", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("আনলক করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSecretPinDialog = false }) {
                    Text("বাতিল", color = TextSecondary)
                }
            }
        )
    }

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

        // ⑥ Admin Panel (এডমিন প্যানেল — শুধুমাত্র সিক্রেট আনলক করা এডমিনের জন্য দৃশ্যমান)
        if (isAdminUnlocked) {
            var showAdminPanel by remember { mutableStateOf(true) }
            val customApiKey by viewModel.settingsManager.customApiKey.collectAsState()
            val customModelId by viewModel.settingsManager.customModelId.collectAsState()
            val customDuasJson by viewModel.settingsManager.customDuasJson.collectAsState()
            val customFeedJson by viewModel.settingsManager.customFeedJson.collectAsState()
            val customQuizJson by viewModel.settingsManager.customQuizJson.collectAsState()

            var tempApiKey by remember(customApiKey) { mutableStateOf(customApiKey) }
            var tempModelId by remember(customModelId) { mutableStateOf(if (customModelId.isNotBlank()) customModelId else "google/gemini-2.5-flash") }

            // Dua form state
            var duaTitle by remember { mutableStateOf("") }
            var duaCat by remember { mutableStateOf("morning") }
            var duaArabic by remember { mutableStateOf("") }
            var duaPron by remember { mutableStateOf("") }
            var duaMeaning by remember { mutableStateOf("") }
            var duaSource by remember { mutableStateOf("") }
            var duaAudioUrl by remember { mutableStateOf("") }

            // Feed form state
            var feedCat by remember { mutableStateOf("নসিহত") }
            var feedText by remember { mutableStateOf("") }
            var feedSource by remember { mutableStateOf("") }

            // Quiz form state
            var quizQuestion by remember { mutableStateOf("") }
            var quizOpt1 by remember { mutableStateOf("") }
            var quizOpt2 by remember { mutableStateOf("") }
            var quizOpt3 by remember { mutableStateOf("") }
            var quizOpt4 by remember { mutableStateOf("") }
            var quizCorrectIdx by remember { mutableStateOf("1") }
            var quizExpl by remember { mutableStateOf("") }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdminPanel = !showAdminPanel },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = Gold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("🔐 এডমিন প্যানেল (AI, ফিড ও কুইজ)", fontWeight = FontWeight.Bold, color = Gold, fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            viewModel.settingsManager.setAdminUnlocked(false)
                            Toast.makeText(context, "🔒 এডমিন প্যানেল লক করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = ErrorRed)
                        }
                        Icon(
                            imageVector = if (showAdminPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = Gold
                        )
                    }
                }

                if (showAdminPanel) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Gold.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("🤖 OpenRouter AI কনফিগারেশন", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempApiKey,
                        onValueChange = { tempApiKey = it },
                        label = { Text("OpenRouter API Key (sk-or-v1-...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempModelId,
                        onValueChange = { tempModelId = it },
                        label = { Text("Model ID (যেমন: google/gemini-2.5-flash)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.settingsManager.setCustomApiKey(tempApiKey.trim())
                            viewModel.settingsManager.setCustomModelId(tempModelId.trim())
                            Toast.makeText(context, "✅ OpenRouter API Key এবং Model ID সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DarkGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("💾 AI সেটিংস সেভ করুন", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Gold.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("🤲 নতুন দোয়া আপলোড ও অডিও কনফিগারেশন", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkGreen.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "💡 অডিও লিংক নির্দেশনা: অডিও লিংকটি অবশ্যই সরাসরি .mp3 ফাইল হতে হবে (যেমন: https://example.com/dua.mp3 বা Archive.org / GitHub Raw-এর ডিরেক্ট লিংক)। Google Drive-এর সাধারণ শেয়ার লিংক কাজ করবে না। ইউজার একবার অডিও প্লে করলেই তা ফোনে অটো-ডাউনলোড ও সেভ হয়ে যাবে, পরে আর এমবি লাগবে না!",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = duaTitle,
                        onValueChange = { duaTitle = it },
                        label = { Text("দোয়ার শিরোনাম (যেমন: বিপদের দোয়া)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ক্যাটাগরি নির্বাচন করুন:", fontSize = 12.sp, color = Gold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("morning" to "সকাল-সন্ধ্যা", "sleep" to "ঘুম", "eat" to "আহার").forEach { (id, label) ->
                            val isSel = duaCat == id
                            Button(
                                onClick = { duaCat = id },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) Gold else SurfaceDark, contentColor = if (isSel) DarkGreen else Color.White),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        listOf("ramadan" to "রমজান", "travel" to "সফর", "salah" to "নামাজ").forEach { (id, label) ->
                            val isSel = duaCat == id
                            Button(
                                onClick = { duaCat = id },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) Gold else SurfaceDark, contentColor = if (isSel) DarkGreen else Color.White),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = duaArabic,
                        onValueChange = { duaArabic = it },
                        label = { Text("আরবি টেক্সট") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = duaPron,
                        onValueChange = { duaPron = it },
                        label = { Text("বাংলা উচ্চারণ") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = duaMeaning,
                        onValueChange = { duaMeaning = it },
                        label = { Text("বাংলা অর্থ") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = duaSource,
                        onValueChange = { duaSource = it },
                        label = { Text("উৎস (যেমন: বুখারী — ১২৩৪)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = duaAudioUrl,
                        onValueChange = { duaAudioUrl = it },
                        label = { Text("অডিও লিংক (.mp3 ডিরেক্ট লিংক)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (duaTitle.isBlank()) {
                                Toast.makeText(context, "দোয়ার শিরোনাম লিখুন!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            try {
                                val currentArr = if (customDuasJson.isNotBlank() && customDuasJson != "[]") org.json.JSONArray(customDuasJson) else org.json.JSONArray()
                                val newObj = org.json.JSONObject()
                                newObj.put("id", "custom_" + System.currentTimeMillis())
                                newObj.put("categoryId", duaCat)
                                newObj.put("title", duaTitle.trim())
                                newObj.put("arabic", duaArabic.trim())
                                newObj.put("pronunciation", duaPron.trim())
                                newObj.put("meaning", duaMeaning.trim())
                                newObj.put("source", if (duaSource.isNotBlank()) duaSource.trim() else "এডমিন আপলোড")
                                newObj.put("audioUrl", duaAudioUrl.trim())
                                currentArr.put(newObj)
                                viewModel.settingsManager.setCustomDuasJson(currentArr.toString())
                                Toast.makeText(context, "✅ নতুন দোয়া সফলভাবে আপলোড হয়েছে!", Toast.LENGTH_SHORT).show()
                                duaTitle = ""
                                duaArabic = ""
                                duaPron = ""
                                duaMeaning = ""
                                duaSource = ""
                                duaAudioUrl = ""
                            } catch (e: Exception) {
                                Toast.makeText(context, "এরর: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = "Upload", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("➕ নতুন দোয়া আপলোড করুন", fontWeight = FontWeight.Bold)
                    }

                    val customList = remember(customDuasJson) { DuaData.getCombinedDuas(customDuasJson).filter { it.id.startsWith("custom_") || it.id.startsWith("c_") } }
                    if (customList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("📋 আপলোডকৃত দোয়াসমূহ (${customList.size}টি):", fontWeight = FontWeight.Bold, color = Gold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        customList.forEach { cDua ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(cDua.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text("ক্যাটাগরি: ${cDua.categoryId} | অডিও: ${if (cDua.audioUrl.isNotBlank()) "যুক্ত" else "নেই"}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    IconButton(
                                        onClick = {
                                            try {
                                                val arr = org.json.JSONArray(customDuasJson)
                                                val newArr = org.json.JSONArray()
                                                for (i in 0 until arr.length()) {
                                                    val obj = arr.getJSONObject(i)
                                                    if (obj.optString("id") != cDua.id) {
                                                        newArr.put(obj)
                                                    }
                                                }
                                                viewModel.settingsManager.setCustomDuasJson(newArr.toString())
                                                Toast.makeText(context, "দোয়া মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {}
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // New Feed Upload Form
                    Text("📜 নতুন ইসলামিক নসিহত / স্ট্যাটাস আপলোড:", fontWeight = FontWeight.Bold, color = AccentGreen, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("নসিহত", "স্ট্যাটাস", "কুরআনের বাণী", "হাদিস").forEach { c ->
                            Button(
                                onClick = { feedCat = c },
                                colors = ButtonDefaults.buttonColors(containerColor = if (feedCat == c) Gold else SurfaceDark, contentColor = if (feedCat == c) DarkGreen else Color.White),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(c, fontSize = 10.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feedText,
                        onValueChange = { feedText = it },
                        label = { Text("স্ট্যাটাস বা নসিহতমূলক বাণী") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feedSource,
                        onValueChange = { feedSource = it },
                        label = { Text("সূত্র / লেখক (যেমন: সহীহ বুখারী বা ইবনুল কায়্যিম)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (feedText.isNotBlank()) {
                                try {
                                    val arr = org.json.JSONArray(customFeedJson)
                                    val obj = org.json.JSONObject()
                                    obj.put("id", "cfeed_" + System.currentTimeMillis())
                                    obj.put("category", feedCat)
                                    obj.put("text", "“${feedText.trim().removePrefix("“").removeSuffix("”")}”")
                                    obj.put("source", if (feedSource.isNotBlank()) feedSource.trim() else "এডমিন ফিড")
                                    obj.put("likes", 250)
                                    arr.put(obj)
                                    viewModel.settingsManager.setCustomFeedJson(arr.toString())
                                    feedText = ""
                                    feedSource = ""
                                    Toast.makeText(context, "✅ স্ট্যাটাস সফলভাবে আপলোড হয়েছে!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {}
                            } else {
                                Toast.makeText(context, "বাণীটি লিখুন!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("➕ ফিডে স্ট্যাটাস যুক্ত করুন", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // New Quiz Upload Form
                    Text("🧠 নতুন দৈনিক ইসলামিক কুইজ আপলোড:", fontWeight = FontWeight.Bold, color = Gold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quizQuestion,
                        onValueChange = { quizQuestion = it },
                        label = { Text("প্রশ্ন লিখুন") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = quizOpt1, onValueChange = { quizOpt1 = it }, label = { Text("অপশন ১") }, singleLine = true, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = quizOpt2, onValueChange = { quizOpt2 = it }, label = { Text("অপশন ২") }, singleLine = true, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = quizOpt3, onValueChange = { quizOpt3 = it }, label = { Text("অপশন ৩") }, singleLine = true, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = quizOpt4, onValueChange = { quizOpt4 = it }, label = { Text("অপশন ৪") }, singleLine = true, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = quizCorrectIdx, onValueChange = { quizCorrectIdx = it }, label = { Text("সঠিক অপশন নং (১-৪)") }, singleLine = true, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = quizExpl, onValueChange = { quizExpl = it }, label = { Text("সংক্ষিপ্ত ব্যাখ্যা") }, singleLine = true, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (quizQuestion.isNotBlank() && quizOpt1.isNotBlank() && quizOpt2.isNotBlank()) {
                                try {
                                    val arr = org.json.JSONArray(customQuizJson)
                                    val obj = org.json.JSONObject()
                                    obj.put("id", "cquiz_" + System.currentTimeMillis())
                                    obj.put("question", quizQuestion.trim())
                                    val optArr = org.json.JSONArray()
                                    optArr.put(quizOpt1.trim()); optArr.put(quizOpt2.trim())
                                    if (quizOpt3.isNotBlank()) optArr.put(quizOpt3.trim())
                                    if (quizOpt4.isNotBlank()) optArr.put(quizOpt4.trim())
                                    obj.put("options", optArr)
                                    val cIdx = (quizCorrectIdx.toIntOrNull() ?: 1) - 1
                                    obj.put("correctIndex", cIdx.coerceIn(0, 3))
                                    obj.put("explanation", if (quizExpl.isNotBlank()) quizExpl.trim() else "সঠিক উত্তর নির্বাচন করা হয়েছে।")
                                    arr.put(obj)
                                    viewModel.settingsManager.setCustomQuizJson(arr.toString())
                                    quizQuestion = ""; quizOpt1 = ""; quizOpt2 = ""; quizOpt3 = ""; quizOpt4 = ""; quizExpl = ""
                                    Toast.makeText(context, "✅ কুইজ সফলভাবে আপলোড হয়েছে!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {}
                            } else {
                                Toast.makeText(context, "প্রশ্ন এবং অন্তত ২টি অপশন লিখুন!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DarkGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("➕ নতুন কুইজ যুক্ত করুন", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.settingsManager.setAdminUnlocked(false)
                            Toast.makeText(context, "🔒 এডমিন প্যানেল লক ও হাইড করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🔒 এডমিন প্যানেল লক করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        }

        // ⑦ Dev Info Credit & Purge data logs
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
                    lineHeight = 16.sp,
                    modifier = Modifier.clickable {
                        secretTapCount++
                        if (secretTapCount == 3) {
                            Toast.makeText(context, "🔐 গোপন এডমিন প্যানেল খুলতে আর ৪ বার ট্যাপ করুন...", Toast.LENGTH_SHORT).show()
                        } else if (secretTapCount >= 7) {
                            secretTapCount = 0
                            showSecretPinDialog = true
                        }
                    }
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
