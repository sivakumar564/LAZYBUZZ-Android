package com.lazybuzz.navigation

/** All required screens. Destinations with a [milestone] remain placeholders until they ship. */
enum class Dest(val route: String, val title: String, val blurb: String = "", val milestone: String = "") {
    Splash("splash", "Splash"),
    Dashboard("dashboard", "LAZYBUZZ"),
    Onboarding("onboarding", "Welcome", "A quick tour of how touchless control works.", "Milestone 2"),
    Permissions("permissions", "Permissions", "Camera, accessibility and overlay access, explained before you grant anything.", "Milestone 2"),
    Preview("preview", "Live camera preview", "Preview the front camera. Frames stay on this device."),
    Studio("studio", "Gesture Studio", "Record, edit and delete your own gestures.", "Milestone 11"),
    Mapping("mapping", "Gesture mapping", "Choose which action each gesture triggers.", "Milestone 5"),
    Calibration("calibration", "Calibration", "Set the movement range that feels comfortable to you.", "Milestone 6"),
    Interaction("interaction", "Interaction mode", "Virtual cursor or direct gesture, and whether the cursor is visible.", "Milestone 7"),
    Floating("floating", "Floating controls", "Bubble settings: pause, switch mode, stop.", "Milestone 10"),
    Privacy("privacy", "Privacy", "What is processed on this device and what is never stored.", "Milestone 2"),
}
