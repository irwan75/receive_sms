package com.ardev.receive_sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.lang.ref.WeakReference

class SMSBroadcastReceiverV2(plugin: WeakReference<ReceiveSmsPlugin>) : BroadcastReceiver() {

    private var plugin: WeakReference<ReceiveSmsPlugin>

    init {
        this.plugin = plugin
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent!!.action) {
            if (plugin.get() == null) {
                return
            } else {
                plugin.get()!!.activity?.unregisterReceiver(this)
            }
            val extras = intent!!.extras
            val status: Status?
            if (extras != null) {
                status = extras[SmsRetriever.EXTRA_STATUS] as Status?
                if (status != null) {
                    if (status.statusCode == CommonStatusCodes.SUCCESS) {
                        Log.d("receiveSms","broadcast receiver")
                        // Get SMS message contents
                        val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?
                        //                            Pattern pattern = Pattern.compile(smsCodeRegexPattern);
                        plugin.get()!!.setCode(message)
                        //                            if (message != null) {
//                                Matcher matcher = pattern.matcher(message);
//
//                                if (matcher.find()) {
//                                    plugin.get().setCode(matcher.group(0));
//                                } else {
//                                    plugin.get().setCode(message);
//                                }
//                            }
                    }
                }
            }
        }
    }
}