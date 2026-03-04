# Rhythm 🌿

Rhythm is a minimalist, calming Android application designed to help you stay on top of your daily routines through simple, hourly reminders. Whether it's for medication, hydration, or mindfulness, Rhythm provides a serene experience to manage your day.

## ✨ Features

- **Hourly Reminders**: Easily set intervals by hour for consistent daily routines.
- **Dual Alert Types**:
    - **Notification Only**: A gentle system nudge for non-critical tasks.
    - **Full-Screen Alarm**: A persistent screen with sound and vibration for important reminders that pops up even when the device is locked.
- **Forest & Flora Theme**: A modern, calm color palette (Greens, Amber, and Coral) designed to reduce stress and focus on wellness.
- **Edge-to-Edge Design**: Fully supports modern Android displays with a immersive, full-screen UI.
- **Smart Dashboard**: Instantly see your next upcoming reminder and your overall daily schedule at a glance.
- **Snooze & Stop**: Built-in flexibility to snooze reminders for 10 minutes or dismiss them.

## 🛠️ Technical Stack

- **UI**: Jetpack Compose (Modern Declarative UI)
- **Architecture**: MVVM + Clean Architecture principles
- **Dependency Injection**: Hilt
- **Database**: Room (for local persistent storage)
- **Scheduling**: AlarmManager (for precise, battery-efficient background triggers)
- **Concurrency**: Kotlin Coroutines & Flow

## 📸 Screenshots

<img width="437" height="946" alt="image" src="https://github.com/user-attachments/assets/7083e0df-aa2c-4f59-bb51-092ee21cd45a" />


## 🚀 Getting Started

1. Clone the repository.
2. Open in Android Studio (Ladybug or newer).
3. Build and run on an Android device (API 29+).

## 📄 Permissions

The app requires the following permissions to function correctly:
- `SCHEDULE_EXACT_ALARM`: To trigger reminders at the exact time.
- `USE_FULL_SCREEN_INTENT`: To show the alarm screen over the lock screen.
- `VIBRATE`: For tactile feedback during alarms.
- `POST_NOTIFICATIONS`: To show system reminders (Android 13+).

---
*Stay in your flow with Rhythm.*
