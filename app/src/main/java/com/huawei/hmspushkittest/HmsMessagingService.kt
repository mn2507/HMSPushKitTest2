package com.huawei.hmspushkittest

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage

class HmsMessagingService : HmsMessageService() {
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Toast.makeText(this, "token$s", Toast.LENGTH_LONG).show()
        Log.d(PUSH_TAG, "Message token: $s")
        getSharedPreferences("_", Context.MODE_PRIVATE).edit()
            .putString("hms_token", s).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(
                PUSH_TAG,
                "Message data payload: " + remoteMessage.data
            )
            processCustomMessage(this, remoteMessage.data)
        }
        if (remoteMessage.notification != null) {
            Log.d(
                PUSH_TAG,
                "Message Notification Body: " + remoteMessage.notification
            )
        }
    }

    override fun onMessageSent(s: String) {
        super.onMessageSent(s)
        Toast.makeText(this, "onMessageSent:$s", Toast.LENGTH_LONG).show()
    }

    private fun processCustomMessage(
        hmsMessagingService: HmsMessagingService,
        data: String
    ) {
    }

    companion object {
        private const val PUSH_TAG = "hms_Message"
        fun getToken(context: Context): String? {
            Log.d("Tagtoken", "getToken()")
            return context.getSharedPreferences("_", Context.MODE_PRIVATE)
                .getString("hms_token", "empty")
        }
    }
}