package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DeenViewModel
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    viewModel: DeenViewModel
) {
    val location by viewModel.settingsManager.location.collectAsState()

    // Dhaka true Qibla heading is approximately 285.3 degrees (Northwest)
    val qiblaAzimuth = 285.3f

    // Simulated / manual compass rotation adjustment to allow smooth slider iteration
    var currentHeading by remember { mutableStateOf(120f) }

    // Check if matched within narrow error gap (+/- 5 degrees)
    val isMatched = remember(currentHeading) {
        val diff = kotlin.math.abs(currentHeading - qiblaAzimuth)
        diff < 6f || diff > 354f
    }

    // Dynamic rotation angle animation for high responsiveness
    val animatedHeading by animateFloatAsState(
        targetValue = currentHeading,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "compass_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper indicator card
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMatched) CompassGreen.copy(alpha = 0.2f) else SurfaceDark.copy(alpha = 0.5f)
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (isMatched) AccentGreen else Color.White.copy(alpha = 0.05f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isMatched) "🕋 কিবলার সঠিক দিকে আছেন!" else "🧭 মোবাইলটি ঘুরিয়ে কিবলা সোজা করুন",
                    color = if (isMatched) AccentGreen else Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "লক্ষ্য: ${qiblaAzimuth}° (উত্তর-পশ্চিম) | বর্তমান কোণ: ${currentHeading.toInt()}°",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Master custom canvas compass
        Box(
            modifier = Modifier
                .size(280.dp)
                .padding(12.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(SurfaceDark, DarkGreen)
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .border(
                    width = 4.dp,
                    brush = Brush.linearGradient(
                        colors = if (isMatched) listOf(AccentGreen, CompassGreen) else listOf(Gold, LightGold)
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Background ticks and letters Canvas matched to virtual device orientation
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(-animatedHeading) // rotate inverse to simulation direction
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2 - 16.dp.toPx()

                // Draw outer dial ticks
                for (i in 0 until 360 step 15) {
                    val angleRad = i * PI / 180f
                    val startX = center.x + (radius - 12.dp.toPx()) * cos(angleRad).toFloat()
                    val startY = center.y + (radius - 12.dp.toPx()) * sin(angleRad).toFloat()
                    val endX = center.x + radius * cos(angleRad).toFloat()
                    val endY = center.y + radius * sin(angleRad).toFloat()

                    drawCircle(
                        color = if (i == 0) Color.Red else Color.White.copy(alpha = 0.3f),
                        radius = if (i % 90 == 0) 3.5f else 1.5f,
                        center = Offset(endX, endY)
                    )
                }

                // Draw N S E W labels (relative to pointer)
                val textRadius = radius - 20.dp.toPx()
                // Drawing simple custom pointers for directions
            }

            // Draw directional pointer indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Central Kaaba cube icon (static visually, compass dial rotates around it)
                Text(
                    text = "🕋",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "Pin",
                    tint = if (isMatched) AccentGreen else Gold,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Draw a golden arrow pointing ALWAYS towards Qibla direction (285.3) relative to the simulated rotation scale
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(qiblaAzimuth - animatedHeading),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val r = size.width / 2 - 18.dp.toPx()

                    // Pointer path (arrowhead pointing north-northwest)
                    val pointerPath = Path().apply {
                        moveTo(center.x, center.y - r) // tip representing Kaaba target orientation
                        lineTo(center.x - 12.dp.toPx(), center.y - r + 24.dp.toPx())
                        lineTo(center.x, center.y - r + 18.dp.toPx())
                        lineTo(center.x + 12.dp.toPx(), center.y - r + 24.dp.toPx())
                        close()
                    }

                    drawPath(
                        path = pointerPath,
                        brush = Brush.verticalGradient(
                            colors = if (isMatched) listOf(AccentGreen, Color.White) else listOf(Gold, LightGold)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Interactivity slider controller
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📱 রোটেশন সিমুলেটর",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Button(
                        onClick = { currentHeading = qiblaAzimuth },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).padding(0.dp)
                    ) {
                        Text("কিবলা সোজাসুজি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = currentHeading,
                    onValueChange = { currentHeading = it },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isMatched) AccentGreen else Gold,
                        activeTrackColor = if (isMatched) CompassGreen else Gold.copy(alpha = 0.3f)
                    )
                )
                Text(
                    text = "কিবলা ম্যাচ করতে স্লাইডারটিকে ২৮৫° তে টেনে আনুন।",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Coordinates & distance info cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📍 আপনার অবস্থান:", fontSize = 13.sp, color = TextSecondary)
                    Text(location + ", বাংলাদেশ", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🕋 কিবলার দিক কোণ:", fontSize = 13.sp, color = TextSecondary)
                    Text("$qiblaAzimuth° (উত্তর-পশ্চিম)", fontSize = 13.sp, color = Gold, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📏 পবিত্র মক্কার দূরত্ব:", fontSize = 13.sp, color = TextSecondary)
                    Text("৬,১৫৭ কিলোমিটার", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
