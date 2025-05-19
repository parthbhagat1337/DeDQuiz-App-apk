package com.example.dedquiz

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.cos
import kotlin.math.sin

class LongHexagon : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            // Define long hexagon with 3:1 width-to-height ratio
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2
            // Adjust radius to create elongated shape
            val radiusX = width / 2
            val radiusY = height / 2

            // Calculate points for a hexagon, stretched horizontally
            val angle = Math.PI / 3 // 60 degrees
            moveTo(
                x = centerX + radiusX * cos(0.0).toFloat(),
                y = centerY + radiusY * sin(0.0).toFloat()
            )
            for (i in 1..6) {
                val x = centerX + radiusX * cos(angle * i).toFloat()
                val y = centerY + radiusY * sin(angle * i).toFloat()
                lineTo(x, y)
            }
            close()
        }
        return Outline.Generic(path)
    }
}