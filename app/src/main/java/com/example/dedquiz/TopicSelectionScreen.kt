package com.example.dedquiz

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dedquiz.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TopicSelectionScreen(
    initialError: String?,
    topics: List<String>,
    onBack: () -> Unit,
    onTopicSelected: (String) -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(initialError?.takeIf { it.isNotBlank() }) }
    var showTitle by remember { mutableStateOf(false) }
    var showBackButton by remember { mutableStateOf(false) }
    val topicAnimations = remember(topics) { mutableStateListOf<Boolean>().apply { addAll(List(topics.size) { false }) } }
    val selectedTopic = remember { mutableStateOf<String?>(null) }
    val blinkState = remember { mutableStateOf(0) } // 0: off, 1: on (blink 1), 2: off, 3: on (blink 2), 4: on (final)

    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "titleAlpha"
    )

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            Log.d("TopicSelection", "Initial error: $initialError, topics: $topics")
            if (topics.isEmpty() && errorMessage == null) {
                errorMessage = "No topics available"
                Log.w("TopicSelection", "No topics received")
                showBackButton = true // Show back button immediately if no topics
            }
            // Animate title
            delay(300)
            showTitle = true
            // Animate topics one by one in fast mode
            if (topics.isNotEmpty()) {
                topics.forEachIndexed { index, _ ->
                    delay(200) // Fast mode: 500ms delay
                    if (index < topicAnimations.size) {
                        topicAnimations[index] = true
                        Log.d("TopicSelection", "Showing topic: ${topics[index]}")
                    }
                }
                // Show back button after all topics are loaded
                delay(500) // Extra delay for smoothness
                showBackButton = true
                Log.d("TopicSelection", "Back button shown")
            }
        } catch (e: Exception) {
            Log.e("TopicSelection", "Error in LaunchedEffect: ${e.message}", e)
            errorMessage = "Unexpected error: ${e.message}"
            showBackButton = true
        }
    }

    // Handle blink animation on topic selection
    LaunchedEffect(selectedTopic.value) {
        if (selectedTopic.value != null) {
            repeat(2) { // Blink twice
                delay(100)
                blinkState.value = blinkState.value + 1 // On
                delay(200)
                blinkState.value = blinkState.value + 1 // Off
            }
            blinkState.value = 4 // Final on state
            onTopicSelected(selectedTopic.value!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TypingText(
            text = "Choose Your Topic",
            speed = 40L,
            modifier = Modifier
                .padding(top = 50.dp, bottom = 24.dp)
                .alpha(titleAlpha),
            color = White,
            fontSize = 32.sp,
            fontFamily = FontFamily.Serif,
            //fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        if (errorMessage?.isNotBlank() == true) {
            TypingText(
                text = errorMessage!!,
                speed = 40L,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                color = RedAlert,
                fontSize = 25.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            )
        } else if (topics.isEmpty()) {
            TypingText(
                text = "No topics available",
                speed = 40L,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                color = RedAlert,
                fontSize = 25.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Ensure it takes available space
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp) // Padding for scroll
            ) {
                items(topics) { topic ->
                    val index = topics.indexOf(topic)
                    if (index < topicAnimations.size && topicAnimations.getOrNull(index) == true) {
                        TopicButton(
                            topic = topic,
                            isSelected = topic == selectedTopic.value,
                            blinkState = blinkState.value,
                            onClick = {
                                if (selectedTopic.value == null) { // Allow only one selection
                                    selectedTopic.value = topic
                                }
                            }
                        )
                    } else {
                        Log.d("TopicSelection", "Skipping topic: $topic, index: $index, animation: ${topicAnimations.getOrNull(index)}")
                    }
                }
            }
        }

        if (showBackButton) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp, bottom = 32.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Cyan,
                    contentColor = Black
                )
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
fun TopicButton(
    topic: String,
    isSelected: Boolean,
    blinkState: Int,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 200),
        label = "buttonScale"
    )
    val fillAlpha by animateFloatAsState(
        targetValue = when (blinkState) {
            1, 3, 4 -> if (isSelected) 0.5f else 0f // On states (blink 1, blink 2, final)
            else -> 0f // Off states
        },
        animationSpec = tween(durationMillis = 100),
        label = "fillAlpha"
    )

    Box(
        modifier = Modifier
            .size(width = 260.dp, height = 60.dp)
            .clip(LongHexagon())
            .border(1.dp, Cyan, LongHexagon())
            .background(Cyan.copy(alpha = fillAlpha))
            .clickable { onClick() }
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        TypingText(
            text = topic,
            speed = 25L,
            modifier = Modifier,
            color = White,
            fontSize = 15.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RadarScan(modifier: Modifier = Modifier) {
    val angle by animateFloatAsState(
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    Canvas(modifier = modifier.size(120.dp)) {
        drawCircle(
            color = PurpleCTO.copy(alpha = 0.1f),
            radius = size.minDimension / 2
        )
        rotate(angle) {
            drawLine(
                color = TechBlue,
                start = Offset(size.width / 2, size.height / 2),
                end = Offset(size.width / 2, 0f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
    }
}