package com.nexomax.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View

class BallView(c: Context) : View(c) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(cv: Canvas) {
        val d = resources.displayMetrics.density
        val s = width / 2f
        p.style = Paint.Style.FILL; p.color = Color.WHITE
        cv.drawCircle(s, s, s, p)
        p.color = Color.parseColor("#2f7fe0")
        cv.drawCircle(s, s, s - 4 * d, p)
        p.style = Paint.Style.STROKE; p.strokeWidth = 3 * d; p.color = Shapes.NAVY
        cv.drawCircle(s, s, s - 1.5f * d, p)
        p.style = Paint.Style.FILL; p.color = Color.WHITE
        p.textSize = 16 * d; p.typeface = Typeface.DEFAULT_BOLD; p.textAlign = Paint.Align.CENTER
        cv.drawText("N", s, s + 6 * d, p)
    }
}
