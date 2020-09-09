package com.huawei.hmspushkittest

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.api.ConnectionResult
import com.huawei.hms.api.HuaweiApiAvailability
import com.huawei.hms.push.HmsMessaging
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_webview.*


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {


    lateinit var etUrl: TextView

    //   private lateinit var wv_response2: WebView
    private lateinit var tvMessage: EditText

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etUrl = findViewById(R.id.et_url)
        tvMessage = findViewById(R.id.et_message_android)
        //   wv_response2 =findViewById(R.id.wv_response)
        etUrl.text = getSavedUrl()
        val inst = HmsInstanceId.getInstance(this)
        btn_show_token.setOnClickListener { getToken(inst) }

//      Runtime Location
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                ACCESS_FINE_LOCATION
            ) !==
            PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(ACCESS_FINE_LOCATION), 1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(ACCESS_FINE_LOCATION), 1
                )
            }
        }

        //Webview scaling
        val webView = WebView(this)

        //textView copy-paste func
        tv_token.setOnLongClickListener {
            val text: String = tv_token.text.toString()
            val manager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", text)
            manager.setPrimaryClip(clipData)
            Toast.makeText(
                applicationContext,
                "Device Token copied",
                Toast.LENGTH_SHORT
            ).show()
            true
        }

        //     wv_response2.settings.javaScriptEnabled = true
        //    wv_response2.addJavascriptInterface(JavaScriptInterface(), Android)
        //     wv_response2.loadUrl(etUrl.text.toString())

        btn_load_android.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            if (et_url.text != null && tvMessage.text != null) {
                intent.putExtra("url", et_url.text.toString())
                intent.putExtra("message", tvMessage.text.toString())

                startActivity(intent)
            } else {
                Toast.makeText(this, "URL cannot be empty", Toast.LENGTH_LONG).show()
            }


            onSetBaseUrl(etUrl.text.toString())
            //     wv_response2.loadUrl(etUrl.text.toString())
        }

        //     btn_send_android.setOnClickListener {
        //         wv_response2.evaluateJavascript(
        //            "javascript: " +
        //                     "updateFromAndroid(\"" + et_message_android.text + "\")", null
        //         )
        //    }
    }

    public fun onSetBaseUrl(url: String) {

        val sharedPref = this?.getSharedPreferences("SHARED", Context.MODE_PRIVATE)
        val customUrl = etUrl.text.toString()
        val editor = sharedPref.edit()

        if (etUrl.length() == 0) {
            editor.putString("url", BASE_URL)
        } else {
            editor.putString("url", customUrl)
        }
        editor.apply()
    }

    fun getSavedUrl(): String {
        val sharedPreference = getSharedPreferences("SHARED", Context.MODE_PRIVATE)
        val baseURL: String = sharedPreference.getString("url", "")!!
        return baseURL
    }

//    override fun onDestroy() {
//        wv_response2.removeJavascriptInterface(Android)
//        super.onDestroy()
//    }

//    private fun injectJavaScriptFunction() {
//        wv_response2.loadUrl(
//            "javascript: " +
//                    "window.androidObj.displayMessageFromAndroid = function(message) { " +
//                    Android + ".textFromWeb(message) }"
//        )
//    }

    private inner class JavaScriptInterface {
        @JavascriptInterface
        fun displayMessageFromWeb(fromWeb: String) {
            tv_response_android.text = fromWeb
            intent.putExtra("messagefromWeb", et_url.text.toString())
        }
    }

    companion object {

        val Android = "javascript_obj"
        private val BASE_URL = "file:///android_asset/webview.html"
//        private val BASE_URL = "https://d1iklor05b0e96.cloudfront.net/LocatedMap/index.html"
    }

    private fun getToken(inst: HmsInstanceId) {
        object : Thread() {
            override fun run() {
                try {
                    val HMS_APPID =
                        AGConnectServicesConfig.fromContext(this@MainActivity)
                            .getString("client/app_id")
                    val token = inst.getToken(HMS_APPID, "HCM")
                    val msg = getString(R.string.hms_token, token)
//                    Log.d(MainActivity.TAG, msg)
                    runOnUiThread { tv_token.text = "" + token }
                    Log.d("token", token)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
        Log.d("Tag3", "entering getToken")
    }

    fun subscribe(topic: String?) {
        try {
            HmsMessaging.getInstance(this@MainActivity).subscribe(topic)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@MainActivity,
                            "subscribe topic success",
                            Toast.LENGTH_SHORT
                        ).show()
//                        Log.i(MainActivity.TAG, "subscribe Complete")
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "subscribe failed: ret = " + task.exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } catch (e: java.lang.Exception) {
//            Log.e(MainActivity.TAG, "subscribe failed: exception=" + e.message)
        }
        Log.d("Tag3", "entering subscribe")
    }

    fun unsubscribe(topic: String?) {
        try {
            HmsMessaging.getInstance(this@MainActivity).unsubscribe(topic)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@MainActivity,
                            "subscribe topic success",
                            Toast.LENGTH_SHORT
                        ).show()
//                        Log.i(MainActivity.TAG, "subscribe Complete")
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "subscribe failed: ret = " + task.exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } catch (e: java.lang.Exception) {
//            Log.e(MainActivity.TAG, "subscribe failed: exception=" + e.message)
        }
        Log.d("Tag4", "entering unsubscribe")
    }

    fun isHuaweiMobileServicesAvailable(context: Context?) {
        val hmsStatus = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context)
        if (hmsStatus == ConnectionResult.SUCCESS
        ) {
            tv_hms_status.visibility = View.VISIBLE
            tv_hms_status.text = "Huawei Mobile Services is unavailable"
            Toast.makeText(
                applicationContext,
                "Huawei Mobile Services is unavailable on this device. Push notifications are not supported",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("Tag0", "entering isHuaweiMobileServicesAvailable 1")
        } else {
            tv_hms_status.visibility = View.VISIBLE
            tv_hms_status.text = "Update required for Huawei Mobile Services"
            Toast.makeText(
                applicationContext,
                "Update required for Huawei Mobile Services",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("Tag0", "entering isHuaweiMobileServicesAvailable 2")
        }
        tv_hms_status.visibility = View.GONE
        Log.d("Tag0", "entering isHuaweiMobileServicesAvailable 3")
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            ACCESS_FINE_LOCATION
                        ) ===
                                PackageManager.PERMISSION_GRANTED)
                    ) {
                        Toast.makeText(this, "Location permission Granted", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Location permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}

