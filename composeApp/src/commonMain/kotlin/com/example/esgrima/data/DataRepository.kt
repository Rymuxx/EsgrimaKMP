package com.example.esgrima.data

import com.example.esgrima.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.ceil
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
    private val adminUser = User("u1", "admin", "admin123", Role.ADMIN)
    private val DummyFencer = Tirador("bye", "PASO LIBRE", Club("---", ""), "---", emptyList())

    private val _fencers = MutableStateFlow<List<Tirador>>(emptyList())
    val fencers: StateFlow<List<Tirador>> = _fencers.asStateFlow()

    private val _referees = MutableStateFlow<List<Arbitro>>(emptyList())
    val referees: StateFlow<List<Arbitro>> = _referees.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(listOf(adminUser))
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _competitions = MutableStateFlow<List<Competicion>>(emptyList())
    val competitions: StateFlow<List<Competicion>> = _competitions.asStateFlow()

    private val _selectedCompetitionId = MutableStateFlow<String?>(null)
    val selectedCompetitionId: StateFlow<String?> = _selectedCompetitionId.asStateFlow()

    init {
        cargarDesdeDisco()
        if (_fencers.value.size < 32 || _referees.value.size < 16) {
            seedData()
        }
    }

    private fun seedData() {
        val nombres = listOf("Juan", "Maria", "Carlos", "Ana", "Pedro", "Lucia", "Diego", "Elena", "Pablo", "Sara", "Luis", "Marta", "Jorge", "Laura", "Raul", "Sofia", "Ines", "Javier", "Carmen", "Miguel")
        val apellidos = listOf("Ramirez", "Rodriguez", "Blanco", "Garcia", "Lopez", "Perez", "Martinez", "Sanz", "Gomez", "Torres", "Ruiz", "Vidal", "Castro", "Ortiz")
        val clubes = listOf("Sala de Armas del Ejercito", "Club de Esgrima Valencia", "Real Club de Esgrima Madrid", "Club de Esgrima Tarragona")

        var i = 1
        while (_fencers.value.size < 32) {
            val lic = (1000 + i).toString()
            val id = "t$lic"
            if (_fencers.value.none { it.id == id }) {
                val nombre = "${nombres.random()} ${apellidos.random()}"
                addFencer(Tirador(id, nombre, Club(clubes.random(), ""), lic, listOf(Arma.entries.random())))
            }
            i++
        }

        var j = 1
        while (_referees.value.size < 16) {
            val lic = (2000 + j).toString()
            val id = "a$lic"
            if (_referees.value.none { it.id == id }) {
                val nombre = "${nombres.random()} ${apellidos.random()}"
                addReferee(Arbitro(id, nombre, lic, listOf(Arma.ESPADA, Arma.SABLE, Arma.FLORETE).shuffled().take(Random.nextInt(1, 4))))
            }
            j++
        }

        if (_competitions.value.isEmpty()) {
            addCompetition(Competicion("test_comp", "Torneo Inaugural 2025", "Federación", "2025-01-25", "Madrid", Arma.ESPADA))
        }
        
        save()
    }

    private fun save() {
        try {
            val json = Json { prettyPrint = true }
            val state = CompetitionState(_fencers.value, _referees.value, _competitions.value, _users.value)
            File("competicion.json").writeText(json.encodeToString(CompetitionState.serializer(), state))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cargarDesdeDisco() {
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

    fun switchRole(role: Role) {
        val user = when (role) {
            Role.ADMIN -> adminUser
            Role.REFEREE -> _users.value.find { it.role == Role.REFEREE } ?: User("temp_ref", "Referee", "", Role.REFEREE)
            Role.FENCER -> _users.value.find { it.role == Role.FENCER } ?: User("temp_fen", "Fencer", "", Role.FENCER)
        }
        _currentUser.value = user
    }

    fun deleteCompetition(id: String) {
        _competitions.value = _competitions.value.filter { it.id != id }
        save()
    }

    fun selectCompetition(id: String?) {
        _selectedCompetitionId.value = id
    }

    fun inscribirseEnCompeticion(competitionId: String) {
        val fencerId = _currentUser.value?.linkedId ?: return
        inscribirseEnCompeticionConId(competitionId, fencerId)
    }

    fun inscribirseEnCompeticionConId(competitionId: String, fencerId: String) {
        _competitions.value = _competitions.value.map { comp ->
            if (comp.id == competitionId) {
                val currentIds = comp.inscritosIds
                val newIds = if (currentIds.contains(fencerId)) currentIds - fencerId else currentIds + fencerId
                comp.copy(inscritosIds = newIds)
            } else comp
        }
        save()
    }

    fun inscribirATodos(competitionId: String) {
        val allFencerIds = _fencers.value.map { it.id }.toSet()
        _competitions.value = _competitions.value.map { comp ->
            if (comp.id == competitionId) {
                comp.copy(inscritosIds = allFencerIds)
            } else comp
        }
        save()
    }

    fun addFencer(fencer: Tirador) {
        if (_fencers.value.any { it.id == fencer.id }) return
        _fencers.value += fencer
        _users.value += User(
            id = "user_${fencer.id}", 
            username = fencer.nombre.split(" ")[0].lowercase() + fencer.numeroAfiliado, 
            password = "password123",
            role = Role.FENCER, 
            linkedId = fencer.id
        )
        save()
    }

    fun deleteFencer(id: String) {
        _fencers.value = _fencers.value.filter { it.id != id }
        _users.value = _users.value.filter { it.linkedId != id }
        _competitions.value = _competitions.value.map { comp ->
            comp.copy(inscritosIds = comp.inscritosIds.filter { it != id }.toSet())
        }
        save()
    }

    fun addReferee(referee: Arbitro) {
        if (_referees.value.any { it.id == referee.id }) return
        _referees.value += referee
        _users.value += User(
            id = "user_${referee.id}", 
            username = referee.nombre.split(" ")[0].lowercase() + referee.numeroAfiliado, 
            password = "password123",
            role = Role.REFEREE, 
            linkedId = referee.id
        )
        save()
    }

    fun addCompetition(comp: Competicion) {
        _competitions.value += comp
        save()
    }

    private fun getRandomSpecialistReferee(arma: Arma): Arbitro? {
        val specialists = _referees.value.filter { it.especialidades.contains(arma) }
        return if (specialists.isNotEmpty()) specialists.random() else _referees.value.firstOrNull()
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
        save()
    }

    fun simulateFullEliminatorias(competitionId: String) {
        var comp = _competitions.value.find { it.id == competitionId } ?: return
        if (comp.rondasEliminatorias.isEmpty()) return

        var currentComp = comp
        while (true) {
            val lastRonda = currentComp.rondasEliminatorias.lastOrNull() ?: break
            
            val updatedAsaltos = lastRonda.asaltos.map { asalto ->
                if (asalto.terminado) asalto
                else {
                    val (s1, s2) = if (Random.nextBoolean()) Pair(15, Random.nextInt(0, 15)) else Pair(Random.nextInt(0, 15), 15)
                    asalto.copy(tocados1 = s1, tocados2 = s2, terminado = true, ganadorId = if (s1 > s2) asalto.tirador1.id else asalto.tirador2.id)
                }
            }
            val rondaActualizada = lastRonda.copy(asaltos = updatedAsaltos)
            currentComp = currentComp.copy(rondasEliminatorias = currentComp.rondasEliminatorias.dropLast(1) + rondaActualizada)

            val ganadores = updatedAsaltos.map { asalto ->
                if (asalto.esBye) asalto.tirador1
                else if (asalto.tocados1 >= asalto.tocados2) asalto.tirador1 
                else asalto.tirador2
            }.filter { it.id != DummyFencer.id }

            if (ganadores.size <= 1) {
                currentComp = currentComp.copy(fase = FaseCompeticion.FINALIZADA)
                break
            }

            val nuevosAsaltos = mutableListOf<Asalto>()
            for (i in ganadores.indices step 2) {
                val t1 = ganadores[i]
                val t2 = ganadores.getOrNull(i + 1)
                if (t2 != null) {
                    val referee = getRandomSpecialistReferee(currentComp.arma) ?: Arbitro("temp", "Sin Arbitro", "0", emptyList())
                    nuevosAsaltos.add(Asalto(
                        id = "e_${competitionId}_${updatedAsaltos.size / 2}_${i/2}_${Clock.System.now().toEpochMilliseconds()}",
                        arbitro = referee,
                        tirador1 = t1,
                        tirador2 = t2,
                        limiteTocados = 15
                    ))
                } else {
                    nuevosAsaltos.add(Asalto(
                        id = "e_${competitionId}_${updatedAsaltos.size / 2}_${i/2}_${Clock.System.now().toEpochMilliseconds()}",
                        arbitro = Arbitro("bye", "BYE", "0", emptyList()),
                        tirador1 = t1,
                        tirador2 = DummyFencer,
                        tocados1 = 15,
                        terminado = true,
                        esBye = true,
                        ganadorId = t1.id,
                        limiteTocados = 15
                    ))
                }
            }

            if (nuevosAsaltos.isEmpty()) break

            val nombre = when(nuevosAsaltos.size) {
                4 -> "Cuartos"
                2 -> "Semifinal"
                1 -> "Final"
                else -> "Tablón de ${nuevosAsaltos.size * 2}"
            }
            currentComp = currentComp.copy(rondasEliminatorias = currentComp.rondasEliminatorias + Ronda(nombre, nuevosAsaltos))
        }

        _competitions.value = _competitions.value.map { if (it.id == competitionId) currentComp else it }
        save()
    }

    fun createPoulesAutomaticamente(competitionId: String, numPoulesInput: Int? = null) {
        val comp = _competitions.value.find { it.id == competitionId } ?: return
        val inscritos = _fencers.value.filter { it.id in comp.inscritosIds }.shuffled()
        
        if (inscritos.size < 2) return

        val numPoules = (numPoulesInput ?: 1).coerceAtMost(inscritos.size / 2).coerceAtLeast(1)
        val baseSize = inscritos.size / numPoules
        val extraFencers = inscritos.size % numPoules
        
        val newPoules = mutableListOf<Poule>()
        var currentPointer = 0
        
        for (index in 0 until numPoules) {
            val sizeForThisPoule = if (index < extraFencers) baseSize + 1 else baseSize
            val fencersInPoule = inscritos.subList(currentPointer, currentPointer + sizeForThisPoule)
            currentPointer += sizeForThisPoule
            
            val asaltos = mutableListOf<Asalto>()
            for (i in fencersInPoule.indices) {
                for (j in i + 1 until fencersInPoule.size) {
                    val referee = getRandomSpecialistReferee(comp.arma) ?: Arbitro("temp", "Sin Arbitro", "0", emptyList())
                    asaltos.add(
                        Asalto(
                            id = "asalto_${competitionId}_${index}_${i}_${j}_${Clock.System.now().toEpochMilliseconds()}",
                            arbitro = referee,
                            tirador1 = fencersInPoule[i],
                            tirador2 = fencersInPoule[j]
                        )
                    )
                }
            }
            newPoules.add(
                Poule(
                    id = "poule_${competitionId}_${index}",
                    nombre = "Poule ${index + 1}",
                    asaltos = asaltos,
                    arbitros = listOfNotNull(getRandomSpecialistReferee(comp.arma)),
                    pistas = listOf("Pista ${index + 1}")
                )
            )
        }

        _competitions.value = _competitions.value.map { c ->
            if (c.id == competitionId) {
                c.copy(poules = newPoules, fase = FaseCompeticion.POULES, rondasEliminatorias = emptyList())
            } else c
        }
        save()
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
        save()
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

    fun generarCuadroEliminatorio(competitionId: String, numClasificados: Int) {
        val comp = _competitions.value.find { it.id == competitionId } ?: return
        val completeRanking = getRankingCalculado(comp)
        val clasificados = completeRanking.take(numClasificados)
        val numTiradores = clasificados.size
        if (numTiradores < 2) return

        val nextPowerOfTwo = 2.0.pow(ceil(log2(numTiradores.toDouble())).coerceAtLeast(3.0)).toInt()
        val order = getSeedingOrder(nextPowerOfTwo)
        
        val asaltos = mutableListOf<Asalto>()
        for (i in 0 until nextPowerOfTwo step 2) {
            val s1 = order[i]
            val s2 = order[i+1]
            val t1 = clasificados.getOrNull(s1 - 1)?.tirador
            val t2 = clasificados.getOrNull(s2 - 1)?.tirador
            
            if (t1 != null && t2 != null) {
                val referee = getRandomSpecialistReferee(comp.arma) ?: Arbitro("temp", "Sin Arbitro", "0", emptyList())
                asaltos.add(Asalto(
                    id = "e_${competitionId}_${nextPowerOfTwo}_${i/2}_${Clock.System.now().toEpochMilliseconds()}",
                    arbitro = referee,
                    tirador1 = t1,
                    tirador2 = t2,
                    limiteTocados = 15
                ))
            } else if (t1 != null) {
                asaltos.add(Asalto(
                    id = "e_${competitionId}_${nextPowerOfTwo}_${i/2}_${Clock.System.now().toEpochMilliseconds()}",
                    arbitro = Arbitro("bye", "BYE", "0", emptyList()),
                    tirador1 = t1,
                    tirador2 = DummyFencer,
                    tocados1 = 15,
                    tocados2 = 0,
                    terminado = true,
                    esBye = true,
                    ganadorId = t1.id,
                    limiteTocados = 15
                ))
            }
        }
        
        val nombre = when(nextPowerOfTwo) {
            64 -> "Tablón de 64"
            32 -> "Dieciseisavos"
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
        save()
    }

    fun avanzarCuadro(competitionId: String) {
        val comp = _competitions.value.find { it.id == competitionId } ?: return
        val actual = comp.rondasEliminatorias.lastOrNull() ?: return
        val ganadores = actual.asaltos.map { asalto ->
            if (asalto.esBye) asalto.tirador1
            else if (asalto.tocados1 >= asalto.tocados2) asalto.tirador1 
            else asalto.tirador2
        }.filter { it.id != DummyFencer.id }

        val nuevosAsaltos = mutableListOf<Asalto>()
        for (i in ganadores.indices step 2) {
            val t1 = ganadores[i]
            val t2 = ganadores.getOrNull(i + 1)
            if (t2 != null) {
                val referee = getRandomSpecialistReferee(comp.arma) ?: Arbitro("temp", "Sin Arbitro", "0", emptyList())
                nuevosAsaltos.add(Asalto(
                    id = "e_${competitionId}_${actual.asaltos.size / 2}_${i/2}_${Clock.System.now().toEpochMilliseconds()}",
                    arbitro = referee,
                    tirador1 = t1,
                    tirador2 = t2,
                    limiteTocados = 15
                ))
            } else {
                nuevosAsaltos.add(Asalto(
                    id = "e_${competitionId}_${actual.asaltos.size / 2}_${i/2}_${Clock.System.now().toEpochMilliseconds()}",
                    arbitro = Arbitro("bye", "BYE", "0", emptyList()),
                    tirador1 = t1,
                    tirador2 = DummyFencer,
                    tocados1 = 15,
                    terminado = true,
                    esBye = true,
                    ganadorId = t1.id,
                    limiteTocados = 15
                ))
            }
        }
        
        if (nuevosAsaltos.isEmpty()) return
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
        save()
    }

    fun updateAsaltoCuadro(competitionId: String, nombreRonda: String, asaltoId: String, t1: Int, t2: Int, terminado: Boolean) {
        _competitions.value = _competitions.value.map { comp ->
            if (comp.id == competitionId) {
                comp.copy(rondasEliminatorias = comp.rondasEliminatorias.map { ronda ->
                    if (ronda.nombre == nombreRonda) {
                        ronda.copy(asaltos = ronda.asaltos.map { asalto ->
                            if (asalto.id == asaltoId) {
                                asalto.copy(tocados1 = t1, tocados2 = t2, terminado = terminado, ganadorId = if (terminado) (if (t1 > t2) asalto.tirador1.id else asalto.tirador2.id) else null)
                            } else asalto
                        })
                    } else ronda
                })
            } else comp
        }
        save()
    }

    fun getRankingCalculado(comp: Competicion): List<Clasificacion> {
        val allAsaltos = comp.poules.flatMap { it.asaltos }
        return _fencers.value.filter { it.id in comp.inscritosIds }.map { fencer ->
            val matches = allAsaltos.filter { it.terminado && (it.tirador1.id == fencer.id || it.tirador2.id == fencer.id) }
            val wins = matches.count { 
                (it.tirador1.id == fencer.id && it.tocados1 > it.tocados2) || 
                (it.tirador2.id == fencer.id && it.tocados2 > it.tocados1) 
            }
            val td = matches.sumOf { m: Asalto -> if (m.tirador1.id == fencer.id) m.tocados1 else m.tocados2 }
            val tr = matches.sumOf { m: Asalto -> if (m.tirador1.id == fencer.id) m.tocados2 else m.tocados1 }
            
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

    fun getRankingGlobal(): List<Clasificacion> {
        val allAsaltos = _competitions.value.flatMap { it.poules.flatMap { p -> p.asaltos } + it.rondasEliminatorias.flatMap { r -> r.asaltos } }
        return _fencers.value.map { fencer ->
            val matches = allAsaltos.filter { it.terminado && (it.tirador1.id == fencer.id || it.tirador2.id == fencer.id) }
            val wins = matches.count { 
                (it.tirador1.id == fencer.id && it.tocados1 > it.tocados2) || 
                (it.tirador2.id == fencer.id && it.tocados2 > it.tocados1) 
            }
            val td = matches.sumOf { m: Asalto -> if (m.tirador1.id == fencer.id) m.tocados1 else m.tocados2 }
            val tr = matches.sumOf { m: Asalto -> if (m.tirador1.id == fencer.id) m.tocados2 else m.tocados1 }
            
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
}
