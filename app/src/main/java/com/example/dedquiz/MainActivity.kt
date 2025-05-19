package com.example.dedquiz

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dedquiz.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeDQuizTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AlmostBlack)
                ) {
                    CtOSBackground() // Persistent background
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "intro") {
                        composable("intro") {
                            IntroScreen(
                                onStartGame = { topics, error ->
                                    val encodedError = error?.let {
                                        URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
                                    } ?: "none"
                                    val topicsString = topics.joinToString(",")
                                    Log.d("IntroScreen", "Navigating with topics: $topics, encodedError: $encodedError")
                                    navController.navigate("topic_selection?error=$encodedError&topics=$topicsString")
                                }
                            )
                        }
                        composable("topic_selection?error={error}&topics={topics}") { backStackEntry ->
                            val errorMessage = backStackEntry.arguments?.getString("error")
                                .let { if (it == "none") null else it }
                            val topicsString = backStackEntry.arguments?.getString("topics") ?: ""
                            val topicsList = if (topicsString.isNotBlank()) topicsString.split(",") else emptyList()
                            Log.d("TopicSelection", "Received errorMessage: $errorMessage, topics: $topicsList")
                            TopicSelectionScreen(
                                initialError = errorMessage,
                                topics = topicsList,
                                onBack = { navController.popBackStack() },
                                onTopicSelected = { topic ->
                                    Log.d("TopicSelection", "Selected topic: $topic")
                                    val encodedTopic = URLEncoder.encode(topic, StandardCharsets.UTF_8.toString())
                                    navController.navigate("module?topic=$encodedTopic")
                                }
                            )
                        }
                        composable("module?topic={topic}") { backStackEntry ->
                            val topic = backStackEntry.arguments?.getString("topic") ?: ""
                            Screen3Module(
                                topic = topic,
                                onStartQuiz = { selectedTopic ->
                                    navController.navigate("mcq?topic=$selectedTopic")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("mcq?topic={topic}") { backStackEntry ->
                            val topic = backStackEntry.arguments?.getString("topic") ?: ""
                            MCQScreen(
                                topic = topic,
                                onBack = { navController.popBackStack() },
                                onFinish = { resultTopic, score, total ->
                                    val encodedTopic = URLEncoder.encode(resultTopic, StandardCharsets.UTF_8.toString())
                                    navController.navigate("result?topic=$encodedTopic&score=$score&total=$total")
                                }
                            )
                        }
                        composable("result?topic={topic}&score={score}&total={total}") { backStackEntry ->
                            val topic = backStackEntry.arguments?.getString("topic")?.let {
                                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                            } ?: ""
                            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
                            val total = backStackEntry.arguments?.getString("total")?.toIntOrNull() ?: 0
                            ResultScreen(
                                topic = topic,
                                score = score,
                                totalQuestions = total,
                                onBackToStart = {
                                    navController.navigate("intro") {
                                        popUpTo("intro") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntroScreen(onStartGame: (List<String>, String?) -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    var topics by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showConnectingText by remember { mutableStateOf(false) }
    var showEstablishedText by remember { mutableStateOf(false) }
    val subtitleAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500, delayMillis = 750),
        label = "subtitleAlpha"
    )
    val developerAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500, delayMillis = 1250),
        label = "developerAlpha"
    )
    val animatedButtonScale by rememberInfiniteTransition(label = "buttonScale").animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedButtonScale"
    )
    val rhombusRotation by rememberInfiniteTransition(label = "rhombusRotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing), // 4000ms rotation
            repeatMode = RepeatMode.Restart
        ),
        label = "rhombusRotation"
    )

    // Animation states for hexagon
    val hexagonOffsetY = remember { Animatable(0f) }
    val hexagonScale = remember { Animatable(1f) }
    val hexagonToCursorProgress = remember { Animatable(0f) }

    // Coroutine scope for Firebase callbacks
    val coroutineScope = rememberCoroutineScope()

    // Firebase connection
    val database = FirebaseDatabase.getInstance("https://dedquiz-c7fd9-default-rtdb.europe-west1.firebasedatabase.app/")
    val ref = database.getReference("topics")

    LaunchedEffect(startAnimation) {
        if (startAnimation) {
            Log.d("IntroScreen", "Starting animation sequence")
            // Move hexagon up and shrink
            launch {
                hexagonOffsetY.animateTo(
                    targetValue = -150f, // Move up
                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                )
                Log.d("IntroScreen", "Hexagon moved up and shrunk")
            }
            launch {
                hexagonScale.animateTo(
                    targetValue = 0.2f, // Shrink
                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                )
            }
            // Transform to cursor
            launch {
                delay(800)
                hexagonToCursorProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 500)
                )
                Log.d("IntroScreen", "Transformed to cursor")
                showConnectingText = true // Start "Connecting to server..."
            }
            // Fetch topics from Firebase
            isLoading = true
            try {
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val topicList = mutableListOf<String>()
                        snapshot.children.forEach { data ->
                            val topicName = data.child("name").getValue(String::class.java)
                            topicName?.let { topicList.add(it) }
                        }
                        topics = topicList.sorted()
                        isLoading = false
                        if (topics.isEmpty()) {
                            errorMessage = "No topics found in database"
                            Log.w("IntroScreen", "No topics loaded")
                        } else {
                            Log.d("IntroScreen", "Topics loaded: $topics")
                        }
                        // Show "Connection Established..." after "Connecting to server..."
                        coroutineScope.launch {
                            val connectingDuration = (75L * "[+] Connection Established".length).toInt() // ~2400ms
                            delay(connectingDuration.toLong())
                            showEstablishedText = true
                            val establishedDuration = (90L * (errorMessage ?: "[+] Loading Modules...").length).toInt() // ~3120ms
                            delay(establishedDuration.toLong())
                            Log.d("IntroScreen", "Navigating to TopicSelectionScreen with topics: $topics, error: $errorMessage")
                            onStartGame(topics, errorMessage)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        errorMessage = "Failed to connect to server: ${error.message}"
                        isLoading = false
                        Log.e("IntroScreen", "Database error: ${error.message}", error.toException())
                        coroutineScope.launch {
                            val connectingDuration = (75L * "[+] Connection Established".length).toInt()
                            delay(connectingDuration.toLong())
                            showEstablishedText = true
                            val establishedDuration = (90L * errorMessage!!.length).toInt()
                            delay(establishedDuration.toLong())
                            Log.d("IntroScreen", "Navigating with error: $errorMessage")
                            onStartGame(emptyList(), errorMessage)
                        }
                    }
                })
            } catch (e: Exception) {
                errorMessage = "Unexpected error: ${e.message}"
                isLoading = false
                Log.e("IntroScreen", "Unexpected error: ${e.message}", e)
                coroutineScope.launch {
                    val connectingDuration = (75L * "[+] Connection Established".length).toInt()
                    delay(connectingDuration.toLong())
                    showEstablishedText = true
                    val establishedDuration = (90L * errorMessage!!.length).toInt()
                    delay(establishedDuration.toLong())
                    Log.d("IntroScreen", "Navigating with error: $errorMessage")
                    onStartGame(emptyList(), errorMessage)
                }
            }
        }
    }

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (appName, subtitle, buttonBox, developer, connectingText, establishedText) = createRefs()

        GlitchingText(
            text = "DeDQuiz",
            modifier = Modifier
                .constrainAs(appName) {
                    top.linkTo(parent.top, margin = 150.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(subtitle.top)
                }
                .padding(bottom = 0.dp)
                .alpha(if (startAnimation) 0f else 1f),
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = White,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )

        TypingText(
            text = "Knowledge is your weapon",
            speed = 30L,
            modifier = Modifier
                .constrainAs(subtitle) {
                    top.linkTo(parent.top, margin = 250.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(buttonBox.top)
                }
                .padding(top = 0.dp, bottom = 10.dp)
                .alpha(if (startAnimation) 0f else subtitleAlpha),
            color = Cyan,
            fontSize = 25.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )

        if (!startAnimation) {
            Box(
                modifier = Modifier
                    .constrainAs(buttonBox) {
                        top.linkTo(parent.top, margin = 40.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(developer.top)
                    }
                    .size(180.dp)
            ) {
                RotatingHexagon(
                    modifier = Modifier.align(Alignment.Center),
                    borderColor = White,
                    rotationDegrees = rhombusRotation
                )

                Button(
                    onClick = { startAnimation = true },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(width = 400.dp, height = 180.dp)
                        .scale(animatedButtonScale),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = White
                    ),
                    shape = Hexagon()
                ) {
                    Text(
                        text = "Start The Game",
                        fontFamily = FontFamily.Serif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            // Animated hexagon transforming to cursor with rotation
            Canvas(
                modifier = Modifier
                    .constrainAs(buttonBox) {
                        top.linkTo(parent.top, margin = 40.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(developer.top)
                    }
                    .size(180.dp)
                    .offset(y = hexagonOffsetY.value.dp)
                    .scale(hexagonScale.value)
                    .rotate(rhombusRotation) // Apply rotation to Canvas
            ) {
                val t = hexagonToCursorProgress.value
                if (t < 1f) {
                    // Draw rotating hexagon (fading out)
                    val outline = Hexagon().createOutline(
                        size = Size(size.width, size.height),
                        layoutDirection = layoutDirection,
                        density = this
                    )
                    if (outline is androidx.compose.ui.graphics.Outline.Generic) {
                        drawPath(
                            path = outline.path,
                            color = White,
                            style = Stroke(width = 2f),
                            alpha = 1f - t
                        )
                    }
                }
                if (t > 0f) {
                    // Draw static cursor (fading in, no rotation)
                    val cursorWidth = 2f
                    val cursorHeight = size.height * 0.2f
                    drawLine(
                        color = White,
                        start = Offset(size.width / 2 - cursorWidth / 2, size.height / 2 - cursorHeight / 2),
                        end = Offset(size.width / 2 - cursorWidth / 2, size.height / 2 + cursorHeight / 2),
                        strokeWidth = cursorWidth,
                        alpha = t
                    )
                }
            }
        }

        if (showConnectingText) {
            TypingText(
                text = "[+] Connection Established",
                speed = 10L,
                modifier = Modifier
                    .constrainAs(connectingText) {
                        top.linkTo(parent.top, margin = 250.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(top = 0.dp, bottom = 10.dp),
                color = White,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            )
        }

        if (showEstablishedText) {
            TypingText(
                text = errorMessage?.takeIf { it.isNotBlank() } ?: "[+] Loading Modules...",
                speed = 40L,
                modifier = Modifier
                    .constrainAs(establishedText) {
                        top.linkTo(connectingText.bottom, margin = 10.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(top = 0.dp, bottom = 10.dp),
                color = if (errorMessage?.isNotBlank() == true) RedAlert else White,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            )
        }

        GlitchingText(
            text = "  Developed by \nParth Dinesh Bhagat",
            color = Cyan,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .constrainAs(developer) {
                    bottom.linkTo(parent.bottom, margin = 30.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .alpha(if (startAnimation) 0f else developerAlpha)
        )
    }
}

@Composable
fun TypingText(
    text: String,
    speed: Long = 75L,
    modifier: Modifier = Modifier,
    color: Color = Cyan,
    fontSize: androidx.compose.ui.unit.TextUnit = 25.sp,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null
) {
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        visibleText = ""
        for (i in text.indices) {
            visibleText += text[i]
            delay(speed)
        }
    }

    Text(
        text = visibleText,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = fontFamily,
        textAlign = textAlign
    )
}

@Composable
fun GlitchingText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = White,
    fontSize: androidx.compose.ui.unit.TextUnit = 56.sp,
    fontFamily: FontFamily? = FontFamily.Serif,
    fontWeight: FontWeight? = FontWeight.Bold,
    textAlign: TextAlign? = TextAlign.Center
) {
    var displayText by remember { mutableStateOf(text) }

    LaunchedEffect(Unit) {
        while (true) {
            val glitch = text.map {
                if (Random.nextFloat() < 0.1f) Random.nextInt(33, 127).toChar() else it
            }.joinToString("")
            displayText = glitch
            delay(100)
            displayText = text
            delay(400)
        }
    }

    Text(
        text = displayText,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        textAlign = textAlign
    )
}

@Composable
fun RotatingHexagon(
    modifier: Modifier = Modifier,
    borderColor: Color,
    rotationDegrees: Float
) {
    Box(
        modifier = modifier
            .size(300.dp)
            .rotate(rotationDegrees)
            .border(1.dp, borderColor, Hexagon())
    )
}

@Preview(showBackground = true)
@Composable
fun IntroScreenPreview() {
    DeDQuizTheme {
        IntroScreen(onStartGame = { _, _ -> })
    }
}