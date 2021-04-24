package com.shahankbhat.imageeditor

import kotlin.math.abs

data class PointXY(val x: Float, val y :Float)

fun PointXY.deltaX(endPoint: PointXY): Float {
    return abs(endPoint.x - this.x)
}

fun PointXY.deltaY(endPoint: PointXY): Float {
    return abs(endPoint.y - this.y)
}