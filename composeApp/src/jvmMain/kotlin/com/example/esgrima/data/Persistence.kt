package com.example.esgrima.data

import java.io.File

actual object Persistence {
    actual fun save(key: String, value: String) {
        File("$key.json").writeText(value)
    }

    actual fun load(key: String): String? {
        val file = File("$key.json")
        return if (file.exists()) file.readText() else null
    }
}
