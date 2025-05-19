package com.example.dedquiz

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dedquiz.ui.theme.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun MCQScreen(
    topic: String,
    onBack: () -> Unit,
    onFinish: (String, Int, Int) -> Unit // New callback for Finish button
) {
    // Decode the topic to handle spaces (e.g., "Indian+History" -> "Indian History")
    val decodedTopic = URLDecoder.decode(topic, StandardCharsets.UTF_8.toString())
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var showDescription by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "fadeIn"
    )

    val database = FirebaseDatabase.getInstance("https://dedquiz-c7fd9-default-rtdb.europe-west1.firebasedatabase.app/")
    val topicsRef = database.getReference("topics")

    LaunchedEffect(decodedTopic) {
        if (decodedTopic.isBlank()) {
            isLoading = false
            errorMessage = "No topic selected"
            Log.e("MCQScreen", "Topic is blank")
            return@LaunchedEffect
        }

        isLoading = true
        errorMessage = null

        // Query to find the topic by name
        topicsRef.orderByChild("name").equalTo(decodedTopic).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                questions = emptyList()
                if (snapshot.exists()) {
                    snapshot.children.firstOrNull()?.let { topicSnapshot ->
                        val questionList = mutableListOf<Question>()
                        topicSnapshot.child("questions").children.forEach { questionSnapshot ->
                            try {
                                val question = questionSnapshot.getValue(Question::class.java)
                                if (question != null && question.options.isNotEmpty()) {
                                    questionList.add(question)
                                } else {
                                    Log.w("MCQScreen", "Invalid question data at ${questionSnapshot.key}")
                                }
                            } catch (e: Exception) {
                                Log.e("MCQScreen", "Error deserializing question at ${questionSnapshot.key}: ${e.message}", e)
                            }
                        }
                        questions = questionList
                        Log.d("MCQScreen", "Loaded ${questions.size} questions for topic: $decodedTopic")
                    }
                } else {
                    Log.w("MCQScreen", "No topic found with name: $decodedTopic")
                }

                isLoading = false
                if (questions.isEmpty()) {
                    errorMessage = "No questions found for topic: $decodedTopic"
                    Log.w("MCQScreen", "No questions loaded")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                errorMessage = "Failed to load questions: ${error.message}"
                Log.e("MCQScreen", "Firebase error: ${error.message}", error.toException())
            }
        })
    }

    fun handleOptionSelection(option: String) {
        if (selectedOption == null) {
            selectedOption = option
            showDescription = true
            val currentQuestion = questions.getOrNull(currentQuestionIndex)
            if (currentQuestion?.correctAnswer == option) {
                score += 1
            }
        }
    }

    fun handleNext() {
        selectedOption = null
        showDescription = false
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex += 1
        }
    }

    fun handleFinish() {
        selectedOption = null
        showDescription = false
        onFinish(decodedTopic, score, questions.size)
    }

    fun handleQuit() {
        currentQuestionIndex = 0
        score = 0
        selectedOption = null
        showDescription = false
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header with topic and score
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeInAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlitchingText(
                text = decodedTopic,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 10.dp),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Score: $score/${questions.size}",
                color = GreenPulse,
                fontSize = 25.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        // Main content (questions, options, description)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .alpha(fadeInAlpha),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                Text(
                    text = "Loading questions...",
                    color = Cyan,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Start
                )
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = RedAlert,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Start
                )
            } else if (questions.isNotEmpty()) {
                val currentQuestion = questions[currentQuestionIndex]

                // Question text
                Text(
                    text = currentQuestion.question,
                    color = White,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    textAlign = TextAlign.Start,
                    softWrap = true
                )

                // Options
                currentQuestion.options.forEach { option ->
                    val isSelected = option == selectedOption
                    val isCorrect = option == currentQuestion.correctAnswer
                    val backgroundColor = when {
                        isSelected && isCorrect -> GreenPulse.copy(alpha = 0.3f)
                        isSelected && !isCorrect -> RedAlert.copy(alpha = 0.3f)
                        else -> Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = 0.9f)
                            .wrapContentHeight()
                            .heightIn(min = 50.dp)
                            .clip(RectangleShape())
                            .border(1.dp, Cyan, RectangleShape())
                            .background(backgroundColor)
                            .clickable(enabled = selectedOption == null) {
                                Log.d("MCQScreen", "Selected option: $option")
                                handleOptionSelection(option)
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = option,
                            color = White,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier
                                .padding(start = 8.dp, end = 8.dp),
                            textAlign = TextAlign.Start,
                            softWrap = true
                        )
                    }
                }

                // Description
                if (showDescription) {
                    Text(
                        text = currentQuestion.description,
                        color = Cyan,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        textAlign = TextAlign.Start,
                        softWrap = true
                    )
                }
            } else {
                Text(
                    text = "No questions available",
                    color = RedAlert,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Start
                )
            }
        }

        // Buttons (Quit and Next/Finish)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { handleQuit() },
                modifier = Modifier
                    .wrapContentWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cyan,
                    contentColor = Color.Black
                ),
                shape = RectangleShape()
            ) {
                Text("Quit")
            }

            if (currentQuestionIndex < questions.size - 1) {
                Button(
                    onClick = { handleNext() },
                    enabled = showDescription,
                    modifier = Modifier
                        .wrapContentWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan,
                        contentColor = Color.Black,
                        disabledContainerColor = Cyan.copy(alpha = 0.3f),
                        disabledContentColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    shape = RectangleShape()
                ) {
                    Text("Next")
                }
            } else {
                Button(
                    onClick = { handleFinish() },
                    enabled = showDescription,
                    modifier = Modifier
                        .wrapContentWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan,
                        contentColor = Color.Black,
                        disabledContainerColor = Cyan.copy(alpha = 0.3f),
                        disabledContentColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    shape = RectangleShape()
                ) {
                    Text("Finish")
                }
            }
        }
    }
}