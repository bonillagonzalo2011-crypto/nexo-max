package com.nexomax.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.PathParser

object Shapes {
    val NAVY = Color.parseColor("#0b2447")
    val SKY = Color.parseColor("#7fd8ff")

    // Mismas 19 formas de la versión web (coordenadas 0..100)
    private val paths = listOf(
        "M50 0V100M0 50H100",
        "M50 6V38M50 62V94M6 50H38M62 50H94",
        "M18 18L82 82M82 18L18 82",
        "M50 0V28M50 72V100M0 50H28M72 50H100",
        "M20 38V20H38M62 20H80V38M80 62V80H62M38 80H20V62",
        "",
        "M50 28V72M28 50H72",
        "M0 50H100M50 50V100",
        "M50 12L88 50L50 88L12 50Z",
        "",
        "M22 66L50 38L78 66",
        "M50 14L86 78H14Z",
        "M50 10L85 30V70L50 90L15 70V30Z",
        "M50 12L72.3 80.7L13.9 38.3L86.1 38.3L27.7 80.7Z",
        "M50 8L58 42L92 50L58 58L50 92L42 58L8 50L42 42Z",
        "M50 82C20 60 12 40 24 28C34 18 46 24 50 34C54 24 66 18 76 28C88 40 80 60 50 82Z",
        "",
        "M38 20L50 32L62 20M80 38L68 50L80 62M62 80L50 68L38 80M20 62L32 50L20 38",
        "M58 8L28 54H48L42 92L72 44H52Z"
    )
    val count = paths.size
    private val cache = HashMap<Int, Path>()

    private fun path(i: Int): Path = cache.getOrPut(i) { PathParser.createPathFromPathData(paths[i]) }

    fun draw(c: Canvas, idx: Int, size: Float, color: Int, stroke: Float, icon: Boolean = false) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.STROKE
        p.color = color
        p.strokeWidth = stroke
        p.strokeJoin = Paint.Join.ROUND
        c.save()
        c.scale(size / 100f, size / 100f)
        if (paths[idx].isNotEmpty()) c.drawPath(path(idx), p)
        when (idx) {
            3 -> c.drawCircle(50f, 50f, 22f, p)
            9 -> c.drawRect(22f, 22f, 78f, 78f, p)
            16 -> { c.drawCircle(50f, 50f, 30f, p); c.drawCircle(50f, 50f, 14f, p) }
            5 -> if (icon) { p.style = Paint.Style.FILL; c.drawCircle(50f, 50f, 12f, p) }
        }
        c.restore()
    }
}
