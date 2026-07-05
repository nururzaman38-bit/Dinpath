package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SplashScreen(
    themeName: String,
    onSplashFinished: () -> Unit
) {
    var startAnimations by remember { mutableStateOf(false) }

    // Pulse animation for the moon emoji/icon
    val infiniteTransition = rememberInfiniteTransition(label = "moon_pulse")
    val moonScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moon_scale"
    )

    // Animated values for visual fade ins
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimations) 1.0f else 0.0f,
        animationSpec = tween(1500),
        label = "alpha_anim"
    )

    val offsetAnim by animateDpAsState(
        targetValue = if (startAnimations) 0.dp else 40.dp,
        animationSpec = tween(1200, easing = EaseOutBounce),
        label = "offset_anim"
    )

    // Star dots random offsets
    val stars = remember {
        List(40) {
            Pair(Random.nextFloat(), Random.nextFloat())
        }
    }

    // Twinkling effect alpha
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_alpha"
    )

    LaunchedEffect(Unit) {
        startAnimations = true
        delay(3200) // 3.2 seconds splash display
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        DarkGreen,
                        BackgroundDark
                    )
                )
            )
    ) {
        // Starry night canvas background
        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEach { (x, y) ->
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha * (0.3f + Random.nextFloat() * 0.4f)),
                    radius = Random.nextFloat() * 3f + 1.5f,
                    center = androidx.compose.ui.geometry.Offset(x * size.width, y * size.height)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Spacer to force alignments
            Spacer(modifier = Modifier.height(1.dp))

            // Main center brand
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = offsetAnim)
                    .scale(alphaAnim)
            ) {
                // Gold glowing moon emoji/emoji container
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .scale(moonScale)
                ) {
                    Text(
                        text = "🌙",
                        fontSize = 72.sp,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "দ্বীনপথ",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "D E E N P A T H",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Divider line
                HorizontalDivider(
                    thickness = 1.5.dp,
                    color = Gold.copy(alpha = 0.6f),
                    modifier = Modifier.width(120.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "দ্বীনের পথে, প্রতিদিন",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Bottom loading indicators
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Circular soft load dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pulseDot = remember { Animatable(0f) }
                    LaunchedEffect(Unit) {
                        pulseDot.animateTo(
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                    }
                    repeat(4) { i ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Gold.copy(alpha = if (pulseDot.value > i * 0.2f) 1.0f else 0.4f), shape = androidx.compose.foundation.shape.CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Powered by দ্বীনপথ Team",
                    color = AccentGreen.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
