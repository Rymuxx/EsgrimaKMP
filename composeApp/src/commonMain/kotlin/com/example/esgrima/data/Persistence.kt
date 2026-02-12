package com.example.esgrima.data

expect object Persistence {
    fun save(key: String, value: String)
    fun load(key: String): String?
}
