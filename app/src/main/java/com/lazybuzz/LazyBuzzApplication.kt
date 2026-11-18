package com.lazybuzz

import android.app.Application
import com.lazybuzz.data.preferences.SettingsRepository

class LazyBuzzApplication : Application() {
    val settings by lazy { SettingsRepository(this) }
}
