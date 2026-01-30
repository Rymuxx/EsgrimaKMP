package com.example.esgrima.data

import com.example.esgrima.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlin.math.log2
import kotlin.math.pow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.random.Random

@Serializable
data class CompetitionState(
    val fencers: List<Tirador>,
    val referees: List<Arbitro>,
    val competitions: List<Competicion>,
    val users: List<User>
)

object DataRepository {
    private val initialClubs = listOf(
        Club("Sala de Armas del Ejercito", "FED-001"),
        Club("Club de Esgrima Valencia", "FED-002"),
        Club("Real Club de Esgrima Madrid", "FED-003"),
        Club("Club de Esgrima Tarragona", "FED-004")
    )

    private val initialFencers = listOf(
        Tirador("t1", "Juan Ramirez", initialClubs[0], "2001"),
        Tirador("t2", "Juan Ramirez", initialClubs[0], "2034"),
        Tirador("t3", "Maria Ramirez", initialClubs[1], "2101"),
        Tirador("t4", "Juan Rodriguez", initialClubs[1], "1015"),
        Tirador("t5", "Juan Random", initialClubs[1], "1021"),
        Tirador("t6", "Pepa Conill", initialClubs[1], "2901"),
        Tirador("t7", "Francisco Clausell", initialClubs[2], "3010"),
        Tirador("t8", "Pablo Blanco", initialClubs[1], "2501"),
        Tirador("t9", "Paz Gonzale", initialClubs[3], "2231"),
        Tirador("t10", "Juanito Palotes", initialClubs[1], "2198"),
        Tirador("t11", "Rodrigo Rodriguez", initialClubs[0], "21"),
        Tirador("t12", "Ana Garcia", initialClubs[2], "4001"),
        Tirador("t13", "Luis Moreno", initialClubs[3], "4002"),
        Tirador("t14", "Elena Sanz", initialClubs[0], "4003"),
        Tirador("t15", "Carlos Ruiz", initialClubs[1], "4004"),
        Tirador("t16", "Sofia Vega", initialClubs[2], "4005")
    )

    private val initialReferees = listOf(
        Arbitro("a1", "Juan Ramirez", "2001", listOf(Arma.ESPADA, Arma.SABLE, Arma.FLORETE)),
        Arbitro("a2", "Juan Ramos", "2317", listOf(Arma.ESPADA, Arma.SABLE, Arma.FLORETE)),
        Arbitro("a3", "Maria Ramirez", "2101", listOf(Arma.ESPADA, Arma.SABLE, Arma.FLORETE)),
        Arbitro("a4", "Juan Rodriguez", "1015", listOf(Arma.SABLE, Arma.FLORETE)),
        Arbitro("a5", "Juan Random", "1021", listOf(Arma.ESPADA, Arma.SABLE)),
        Arbitro("a6", "Juanito Palotes", "2198", listOf(Arma.ESPADA, Arma.SABLE, Arma.FLORETE)),
        Arbitro("a7", "Rodrigo Rodriguez", "21", listOf(Arma.ESPADA, Arma.SABLE, Arma.FLORETE))
    )

    private val initialUsers = listOf(
        User("u1", "admin", "admin123", Role.ADMIN),
        User("u2", "arbitro", "arbitro123", Role.REFEREE, "a1"),
        User("u3", "tirador", "tirador123", Role.FENCER, "t3")
    )

    private val demoCompetitions = listOf(
        Competicion(
            id = "c1",
            nombre = "Octavos de Final Demo (16 Tiradores)",
            entidadOrganizadora = "Federación Nacional",
            fecha = "2025-02-15",
            lugar = "Madrid",
            arma = Arma.ESPADA,
            inscritosIds = initialFencers.map { it.id }.toSet()
        ),
        Competicion(
            id = "c2",
            nombre = "Cuartos de Final Demo (8 Tiradores)",
            entidadOrganizadora = "Club Esgrima Valencia",
            fecha = "2025-03-10",
            lugar = "Valencia",
            arma = Arma.FLORETE,
            inscritosIds = initialFencers.take(8).map { it.id }.toSet()
        ),
        Competicion(
            id = "c3",
            nombre = "Final a 4 Demo",
            entidadOrganizadora = "Sala de Armas Ejercito",
            fecha = "2025-04-05",
            lugar = "Toledo",
            arma = Arma.SABLE,
            inscritosIds = initialFencers.take(4).map { it.id }.toSet()
        )
    )

    private val _fencers = MutableStateFlow<List<Tirador>>(initialFencers)
    val fencers: StateFlow<List<Tirador>> = _fencers.asStateFlow()

    private val _referees = MutableStateFlow<List<Arbitro>>(initialReferees)
    val referees: StateFlow<List<Arbitro>> = _referees.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(initialUsers)
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _competitions = MutableStateFlow<List<Competicion>>(demoCompetitions)
    val competitions: StateFlow<List<Competicion>> = _competitions.asStateFlow()

    private val _selectedCompetitionId = MutableStateFlow<String?>(null)
    val selectedCompetitionId: StateFlow<String?> = _selectedCompetitionId.asStateFlow()

    fun login(username: String, password: String): Boolean {
        val user = _users.value.find { 
            it.username.lowercase() == username.lowercase() && it.password == password 
        }
        return if (user != null) {
            _currentUser.value = user
            true
        } else false
    }

    fun logout() {
        _currentUser.value = null
    }

    fun selectCompetition(id: String?) {
        _selectedCompetitionId.value = id
    }

    fun inscribirseEnCompeticion(competitionId: String) {
        val user = _currentUser.value ?: return
        val fencerId = user.linkedId ?: return
        if (user.role != Role.FENCER) return
        
        _competitions.value = _competitions.value.map { comp ->
            if (comp.id == competitionId) {
                comp.copy(inscritosIds = comp.inscritosIds + fencerId)
            } else comp
        }
    }

    fun addFencer(fencer: Tirador) {
        _fencers.value += fencer
        _users.value += User(
            id = Clock.System.now().toEpochMilliseconds().toString(), 
            username = fencer.nombre.split(" ")[0].lowercase(), 
            password = "password123",
            role = Role.FENCER, 
            linkedId = fencer.id
        )
    }

    fun addReferee(referee: Arbitro) {
        _referees.value += referee
        _users.value += User(
            id = Clock.System.now().toEpochMilliseconds().toString(), 
            username = referee.nombre.split(" ")[0].lowercase(), 
            password = "password123",
            role = Role.REFEREE, 
            linkedId = referee.id
        )
    }

    fun addCompetition(comp: Competicion) {
        _competitions.value += comp
    }

    private fun getRandomSpecialistReferee(arma: Arma): Arbitro {
        val specialists = _referees.value.filter { it.especialidades.contains(arma) }
        return if (specialists.isNotEmpty()) specialists.random() else initialReferees.first { it.especialidades.contains(arma) }
    }

    fun simulateResults(competitionId: String) {
        _competitions.value = _competitions.value.map { comp ->
            if (comp.id == competitionId) {
                val simPoules = comp.poules.map { poule ->
                    poule.copy(asaltos = poule.asaltos.map { asalto ->
                        val (s1, s2) = if (Random.nextBoolean()) Pair(5, Random.nextInt(0, 5)) else Pair(Random.nextInt(0, 5), 5)
                        asalto.copy(tocados1 = s1, tocados2 = s2, terminado = true)
                    })
                }
                comp.copy(poules = simPoules, fase = FaseCompeticion.POULES)
            } else comp
        }
    }

    fun createPoules(competitionId: String, numPoules: Int) {
        val comp = _competitions.value.find { it.id == competitionId } ?: return
        val currentFencers = _fencers.value.filter { it.id in comp.inscritosIds }.shuffled()
        if (currentFencers.isEmpty()) return

        val fencersPerPoule = (currentFencers.size + numPoules - 1) / numPoules
        val chunks = currentFencers.chunked(fencersPerPoule)
        
        val newPoules = chunks.mapIndexed { index, fencersInPoule ->
            val asaltos = mutableListOf<Asalto>()
            for (i in fencersInPoule.indices) {
                for (j in i + 1 until fencersInPoule.size) {
                    asaltos.add(
                        Asalto(
                            id = "asalto_${competitionId}_${index}_${i}_${j}",
                            arbitro = getRandomSpecialistReferee(comp.arma),
                            tirador1 = fencersInPoule[i],
                            tirador2 = fencersInPoule[j]
                        )
                    )
                }
            }
            Poule(
                id = "poule_${competitionId}_${index}",
                nombre = "Poule ${index + 1}",
                asaltos = asaltos.shuffled(),
                arbitros = listOf(getRandomSpecialistReferee(comp.arma)),
                pistas = listOf("Pista ${index + 1}")
            )
        }

        _competitions.value = _competitions.value.map { c ->
            if (c.id == competitionId) {
                c.copy(poules = newPoules, fase = FaseCompeticion.POULES, rondasEliminatorias = emptyList())
            } else c
        }
    }

    fun updateAsalto(competitionId: String, pouleId: String, asaltoId: String, t1: Int, t2: Int, terminado: Boolean) {
        _competitions.value = _competitions.value.map { comp ->
            if (comp.id == competitionId) {
                comp.copy(poules = comp.poules.map { poule ->
                    if (poule.id == pouleId) {
                        poule.copy(asaltos = poule.asaltos.map { asalto ->
                            if (asalto.id == asaltoId) {
                                asalto.copy(tocados1 = t1, tocados2 = t2, terminado = terminado)
                            } else asalto
                        })
                    } else poule
                })
            } else comp
        }
    }

    private fun getSeedingOrder(n: Int): List<Int> {
        var seeds = listOf(1, 2)
        while (seeds.size < n) {
            val nextSeeds = mutableListOf<Int>()
            val sum = seeds.size * 2 + 1
            for (seed in seeds) {
                nextSeeds.add(seed)
                nextSeeds.add(sum - seed)
            }
            seeds = nextSeeds
        }
        return seeds
    }

    fun generarCuadroEliminatorio(competitionId: String) {
        val comp = _competitions.value.find { it.id == competitionId } ?: return
        val ranking = getRankingCalculado(comp)
        val numTiradores = ranking.size
        if (numTiradores == 0) return

        val nextPowerOfTwo = 2.0.pow(kotlin.math.ceil(log2(numTiradores.toDouble()))).toInt()
        val order = getSeedingOrder(nextPowerOfTwo)
        
        val asaltos = mutableListOf<Asalto>()
        for (i in 0 until nextPowerOfTwo step 2) {
            val s1 = order[i]
            val s2 = order[i+1]
            
            val t1 = ranking.getOrNull(s1 - 1)?.tirador
            val t2 = ranking.getOrNull(s2 - 1)?.tirador
            
            if (t1 != null && t2 != null) {
                asaltos.add(Asalto(
                    id = "e_${competitionId}_${nextPowerOfTwo}_${i/2}",
                    arbitro = getRandomSpecialistReferee(comp.arma),
                    tirador1 = t1,
                    tirador2 = t2
                ))
            } else if (t1 != null) {
                asaltos.add(Asalto(
                    id = "e_${competitionId}_${nextPowerOfTwo}_${i/2}",
                    arbitro = initialReferees[0],
                    tirador1 = t1,
                    tirador2 = t1,
                    tocados1 = 15,
                    tocados2 = 0,
                    terminado = true,
                    esBye = true
                ))
            }
        }
        
        val nombre = when(nextPowerOfTwo) {
            16 -> "Octavos"
            8 -> "Cuartos"
            4 -> "Semifinal"
            2 -> "Final"
            else -> "Tablón de $nextPowerOfTwo"
        }

        _competitions.value = _competitions.value.map { c ->
            if (c.id == competitionId) {
                c.copy(rondasEliminatorias = listOf(Ronda(nombre, asaltos)), fase = FaseCompeticion.ELIMINATORIAS)
            } else c
        }
    }

    fun avanzarCuadro(competitionId: String) {
        val comp = _competitions.value.find { it.id == competitionId } ?: return
        val actual = comp.rondasEliminatorias.lastOrNull() ?: return
        if (actual.asaltos.size <= 1 && actual.asaltos.all { it.terminado }) return

        val ganadores = actual.asaltos.map { asalto ->
            if (asalto.esBye) asalto.tirador1
            else if (asalto.tocados1 >= asalto.tocados2) asalto.tirador1 
            else asalto.tirador2
        }

        val nuevosAsaltos = mutableListOf<Asalto>()
        for (i in ganadores.indices step 2) {
            val t1 = ganadores[i]
            val t2 = ganadores.getOrNull(i + 1)
            if (t2 != null) {
                nuevosAsaltos.add(Asalto(
                    id = "e_${competitionId}_${actual.asaltos.size}_${i/2}",
                    arbitro = getRandomSpecialistReferee(comp.arma),
                    tirador1 = t1,
                    tirador2 = t2
                ))
            } else {
                nuevosAsaltos.add(Asalto(
                    id = "e_${competitionId}_${actual.asaltos.size}_${i/2}",
                    arbitro = initialReferees[0],
                    tirador1 = t1,
                    tirador2 = t1,
                    tocados1 = 15,
                    terminado = true,
                    esBye = true
                ))
            }
        }
        
        val nombre = when(nuevosAsaltos.size) {
            4 -> "Cuartos"
            2 -> "Semifinal"
            1 -> "Final"
            else -> "Tablón de ${nuevosAsaltos.size * 2}"
        }
        
        _competitions.value = _competitions.value.map { c ->
            if (c.id == competitionId) {
                c.copy(rondasEliminatorias = c.rondasEliminatorias + Ronda(nombre, nuevosAsaltos))
            } else c
        }
    }

    fun updateAsaltoCuadro(competitionId: String, nombreRonda: String, asaltoId: String, t1: Int, t2: Int, terminado: Boolean) {
        _competitions.value = _competitions.value.map { comp ->
            if (comp.id == competitionId) {
                comp.copy(rondasEliminatorias = comp.rondasEliminatorias.map { ronda ->
                    if (ronda.nombre == nombreRonda) {
                        ronda.copy(asaltos = ronda.asaltos.map { asalto ->
                            if (asalto.id == asaltoId) {
                                asalto.copy(tocados1 = t1, tocados2 = t2, terminado = terminado)
                            } else asalto
                        })
                    } else ronda
                })
            } else comp
        }
    }

    fun getRankingCalculado(comp: Competicion): List<Clasificacion> {
        val allAsaltos = comp.poules.flatMap { it.asaltos }
        return _fencers.value.filter { it.id in comp.inscritosIds }.map { fencer ->
            val matches = allAsaltos.filter { it.terminado && (it.tirador1.id == fencer.id || it.tirador2.id == fencer.id) }
            val wins = matches.count { 
                (it.tirador1.id == fencer.id && it.tocados1 > it.tocados2) || 
                (it.tirador2.id == fencer.id && it.tocados2 > it.tocados1) 
            }
            val td = matches.sumOf { if (it.tirador1.id == fencer.id) it.tocados1 else it.tocados2 }
            val tr = matches.sumOf { if (it.tirador1.id == fencer.id) it.tocados2 else it.tocados1 }
            
            val v_m = if (matches.isNotEmpty()) wins.toDouble() / matches.size else 0.0
            Clasificacion(
                tirador = fencer,
                victorias = wins,
                combates = matches.size,
                v_m = v_m,
                td = td,
                tr = tr,
                indice = td - tr
            )
        }.sortedWith(
            compareByDescending<Clasificacion> { it.v_m }
                .thenByDescending { it.indice }
                .thenByDescending { it.td }
        ).mapIndexed { index, clasificacion -> clasificacion.copy(posicion = index + 1) }
    }

    fun guardarEnDisco() {
        try {
            val json = Json { prettyPrint = true }
            val state = CompetitionState(_fencers.value, _referees.value, _competitions.value, _users.value)
            File("competicion.json").writeText(json.encodeToString(CompetitionState.serializer(), state))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cargarDesdeDisco() {
        try {
            val file = File("competicion.json")
            if (file.exists()) {
                val json = Json { ignoreUnknownKeys = true }
                val state = json.decodeFromString(CompetitionState.serializer(), file.readText())
                _fencers.value = state.fencers
                _referees.value = state.referees
                _competitions.value = state.competitions
                _users.value = state.users
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
