📂 Android Accessibility-Based File Navigator

This project is an Android application that automatically navigates through a predefined folder structure and selects a target file — entirely without manual user interaction.

Using the Android Accessibility API, the app simulates user navigation by:

Opening a file picker or file manager interface

Traversing directories based on a given folder hierarchy

Scrolling when necessary

Detecting UI elements dynamically

Selecting the final file automatically

🚀 Purpose

The goal of this project is to demonstrate how the Accessibility framework can be leveraged to automate UI-driven file selection workflows in Android environments where direct file access may not be possible.

🛠️ Technologies Used

Java / Kotlin

Android SDK

AccessibilityService

UI node tree inspection (AccessibilityNodeInfo)

Coroutine / background task handling (if applicable)

⚙️ How It Works

The app receives a folder path structure (e.g., FolderA → SubfolderB → TargetFile.pdf)

It monitors the active window.

It searches for matching folder names in the UI hierarchy.

It performs scroll-and-click operations until the item is found.

It proceeds recursively until the final file is selected.

⚠️ Important Notes

Requires Accessibility permission to function.

Designed for controlled environments and automation use cases.

Behavior may vary depending on the target file manager UI implementation.