package com.nexomax.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

class ShapeIcon(c: Context, val idx: Int) : View(c) {
    var sel = false
    private val bg = Paint(Paint.ANTI_ALIAS_FLAG)
    private val br = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    override fun onDraw(cv: Canvas) {
        val d = resources.displayMetrics.density
        val w = width.toFloat(); val h = height.toFloat(); val st = 3 * d
        bg.color = if (sel) Shapes.SKY else Color.WHITE
        br.strokeWidth = st; br.color = Shapes.NAVY
        val r = RectF(st / 2, st / 2, w - st / 2, h - st / 2)
        cv.drawRoundRect(r, 8 * d, 8 * d, bg)
        cv.drawRoundRect(r, 8 * d, 8 * d, br)
        val s = 18 * d
        cv.save()
        cv.translate((w - s) / 2, (h - s) / 2)
        Shapes.draw(cv, idx, s, Shapes.NAVY, 9f, true)
        cv.restore()
    }
}
