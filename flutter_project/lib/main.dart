import 'dart:async';
import 'package:flutter/material.dart';
import 'models/clock_settings.dart';
import 'services/storage_service.dart';
import 'screens/home_screen.dart';

/// Entry point of the Custom Digital Clock Flutter Learning Project.
///
/// Educational Concepts Covered:
/// 1. Main entry function `main()` initializing Flutter bindings and loading saved preferences.
/// 2. Asynchronous local persistence using SharedPreferences via `StorageService`.
/// 3. Reactive state management: top-level state holder passing state and mutation callbacks.
/// 4. Live periodic Timer ticking every second.
/// 5. Full-screen wake lock and immersive system mode.
void main() async {
  // Ensure Flutter engine bindings are initialized before accessing asynchronous services
  WidgetsFlutterBinding.ensureInitialized();

  // Load user settings saved in previous sessions
  final initialSettings = await StorageService.loadSettings();

  runApp(DigitalClockApp(initialSettings: initialSettings));
}

class DigitalClockApp extends StatefulWidget {
  final ClockSettings initialSettings;

  const DigitalClockApp({super.key, required this.initialSettings});

  @override
  State<DigitalClockApp> createState() => _DigitalClockAppState();
}

class _DigitalClockAppState extends State<DigitalClockApp> {
  late ClockSettings _settings;
  late DateTime _currentTime;
  Timer? _ticker;

  @override
  void initState() {
    super.initState();
    _settings = widget.initialSettings;
    _currentTime = DateTime.now();

    // Start clock timer updating on the second
    _ticker = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (mounted) {
        setState(() {
          _currentTime = DateTime.now();
        });
      }
    });
  }

  @override
  void dispose() {
    _ticker?.cancel();
    super.dispose();
  }

  /// Updates settings in runtime state and persists them to local storage
  void _updateSettings(ClockSettings newSettings) {
    setState(() {
      _settings = newSettings;
    });
    StorageService.saveSettings(newSettings);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Digital Clock',
      debugShowCheckedModeBanner: false,
      theme: ThemeData.dark().copyWith(
        scaffoldBackgroundColor: const Color(0xFF0D1117),
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xFF0D1117),
          elevation: 0,
        ),
      ),
      home: HomeScreen(
        currentTime: _currentTime,
        settings: _settings,
        onUpdateSettings: _updateSettings,
      ),
    );
  }
}
