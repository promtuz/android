import 'package:flutter/material.dart';
import 'package:promtuz/data/demo/demo_chats.dart';
import 'package:promtuz/ui/components/home_top_bar.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      extendBodyBehindAppBar: true,
      appBar: HomeTopBar("Promtuz"),
      body: ListView.builder(
        itemCount: dummyChats.length,
        padding: EdgeInsets.only(
          top: 8 + kToolbarHeight + MediaQuery.of(context).padding.top,
          left: 12,
          right: 12,
          bottom: 8 + MediaQuery.of(context).padding.bottom,
        ),
        itemBuilder: (ctx, index) {
          return Container(
            margin: EdgeInsets.only(top: 4),
            child: ClipRRect(
              borderRadius: groupedClipRadius(index, dummyChats.length),
              child: Material(
                color: Theme.of(context).colorScheme.surfaceContainerLow,
                child: InkResponse(
                onTap: () {},
                containedInkWell: true,
                highlightColor: Colors.transparent,
                hoverColor: Colors.transparent,
                splashColor: Colors.white.withAlpha(40),
                child: ListTile(title: Text(dummyChats[index].name))
              ),
              ),
            ),
          );
        },
      ),
    );
  }
}

BorderRadius groupedClipRadius(
  int index,
  int size, {
  double major = 32,
  double minor = 15,
}) {
  if (size == 1) return BorderRadius.circular(major);
  if (index == 0)
    return BorderRadius.only(
      topLeft: Radius.circular(major),
      topRight: Radius.circular(major),
      bottomLeft: Radius.circular(minor),
      bottomRight: Radius.circular(minor),
    );
  if (index == size - 1)
    return BorderRadius.only(
      topLeft: Radius.circular(minor),
      topRight: Radius.circular(minor),
      bottomLeft: Radius.circular(major),
      bottomRight: Radius.circular(major),
    );
  else
    return BorderRadius.circular(minor);
}
