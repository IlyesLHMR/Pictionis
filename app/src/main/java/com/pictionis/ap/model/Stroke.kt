package com.pictionis.ap.model

data class Stroke(
    var id: String = "",
    var userId: String = "",
    var color: Int = 0,
    var strokeWidth: Float = 6f,
    var points: List<Point> = emptyList()
)