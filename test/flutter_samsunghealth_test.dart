import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_samsunghealth/flutter_samsunghealth.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_samsunghealth');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterSamsunghealth.platformVersion, '42');
  });
}
