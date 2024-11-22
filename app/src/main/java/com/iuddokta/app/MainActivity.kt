package com.iuddokta.app

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.iuddokta.app.ui.theme.IUddoktaTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.webkit.WebChromeClient
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    private var webView: WebView? = null
    private val showExitDialog = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IUddoktaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WebViewWithPullToRefresh(
                        url = "https://iuddokta.com",
                        onWebViewCreated = { webViewInstance ->
                            webView = webViewInstance
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
            if (showExitDialog.value) {
                ExitConfirmationDialog(
                    onConfirm = {
                        // User confirmed, exit the app
                        super.onBackPressed()
                    },
                    onDismiss = {
                        // User dismissed, just close the dialog
                        showExitDialog.value = false
                    }
                )
            }
        }
    }

    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            // Show confirmation dialog before closing
            showExitDialog.value = true
        }
    }
}

@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Exit App?")
        },
        text = {
            Text("Are you sure you want to exit the app?")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Yes")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}

@Composable
fun WebViewWithPullToRefresh(
    url: String,
    onWebViewCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    AndroidView(
        factory = { ctx ->
            SwipeRefreshLayout(ctx).apply {
                val webView = WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            isRefreshing = false
                        }
                    }
                    webChromeClient = WebChromeClient()
                    loadUrl(url)
                }
                addView(webView)
                onWebViewCreated(webView) // Provide the WebView instance

                setOnRefreshListener {
                    isRefreshing = true
                    webView.reload()
                }
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { swipeRefreshLayout ->
            swipeRefreshLayout.isRefreshing = isRefreshing
        }
    )

    // Show Loading Indicator while the page is loading
    if (isLoading) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}