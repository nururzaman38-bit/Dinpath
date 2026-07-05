package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class Hadith(
    val id: String,
    val collection: String,
    val number: String,
    val text: String,
    val narrator: String,
    val reference: String
)

object HadithData {
    val items = listOf(
        Hadith(
            "h1", "সহীহ বুখারী", "১",
            "“প্রতিটি কাজের ফলাফল নিয়তের ওপর নির্ভরশীল। প্রত্যেক মানুষ তার নিয়ত অনুযায়ীই প্রতিফল পাবে।”",
            "ওমর ইবনুল খাত্তাব (রাঃ)",
            "সহীহ বুখারী — ১"
        ),
        Hadith(
            "h2", "সহীহ বুখারী", "৬০১৮",
            "“যে ব্যক্তি আল্লাহ ও শেষ দিবসের ওপর বিশ্বাস রাখে, সে যেন সর্বদা উত্তম কথা বলে অথবা চুপ থাকে।”",
            "আবু হুরায়রা (রাঃ)",
            "সহীহ বুখারী — ৬০১৮"
        ),
        Hadith(
            "h3", "সহীহ মুসলিম", "১৯১২",
            "“ধৈর্য হলো আলো এবং দান করা হলো দলিল সদৃশ।”",
            "আবু মালিক আল-আশআরী (রাঃ)",
            "সহীহ মুসলিম — ১৯১২"
        ),
        Hadith(
            "h4", "সহীহ মুসলিম", "২৫৬৪",
            "“প্রকৃত মুসলিম সেই ব্যক্তি, যার জবান ও হাত থেকে অন্য মুসলিম নিরাপদ থাকে।”",
            "আবদুল্লাহ ইবনে আমর (রাঃ)",
            "সহীহ মুসলিম — ২৫৬৪"
        ),
        Hadith(
            "h5", "সুনানে আবু দাউদ", "৪৮৪৫",
            "“যে ব্যক্তি মানুষের কৃতজ্ঞতা প্রকাশ করে না, সে আল্লাহর প্রতিও কৃতজ্ঞতা প্রকাশ করে না।”",
            "আবু বকর (রাঃ)",
            "সুনানে আবু দাউদ — ৪৮৪৫"
        ),
        Hadith(
            "h6", "জামে তিরমিযী", "১৯৫৬",
            "“তোমাদের মধ্যে সর্বশ্রেষ্ঠ সেই ব্যক্তি, যার চরিত্র সবচেয়ে উত্তম।”",
            "আবদুল্লাহ ইবনে মাসউদ (রাঃ)",
            "জামে তিরমিযী — ১৯৫৬"
        ),
        Hadith(
            "h7", "সুনানে ইবনে মাজাহ", "২২৪",
            "“জ্ঞান অর্জন করা প্রত্যেক মুসলিমের ওপর ফরজ (অবশ্যই পালনীয়Duty)।”",
            "আনাস ইবনে মালিক (রাঃ)",
            "সুনানে ইবনে মাজাহ — ২২৪"
        ),
        Hadith(
            "h8", "সুনানে নাসায়ী", "৪৯৫৮",
            "“তোমরা দ্বীনকে সহজ করো, কঠিন করো না; এবং মানুষকে সুসংবাদ দাও, দূরে ঠেলে দিও না।”",
            "আবু মূসা আল-আশআরী (রাঃ)",
            "সুনানে নাসায়ী — ৪৯৫৮"
        )
    )
}

@Composable
fun HadithScreen(
    viewModel: DeenViewModel
) {
    val context = LocalContext.current
    var selectedCollection by remember { mutableStateOf<String?>(null) }
    val bookmarks by viewModel.allBookmarks.collectAsState()

    if (selectedCollection == null) {
        // ① Hadith Home Layout (11A)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📚 হাদিস শরীফ সংকলন",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gold
            )

            // Special highlighted card of today
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌟 আজকের নির্বাচিত হাদিস", fontWeight = FontWeight.Bold, color = Gold, fontSize = 14.sp)
                        Text("বুখারী ও মুসলিম", color = TextSecondary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "“প্রকৃত মুসলিম সেই ব্যক্তি, যার জবান (কথা) ও হাত থেকে অন্য মুসলিম সম্পূর্ণ নিরাপদ থাকে।”",
                        fontSize = 15.sp,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "বর্ণনাকারী: হযরত আবদুল্লাহ ইবনে আমর (রঃ)", fontSize = 11.sp, color = TextSecondary)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "হাদিস:\n“প্রকৃত মুসলিম সেই ব্যক্তি, যার জবান ও হাত থেকে অন্য মুসলিম সম্পূর্ণ নিরাপদ থাকে।”\n- সহীহ বুখারী ও মুসলিম, দ্বীনপথ অ্যাপ")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "শেয়ার"))
                        }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Gold)
                        }
                    }
                }
            }

            // List of the six canonical Hadith collections
            Text(text = "📁 কুতুবে সিত্তা বা বিখ্যাত হাদিস সংকলনসমূহ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)

            val collections = listOf(
                Pair("সহীহ বুখারী", "৭,২৭৫ টি হাদিস গ্রন্থিত"),
                Pair("সহীহ মুসলিম", "৫,৩৬২ টি হাদিস গ্রন্থিত"),
                Pair("সুনানে আবু দাউদ", "৫,২৭৪ টি হাদিস"),
                Pair("জামে তিরমিযী", "৩,৯৫৬ টি হাদিস"),
                Pair("সুনানে ইবনে মাজাহ", "৪,৩৪১ টি হাদিস"),
                Pair("সুনানে নাসায়ী", "৫,৭৫৮ টি হাদিস")
            )

            collections.forEach { (name, desc) ->
                Card(
                    onClick = { selectedCollection = name },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📗", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = desc, color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                        Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = "Open", tint = Gold, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    } else {
        // ② Hadith details listing
        val col = selectedCollection!!
        val colHadiths = remember(col) { HadithData.items.filter { it.collection == col } }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedCollection = null }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Gold)
                }

                Text(
                    text = col,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.width(48.dp)) // push centering alignment
            }

            if (colHadiths.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📖", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("এই ক্যাটালগে হাদিস শীঘ্রই যোগ করা হবে ইনশাআল্লাহ", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(colHadiths) { hadith ->
                        val isBookmarked = bookmarks.any { it.type == "hadith" && it.itemId == hadith.id }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "হাদিস নং: ${hadith.number}",
                                        color = Gold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )

                                    IconButton(
                                        onClick = {
                                            viewModel.toggleBookmark(
                                                type = "hadith",
                                                itemId = hadith.id,
                                                title = "হাদিস শরীফ: " + hadith.collection,
                                                subtitle = hadith.text.take(60) + "..."
                                            )
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = Gold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = hadith.text,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("বর্ণনায়: ${hadith.narrator}", fontSize = 11.sp, color = TextSecondary)
                                        Text("তথ্যসূত্র: ${hadith.reference}", fontSize = 10.sp, color = TextSecondary.copy(alpha = 0.7f))
                                    }

                                    IconButton(onClick = {
                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "আল-হাদিস:\n${hadith.text}\n- ${hadith.collection} (${hadith.reference})\nদ্বীনপথ অ্যাপ থেকে শেয়ারকৃত")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "হাদিস শেয়ার করুন"))
                                    }) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Gold, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
