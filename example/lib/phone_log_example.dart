import 'package:flutter/material.dart';
import 'package:phone_log/phone_log.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Iterable<CallRecord> _callRecords;

  @override
  initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  initPlatformState() async {
    var callLogs = await PhoneLog.getPhoneLogs();
    setState(() {_callRecords = callLogs;});
  }

  @override
  Widget build(BuildContext context) {
    var children = <Widget>[
      new RaisedButton(onPressed: checkPermission,
          child: new Text("Check permission")),
      new RaisedButton(onPressed: requestPermission,
          child: new Text("Request permission")),
    ];
    for (CallRecord call in _callRecords ?? []){
      children.addAll([new Container(height: 16.0,),
      new Row(children: <Widget>[new Text(call.formattedNumber ?? call.number ?? 'unknow'),
      new Padding(child: new Text(call.callType), padding: const EdgeInsets.only(left: 8.0),),],
        crossAxisAlignment: CrossAxisAlignment.center,),
      new Row(children: <Widget>[
      new Padding(child: new Text(call.dateYear.toString() +
          '-' + call.dateMonth.toString() +
          '-' + call.dateDay.toString() +
          '  ' + call.dateHour.toString() +
      ': ' + call.dateMinute.toString() +
      ': ' + call.dateSecond.toString()), padding: const EdgeInsets.only(left: 8.0),),
      new Padding(child: new Text(call.duration.toString() + 'seconds'), padding: const EdgeInsets.only(left: 8.0))],
        crossAxisAlignment: CrossAxisAlignment.center,)]);
    }

    return new MaterialApp(
      home:new Scaffold(
        appBar: new AppBar(title: new Text('PhoneLog plugin example')),
        body: new Center(
          child: new Column(children: children),
        ),
      ),
    );
  }
}

requestPermission() async {
  bool res = await PhoneLog.requestPermission();
  print("permission request result is " + res.toString());
}

checkPermission() async {
  bool res = await PhoneLog.checkPermission();
  print("permission is " + res.toString());
}