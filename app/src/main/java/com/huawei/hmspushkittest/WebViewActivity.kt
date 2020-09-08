package com.huawei.hmspushkittest

import android.os.Bundle
import android.os.Parcelable
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hmspushkittest.MainActivity.Companion.Android
import kotlinx.android.synthetic.main.activity_main.*

class WebViewActivity : AppCompatActivity() {

    private lateinit var wvResponse2: WebView
    private lateinit var url: String
    private lateinit var messages: String

    private inner class JavaScriptInterface {
        @JavascriptInterface
        fun displayMessageFromWeb(fromWeb: String) {
            tv_response_android.text = fromWeb
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        wvResponse2 = findViewById(R.id.wv_response3)


        if (intent != null && intent.extras != null) {
            val bundle = intent.extras
            if (bundle != null) {
                url = intent.extras!!["url"].toString()
                messages = intent.extras!!["message"].toString()
            }
        }
        wvResponse2.settings.javaScriptEnabled = true
        wvResponse2.addJavascriptInterface(JavaScriptInterface(), Android)
        wvResponse2.loadUrl(url)
    }

    override fun onDestroy() {
        wvResponse2.removeJavascriptInterface(MainActivity.Android)
        super.onDestroy()
    }

    private fun injectJavaScriptFunction() {
        wvResponse2.loadUrl(
            "javascript: " +
                    "window.androidObj.displayMessageFromAndroid = function(message) { " +
                    MainActivity.Android + ".textFromWeb(message) }"
        )
    }


}