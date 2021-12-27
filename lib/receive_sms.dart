import 'dart:async';

import 'package:flutter/services.dart';

class ReceiveSms {
  ReceiveSms._privateConstructor();

  static final ReceiveSms _instance = ReceiveSms._privateConstructor();

  static ReceiveSms get getInstance => _instance;

  static const String _receiveChannel = "METHOD_SMS";
  static const String _eventChannel = "EVENT_SMS";
  static const String _receiveChannelAppSignature = "GET_APP_SIGNATURE";
  static const String _removeListener = "REMOVE_LISTENER";
  static const String _listenerOTPCode = "LISTENER_OTP_CODE";
  static const String _smsCode = "SMS_CODE";

  static const MethodChannel _channel = MethodChannel(_receiveChannel);
  static const EventChannel _messageChannel = EventChannel(_eventChannel);

  final StreamController<String> _code = StreamController.broadcast();

  Future<void> _didReceive(MethodCall method) async {
    if (method.method == _smsCode) {
      _code.add(method.arguments);
    }
  }

  static Future<bool> get listenerRemove async {
    final bool? isRemoveListener = await _channel.invokeMethod(_removeListener);
    return isRemoveListener ?? false;
  }

  static Future<String> get listenerOtpCode async {
    final String? listener = await _channel.invokeMethod(_listenerOTPCode);
    return listener ?? "";
  }

  static Future<String?> get getAppSignature async {
    final String? version =
        await _channel.invokeMethod(_receiveChannelAppSignature);
    return version;
  }

  static String dataOtp(String value) => value;

  static Stream<String>? _magneticEvent;

  static Stream<String> get listenerDataOtp =>
      // if (_magneticEvent == null) {
      _messageChannel.receiveBroadcastStream().map((event) => dataOtp(event));
  // }
  // return _magneticEvent!;
  // }

}
