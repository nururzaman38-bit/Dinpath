package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun DuaScreen(
    viewModel: DeenViewModel
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<DuaCategory?>(null) }
    var selectedDuaIndex by remember { mutableStateOf<Int?>(null) }

    val bookmarks by viewModel.allBookmarks.collectAsState()
    val isPlayingDua by viewModel.isPlayingDua.collectAsState()
    val playingDuaTitle by viewModel.playingDuaTitle.collectAsState()

    if (selectedCategory == null) {
        // ① Categories Grid View (9A)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            Text(
                text = "🤲 দোয়া ও মুনাজাত সংকলন",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(DuaData.categories) { cat ->
                    Card(
                        onClick = {
                            selectedCategory = cat
                            selectedDuaIndex = 0 // start on first dua in cat
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(cat.icon, fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = cat.name.split(" ").drop(1).joinToString(" "),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${cat.count} টি দোয়া",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    } else {
        // ② Detailed scroll views with swipe navigation indexes (9B)
        val cat = selectedCategory!!
        val categoryDuas = remember(cat) { DuaData.duas.filter { it.categoryId == cat.id } }
        val activeIndex = selectedDuaIndex ?: 0
        val targetDua = categoryDuas.getOrNull(activeIndex) ?: categoryDuas.first()

        val isBookmarkedByDB = bookmarks.any { it.type == "dua" && it.itemId == targetDua.id }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    selectedCategory = null
                    selectedDuaIndex = null
                }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Gold)
                }

                Text(
                    text = cat.name.split(" ").drop(1).joinToString(" "),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )

                // Bookmark icon action
                IconButton(onClick = {
                    viewModel.toggleBookmark(
                        type = "dua",
                        itemId = targetDua.id,
                        title = "দোয়া: " + targetDua.title,
                        subtitle = targetDua.meaning.take(60) + "..."
                    )
                }) {
                    Icon(
                        imageVector = if (isBookmarkedByDB) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save",
                        tint = if (isBookmarkedByDB) Color.Red else Gold
                    )
                }
            }

            // Progress text indicating "দোয়া ১ / ১২"
            Text(
                text = "দোয়া: ${activeIndex + 1} / ${categoryDuas.size}",
                fontSize = 12.sp,
                color = Gold,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Main central scrollable card detailing Arabic text, phonetic translation, meaning and references
            Card(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = targetDua.title,
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Scrollable section for textual languages
                        Column(
                            modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Arabic Calligraphy Text
                            Text(
                                text = targetDua.arabic,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = 34.sp
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Pronunciation phonetics
                            Text(
                                text = "উচ্চারণ:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = targetDua.pronunciation,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                color = TextSecondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Translated meaning
                            Text(
                                text = "অর্থ:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "“" + targetDua.meaning + "”",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = 21.sp
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Reference source
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📚 উৎসঃ ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold)
                                Text(targetDua.source, fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    // Sound playback reciting button + share triggers
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isThisDuaPlaying = isPlayingDua && playingDuaTitle == targetDua.title
                        Button(
                            onClick = {
                                if (isThisDuaPlaying) {
                                    viewModel.stopDuaAudio()
                                } else {
                                    viewModel.playDuaAudio(targetDua.title)
                                    Toast.makeText(context, "দোয়ার মধুমাখা তিলাওয়াত অডিও শুরু হচ্ছে...", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isThisDuaPlaying) AccentGreen else PrimaryGreen
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isThisDuaPlaying) Icons.Default.Pause else Icons.Default.VolumeUp,
                                    contentDescription = if (isThisDuaPlaying) "Stop" else "Play",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isThisDuaPlaying) "থামান" else "শুনুন",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Button(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "দোয়া: ${targetDua.title}\n\nআরবি: ${targetDua.arabic}\n\nউচ্চারণ: ${targetDua.pronunciation}\n\nঅর্থ: ${targetDua.meaning}\n\nউৎস: ${targetDua.source}\n- দ্বীনপথ অ্যাপ")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "দোয়া শেয়ার করুন"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Gold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("শেয়ার", fontSize = 12.sp, color = Gold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation selector: [ ← আগের দোয়া ] and [ পরের দোয়া → ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (activeIndex > 0) selectedDuaIndex = activeIndex - 1
                    },
                    enabled = activeIndex > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("← আগের", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        if (activeIndex < categoryDuas.size - 1) selectedDuaIndex = activeIndex + 1
                    },
                    enabled = activeIndex < categoryDuas.size - 1,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("পরের →", fontSize = 13.sp)
                }
            }
        }
    }
}
