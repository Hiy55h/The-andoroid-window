package com.hiy55h.astraos

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(10, 18, 35)
        window.navigationBarColor = Color.rgb(10, 18, 35)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 20)
            setBackgroundColor(Color.rgb(12, 22, 42))
        }
        root.addView(TextView(this).apply {
            text = "AstraOS"
            textSize = 34f
            setTextColor(Color.WHITE)
        })
        root.addView(TextView(this).apply {
            text = "Android Desktop Environment  •  Preview 0.1"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 4, 0, 24)
        })
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 16)
            setBackgroundColor(Color.rgb(29, 44, 70))
        }
        card.addView(TextView(this).apply {
            text = "Browser"
            textSize = 22f
            setTextColor(Color.WHITE)
        })
        card.addView(TextView(this).apply {
            text = "Chrome-style desktop browser · tabs · zoom · extensions"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 8, 0, 16)
        })
        card.addView(Button(this).apply {
            text = "Open Browser"
            setOnClickListener { startActivity(Intent(this@MainActivity, BrowserActivity::class.java)) }
        })
        root.addView(card, LinearLayout.LayoutParams(-1, -2))
        root.addView(TextView(this).apply {
            text = "Window manager preview: app windows will be added here.\nExternal Android apps can only become true resizable windows when the device supports freeform/multi-window mode."
            textSize = 13f
            setTextColor(Color.LTGRAY)
            setPadding(2, 24, 2, 0)
        })
        setContentView(root)
    }
}
