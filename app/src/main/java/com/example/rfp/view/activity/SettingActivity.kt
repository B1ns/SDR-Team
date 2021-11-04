package com.example.rfp.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebSettings
import com.example.rfp.R
import kotlinx.android.synthetic.main.activity_setting.*
import android.webkit.WebView

import android.webkit.WebViewClient


class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        introduce_webview.settings.apply {
            loadWithOverviewMode = true
            useWideViewPort = true
            javaScriptEnabled = true
            setSupportZoom(true)
            setSupportMultipleWindows(true)
        }
        introduce_webview.apply {
            loadUrl("http://radiofrequency.dothome.co.kr/")
        }

        introduce_webview.webViewClient = WebClient()

    }

    internal class WebClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }
}


