package com.huawei.hmspushkittest

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
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


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {

    lateinit var etUrl: TextView
    private val locationPermission = ACCESS_FINE_LOCATION
    private val LOCATION_PERMISSION_CODE = 100

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etUrl = findViewById(R.id.et_url)
        etUrl.text = getSavedUrl()
        val inst = HmsInstanceId.getInstance(this)
        btn_show_token.setOnClickListener { getToken(inst) }

        btn_load_android.setOnClickListener {
            checkPermission(
                locationPermission,
                LOCATION_PERMISSION_CODE
            )
            Log.d("Tag0", "entering checkPermission btn")
        }

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

        wv_response.settings.javaScriptEnabled = true
        wv_response.addJavascriptInterface(JavaScriptInterface(), JAVASCRIPT_OBJ)
        wv_response.loadUrl(etUrl.text.toString())

        btn_load_android.setOnClickListener {
            onSetBaseUrl(etUrl.text.toString())
            wv_response.loadUrl(etUrl.text.toString())
        }

        btn_send_android.setOnClickListener {
            wv_response.evaluateJavascript(
                "javascript: " +
                        "updateFromAndroid(\"" + et_message_android.text + "\")", null
            )
        }
    }

    public fun onSetBaseUrl(url: String) {

        val sharedPref = this?.getSharedPreferences("SHARED", Context.MODE_PRIVATE)

        val editor = sharedPref.edit()
        editor.putString("url", etUrl.text.toString())
        editor.apply()
    }

    fun getSavedUrl(): String {
        val sharedPreference = getSharedPreferences("SHARED", Context.MODE_PRIVATE)
        val baseURL: String = sharedPreference.getString("url", "")!!
        return baseURL
    }

    override fun onDestroy() {
        wv_response.removeJavascriptInterface(JAVASCRIPT_OBJ)
        super.onDestroy()
    }

    private fun injectJavaScriptFunction() {
        wv_response.loadUrl(
            "javascript: " +
                    "window.androidObj.displayMessageFromAndroid = function(message) { " +
                    JAVASCRIPT_OBJ + ".textFromWeb(message) }"
        )
    }

    private inner class JavaScriptInterface {
        @JavascriptInterface
        fun displayMessageFromWeb(fromWeb: String) {
            tv_response_android.text = fromWeb
        }
    }

    companion object {

        private val JAVASCRIPT_OBJ = "javascript_obj"
        private val BASE_URL = "https://www.google.com/"
        private const val REQUEST_LOCATION_PERMISSION = 1
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

//
//        if (ContextCompat.checkSelfPermission(
//                this@MainActivity,
//                ACCESS_FINE_LOCATION
//            ) !==
//            PackageManager.PERMISSION_GRANTED
//        ) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(
//                    this@MainActivity,
//                    ACCESS_FINE_LOCATION
//                )
//            ) {
//                ActivityCompat.requestPermissions(
//                    this@MainActivity,
//                    arrayOf(ACCESS_FINE_LOCATION), 1
//                )
//            } else {
//                ActivityCompat.requestPermissions(
//                    this@MainActivity,
//                    arrayOf(ACCESS_FINE_LOCATION), 1
//                )
//            }
//        }




//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        when (requestCode) {
//            1 -> {
//                if (grantResults.isNotEmpty() && grantResults[0] ==
//                    PackageManager.PERMISSION_GRANTED
//                ) {
//                    if ((ContextCompat.checkSelfPermission(
//                            this@MainActivity,
//                            ACCESS_FINE_LOCATION
//                        ) ===
//                                PackageManager.PERMISSION_GRANTED)
//                    ) {
//                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
//                }
//                return
//            }
//        }
//    }

    fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission)
            == PackageManager.PERMISSION_DENIED
        ) {

            // Requesting the permission
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(permission),
                requestCode
            )
        } else {
            Toast.makeText(
                this@MainActivity,
                "Permission already granted",
                Toast.LENGTH_SHORT
            )
                .show()
        }
        Log.d("Tag0", "entering checkPermission")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super
            .onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    this@MainActivity,
                    "Camera Permission Granted",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Camera Permission Denied",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
        Log.d("Tag0", "entering onRequestPermissionsResult")
    }

}

