import 'package:flutter/material.dart';

/// Reusable settings card tile.
///
/// Educational Note:
/// - Building small, reusable widgets with consistent padding and theme styles
///   prevents code duplication and keeps your screens readable.
class SettingCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry padding;

  const SettingCard({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(16.0),
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Colors.white.withOpacity(0.05),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16.0),
      ),
      elevation: 0,
      child: Padding(
        padding: padding,
        child: child,
      ),
    );
  }
}
