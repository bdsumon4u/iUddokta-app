package com.iuddokta.app

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.iuddokta.app.ui.theme.IUddoktaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WebView.setWebContentsDebuggingEnabled(true)

        setContent {
            IUddoktaTheme {
                WebViewWithPullToRefresh("https://iuddokta.com")
            }
        }
    }
}

@Composable
fun WebViewWithPullToRefresh(url: String) {
    var isRefreshing by remember { mutableStateOf(false) }
    var webViewRef: WebView? = null

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            isRefreshing = true
            webViewRef?.reload()
        }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    loadUrl(url)
                }.also {
                    webViewRef = it
                }
            },
            update = { webView ->
                webView.setOnPageFinishedListener {
                    isRefreshing = false
                }
            }
        )
    }
}

fun WebView.setOnPageFinishedListener(onPageFinished: () -> Unit) {
    this.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            onPageFinished()
        }
    }
}
