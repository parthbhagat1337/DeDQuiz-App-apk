package com.example.dedquiz

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class ParticleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<Particle>()
    private val paint = Paint().apply {
        color = 0xFF6495ED.toInt() // A more subtle blue
        style = Paint.Style.FILL
    }
    private val linePaint = Paint().apply {
        color = 0xFFADD8E6.toInt() // Light blue
        style = Paint.Style.STROKE
        strokeWidth = 1.5f // Slightly thicker lines
        alpha = 75
    }

    init {
        repeat(80) {
            particles.add(Particle(
                x = Random.nextFloat() * width,
                y = Random.nextFloat() * height,
                vx = Random.nextFloat() * 4 - 2,
                vy = Random.nextFloat() * 4 - 2,
                size = Random.nextFloat() * 3 + 1,
                color = 0xFF6495ED.toInt()
            ))
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (particle in particles) {
            paint.color = particle.color
            canvas.drawCircle(particle.x, particle.y, particle.size, paint)
        }

        for (i in particles.indices) {
            for (j in i + 1 until particles.size) {
                val p1 = particles[i]
                val p2 = particles[j]
                val distance = kotlin.math.hypot(p1.x - p2.x, p1.y - p2.y)
                if (distance < 100) {
                    linePaint.alpha = (255 * (1 - distance / 100)).toInt().coerceIn(0, 255)
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, linePaint)
                }
            }
        }

        for (particle in particles) {
            particle.x += particle.vx
            particle.y += particle.vy
            if (particle.x < 0 || particle.x > width) particle.vx = -particle.vx
            if (particle.y < 0 || particle.y > height) particle.vy = -particle.vy
        }

        invalidate()
    }

    private data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val size: Float,
        val color: Int
    )
}