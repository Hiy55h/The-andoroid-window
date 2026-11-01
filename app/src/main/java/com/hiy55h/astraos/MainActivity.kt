package com.hiy55h.astraos

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.DateFormat
import java.util.Date

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private lateinit var windows: FrameLayout
    private lateinit var taskbar: LinearLayout
    private val desktopWindows = mutableListOf<DesktopWindow>()
    private var windowCount = 0
    private val bg = Color.rgb(9, 18, 34)
    private val panel = Color.rgb(25, 38, 59)
    private val accent = Color.rgb(75, 139, 235)
    private val clockHandler = Handler(Looper.getMainLooper())
    private val clockTick = object : Runnable {
        override fun run() {
            if (::taskbar.isInitialized && taskbar.childCount > 0) {
                val clock = taskbar.getChildAt(taskbar.childCount - 1) as? TextView
                clock?.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
            }
            clockHandler.postDelayed(this, 15000)
        }
    }

    private data class DesktopWindow(
        val title: String,
        val card: LinearLayout,
        val taskButton: Button,
        var minimized: Boolean = false,
        var maximized: Boolean = false,
        var previousParams: FrameLayout.LayoutParams? = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        showLockScreen()
    }

    override fun onResume() {
        super.onResume()
        if (::taskbar.isInitialized) clockHandler.post(clockTick)
    }

    override fun onPause() {
        clockHandler.removeCallbacks(clockTick)
        super.onPause()
    }

    override fun onDestroy() {
        clockHandler.removeCallbacks(clockTick)
        desktopWindows.forEach { (it.card.getChildAt(1) as? WebView)?.destroy() }
        super.onDestroy()
    }

    private fun showLockScreen() {
        val lock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(32), dp(32), dp(32), dp(32))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(7, 16, 37), Color.rgb(22, 69, 121))
            )
        }
        lock.addView(TextView(this).apply {
            text = "ASTRAOS"
            textSize = 42f
            letterSpacing = 0.16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        })
        lock.addView(TextView(this).apply {
            text = "Your Android desktop"
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(30))
        })
        lock.addView(Button(this).apply {
            text = "Unlock"
            isAllCaps = false
            setOnClickListener { showDesktop() }
        })
        setContentView(lock)
    }

    private fun showDesktop() {
        desktopWindows.clear()
        windowCount = 0
        root = FrameLayout(this).apply { setBackgroundColor(bg) }

        val desktop = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(26), dp(22), dp(26), dp(70))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(8, 22, 47), Color.rgb(24, 58, 99))
            )
        }
        desktop.addView(TextView(this).apply {
            text = "AstraOS"
            textSize = 36f
            setTextColor(Color.WHITE)
            letterSpacing = 0.04f
        })
        desktop.addView(TextView(this).apply {
            text = "Desktop  •  Version 0.1.0"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 0, 0, dp(22))
        })
        val shortcuts = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        shortcuts.addView(shortcut("🌐\nBrowser") { openBrowserWindow() })
        shortcuts.addView(shortcut("▦\nApps") { showAppsWindow() })
        shortcuts.addView(shortcut("⚙\nSettings") { showSettingsWindow() })
        desktop.addView(shortcuts)
        root.addView(desktop, FrameLayout.LayoutParams(-1, -1))

        windows = FrameLayout(this)
        val windowArea = FrameLayout.LayoutParams(-1, -1)
        windowArea.bottomMargin = dp(58)
        root.addView(windows, windowArea)

        taskbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(4))
            setBackgroundColor(Color.rgb(14, 21, 34))
        }
        taskbar.addView(Button(this).apply {
            text = "✦ Start"
            isAllCaps = false
            setOnClickListener { showStartMenu() }
        })
        taskbar.addView(TextView(this).apply {
            text = "  AstraOS"
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
        }, LinearLayout.LayoutParams(0, -1, 1f))
        taskbar.addView(TextView(this).apply {
            text = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(10), 0, dp(8), 0)
        })
        root.addView(taskbar, FrameLayout.LayoutParams(-1, dp(58), Gravity.BOTTOM))
        setContentView(root)
        clockHandler.removeCallbacks(clockTick)
        clockHandler.post(clockTick)
    }

    private fun shortcut(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        textSize = 14f
        isAllCaps = false
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(dp(104), dp(78)).apply {
            rightMargin = dp(10)
        }
    }

    private fun showStartMenu() {
        val choices = arrayOf("Browser", "Installed apps", "Settings", "Lock screen")
        AlertDialog.Builder(this)
            .setTitle("AstraOS Start")
            .setItems(choices) { _, which ->
                when (which) {
                    0 -> openBrowserWindow()
                    1 -> showAppsWindow()
                    2 -> showSettingsWindow()
                    3 -> showLockScreen()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showSettingsWindow() {
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(14))
        }
        body.addView(TextView(this).apply {
            text = "AstraOS Settings"
            textSize = 21f
            setTextColor(Color.WHITE)
        })
        body.addView(TextView(this).apply {
            text = "Version: 0.1.0\nPlatform: Android 11+\nTarget: ARM 32-bit (armeabi-v7a)\nInput: Keyboard + mouse\n\nInstalled Android apps open through Android. Whether they can run inside floating windows depends on your device's Android windowing support."
            textSize = 15f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(12), 0, dp(12))
        })
        body.addView(Button(this).apply {
            text = "Open Android system settings"
            isAllCaps = false
            setOnClickListener {
                try {
                    startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                } catch (_: Exception) {
                    Toast.makeText(this@MainActivity, "Settings are not available", Toast.LENGTH_SHORT).show()
                }
            }
        })
        createWindow("Settings", body)
    }

    private fun showAppsWindow() {
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setBackgroundColor(Color.rgb(31, 42, 59))
        }
        val pm = packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(launcherIntent, 0)
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        if (apps.isEmpty()) {
            list.addView(TextView(this).apply {
                text = "No launchable apps found."
                setTextColor(Color.WHITE)
                setPadding(dp(12), dp(16), dp(12), dp(16))
            })
        } else {
            apps.forEach { info ->
                val appName = info.loadLabel(pm).toString()
                list.addView(Button(this).apply {
                    text = appName
                    isAllCaps = false
                    gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    setOnClickListener {
                        val launch = pm.getLaunchIntentForPackage(info.activityInfo.packageName)
                        if (launch != null) {
                            try {
                                startActivity(launch)
                            } catch (_: Exception) {
                                Toast.makeText(this@MainActivity, "Could not open $appName", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }, LinearLayout.LayoutParams(-1, dp(48)).apply {
                    bottomMargin = dp(4)
                })
            }
        }
        scroll.addView(list)
        createWindow("Installed apps", scroll, 520, 390)
    }

    private fun openBrowserWindow() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
            setBackgroundColor(Color.rgb(37, 48, 66))
        }
        val address = EditText(this).apply {
            isSingleLine = true
            textSize = 14f
            hint = "Search or enter website"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(dp(8), 0, dp(8), 0)
            setBackgroundColor(Color.rgb(57, 70, 91))
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
            override fun onPageFinished(view: WebView, url: String) {
                address.setText(url)
            }
        }
        web.webChromeClient = WebChromeClient()

        fun navigate() {
            val value = address.text.toString().trim()
            if (value.isEmpty()) return
            val url = when {
                value.contains("://") -> value
                value.contains(".") && !value.contains(" ") -> "https://$value"
                else -> "https://www.google.com/search?q=" + java.net.URLEncoder.encode(value, "UTF-8")
            }
            web.loadUrl(url)
        }

        toolbar.addView(Button(this).apply {
            text = "←"
            setOnClickListener { if (web.canGoBack()) web.goBack() }
        })
        toolbar.addView(address, LinearLayout.LayoutParams(0, dp(42), 1f))
        toolbar.addView(Button(this).apply {
            text = "Go"
            isAllCaps = false
            setOnClickListener { navigate() }
        })
        address.setOnEditorActionListener { _, _, _ -> navigate(); true }
        content.addView(toolbar)
        content.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))
        createWindow("Browser", content, 620, 420)
        web.loadUrl("https://www.google.com")
    }

    private fun createWindow(title: String, body: View, widthDp: Int = 470, heightDp: Int = 340) {
        windowCount++
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.rgb(31, 43, 62))
                cornerRadius = dp(9).toFloat()
                setStroke(dp(1), Color.rgb(91, 119, 158))
            }
            clipToOutline = true
            elevation = dp(14).toFloat()
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(9), 0, dp(4), 0)
            setBackgroundColor(Color.rgb(19, 30, 48))
        }
        header.addView(TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
        }, LinearLayout.LayoutParams(0, dp(42), 1f))

        val taskButton = Button(this).apply {
            text = title
            textSize = 11f
            isAllCaps = false
            setOnClickListener {
                val record = desktopWindows.firstOrNull { it.card === card } ?: return@setOnClickListener
                if (record.minimized) restoreWindow(record)
                else {
                    record.minimized = true
                    card.visibility = View.GONE
                }
            }
        }
        header.addView(windowControl("—") {
            val record = desktopWindows.firstOrNull { it.card === card } ?: return@windowControl
            record.minimized = true
            card.visibility = View.GONE
        })
        header.addView(windowControl("□") {
            val record = desktopWindows.firstOrNull { it.card === card } ?: return@windowControl
            toggleMaximize(record)
        })
        header.addView(windowControl("×") {
            windows.removeView(card)
            desktopWindows.removeAll { it.card === card }
            taskbar.removeView(taskButton)
        })

        card.addView(header)
        card.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))
        val params = FrameLayout.LayoutParams(dp(widthDp), dp(heightDp), Gravity.TOP or Gravity.START)
        params.leftMargin = dp(20 + (windowCount * 22) % 130)
        params.topMargin = dp(18 + (windowCount * 18) % 90)
        windows.addView(card, params)
        val record = DesktopWindow(title, card, taskButton)
        desktopWindows.add(record)
        taskbar.addView(taskButton, taskbar.childCount - 1)
        card.setOnClickListener { card.bringToFront() }
        header.setOnTouchListener(object : View.OnTouchListener {
            private var downX = 0f
            private var downY = 0f
            private var startLeft = 0
            private var startTop = 0
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                val lp = card.layoutParams as FrameLayout.LayoutParams
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX
                        downY = event.rawY
                        startLeft = lp.leftMargin
                        startTop = lp.topMargin
                        card.bringToFront()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!record.maximized) {
                            lp.leftMargin = (startLeft + event.rawX - downX).toInt().coerceAtLeast(0)
                            lp.topMargin = (startTop + event.rawY - downY).toInt().coerceAtLeast(0)
                            card.layoutParams = lp
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> return true
                }
                return false
            }
        })
        card.bringToFront()
    }

    private fun windowControl(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        textSize = 14f
        minWidth = dp(36)
        minimumWidth = dp(36)
        setPadding(0, 0, 0, 0)
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun toggleMaximize(record: DesktopWindow) {
        val lp = record.card.layoutParams as FrameLayout.LayoutParams
        if (!record.maximized) {
            record.previousParams = FrameLayout.LayoutParams(lp)
            lp.width = -1
            lp.height = -1
            lp.leftMargin = 0
            lp.topMargin = 0
            lp.bottomMargin = 0
            lp.rightMargin = 0
            record.maximized = true
        } else {
            val old = record.previousParams
            if (old != null) {
                lp.width = old.width
                lp.height = old.height
                lp.leftMargin = old.leftMargin
                lp.topMargin = old.topMargin
                lp.bottomMargin = old.bottomMargin
                lp.rightMargin = old.rightMargin
            }
            record.maximized = false
        }
        record.card.layoutParams = lp
        record.card.bringToFront()
    }

    private fun restoreWindow(record: DesktopWindow) {
        record.minimized = false
        record.card.visibility = View.VISIBLE
        record.card.bringToFront()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
