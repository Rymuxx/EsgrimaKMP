package com.example.esgrima.data

import java.io.File

actual object Persistence {
    // Nota: En una app real de Android usaríamos SharedPreferences o DataStore, 
    // pero para mantener la paridad absoluta con el código original del profesor 
    // y que vea los archivos en la carpeta de ejecución, usamos File.
    // En Android esto se guarda en el directorio interno de la app.
    
    private var filesDir: File? = null
    
    fun init(dir: File) {
        filesDir = dir
    }

    actual fun save(key: String, value: String) {
        val file = if (filesDir != null) File(filesDir, "$key.json") else File("$key.json")
        file.writeText(value)
    }

    actual fun load(key: String): String? {
        val file = if (filesDir != null) File(filesDir, "$key.json") else File("$key.json")
        return if (file.exists()) file.readText() else null
    }
}
