package com.example.dedquiz

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.dedquiz.ui.theme.TechBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.hypot
import kotlin.random.Random

@Composable
fun CtOSBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 100, // Balanced for performance
    connectionDistance: Float = 120f, // Visible connections
    particleColor: Color = TechBlue,
    lineColor: Color = Color.White,
    particleMinSize: Float = 1f,
    particleMaxSize: Float = 3f,
    lineStrokeWidth: Float = 3f
) {
    val particles = remember { mutableStateListOf<CtosParticle>() }
    val alpha = remember { Animatable(0f) }

    // Initialize particles
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(durationMillis = 1000))
        particles.clear()
        repeat(particleCount) {
            particles.add(
                CtosParticle(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    vx = Random.nextFloat() * 2 - 1,
                    vy = Random.nextFloat() * 2 - 1,
                    size = Random.nextFloat() * (particleMaxSize - particleMinSize) + particleMinSize,
                    color = particleColor.copy(alpha = Random.nextFloat() * 0.5f + 0.3f)
                )
            )
        }
    }

    // Flicker effect for some particles
    val flickerAlpha by animateFloatAsState(
        targetValue = if (Random.nextBoolean()) 0.5f else 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flickerAlpha"
    )

    // Update particle positions
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(30) // Slower for performance
            val updatedParticles = mutableListOf<CtosParticle>()
            particles.forEach { particle ->
                var newX = particle.x + particle.vx * 0.001f
                var newY = particle.y + particle.vy * 0.001f
                var newVx = particle.vx
                var newVy = particle.vy

                // Bounce off screen edges
                if (newX < 0f || newX > 1f) {
                    newVx = -newVx
                    newX = newX.coerceIn(0f, 1f)
                }
                if (newY < 0f || newY > 1f) {
                    newVy = -newVy
                    newY = newY.coerceIn(0f, 1f)
                }

                updatedParticles.add(
                    particle.copy(x = newX, y = newY, vx = newVx, vy = newVy)
                )
            }
            particles.clear()
            particles.addAll(updatedParticles)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Draw particles
        particles.forEach { particle ->
            val particleAlpha = if (Random.nextFloat() < 0.3f) flickerAlpha else particle.color.alpha
            drawCircle(
                color = particle.color.copy(alpha = particleAlpha * alpha.value),
                radius = particle.size,
                center = Offset(particle.x * canvasWidth, particle.y * canvasHeight)
            )
        }

        // Draw connecting lines
        for (i in particles.indices) {
            for (j in i + 1 until particles.size) {
                val p1 = particles[i]
                val p2 = particles[j]
                val dx = (p2.x - p1.x) * canvasWidth
                val dy = (p2.y - p1.y) * canvasHeight
                val distance = hypot(dx, dy)
                if (distance < connectionDistance) {
                    val lineAlpha = (0.3f * (1 - distance / connectionDistance)).coerceIn(0f, 0.2f)
                    drawLine(
                        color = lineColor.copy(alpha = lineAlpha * alpha.value),
                        start = Offset(p1.x * canvasWidth, p1.y * canvasHeight),
                        end = Offset(p2.x * canvasWidth, p2.y * canvasHeight),
                        strokeWidth = lineStrokeWidth
                    )
                }
            }
        }
    }
}

data class CtosParticle(
    val x: Float, // Normalized [0,1]
    val y: Float, // Normalized [0,1]
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color
)