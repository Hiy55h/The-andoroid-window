package com.hiy55h.astraos

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class BrowserActivity : Activity() {
    private lateinit var tabsBar: LinearLayout
    private lateinit var address: EditText
    private lateinit var pageHost: LinearLayout
    private val tabs = mutableListOf<WebView>()
    private var active = 0
    private val scripts = mutableListOf<String>()
    private val prefs by lazy { getSharedPreferences("astra_extensions", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        scripts.addAll(prefs.getStringSet("scripts", emptySet()) ?: emptySet())
        buildBrowser()
        addTab(intent?.dataString ?: "https://www.google.com")
    }

    private fun buildBrowser() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(24, 27, 34))
        }
        tabsBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(6, 5, 6, 5)
            setBackgroundColor(Color.rgb(32, 36, 45))
        }
        root.addView(tabsBar, LinearLayout.LayoutParams(-1, 48))

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(5, 4, 5, 4)
            setBackgroundColor(Color.rgb(43, 47, 57))
        }
        fun button(label: String, action: () -> Unit) = Button(this).apply {
            text = label
            textSize = 12f
            minWidth = 42
            setPadding(4, 0, 4, 0)
            setOnClickListener { action() }
        }
        toolbar.addView(button("←") { current()?.goBack() })
        toolbar.addView(button("→") { current()?.goForward() })
        toolbar.addView(button("↻") { current()?.reload() })
        address = EditText(this).apply {
            singleLine = true
            textSize = 14f
            hint = "Search or enter address"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(12, 0, 12, 0)
            setBackgroundColor(Color.rgb(62, 67, 78))
            imeOptions = EditorInfo.IME_ACTION_GO
            setOnEditorActionListener { _, _, _ -> navigate(text.toString()); true }
        }
        toolbar.addView(address, LinearLayout.LayoutParams(0, 42, 1f))
        toolbar.addView(button("−") { current()?.zoomOut() })
        toolbar.addView(button("+") { current()?.zoomIn() })
        toolbar.addView(button("＋") { addTab("https://www.google.com") })
        toolbar.addView(button("Ext") { showExtensions() })
        root.addView(toolbar, LinearLayout.LayoutParams(-1, 52))

        pageHost = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }
        root.addView(pageHost, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun current(): WebView? = tabs.getOrNull(active)

    private fun addTab(url: String) {
        current()?.visibility = View.GONE
        val web = WebView(this)
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
            if (android.os.Build.VERSION.SDK_INT >= 26) safeBrowsingEnabled = true
        }
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (active < tabs.size && tabs[active] === view) address.setText(url)
                scripts.forEach { script -> view.evaluateJavascript(script, null) }
            }
        }
        web.webChromeClient = WebChromeClient()
        tabs.add(web)
        active = tabs.lastIndex
        pageHost.addView(web, ViewGroup.LayoutParams(-1, -1))
        refreshTabs()
        web.loadUrl(normalize(url))
    }

    private fun refreshTabs() {
        tabsBar.removeAllViews()
        tabs.forEachIndexed { index, _ ->
            val tab = TextView(this).apply {
                text = "  Tab ${index + 1}  ×  "
                textSize = 13f
                setTextColor(Color.WHITE)
                setPadding(8, 8, 8, 8)
                setBackgroundColor(if (index == active) Color.rgb(70, 82, 105) else Color.TRANSPARENT)
                setOnClickListener { switchTab(index) }
                setOnLongClickListener {
                    if (tabs.size > 1) closeTab(index)
                    true
                }
            }
            tabsBar.addView(tab)
        }
            tabsBar.addView(TextView(this).apply {
            text = "  ＋  "
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(12, 5, 12, 5)
            setOnClickListener { addTab("https://www.google.com") }
        })
    }

    private fun switchTab(index: Int) {
        if (index !in tabs.indices) return
        current()?.visibility = View.GONE
        active = index
        current()?.visibility = View.VISIBLE
        address.setText(current()?.url ?: "")
        refreshTabs()
    }

    private fun closeTab(index: Int) {
        val web = tabs.removeAt(index)
        pageHost.removeView(web)
        web.destroy()
        if (tabs.isEmpty()) { finish(); return }
        active = (index.coerceAtMost(tabs.lastIndex))
        tabs.forEachIndexed { i, view -> view.visibility = if (i == active) View.VISIBLE else View.GONE }
        address.setText(current()?.url ?: "")
        refreshTabs()
    }

    private fun navigate(raw: String) {
        val value = raw.trim()
        if (value.isEmpty()) return
        val url = if (value.contains("://")) value else if (value.contains(".") && !value.contains(" ")) "https://$value" else "https://www.google.com/search?q=" + java.net.URLEncoder.encode(value, "UTF-8")
        current()?.loadUrl(url)
    }

    private fun normalize(value: String): String = if (value.contains("://")) value else "https://$value"

    private fun showExtensions() {
        val options = arrayOf("Installed scripts: ${scripts.size}", "Add site script", "Clear all scripts", "About extensions")
        AlertDialog.Builder(this)
            .setTitle("AstraOS Extensions")
            .setItems(options) { _, which ->
                when (which) {
                    1 -> addExtension()
                    2 -> {
                        scripts.clear()
                        prefs.edit().remove("scripts").apply()
                        Toast.makeText(this, "Extensions cleared", Toast.LENGTH_SHORT).show()
                    }
                    3 -> AlertDialog.Builder(this).setTitle("AstraOS Extensions")
                        .setMessage("AstraOS currently supports simple user scripts injected into pages. It does not yet support Chrome Web Store extensions or the full Chrome extensions API.")
                        .setPositiveButton("OK", null).show()
                }
            }.setNegativeButton("Close", null).show()
    }

    private fun addExtension() {
        val input = EditText(this).apply {
            hint = "JavaScript to run on pages (advanced)"
            minLines = 4
            gravity = android.view.Gravity.TOP
        }
        AlertDialog.Builder(this)
            .setTitle("Add user script")
            .setMessage("Only add scripts you understand and trust.")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val code = input.text.toString().trim()
                if (code.isNotEmpty()) {
                    scripts.add(code)
                    prefs.edit().putStringSet("scripts", scripts.toSet()).apply()
                    current()?.evaluateJavascript(code, null)
                }
            }
            .setNegativeButton("Cancel", null).show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.dataString?.let { addTab(it) }
    }

    override fun onDestroy() {
        tabs.forEach { it.destroy() }
        tabs.clear()
        super.onDestroy()
    }
}
