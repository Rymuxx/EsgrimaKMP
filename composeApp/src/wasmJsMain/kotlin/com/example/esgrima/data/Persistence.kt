package com.example.esgrima.data

import kotlinx.browser.window

actual object Persistence {
    actual fun save(key: String, value: String) {
        window.localStorage.setItem(key, value)
    }

    actual fun load(key: String): String? {
        return window.localStorage.getItem(key)
    }
}
