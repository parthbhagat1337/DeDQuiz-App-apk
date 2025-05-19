package com.example.dedquiz

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class Hexagon : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2

            val angle = Math.PI / 3
            moveTo(
                x = centerX + radius * kotlin.math.cos(0.0).toFloat(),
                y = centerY + radius * kotlin.math.sin(0.0).toFloat()
            )
            for (i in 1..6) {
                val x = centerX + radius * kotlin.math.cos(angle * i).toFloat()
                val y = centerY + radius * kotlin.math.sin(angle * i).toFloat()
                lineTo(x, y)
            }
            close()
        }
        return Outline.Generic(path)
    }
}
