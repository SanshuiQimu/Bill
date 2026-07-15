package com.sanshuiqimu.bill.ui.components

import android.annotation.SuppressLint
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
 *
 * 注意：调用方必须通过 modifier 指定宽高（如 fillMaxWidth + height），
 * 否则 WebView 尺寸为 0 不会渲染。
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
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            // 透明背景
            setBackgroundColor(Color.TRANSPARENT)

            // 禁用滚动条
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            webViewClient = WebViewClient()

            // 加载 dock.html
            loadUrl("file:///android_asset/dock.html")
        }
    }

    // 每次回调变化时重新绑定 JavascriptInterface
    DisposableEffect(onItemSelected) {
        webView.removeJavascriptInterface("AndroidDock")
        webView.addJavascriptInterface(
            DockBridge(onItemSelected),
            "AndroidDock"
        )
        onDispose { }
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
