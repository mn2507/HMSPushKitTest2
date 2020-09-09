package com.huawei.hmspushkittest

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.view.Window
import android.webkit.JavascriptInterface
import android.webkit.WebView
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
    private lateinit var dialog: String

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
        showAlertDialog(messages)
        wvResponse2.settings.javaScriptEnabled = true
        wvResponse2.addJavascriptInterface(JavaScriptInterface(), Android)
        wvResponse2.loadUrl(url)
    }

    fun showAlertDialog(message : String) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@WebViewActivity)
        alertDialog.setTitle("Message from App")
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton(
            "CLOSE"
        ) { _, _ -> }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
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