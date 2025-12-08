import 'package:flutter/material.dart';
import 'package:promtuz/screens/home.dart';

final appRoutes = <String, WidgetBuilder>{
  "/": (context) => const HomeScreen(),
  "/manageSpace": (context) {
    return Scaffold(appBar: AppBar(title: const Text('Manage Space')));
  },
};
