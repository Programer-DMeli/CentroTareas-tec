package com.carbajal.centrotareas.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carbajal.centrotareas.data.TareasRepository
import com.carbajal.centrotareas.data.TareasRepositoryLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface DosCargasState {
    object Idle : DosCargasState
    object Cargando : DosCargasState
    data class Exito(
        val modo: String,
        val perfil: String,
        val cantidadPedidos: Int,
        val ms: Long
    ) : DosCargasState
    data class Error(val mensaje: String) : DosCargasState
}

class TareasViewModel(
    private val repository: TareasRepository = TareasRepositoryLocal()
) : ViewModel() {

    private val _contador = MutableStateFlow(0)
    val contador: StateFlow<Int> = _contador.asStateFlow()

    private val _mensaje = MutableStateFlow("Listo")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    private val _progreso = MutableStateFlow(0f)
    val progreso: StateFlow<Float> = _progreso.asStateFlow()

    private var trabajo: Job? = null
    private val _corriendo = MutableStateFlow(false)
    val corriendo: StateFlow<Boolean> = _corriendo.asStateFlow()

    // --- Step 5: Primos states ---
    private val _primosInput = MutableStateFlow("500000")
    val primosInput: StateFlow<String> = _primosInput.asStateFlow()

    private val _primosResultado = MutableStateFlow("")
    val primosResultado: StateFlow<String> = _primosResultado.asStateFlow()

    private val _primosCorriendo = MutableStateFlow(false)
    val primosCorriendo: StateFlow<Boolean> = _primosCorriendo.asStateFlow()

    private val _primosProgreso = MutableStateFlow(0f)
    val primosProgreso: StateFlow<Float> = _primosProgreso.asStateFlow()

    private var trabajoPrimos: Job? = null

    // --- Step 6 & 7: Dos cargas states & simularError ---
    private val _dosCargasState = MutableStateFlow<DosCargasState>(DosCargasState.Idle)
    val dosCargasState: StateFlow<DosCargasState> = _dosCargasState.asStateFlow()

    private val _simularError = MutableStateFlow(false)
    val simularError: StateFlow<Boolean> = _simularError.asStateFlow()

    fun cambiarSimularError(valor: Boolean) {
        _simularError.value = valor
    }

    fun incrementarContador() {
        _contador.value += 1
    }

    fun tareaLarga() {
        if (_corriendo.value) return

        trabajo = viewModelScope.launch {
            _corriendo.value = true
            _progreso.value = 0f
            _mensaje.value = "Trabajando..."
            Log.d("HILO", "Antes: ${Thread.currentThread().name}")

            try {
                withContext(Dispatchers.Default) {
                    Log.d("HILO", "Dentro: ${Thread.currentThread().name}")
                    for (i in 1..100) {
                        delay(40)
                        _progreso.value = i / 100f
                    }
                }
                Log.d("HILO", "Despues: ${Thread.currentThread().name}")
                _mensaje.value = "Terminó"
            } catch (e: kotlinx.coroutines.CancellationException) {
                _mensaje.value = "Cancelado"
                throw e
            } finally {
                _corriendo.value = false
            }
        }
    }

    fun cancelar() {
        trabajo?.cancel()
        _mensaje.value = "Cancelado"
    }

    fun onPrimosInputChanged(input: String) {
        _primosInput.value = input.filter { it.isDigit() }
    }

    fun contarPrimos() {
        val hasta = _primosInput.value.toIntOrNull() ?: return
        if (hasta < 2) return
        if (_primosCorriendo.value) return

        trabajoPrimos = viewModelScope.launch {
            _primosCorriendo.value = true
            _primosResultado.value = "Calculando..."
            _primosProgreso.value = 0f
            val inicio = System.currentTimeMillis()

            try {
                val cantidad = repository.contarPrimos(hasta) { progreso ->
                    _primosProgreso.value = progreso
                }
                val tiempo = System.currentTimeMillis() - inicio
                _primosResultado.value = "Primos hasta $hasta: $cantidad\nTiempo: $tiempo ms"
            } catch (e: kotlinx.coroutines.CancellationException) {
                _primosResultado.value = "Cancelado"
                throw e
            } finally {
                _primosCorriendo.value = false
            }
        }
    }

    fun cancelarPrimos() {
        trabajoPrimos?.cancel()
        _primosResultado.value = "Cancelado"
    }

    fun cargarUnaPorUna() {
        if (_dosCargasState.value is DosCargasState.Cargando) return

        viewModelScope.launch {
            _dosCargasState.value = DosCargasState.Cargando
            val inicio = System.currentTimeMillis()

            try {
                kotlinx.coroutines.withTimeout(2000L) {
                    val perfil = repository.cargarPerfil()
                    val pedidos = repository.cargarPedidos(_simularError.value)
                    val tiempo = System.currentTimeMillis() - inicio
                    _dosCargasState.value = DosCargasState.Exito(
                        modo = "Una por una",
                        perfil = perfil,
                        cantidadPedidos = pedidos.size,
                        ms = tiempo
                    )
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                _dosCargasState.value = DosCargasState.Error("Tardó demasiado")
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _dosCargasState.value = DosCargasState.Error("No se pudo cargar: ${e.localizedMessage}")
            }
        }
    }

    fun cargarALaVez() { // Se implementó cargar a la vez utilizando coroutineScope y async para ejecutar ambas operaciones en paralelo
        if (_dosCargasState.value is DosCargasState.Cargando) return

        viewModelScope.launch {
            _dosCargasState.value = DosCargasState.Cargando
            val inicio = System.currentTimeMillis()

            try {
                kotlinx.coroutines.withTimeout(2000L) {
                    val resultado = coroutineScope {
                        val a = async { repository.cargarPerfil() }
                        val b = async { repository.cargarPedidos(_simularError.value) }
                        Pair(a.await(), b.await())
                    }
                    val tiempo = System.currentTimeMillis() - inicio
                    _dosCargasState.value = DosCargasState.Exito(
                        modo = "A la vez",
                        perfil = resultado.first,
                        cantidadPedidos = resultado.second.size,
                        ms = tiempo
                    )
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                _dosCargasState.value = DosCargasState.Error("Tardó demasiado")
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _dosCargasState.value = DosCargasState.Error("No se pudo cargar: ${e.localizedMessage}")
            }
        }
    }
}
