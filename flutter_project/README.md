# Custom Full-Screen Digital Clock App — Educational Learning Guide

Welcome to the **Custom Full-Screen Digital Clock** project! This project was built to clearly explain key mobile development concepts: UI/UX, state management, local storage, timers, full-screen mode, and hardware awake flags.

---

## 📚 1. Architecture & Technology

This repository contains:
1. **Fully Running Android Native App (Kotlin + Jetpack Compose + Room Database)** in the root directory. Ready to run, preview, and build directly to APK!
2. **Complete Flutter Project (Dart + SharedPreferences + Wakelock)** in the `/flutter_project/` folder following standard Flutter conventions.

Both implementations share identical architecture patterns:
- **Unidirectional Data Flow (UDF)**: UI states flow down, and events/callbacks flow up.
- **Single Source of Truth**: User preferences are kept in persistent storage and mirrored in reactive memory state.

---

## ⏱️ 2. Live Time & Timers

### How It Works
- **Ticker Loop**: A background coroutine (`viewModelScope.launch`) or Dart `Timer.periodic` fires once per second.
- **Sub-Second Precision**: To prevent the seconds digit from flipping slightly off-beat, the ticker calculates the exact milliseconds remaining until the start of the next whole second:
  ```kotlin
  val millisUntilNextSecond = 1000L - (System.currentTimeMillis() % 1000L)
  delay(millisUntilNextSecond)
  ```
- **Lifecycle Safety**: When the user navigates away or closes the app, the coroutine job / timer is automatically cancelled to prevent memory leaks and battery drain.

---

## 🔤 3. 10 Clock Font Styles

The app provides 10 distinct typographic styles:
1. **Digital** (Monospace Courier bold with wide tracking)
2. **Minimal** (Ultra-clean sans-serif light weight)
3. **Thin** (Delicate hairline stroke with subtle spacing)
4. **Bold** (Heavy high-contrast display typeface)
5. **Futuristic** (Wide-tracked modern tech style)
6. **Seven Segment** (Custom Canvas-drawn physical LED segment display with inactive segment ghosting)
7. **Rounded** (Friendly curved terminal sans-serif)
8. **Retro** (Classic terminal dot-matrix monospace)
9. **Monospace** (Crisp uniform-width coding font)
10. **Serif** (Refined editorial Roman style)

### How Fonts are Loaded & Applied
- **Jetpack Compose**: Composed via `FontFamily`, `FontWeight`, `letterSpacing`, and custom `Canvas` rendering.
- **Flutter**: Loaded either from local assets declared in `pubspec.yaml`, standard system font families, or on-demand via `google_fonts`.

---

## 🎨 4. Clock Color & HSV Model

- **Preset Swatches**: 12 curated high-contrast colors (White, Dark Charcoal, Red, Blue, Green, Yellow, Purple, Orange, Cyan, Neon Green, Hot Pink, Amber).
- **Custom Color Picker (HSV)**:
  - **Hue (0° - 360°)**: Traverses the color wheel spectrum.
  - **Saturation (0% - 100%)**: Controls color purity/intensity.
  - **Value / Brightness (20% - 100%)**: Adjusts luminance.
  - Using HSV is far more natural for users than manually tweaking three separate Red, Green, and Blue sliders.

---

## 🖼️ 5. Backgrounds & Privacy-First Image Selection

1. **Solid Backgrounds**: Deep obsidian, slate, charcoal, midnight blue, forest green, etc.
2. **Gradient Backgrounds**: Multi-stop linear gradients (Midnight Horizon, Neon Cyber, Sunset Glow, Emerald Deep, Royal Velvet, Aurora Borealis).
3. **Gallery Image Selection**:
   - **Zero-Permission Privacy**: Uses modern system Photo Picker (`ActivityResultContracts.PickVisualMedia` in Android, `image_picker` in Flutter).
   - The app does **not** request broad `READ_EXTERNAL_STORAGE` permissions. Instead, the OS presents its own secure photo picker and grants read-only access to only the selected image via a scoped URI.

---

## 📱 6 & 7. Full-Screen Mode & Keeping the Screen Awake

### Keeping Screen Awake (Learning Objective)
- **Native Android**:
  ```kotlin
  // When entering full-screen:
  activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

  // When exiting full-screen:
  activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
  ```
  `FLAG_KEEP_SCREEN_ON` requires **zero dangerous permissions** and is automatically suspended by the Android window manager whenever the user minimizes or switches apps.
- **Flutter**:
  ```dart
  // On enter:
  WakelockPlus.enable();

  // On exit:
  WakelockPlus.disable();
  ```

### Immersive System Bars
- Navigation and status bars are hidden using `WindowInsetsControllerCompat` (Android) or `SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky)` (Flutter).
- **Exit Gesture**: Double-tap anywhere on the display or tap the subtle corner 'X' button to return to the controls.

---

## ⚙️ 8. Time Settings & Geometry

- **12-Hour vs 24-Hour**: Formatted via `DateTimeFormatter` (`hh:mm a` vs `HH:mm`).
- **Show Seconds Toggle**: Adds or omits the `:ss` segment dynamically.
- **Show Date Toggle**: Toggles full weekday, month, day, and year display.
- **Clock Size**: Small, Medium, Large, Extra Large (dynamically scaled font sizes).
- **Clock Position**: Top, Center, Bottom alignment within the display container.

---

## 💾 9. Local Storage Persistence

- **Android Native**: **Room Database** (SQLite abstraction) stores settings in a `ClockSettings` table. Changes trigger reactive `Flow` updates.
- **Flutter**: **SharedPreferences** key-value storage saves all attributes asynchronously.

---

## 📁 10. Flutter Project Structure

```
flutter_project/
├── pubspec.yaml
├── README.md
└── lib/
    ├── main.dart
    ├── models/
    │   └── clock_settings.dart
    ├── services/
    │   └── storage_service.dart
    ├── widgets/
    │   ├── clock_display.dart
    │   └── setting_tile.dart
    └── screens/
        ├── home_screen.dart
        ├── font_screen.dart
        ├── color_screen.dart
        ├── background_screen.dart
        ├── settings_screen.dart
        └── fullscreen_screen.dart
```

### Running the Flutter Project
```bash
cd flutter_project
flutter pub get
flutter run
```

### Building the Android APK
The compiled release/debug APK has been generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```
