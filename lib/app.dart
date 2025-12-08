import 'package:flutter/material.dart';
import 'package:promtuz/ui/theme.dart';

import "navigation/root.dart";

class Promtuz extends StatelessWidget {
  const Promtuz({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Promtuz',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(useMaterial3: true, colorScheme: primaryColors, splashFactory: InkSparkle.splashFactory),
      routes: appRoutes,
    );
  }
}
