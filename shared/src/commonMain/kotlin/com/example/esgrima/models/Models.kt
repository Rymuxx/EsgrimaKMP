package com.example.esgrima.models

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    ADMIN, REFEREE, FENCER
}

@Serializable
data class User(
    val id: String,
    val username: String,
    val password: String,
    val role: Role,
    val linkedId: String? = null
)

@Serializable
enum class Arma {
    ESPADA, FLORETE, SABLE
}

@Serializable
data class Club(
    val nombre: String,
    val numeroFiliacion: String
)

@Serializable
data class Tirador(
    val id: String,
    val nombre: String,
    val club: Club,
    val numeroAfiliado: String,
    val especialidades: List<Arma> = emptyList()
)

@Serializable
data class Arbitro(
    val id: String,
    val nombre: String,
    val numeroAfiliado: String,
    val especialidades: List<Arma>
)

@Serializable
data class Asalto(
    val id: String,
    val arbitro: Arbitro,
    val tirador1: Tirador,
    val tirador2: Tirador,
    val tocados1: Int = 0,
    val tocados2: Int = 0,
    val tiempo: String = "03:00", // Tiempo del asalto
    val terminado: Boolean = false,
    val esBye: Boolean = false,
    val ganadorId: String? = null,
    val limiteTocados: Int = 5 // 5 para poules, 15 para eliminatorias
)

@Serializable
data class Poule(
    val id: String,
    val nombre: String,
    val asaltos: List<Asalto>,
    val arbitros: List<Arbitro>,
    val pistas: List<String>
)

@Serializable
data class Ronda(
    val nombre: String, // "Octavos", "Cuartos", "Semifinal", "Final"
    val asaltos: List<Asalto>
)

@Serializable
data class Competicion(
    val id: String,
    val nombre: String,
    val entidadOrganizadora: String,
    val fecha: String,
    val lugar: String,
    val arma: Arma,
    val fase: FaseCompeticion = FaseCompeticion.INSCRIPCION,
    val inscritosIds: Set<String> = emptySet(),
    val poules: List<Poule> = emptyList(),
    val rondasEliminatorias: List<Ronda> = emptyList(),
    val numClasificadosCorte: Int = 16 // Configurable para el tablón
)

@Serializable
enum class FaseCompeticion {
    INSCRIPCION, POULES, ELIMINATORIAS, FINALIZADA
}

@Serializable
data class Clasificacion(
    val tirador: Tirador,
    val victorias: Int,
    val combates: Int,
    val v_m: Double,
    val td: Int,
    val tr: Int,
    val indice: Int,
    val posicion: Int = 0
)
