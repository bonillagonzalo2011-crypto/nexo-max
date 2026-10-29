package com.nexomax.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var btn: Button

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val d = resources.displayMetrics.density
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding((24 * d).toInt(), 0, (24 * d).toInt(), 0)
        }
        root.addView(TextView(this).apply {
            text = "NEXO MAX"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Shapes.NAVY)
            gravity = Gravity.CENTER
        })
        btn = Button(this).apply {
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#2f7fe0"))
            setOnClickListener { toggle() }
        }
        root.addView(btn, LinearLayout.LayoutParams((280 * d).toInt(), (56 * d).toInt()).apply { topMargin = (40 * d).toInt() })
        root.addView(TextView(this).apply {
            text = "Necesita el permiso «Mostrar sobre otras apps»"
            textSize = 12f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = (16 * d).toInt() })
        setContentView(root)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() { btn.text = if (OverlayService.running) "DESINJETAR" else "INJETAR" }

    private fun toggle() {
        val i = Intent(this, OverlayService::class.java)
        if (OverlayService.running) {
            stopService(i)
        } else {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Activa «Mostrar sobre otras apps» y vuelve a pulsar INJETAR", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                return
            }
            startForegroundService(i)
        }
        btn.postDelayed({ refresh() }, 300)
    }
}
