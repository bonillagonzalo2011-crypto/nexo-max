package com.nexomax.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class CrosshairView(c: Context) : View(c) {
    var hue = 202f
    var mira = false
    var shape = 0
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        val s = width.toFloat()
        val col = Color.HSVToColor(floatArrayOf(hue, 0.96f, 1f))
        val bw = maxOf(3f, s * 6f / 90f)
        p.style = Paint.Style.FILL
        p.color = col
        p.alpha = 71
        canvas.drawCircle(s / 2, s / 2, s / 2, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = bw
        p.color = col
        canvas.drawCircle(s / 2, s / 2, s / 2 - bw / 2, p)
        p.style = Paint.Style.FILL
        canvas.drawCircle(s / 2, s / 2, s * 10f / 90f, p)
        if (mira) Shapes.draw(canvas, shape, s, col, 3.5f)
    }
}
