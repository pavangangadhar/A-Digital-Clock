import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../models/clock_settings.dart';
import '../widgets/clock_display.dart';

/// BackgroundScreen
///
/// Educational Note (How Image Selection Works in Flutter):
/// - Using package `image_picker`:
///   final ImagePicker picker = ImagePicker();
///   final XFile? image = await picker.pickImage(source: ImageSource.gallery);
/// - On Android, this triggers the native ActivityResultContracts.PickVisualMedia / Intent.ACTION_GET_CONTENT.
/// - On iOS, it presents a UIImagePickerController or PHPickerViewController.
/// - The chosen image file path is saved into ClockSettings and stored in SharedPreferences.
class BackgroundScreen extends StatefulWidget {
  final DateTime currentTime;
  final ClockSettings settings;
  final Function(ClockSettings) onUpdateSettings;

  const BackgroundScreen({
    super.key,
    required this.currentTime,
    required this.settings,
    required this.onUpdateSettings,
  });

  @override
  State<BackgroundScreen> createState() => _BackgroundScreenState();
}

class _BackgroundScreenState extends State<BackgroundScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  static const List<Map<String, dynamic>> solidColors = [
    {'name': 'Obsidian', 'color': Color(0xFF0D1117)},
    {'name': 'Black', 'color': Color(0xFF000000)},
    {'name': 'Dark Charcoal', 'color': Color(0xFF1A1E24)},
    {'name': 'Slate Navy', 'color': Color(0xFF0F172A)},
    {'name': 'Midnight Indigo', 'color': Color(0xFF1E1B4B)},
    {'name': 'Forest Green', 'color': Color(0xFF062B21)},
    {'name': 'Deep Crimson', 'color': Color(0xFF2E081F)},
    {'name': 'Steel Gray', 'color': Color(0xFF374151)},
  ];

  static const List<Map<String, dynamic>> gradients = [
    {
      'name': 'Midnight Horizon',
      'colors': [Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF0A0E17)],
    },
    {
      'name': 'Neon Cyber',
      'colors': [Color(0xFF1F0038), Color(0xFF0C1033), Color(0xFF002233)],
    },
    {
      'name': 'Sunset Glow',
      'colors': [Color(0xFF2B0938), Color(0xFF5A1827), Color(0xFF1A0B10)],
    },
    {
      'name': 'Emerald Deep',
      'colors': [Color(0xFF061A14), Color(0xFF0D3B2E), Color(0xFF04120D)],
    },
    {
      'name': 'Royal Velvet',
      'colors': [Color(0xFF20072B), Color(0xFF38104A), Color(0xFF0E0414)],
    },
    {
      'name': 'Aurora Borealis',
      'colors': [Color(0xFF051C2C), Color(0xFF0B3C49), Color(0xFF031926)],
    },
  ];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(
      length: 3,
      vsync: this,
      initialIndex: widget.settings.bgType == 'gradient'
          ? 1
          : widget.settings.bgType == 'image'
              ? 2
              : 0,
    );
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _pickImage() async {
    final picker = ImagePicker();
    final pickedFile = await picker.pickImage(source: ImageSource.gallery);
    if (pickedFile != null) {
      widget.onUpdateSettings(
        widget.settings.copyWith(
          bgType: 'image',
          customImagePath: pickedFile.path,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0D1117),
      appBar: AppBar(
        title: const Text('Background', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: const Color(0xFF0D1117),
        elevation: 0,
        bottom: TabBar(
          controller: _tabController,
          indicatorColor: widget.settings.clockColor,
          tabs: const [
            Tab(text: 'Solid'),
            Tab(text: 'Gradients'),
            Tab(text: 'Photo'),
          ],
        ),
      ),
      body: Column(
        children: [
          // Live Preview Header Box
          Container(
            height: 160,
            margin: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(20),
              color: widget.settings.bgType == 'solid'
                  ? widget.settings.bgSolidColor
                  : null,
              gradient: widget.settings.bgType == 'gradient'
                  ? LinearGradient(
                      begin: Alignment.topCenter,
                      end: Alignment.bottomCenter,
                      colors: gradients[widget.settings.bgGradientIndex]['colors'],
                    )
                  : null,
              image: widget.settings.bgType == 'image' && widget.settings.customImagePath != null
                  ? DecorationImage(
                      image: FileImage(File(widget.settings.customImagePath!)),
                      fit: BoxFit.cover,
                    )
                  : null,
            ),
            child: Center(
              child: ClockDisplay(
                dateTime: widget.currentTime,
                settings: widget.settings,
                isFullScreen: false,
                previewScale: 0.72,
              ),
            ),
          ),

          // Tab content
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: [
                // 1. Solid Colors
                ListView(
                  padding: const EdgeInsets.all(16),
                  children: [
                    Wrap(
                      spacing: 14,
                      runSpacing: 14,
                      children: solidColors.map((item) {
                        final color = item['color'] as Color;
                        final isSelected = widget.settings.bgType == 'solid' &&
                            widget.settings.bgSolidColor.value == color.value;

                        return GestureDetector(
                          onTap: () {
                            widget.onUpdateSettings(
                              widget.settings.copyWith(
                                bgType: 'solid',
                                bgSolidColor: color,
                              ),
                            );
                          },
                          child: Container(
                            width: 56,
                            height: 56,
                            decoration: BoxDecoration(
                              color: color,
                              shape: BoxShape.circle,
                              border: Border.all(
                                color: isSelected ? widget.settings.clockColor : Colors.white24,
                                width: isSelected ? 3 : 1,
                              ),
                            ),
                            child: isSelected
                                ? Icon(Icons.check, color: widget.settings.clockColor, size: 24)
                                : null,
                          ),
                        );
                      }).toList(),
                    ),
                  ],
                ),

                // 2. Gradients
                ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: gradients.length,
                  itemBuilder: (context, index) {
                    final grad = gradients[index];
                    final isSelected = widget.settings.bgType == 'gradient' &&
                        widget.settings.bgGradientIndex == index;

                    return GestureDetector(
                      onTap: () {
                        widget.onUpdateSettings(
                          widget.settings.copyWith(
                            bgType: 'gradient',
                            bgGradientIndex: index,
                          ),
                        );
                      },
                      child: Container(
                        height: 64,
                        margin: const EdgeInsets.only(bottom: 12),
                        padding: const EdgeInsets.symmetric(horizontal: 16),
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(16),
                          gradient: LinearGradient(
                            colors: grad['colors'] as List<Color>,
                          ),
                          border: isSelected
                              ? Border.all(color: widget.settings.clockColor, width: 2)
                              : null,
                        ),
                        alignment: Alignment.centerLeft,
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              grad['name'] as String,
                              style: const TextStyle(
                                color: Colors.white,
                                fontWeight: FontWeight.bold,
                                fontSize: 14,
                              ),
                            ),
                            if (isSelected)
                              CircleAvatar(
                                radius: 12,
                                backgroundColor: widget.settings.clockColor,
                                child: const Icon(Icons.check, size: 16, color: Colors.black),
                              ),
                          ],
                        ),
                      ),
                    );
                  },
                ),

                // 3. Custom Image Picker
                ListView(
                  padding: const EdgeInsets.all(16),
                  children: [
                    Container(
                      padding: const EdgeInsets.all(18),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.05),
                        borderRadius: BorderRadius.circular(18),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'Gallery Image Background',
                            style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
                          ),
                          const SizedBox(height: 8),
                          const Text(
                            'Pick any wallpaper or photograph from your device gallery to display behind the clock. In Flutter, this uses the image_picker plugin.',
                            style: TextStyle(color: Colors.white70, fontSize: 13),
                          ),
                          const SizedBox(height: 16),
                          SizedBox(
                            width: double.infinity,
                            height: 48,
                            child: ElevatedButton.icon(
                              style: ElevatedButton.styleFrom(
                                backgroundColor: widget.settings.clockColor,
                                foregroundColor: Colors.black,
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(14),
                                ),
                              ),
                              onPressed: _pickImage,
                              icon: const Icon(Icons.add_photo_alternate),
                              label: const Text(
                                'Select Image from Gallery',
                                style: TextStyle(fontWeight: FontWeight.bold),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
