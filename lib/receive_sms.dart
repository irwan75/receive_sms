import 'dart:async';

import 'package:flutter/services.dart';

class ReceiveSms {

  static final ReceiveSms _instance = ReceiveSms._();

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
  
  ReceiveSms._() {
    _channel.setMethodCallHandler(_didReceive);
  }

  Future<void> _didReceive(MethodCall method) async {
    if (method.method == _smsCode) {
      _code.add(method.arguments);
    }
  }

  Stream<String> get code => _code.stream;

  Future<void> listenerRemove() async {
    await _channel.invokeMethod(_removeListener);
  }

  Future<void> listenerOtpCode() async {
    await _channel.invokeMethod(_listenerOTPCode);
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


mixin CodeAutoFill {
  final ReceiveSms _autoFill = ReceiveSms.getInstance;
  String? code;
  StreamSubscription? _subscription;

  void listenForCode() {
    _subscription = _autoFill.code.listen((code) {
      this.code = code;
      codeUpdated();
    });
    _autoFill.listenerOtpCode();
  }

  Future<void> cancel() async {
    return _subscription?.cancel();
  }

  Future<void> unregisterListener() {
    _subscription?.cancel();
    return _autoFill.listenerRemove();
  }

  void codeUpdated();
}