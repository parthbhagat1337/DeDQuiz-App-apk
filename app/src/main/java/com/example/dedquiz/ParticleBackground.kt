package com.example.dedquiz

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.hypot
import kotlin.random.Random

@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 80,
    connectionDistance: Float = 100f,
    particleColor: Color = Color.Blue,
    lineColor: Color = Color.LightGray,
    particleMinSize: Float = 1f,
    particleMaxSize: Float = 4f,
    lineStrokeWidth: Float = 1.5f
) {
    val particles = remember { mutableStateListOf<Particle>() }
    val alpha = remember { Animatable(0f) }

    // Initialize particles
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(durationMillis = 1000))
        particles.clear()
        repeat(particleCount) {
            particles.add(
                Particle(
                    x = Random.nextFloat() * 1000f,
                    y = Random.nextFloat() * 2000f,
                    vx = Random.nextFloat() * 2 - 1,
                    vy = Random.nextFloat() * 2 - 1,
                    size = Random.nextFloat() * (particleMaxSize - particleMinSize) + particleMinSize,
                    color = particleColor.copy(alpha = Random.nextFloat() * 0.7f + 0.3f)
                )
            )
        }
    }

    // Update particles on every animation cycle
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(25)
            val updatedParticles = mutableListOf<Particle>()
            particles.forEach { particle ->
                var newX = particle.x + particle.vx
                var newY = particle.y + particle.vy

                var newVx = particle.vx
                var newVy = particle.vy

                if (newX < 0 || newX > 1000f) {
                    newVx = -newVx
                    newX += newVx
                }
                if (newY < 0 || newY > 2000f) {
                    newVy = -newVy
                    newY += newVy
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
        // Draw particles
        particles.forEach { particle ->
            drawCircle(
                color = particle.color,
                radius = particle.size,
                center = Offset(particle.x, particle.y)
            )
        }

        // Draw connecting lines
        for (i in particles.indices) {
            for (j in i + 1 until particles.size) {
                val p1 = particles[i]
                val p2 = particles[j]
                val distance = hypot(p1.x - p2.x, p1.y - p2.y)
                if (distance < connectionDistance) {
                    val lineAlpha =
                        0.1f + (0.3f * (1 - distance / connectionDistance)).coerceIn(0f, 0.3f)
                    drawLine(
                        color = lineColor.copy(alpha = lineAlpha),
                        start = Offset(p1.x, p1.y),
                        end = Offset(p2.x, p2.y),
                        strokeWidth = lineStrokeWidth
                    )
                }
            }
        }
    }
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val color: Color
)