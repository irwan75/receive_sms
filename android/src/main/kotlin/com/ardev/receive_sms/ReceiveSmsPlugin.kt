package com.ardev.receive_sms

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

import android.content.IntentFilter
import java.util.regex.Pattern
import android.app.Activity
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.oval.sms_receiver.SMSBroadcastReceiver
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import java.lang.Exception
import java.lang.ref.WeakReference

/** ReceiveSmsPlugin */
class ReceiveSmsPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    EventChannel.StreamHandler {
    //  class ReceiveSmsPlugin: FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    internal var activity: Activity? = null

    //    private var smsReceiver: SmsReceiver? = null
    private var eventSink: EventChannel.EventSink? = null
    private var broadcastReceiver: SMSBroadcastReceiverV2? = null
//    private var handler: Handler? = null

    //    private final PluginRegistry.ActivityResultListener activityResultListener = new PluginRegistry.ActivityResultListener() {
    //        @Override
    //        public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    //            if (requestCode == SmsAutoFillPlugin.PHONE_HINT_REQUEST && pendingHintResult != null) {
    //                if (resultCode == Activity.RESULT_OK && data != null) {
    ////                    Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
    ////                    final String phoneNumber = credential.getId();
    //                    pendingHintResult.success("");
    //                } else {
    //                    pendingHintResult.success(null);
    //                }
    //                return true;
    //            }
    //            return false;
    //        }
    //    };

    companion object {
        private val smsBroadcastReceiver by lazy { SMSBroadcastReceiver() }
        private val receiveChannel: String = "METHOD_SMS"
        private val eventChannel: String = "EVENT_SMS"
        private val receiveChannelAppSignature: String = "GET_APP_SIGNATURE"
        private val removeListener: String = "REMOVE_LISTENER"
        private val listenerOTPCode = "LISTENER_OTP_CODE"
        private val smsCode = "SMS_CODE"
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, receiveChannel)
        channel.setMethodCallHandler(this)
        val event = EventChannel(flutterPluginBinding.binaryMessenger, eventChannel)
        event.setStreamHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            receiveChannelAppSignature -> {
                val signatureHelper = AppSignatureHelper(activity!!.applicationContext)
                val appSignature: String = signatureHelper.appSignature
                result.success(appSignature)
            }
            removeListener -> {
                var resultStopListening = stopListening()
                when(resultStopListening){
                    true-> Log.d("stopListening","Stop Listening True")
                    false-> Log.d("stopListening","Stop Listening False")
                }
            }
            listenerOTPCode -> {
                //                final String smsCodeRegexPattern = call.argument("smsCodeRegexPattern");
                val client = SmsRetriever.getClient(activity!!)
                val task = client.startSmsRetriever()
                task.addOnSuccessListener {

//                        unregisterReceiver();// unregister existing receiver
//                        broadcastReceiver = new SmsBroadcastReceiver(new WeakReference<>(SmsAutoFillPlugin.this),
//                                smsCodeRegexPattern);

                    broadcastReceiver =
                        SMSBroadcastReceiverV2(WeakReference<ReceiveSmsPlugin>(this@ReceiveSmsPlugin))
                    activity!!.registerReceiver(
                        broadcastReceiver,
                        IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
                    )
                    result.success(null)
                }
                task.addOnFailureListener {
                    result.error(
                        "ERROR_START_SMS_RETRIEVER",
                        "Can't start sms retriever",
                        null
                    )
                }
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    fun setCode(code: String?) {
        channel.invokeMethod(smsCode, code)
    }

//    private fun registerReceiver() {
//        if (eventSink == null) {
//            eventSink!!.success("cancel")
//            return
//        }
//        if (activity == null) {
//            eventSink!!.success("activity null")
//        } else {
//            eventSink!!.success("activity not null")
//        }
//        smsReceiver = SmsReceiver()
//        smsReceiver!!.smsReceiverListener = object : SmsReceiver.SmsReceiverListener {
//            override fun onSuccess(message: String?) {
//                eventSink!!.success("masuk sms")
//            }
//        }
//        val intentFilter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
//        activity?.registerReceiver(smsReceiver, intentFilter)
//    }

    private fun getOtpMessage(message: String?): String {
        val otpPattern = Pattern.compile("(|^)\\d{6}")
        val matcher = otpPattern.matcher(message)
        if (matcher.find()) {
            return matcher.group(0)
        }
        return ""
    }

    private fun stopListening():Boolean {
        try {
            activity?.unregisterReceiver(smsBroadcastReceiver)
            return true
        } catch (e: Exception) {
            // Ignored
            return false
        }

    }

    private fun startListening() {
        val client = SmsRetriever.getClient(activity!!)
        val retriever = client.startSmsRetriever()
        retriever.addOnSuccessListener {
            val listener = object : SMSBroadcastReceiver.Listener {
                override fun onSMSReceived(message: String) {
                    stopListening()
                    eventSink?.success(getOtpMessage(message))
                }

                override fun onTimeout() {
                    stopListening()
                }
            }
            smsBroadcastReceiver.injectListener(listener)
            activity?.registerReceiver(
                smsBroadcastReceiver,
                IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            )
        }
        retriever.addOnFailureListener {
            stopListening()
        }
    }


//    private fun receiveMsg() {
//        eventSink!!.success("masuk receiver")
//        var br = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                eventSink!!.success("centerr")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    for (sms: SmsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
//                        eventSink!!.success("masuk broadcast")
//                    }
//                }
//            }
//
//        }
//        activity?.registerReceiver(br, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
//    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
    }

//  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
//    this.activity = binding.activity
//    eventSink!!.success("onAttached")
//  }
//
//  override fun onDetachedFromActivityForConfigChanges() {
//  }
//
//  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
//    onAttachedToActivity(binding)
//  }
//
//  override fun onDetachedFromActivity() {
//    unRegisterReceiver()
//    eventSink!!.success("onDetached")
//  }

    //UnregisterSensor

//    private fun unRegisterReceiver() {
//        if (eventSink == null) return
//        else if (smsReceiver != null) {
//            try {
//                activity!!.unregisterReceiver(smsReceiver)
//            } catch (ex: Exception) {
//                // silent catch to avoir crash if receiver is not registered
//            }
//            smsReceiver = null
//        }
//    }

//    private val runnable = Runnable {
//        sendNewRandomNumber()
//    }


//    fun sendNewRandomNumber() {
//        val randomNumber = Random().nextInt(9)
//        eventSink?.success("$randomNumber")
//        handler?.postDelayed(runnable, 1000)
//    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
//    handler = Handler()
//    handler?.post(runnable)
//    registerReceiver()
//    receiveMsg()
        startListening()
    }

    override fun onCancel(arguments: Any?) {
        stopListening()
//        unRegisterReceiver()
//    handler?.removeCallbacks(runnable)
        eventSink = null
    }
}

//class SmsReceiver : BroadcastReceiver() {
//
//    var smsReceiverListener: SmsReceiverListener? = null
//
//    override fun onReceive(context: Context?, intent: Intent?) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            for (sms: SmsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
//                smsReceiverListener?.onSuccess(sms.displayMessageBody)
//            }
//
//        }
//    }
//
//    interface SmsReceiverListener {
//        fun onSuccess(message: String?)
//    }
//}
