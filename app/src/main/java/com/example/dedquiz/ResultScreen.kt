package com.example.dedquiz

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dedquiz.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun ResultScreen(
    topic: String,
    score: Int,
    totalQuestions: Int,
    onBackToStart: () -> Unit
) {
    val percentage = if (totalQuestions > 0) ((score.toFloat() / totalQuestions) * 100).roundToInt() else 0
    var startAnimation by remember { mutableStateOf(false) }
    val animatedPercentage by animateIntAsState(
        targetValue = if (startAnimation) percentage else 0,
        animationSpec = tween(durationMillis = 1000),
        label = "percentageAnimation"
    )
    val isHighScore = percentage >= 70
    val percentageColor = if (isHighScore) GreenPulse else RedAlert
    val borderColor = percentageColor

    // Start animation after 500ms delay
    LaunchedEffect(Unit) {
        delay(500)
        startAnimation = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Topic at the top
        GlitchingText(
            text = topic,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp, bottom = 15.dp),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Rotating hexagon with animated percentage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f),
            contentAlignment = Alignment.Center
        ) {
            // Rotating hexagon
            RotatingHexagon(
                modifier = Modifier.size(250.dp),
                borderColor = borderColor,
                rotationDurationMillis = 3000 // Faster rotation
            )

            // Static percentage text with glitching and pulse
            GlitchingPercentageText(
                percentage = animatedPercentage,
                color = percentageColor,
                startAnimation = startAnimation,
                modifier = Modifier
                    .size(100.dp)
                    .background(AlmostBlack.copy(alpha = 0.5f))
            )
        }

        // Statistics section
        Column(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.9f)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Correct Answers: $score",
                color = GreenPulse,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Incorrect Answers: ${totalQuestions - score}",
                color = RedAlert,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Total Number of Questions: $totalQuestions",
                color = Cyan,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Spacer to separate statistics from button
        Spacer(modifier = Modifier.height(32.dp))

        // Back to Start button
        Button(
            onClick = { onBackToStart() },
            modifier = Modifier
                .wrapContentWidth()
                .padding(bottom = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Cyan,
                contentColor = Color.Black
            ),
            shape = RectangleShape()
        ) {
            Text("Back to Start")
        }
    }
}

@Composable
fun GlitchingPercentageText(
    percentage: Int,
    color: Color,
    startAnimation: Boolean,
    modifier: Modifier = Modifier
) {
    var displayText by remember { mutableStateOf("0%") }
    var isGlitching by remember { mutableStateOf(false) }

    // Glitching effect when animation starts
    LaunchedEffect(startAnimation, percentage) {
        if (startAnimation) {
            isGlitching = true
            while (isGlitching) {
                val glitch = "$percentage%".map {
                    if (Random.nextFloat() < 0.1f) Random.nextInt(33, 127).toChar() else it
                }.joinToString("")
                displayText = glitch
                delay(100)
                displayText = "$percentage%"
                delay(400)
            }
        } else {
            displayText = "0%"
        }
    }

    // Stop glitching after animation duration
    LaunchedEffect(startAnimation) {
        if (startAnimation) {
            delay(1000) // Match animateIntAsState duration
            isGlitching = false
            displayText = "$percentage%"
        }
    }

    // Pulse animation (starts with animation, continues infinitely)
    val pulseScale by rememberInfiniteTransition(label = "pulseAnimation").animateFloat(
        initialValue = 1f,
        targetValue = if (startAnimation) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = color,
            fontSize = 30.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.scale(pulseScale)
        )
    }
}

@Composable
fun RotatingHexagon(
    modifier: Modifier = Modifier,
    borderColor: Color,
    rotationDurationMillis: Int = 5000
) {
    val rotationDegrees by rememberInfiniteTransition(label = "hexagonRotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationDegrees"
    )

    Box(
        modifier = modifier
            .rotate(rotationDegrees)
            .border(1.dp, borderColor, Hexagon())
    )
}