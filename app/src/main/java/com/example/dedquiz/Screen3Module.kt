package com.example.dedquiz

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dedquiz.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun Screen3Module(
    topic: String,
    onStartQuiz: (String) -> Unit,
    onBack: () -> Unit
) {
    // Decode the topic to handle spaces (e.g., "Indian+History" -> "Indian History")
    val decodedTopic = URLDecoder.decode(topic, StandardCharsets.UTF_8.toString())
    // Fallback for empty topic
    val displayTopic = decodedTopic.takeIf { it.isNotBlank() } ?: "Unknown Topic"
    Log.d("Screen3Module", "Received topic: '$topic', decoded: '$decodedTopic', displaying: '$displayTopic'")

    var startTransition by remember { mutableStateOf(false) }
    var startFade by remember { mutableStateOf(false) }
    val transitionProgress by animateFloatAsState(
        targetValue = if (startFade) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        label = "transitionProgress"
    )
    // Shape index for cycling
    var shapeIndex by remember { mutableStateOf(0) }
    val shapes = listOf(TriangleShape(), CustomCircleShape(), RectangleShape(), KiteShape())
    val currentShape = if (startTransition) LongHexagon() else shapes[shapeIndex % shapes.size]

    // Continuous rotation
    val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Blinking border (opacity pulse)
    val borderAlpha by rememberInfiniteTransition(label = "borderAlpha").animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )

    // Cycle shapes every 200ms
    LaunchedEffect(Unit) {
        while (!startTransition) {
            delay(200)
            shapeIndex += 1
            Log.d("Screen3Module", "Switched to shape index: $shapeIndex")
        }
    }

    // Start fade after 1000ms when transition begins
    LaunchedEffect(startTransition) {
        if (startTransition) {
            delay(500) // Wait 1 second before fading
            startFade = true
            Log.d("Screen3Module", "Starting hexagon fade after 1-second delay")
        }
    }

    // Navigate after fade completes
    LaunchedEffect(transitionProgress) {
        if (transitionProgress == 1f) {
            Log.d("Screen3Module", "Transition complete, navigating to MCQScreen with topic: $decodedTopic")
            onStartQuiz(decodedTopic) // Pass decoded topic to MCQScreen
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Topic text just above center
        Spacer(modifier = Modifier.weight(0.3f)) // Push topic down slightly
        GlitchingText(
            text = displayTopic,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 25.dp),
            color = White,
            fontSize = 45.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Shape button with rotation and blinking
            if (!startFade || transitionProgress < 1f) {
                Box(
                    modifier = Modifier
                        .size(width = 140.dp, height = 100.dp)
                        .clip(currentShape)
                        .border(1.dp, White.copy(alpha = borderAlpha), currentShape)
                        .background(if (startTransition) Cyan else Color.Transparent)
                        .rotate(rotation) // Rotate shape
                        .scale(1f) // Fixed scale
                        .alpha(1f - transitionProgress) // Fade out
                        .clickable(enabled = !startTransition) {
                            Log.d("Screen3Module", "Start Quiz clicked, transitioning to hexagon")
                            startTransition = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Static text (no rotation)
                    Box(
                        modifier = Modifier
                            .rotate(-rotation) // Counter-rotate to keep text static
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Start Quiz",
                            color = White,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.1f)) // Balance layout
        Button(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp, bottom = 200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Cyan,
                contentColor = Black
            )
        ) {
            Text("Back")
        }
    }
}

// Custom Shapes (unchanged)
class TriangleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): Outline {
        val path = Path().apply {
            moveTo(size.width / 2, 0f) // Top
            lineTo(size.width, size.height) // Bottom-right
            lineTo(0f, size.height) // Bottom-left
            close()
        }
        return Outline.Generic(path)
    }
}

class CustomCircleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): Outline {
        val path = Path().apply {
            val radius = minOf(size.width, size.height) / 2
            val centerX = size.width / 2
            val centerY = size.height / 2
            moveTo(centerX + radius, centerY)
            for (i in 0..360 step 5) {
                val angle = Math.toRadians(i.toDouble())
                lineTo(
                    centerX + radius * cos(angle).toFloat(),
                    centerY + radius * sin(angle).toFloat()
                )
            }
            close()
        }
        return Outline.Generic(path)
    }
}

class RectangleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

class KiteShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): Outline {
        val path = Path().apply {
            moveTo(size.width / 2, 0f) // Top
            lineTo(size.width, size.height * 0.3f) // Right
            lineTo(size.width / 2, size.height) // Bottom
            lineTo(0f, size.height * 0.3f) // Left
            close()
        }
        return Outline.Generic(path)
    }
}