package com.sanshuiqimu.bill.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
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
 * JS 回调桥梁
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
 * 液态玻璃 Dock - WebView 实现
 *
 * 加载 assets/dock.html，保留完整的液态玻璃视觉效果和弹簧动画交互。
 * WebView 背景透明，悬浮在原生内容上方。
 *
 * 调用方必须通过 modifier 指定宽度和高度。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiquidGlassDockWebView(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 用数组包装回调，确保 remember 不捕获过时引用
    val callbackRef = remember { arrayOf<((Int) -> Unit)?>(null) }
    callbackRef[0] = onItemSelected

    val webView = remember {
        WebView(context).apply {
            // 基础设置
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            // 透明背景 - 关键！
            setBackgroundColor(Color.TRANSPARENT)

            // 禁用滚动
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            // 确保布局参数正确
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // 页面加载完成后通知 Android
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // 页面就绪后同步当前选中项
                    view?.evaluateJavascript("window.setDockIndex($selectedIndex);", null)
                }
            }

            // 绑定 JS 接口
            addJavascriptInterface(
                DockBridge { index -> callbackRef[0]?.invoke(index) },
                "AndroidDock"
            )

            // 加载 dock
            loadUrl("file:///android_asset/dock.html")
        }
    }

    // 外部 selectedIndex 变化时同步到 HTML
    LaunchedEffect(selectedIndex) {
        webView.evaluateJavascript("window.setDockIndex($selectedIndex);", null)
    }

    // 销毁清理
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
