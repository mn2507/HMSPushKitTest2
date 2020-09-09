package com.huawei.hmspushkittest

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Window
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hmspushkittest.MainActivity.Companion.Android
import kotlinx.android.synthetic.main.activity_main.*

class WebViewActivity : AppCompatActivity() {

    private lateinit var wvResponse2: WebView
    private lateinit var url: String
    private lateinit var messages: String
    private lateinit var messageWeb: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        wvResponse2 = findViewById(R.id.wv_response3)


        if (intent != null && intent.extras != null) {
            val bundle = intent.extras
            if (bundle != null) {
                url = intent.extras!!["url"].toString()
                messages = intent.extras!!["message"].toString()
                messageWeb = intent.extras!!["messagefromWeb"].toString()
            }
        }

        wvResponse2.settings.javaScriptEnabled = true
        wvResponse2.addJavascriptInterface(JavaScriptInterface(), Android)
        wvResponse2.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (url == BASE_URL) {
                    injectJavaScriptFunction()
                }
            }
        }
        wvResponse2.loadUrl(url)
    }

    fun showAlertDialog(message: String) {

        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@WebViewActivity)
        alertDialog.setTitle("Message from WebView")
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton(
            "CLOSE"
        ) { _, _ -> }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    override fun onDestroy() {
        wvResponse2.removeJavascriptInterface(Android)
        super.onDestroy()
    }

    private fun injectJavaScriptFunction() {

        wvResponse2.loadUrl(
            "javascript: " +
                    "window.androidObj.displayMessageFromAndroid = function(message) { " +
                    Android + ".textFromWeb(message) }"
        )
    }

    private inner class JavaScriptInterface {
        @JavascriptInterface
        fun textFromWeb(fromWeb: String) {
            showAlertDialog(fromWeb)
        }
    }


    companion object {
        private const val Android = "Android"
        private const val BASE_URL = "https://d1iklor05b0e96.cloudfront.net/LocatedMap/index.html"
    }
}