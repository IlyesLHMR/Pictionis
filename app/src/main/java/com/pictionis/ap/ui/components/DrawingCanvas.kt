package com.pictionis.ap.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pictionis.ap.model.Point
import com.pictionis.ap.model.Stroke
import kotlin.math.hypot
import java.util.UUID

@Composable
fun DrawingCanvas(
    strokesRemote: List<Stroke>,
    currentUserId: String?,
    isDrawingEnabled: Boolean = false,
    brushColor: Color = Color.Black,
    brushSize: Float = 6f,
    sampleDistance: Float = 6f,
    modifier: Modifier = Modifier,
    onStrokeFinished: (Stroke) -> Unit
) {
    val currentPoints = remember { mutableStateListOf<Point>() }

    Box(
        modifier = modifier
            .background(Color(0xFFF5F5F5))
            .pointerInput(isDrawingEnabled) {
                if (!isDrawingEnabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        currentPoints.clear()
                        currentPoints.add(Point(offset.x, offset.y))
                    },
                    onDrag = { change, _ ->
                        val p = change.position
                        val last = currentPoints.lastOrNull()
                        if (last == null || distance(last.x, last.y, p.x, p.y) >= sampleDistance) {
                            currentPoints.add(Point(p.x, p.y))
                        }
                    },
                    onDragEnd = {
                        val uid = currentUserId ?: ""
                        if (currentPoints.isNotEmpty()) {
                            val stroke = Stroke(
                                id = UUID.randomUUID().toString(),
                                userId = uid,
                                color = brushColor.toArgb(),
                                strokeWidth = brushSize,
                                points = currentPoints.toList()
                            )
                            onStrokeFinished(stroke)
                        }
                        currentPoints.clear()
                    },
                    onDragCancel = {
                        currentPoints.clear()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // draw remote strokes
            strokesRemote.forEach { s ->
                if (s.points.isEmpty()) return@forEach
                val path = Path().apply {
                    val first = s.points.first()
                    moveTo(first.x, first.y)
                    s.points.drop(1).forEach { p -> lineTo(p.x, p.y) }
                }
                drawPath(path = path, color = Color(s.color), style = DrawStroke(width = s.strokeWidth))
            }

            // draw current (local) stroke
            if (currentPoints.isNotEmpty()) {
                val path = Path().apply {
                    val first = currentPoints.first()
                    moveTo(first.x, first.y)
                    currentPoints.drop(1).forEach { p -> lineTo(p.x, p.y) }
                }
                drawPath(path = path, color = brushColor, style = DrawStroke(width = brushSize))
            }
        }
    }
}

private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return hypot(x2 - x1, y2 - y1)
}

@Preview(showBackground = true)
@Composable
fun PreviewDrawingCanvas() {
    // Preview uses a small fixed size so it renders in the preview window
    DrawingCanvas(
        strokesRemote = emptyList(),
        currentUserId = null,
        isDrawingEnabled = false,
        brushColor = Color.Black,
        brushSize = 6f,
        sampleDistance = 6f,
        modifier = Modifier.size(240.dp),
        onStrokeFinished = {}
    )
}