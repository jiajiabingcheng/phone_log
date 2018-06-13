import 'package:fixnum/fixnum.dart';
import 'package:flutter/services.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import 'package:phone_log/phone_log.dart';

void main() {
  group('Phone log plugin', () {
    String invokedMethod;
    dynamic arguments;

    setUp(() {
      final MockPlatformChannel mockChannel = new MockPlatformChannel();

      when(mockChannel.invokeMethod(typed(any), any))
          .thenAnswer((Invocation invocation) {
        invokedMethod = invocation.positionalArguments[0];
        arguments = invocation.positionalArguments[1];
      });

      PhoneLog.setChannel(mockChannel);

    });

    test('fetch phone log', () async {
      await PhoneLog.getPhoneLogs(
          startDate: new Int64(123456789), duration: new Int64(12));
      expect(invokedMethod, 'getPhoneLogs');
      expect(arguments,
          {'startDate': '123456789', 'duration': '12'});
    });

    test('check permission', () async {
      await PhoneLog.checkPermission();
      expect(invokedMethod, 'checkPermission');
      expect(arguments, null);
    });

    test('request permission', () async {
      await PhoneLog.requestPermission();
      expect(invokedMethod, 'requestPermission');
      expect(arguments, null);
    });
  });
}

class MockPlatformChannel extends Mock implements MethodChannel {}
