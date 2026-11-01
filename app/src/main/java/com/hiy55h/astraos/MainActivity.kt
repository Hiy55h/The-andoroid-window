package com.hiy55h.astraos

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private lateinit var windows: FrameLayout
    private lateinit var taskbar: LinearLayout
    private var windowCount = 0
    private val bg = Color.rgb(12, 22, 42)
    private val panel = Color.rgb(27, 42, 66)
    private val accent = Color.rgb(74, 132, 220)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setStatusBarColor(bg)
        window.setNavigationBarColor(bg)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        showLockScreen()
    }

    private fun showLockScreen() {
        val lock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackground(GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(9, 19, 42), Color.rgb(25, 65, 112))))
        }
        lock.addView(TextView(this).apply {
            text = "ASTRAOS"
            textSize = 38f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        })
        lock.addView(TextView(this).apply {
            text = "Your Android desktop"
            textSize = 15f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 28)
        })
        lock.addView(Button(this).apply {
            text = "Unlock"
            setOnClickListener { showDesktop() }
        })
        setContentView(lock)
    }

    private fun showDesktop() {
        root = FrameLayout(this).apply { setBackgroundColor(bg) }
        val desktop = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 70)
            setBackground(GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(10, 25, 51), Color.rgb(24, 54, 91))))
        }
        desktop.addView(TextView(this).apply {
            text = "AstraOS"
            textSize = 34f
            setTextColor(Color.WHITE)
        })
        desktop.addView(TextView(this).apply {
            text = "Desktop"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 0, 0, 20)
        })
        val shortcuts = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        shortcuts.addView(launcherButton("🌐\nBrowser") { openBrowserWindow() })
        shortcuts.addView(launcherButton("⚙\nSettings") { openInfoWindow("Settings", "AstraOS settings\n\nDisplay: Desktop mode\nInput: Keyboard + mouse\nBuild: Preview 0.2") })
        shortcuts.addView(launcherButton("▦\nApps") { showApps() })
        desktop.addView(shortcuts)
        root.addView(desktop, FrameLayout.LayoutParams(-1, -1))

        windows = FrameLayout(this)
        val wp = FrameLayout.LayoutParams(-1, -1)
        wp.bottomMargin = dp(54)
        root.addView(windows, wp)

        taskbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(10, 4, 10, 4)
            setBackgroundColor(Color.rgb(18, 25, 39))
        }
        taskbar.addView(Button(this).apply {
            text = "✦ Start"
            setOnClickListener { showStartMenu() }
        })
        taskbar.addView(TextView(this).apply {
            text = "  AstraOS"
            textSize = 14f
            setTextColor(Color.WHITE)
        })
        root.addView(taskbar, FrameLayout.LayoutParams(-1, dp(54), Gravity.BOTTOM))
        setContentView(root)
    }

    private fun launcherButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        textSize = 14f
        isAllCaps = false
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(dp(100), dp(76)).apply { rightMargin = dp(10) }
    }

    private fun showStartMenu() {
        val items = arrayOf("Browser", "Settings", "Installed apps", "Lock screen")
        AlertDialog.Builder(this).setTitle("AstraOS Start")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> openBrowserWindow()
                    1 -> openInfoWindow("Settings", "AstraOS settings\n\nKeyboard and mouse ready.\nAndroid system settings can be opened separately.")
                    2 -> showApps()
                    3 -> showLockScreen()
                }
            }.show()
    }

    private fun showApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(pm).toString().lowercase() }
        val labels = apps.map { it.loadLabel(pm).toString() }.toTypedArray()
        if (labels.isEmpty()) {
            Toast.makeText(this, "No launchable apps found", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this).setTitle("Installed apps")
            .setItems(labels) { _, which ->
                val launch = pm.getLaunchIntentForPackage(apps[which].activityInfo.packageName)
                if (launch != null) startActivity(launch)
            }.setNegativeButton("Close", null).show()
    }

    private fun openInfoWindow(title: String, message: String) {
        val body = TextView(this).apply {
            text = message
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(18, 18, 18, 18)
        }
        createWindow(title, body)
    }

    private fun openBrowserWindow() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(5, 4, 5, 4)
            setBackgroundColor(Color.rgb(40, 48, 62))
        }
        val address = EditText(this).apply {
            setSingleLine(true)
            textSize = 14f
            hint = "Search or enter website"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(8, 0, 8, 0)
            setBackgroundColor(Color.rgb(63, 73, 91))
            imeOptions = EditorInfo.IME_ACTION_GO
        }
        val web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.useWideViewPort = true
        web.settings.loadWithOverviewMode = true
        web.settings.builtInZoomControls = true
        web.settings.displayZoomControls = false
        web.settings.userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) { address.setText(url) }
        }
        web.webChromeClient = WebChromeClient()
        fun go() {
            val raw = address.text.toString().trim()
            if (raw.isEmpty()) return
            val target = if (raw.contains("://")) raw else if (raw.contains(".") && !raw.contains(" ")) "https://$raw"
                else "https://www.google.com/search?q=" + java.net.URLEncoder.encode(raw, "UTF-8")
            web.loadUrl(target)
        }
        bar.addView(address, LinearLayout.LayoutParams(0, dp(42), 1f))
        bar.addView(Button(this).apply { text = "Go"; setOnClickListener { go() } })
        address.setOnEditorActionListener { _, _, _ -> go(); true }
        content.addView(bar)
        content.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))
        web.loadUrl("https://www.google.com")
        createWindow("Browser", content)
    }

    private fun createWindow(title: String, body: View) {
        windowCount++
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackground(GradientDrawable().apply {
                setColor(Color.rgb(35, 45, 62))
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), Color.rgb(95, 119, 153))
            })
            clipToOutline = true
            elevation = dp(12).toFloat()
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(10, 2, 5, 2)
            setBackgroundColor(Color.rgb(24, 34, 51))
        }
        header.addView(TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(Color.WHITE)
        }, LinearLayout.LayoutParams(0, dp(42), 1f))
        header.addView(Button(this).apply {
            text = "×"
            minWidth = dp(42)
            setOnClickListener { windows.removeView(card) }
        })
        card.addView(header)
        card.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))
        val params = FrameLayout.LayoutParams(dp(430), dp(310), Gravity.TOP or Gravity.START)
        params.leftMargin = dp(24 + (windowCount * 18) % 100)
        params.topMargin = dp(24 + (windowCount * 16) % 80)
        windows.addView(card, params)
        card.setOnClickListener { card.bringToFront() }
        header.setOnTouchListener(object : View.OnTouchListener {
            var downX = 0f
            var downY = 0f
            var startLeft = 0
            var startTop = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val p = card.layoutParams as FrameLayout.LayoutParams
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX; downY = event.rawY
                        startLeft = p.leftMargin; startTop = p.topMargin
                        card.bringToFront()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        p.leftMargin = (startLeft + event.rawX - downX).toInt().coerceAtLeast(0)
                        p.topMargin = (startTop + event.rawY - downY).toInt().coerceAtLeast(0)
                        card.layoutParams = p
                        return true
                    }
                    MotionEvent.ACTION_UP -> return true
                }
                return false
            }
        })
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
