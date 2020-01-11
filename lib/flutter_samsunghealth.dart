import 'dart:async';

import 'package:flutter/services.dart';

class FlutterSamsunghealth {
  static const MethodChannel _channel = const MethodChannel('flutter_samsunghealth');

  static Future<bool> connect(data) async {
    final bool response = await _channel.invokeMethod('connect');
    return response;
  }

  static Future<List<dynamic>> getActivity(int startTime, int endTime) async {
    final List<dynamic> response = await _channel.invokeMethod('get_data', {
      'startTime': startTime,
      'endTime': endTime
    });
    return response;
  }
}
