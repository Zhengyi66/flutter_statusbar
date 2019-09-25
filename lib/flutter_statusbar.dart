import 'dart:async';

import 'package:flutter/services.dart';

class FlutterStatusbar {
  static const MethodChannel _channel =
      const MethodChannel('flutter_statusbar');

  static Future<int> get statusBarLightMode async {
    final int result = await _channel.invokeMethod('statusBarLightMode');
    return result;
  }
}
