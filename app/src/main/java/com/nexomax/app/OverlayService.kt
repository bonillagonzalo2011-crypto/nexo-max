package com.nexomax.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import kotlin.math.abs

class OverlayService : Service() {
    companion object { @Volatile var running = false }

    private lateinit var wm: WindowManager
    private lateinit var ctx: Context
    private val dm get() = resources.displayMetrics

    private var ball: View? = null
    private var ballLp: WindowManager.LayoutParams? = null
    private var menu: LinearLayout? = null
    private var menuLp: WindowManager.LayoutParams? = null
    private var menuOpen = false
    private var cross: CrosshairView? = null
    private var crossLp: WindowManager.LayoutParams? = null

    private var bolita = false
    private var fijado = false
    private var mira = false
    private var shape = 0
    private var hue = 202f
    private var t = 0.27f

    private lateinit var rowFijar: View
    private lateinit var rowTam: View
    private lateinit var rowCol: View
    private lateinit var rowMira: View
    private lateinit var rowForma: View
    private lateinit var swFijar: Switch
    private val icons = ArrayList<ShapeIcon>()

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        running = true
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        ctx = ContextThemeWrapper(this, android.R.style.Theme_Material_Light)
        startFg()
        showBall(dm.widthPixels - dp(64f), (dm.heightPixels * 0.4f).toInt())
    }

    override fun onStartCommand(i: Intent?, f: Int, s: Int) = START_NOT_STICKY

    override fun onDestroy() {
        running = false
        try { ball?.let { wm.removeView(it) } } catch (_: Exception) {}
        try { if (menuOpen) menu?.let { wm.removeView(it) } } catch (_: Exception) {}
        try { cross?.let { wm.removeView(it) } } catch (_: Exception) {}
        super.onDestroy()
    }

    private fun startFg() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel("nexo", "NEXO MAX", NotificationManager.IMPORTANCE_LOW))
        val n = Notification.Builder(this, "nexo")
            .setContentTitle("NEXO MAX activo")
            .setContentText("Pulsa DESINJETAR en la app para cerrar")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
        if (Build.VERSION.SDK_INT >= 34) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        else startForeground(1, n)
    }

    private fun dp(v: Float) = (v * dm.density).toInt()

    private fun lp(w: Int, h: Int) = WindowManager.LayoutParams(
        w, h, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START }

    // Arrastra "target" tocando "handle"; si solo es un toque, llama onTap
    private fun drag(handle: View, target: View, p: WindowManager.LayoutParams, onTap: (() -> Unit)?) {
        var sx = 0f; var sy = 0f; var ox = 0; var oy = 0; var moved = false
        handle.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { sx = e.rawX; sy = e.rawY; ox = p.x; oy = p.y; moved = false }
                MotionEvent.ACTION_MOVE -> {
                    val dx = e.rawX - sx; val dy = e.rawY - sy
                    if (abs(dx) > dp(6f) || abs(dy) > dp(6f)) moved = true
                    if (moved) {
                        p.x = (ox + dx).toInt().coerceIn(0, maxOf(0, dm.widthPixels - target.width))
                        p.y = (oy + dy).toInt().coerceIn(0, maxOf(0, dm.heightPixels - target.height))
                        wm.updateViewLayout(target, p)
                    }
                }
                MotionEvent.ACTION_UP -> if (!moved) onTap?.invoke()
            }
            true
        }
    }

    // ---------- Bola flotante ----------
    private fun showBall(x: Int, y: Int) {
        val v = BallView(ctx)
        val p = lp(dp(52f), dp(52f)).apply { this.x = x; this.y = y }
        ball = v; ballLp = p
        wm.addView(v, p)
        drag(v, v, p) { openMenu() }
    }

    // ---------- Menú ----------
    private fun openMenu() {
        val b = ball ?: return
        val bp = ballLp ?: return
        val m = menu ?: buildMenu().also { menu = it }
        val mp = lp(dp(240f), WindowManager.LayoutParams.WRAP_CONTENT).apply {
            x = (bp.x - dp(190f)).coerceIn(0, maxOf(0, dm.widthPixels - dp(240f)))
            y = (bp.y - dp(5f)).coerceIn(0, maxOf(0, dm.heightPixels - dp(430f)))
        }
        menuLp = mp
        wm.removeView(b); ball = null
        wm.addView(m, mp)
        menuOpen = true
        drag(m.getChildAt(0), m, mp, null)
    }

    private fun closeMenu() {
        val m = menu ?: return
        val mp = menuLp ?: return
        val bx = (mp.x + m.width - dp(46f)).coerceIn(0, maxOf(0, dm.widthPixels - dp(52f)))
        val by = (mp.y + dp(1f)).coerceIn(0, maxOf(0, dm.heightPixels - dp(52f)))
        wm.removeView(m); menuOpen = false
        showBall(bx, by)
    }

    private fun bringMenuTop() {
        val m = menu ?: return
        val mp = menuLp ?: return
        if (menuOpen) { wm.removeView(m); wm.addView(m, mp) }
    }

    private fun label(s: String) = TextView(ctx).apply {
        text = s; textSize = 16f; typeface = Typeface.DEFAULT_BOLD; setTextColor(Shapes.NAVY)
    }

    private fun switchRow(name: String, on: (Boolean) -> Unit): Pair<LinearLayout, Switch> {
        val sw = Switch(ctx)
        sw.setOnCheckedChangeListener { _, c -> on(c) }
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16f), dp(6f), dp(16f), dp(6f))
            addView(label(name), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(sw)
        }
        return row to sw
    }

    private fun seekRow(init: Int, rainbow: Boolean, cb: (Int) -> Unit): LinearLayout {
        val sb = SeekBar(ctx).apply { max = 100; progress = init }
        if (rainbow) {
            sb.background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED)
            ).apply { cornerRadius = dp(6f).toFloat() }
        }
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) { cb(p) }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        return LinearLayout(ctx).apply {
            setPadding(dp(12f), dp(6f), dp(12f), dp(6f))
            addView(sb, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(30f)))
        }
    }

    private fun buildMenu(): LinearLayout {
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.WHITE); cornerRadius = dp(28f).toFloat(); setStroke(dp(4f), Shapes.NAVY)
            }
            setPadding(dp(4f), dp(4f), dp(4f), dp(12f))
        }
        // Cabecera (se arrastra desde aquí)
        val r = dp(24f).toFloat()
        val header = FrameLayout(ctx).apply {
            background = GradientDrawable().apply {
                setColor(Shapes.SKY); cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f)
            }
        }
        header.addView(TextView(ctx).apply {
            text = "NEXO MAX"; textSize = 14f; typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE); setShadowLayer(4f, 2f, 2f, Shapes.NAVY)
        }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER_VERTICAL or Gravity.START).apply { leftMargin = dp(14f) })
        val x = View(ctx).apply {
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.WHITE); setStroke(dp(3f), Shapes.NAVY) }
            setOnClickListener { closeMenu() }
        }
        header.addView(x, FrameLayout.LayoutParams(dp(30f), dp(30f), Gravity.CENTER_VERTICAL or Gravity.END).apply { rightMargin = dp(12f) })
        root.addView(header, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(50f)))

        val (rBol, swBol) = switchRow("Bolita") { c ->
            bolita = c
            if (c) showCross() else { hideCross(); swFijar.isChecked = false }
            refresh()
        }
        val (rFij, swF) = switchRow("Fijar") { c -> fijado = c; applyFijado() }
        swFijar = swF; rowFijar = rFij
        rowTam = seekRow((t * 100).toInt(), false) { p -> t = p / 100f; updateCross() }
        rowCol = seekRow((hue / 360f * 100).toInt(), true) { p -> hue = p / 100f * 360f; updateCross() }
        val (rMir, _) = switchRow("Mira") { c -> mira = c; updateCross(); refresh() }
        rowMira = rMir

        val strip = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
        for (i in 0 until Shapes.count) {
            val ic = ShapeIcon(ctx, i).apply { sel = (i == shape); setOnClickListener { selectShape(i) } }
            icons.add(ic)
            strip.addView(ic, LinearLayout.LayoutParams(dp(32f), dp(32f)).apply { rightMargin = dp(8f) })
        }
        val hs = HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false; addView(strip) }
        rowForma = LinearLayout(ctx).apply {
            setPadding(dp(16f), dp(6f), dp(12f), dp(6f))
            addView(hs, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        root.addView(rBol); root.addView(rowFijar); root.addView(rowTam)
        root.addView(rowCol); root.addView(rowMira); root.addView(rowForma)
        refresh()
        return root
    }

    private fun refresh() {
        val v = if (bolita) View.VISIBLE else View.GONE
        rowFijar.visibility = v; rowTam.visibility = v; rowCol.visibility = v; rowMira.visibility = v
        rowForma.visibility = if (bolita && mira) View.VISIBLE else View.GONE
    }

    private fun selectShape(i: Int) {
        shape = i
        icons.forEachIndexed { k, ic -> ic.sel = (k == i); ic.invalidate() }
        updateCross()
    }

    // ---------- Bola celeste (mira) ----------
    private fun crossSize() = dp(50f + t * 150f)

    private fun showCross() {
        val s = crossSize()
        val v = CrosshairView(ctx).also { it.hue = hue; it.mira = mira; it.shape = shape }
        val p = lp(s, s).apply { x = (dm.widthPixels - s) / 2; y = (dm.heightPixels - s) / 2 }
        cross = v; crossLp = p
        wm.addView(v, p)
        drag(v, v, p, null)
        bringMenuTop()
    }

    private fun hideCross() {
        cross?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        cross = null; crossLp = null; fijado = false
    }

    private fun updateCross() {
        val v = cross ?: return
        val p = crossLp ?: return
        val s = crossSize()
        val diff = (p.width - s) / 2
        p.x = maxOf(0, p.x + diff); p.y = maxOf(0, p.y + diff)
        p.width = s; p.height = s
        v.hue = hue; v.mira = mira; v.shape = shape
        wm.updateViewLayout(v, p)
        v.invalidate()
    }

    private fun applyFijado() {
        val v = cross ?: return
        val p = crossLp ?: return
        val f = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        p.flags = if (fijado) p.flags or f else p.flags and f.inv()
        wm.updateViewLayout(v, p)
    }
}
