import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';

enum HomeMoreMenu { profile, settings }

class HomeTopBar extends StatelessWidget implements PreferredSizeWidget {
  final String title;

  const HomeTopBar(this.title, {super.key});

  @override
  Widget build(BuildContext context) {
    return AppBar(
      backgroundColor: Colors.transparent,
      surfaceTintColor: Colors.transparent,
      elevation: 0,
      scrolledUnderElevation: 0,
      titleSpacing: 0,
      flexibleSpace: ClipRect(
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 16, sigmaY: 16),
          child: Container(
            color: Theme.of(context).colorScheme.surface.withAlpha(0x15),
          ),
        ),
      ),

      leading: Container(
        margin: EdgeInsets.only(left: 12, right: 12),
        width: 32.0,
        child: SvgPicture.asset(
          "assets/app.svg",
          semanticsLabel: "Promtuz Logo",
        ),
      ),
      title: Text(
        title,
        style: TextStyle(fontSize: 26, fontFamily: "Cal Sans"),
      ),
      actions: [
        PopupMenuButton<HomeMoreMenu>(
          itemBuilder: (ctx) => [
            const PopupMenuItem(
              value: HomeMoreMenu.profile,
              child: Text("Profile"),
            ),
            const PopupMenuItem(
              value: HomeMoreMenu.settings,
              child: Text("Settings"),
            ),
          ],
        ),
      ],
    );
  }

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);
}
