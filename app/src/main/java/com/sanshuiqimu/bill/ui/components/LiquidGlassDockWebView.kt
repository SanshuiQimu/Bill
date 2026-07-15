package com.sanshuiqimu.bill.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * WebView 与 Android 之间的通信桥梁
 */
class DockBridge(
    private val onItemSelected: (Int) -> Unit
) {
    @JavascriptInterface
    fun onDockItemSelected(index: Int) {
        onItemSelected(index)
    }
}

/**
 * 液态玻璃 Dock - WebView 版本
 *
 * 原模原样加载 dock.html，通过 JavascriptInterface 与 Android 通信。
 * WebView 背景透明，悬浮在内容上方。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiquidGlassDockWebView(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 创建并记住 WebView
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            settings.mediaPlaybackRequiresUserGesture = false

            // 透明背景
            setBackgroundColor(Color.TRANSPARENT)
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

            webViewClient = WebViewClient()

            // 添加 JS 接口
            addJavascriptInterface(
                DockBridge(onItemSelected),
                "AndroidDock"
            )

            // 加载 dock.html
            loadUrl("file:///android_asset/dock.html")
        }
    }

    // 当 selectedIndex 从外部变化时，调用 JS 同步 dock 位置
    LaunchedEffect(selectedIndex) {
        webView.evaluateJavascript("window.setDockIndex($selectedIndex);", null)
    }

    // WebView 销毁时清理
    DisposableEffect(webView) {
        onDispose {
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}
