package com.carbajal.centrotareas.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

object AppContextHolder {
    fun getContext(): android.content.Context? {
        return try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val method = activityThread.getMethod("currentApplication")
            method.invoke(null) as? android.content.Context
        } catch (e: Exception) {
            null
        }
    }
}

class TareasRepositoryLocal : TareasRepository {

    private val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create(
            produceFile = {
                val context = AppContextHolder.getContext()
                    ?: throw IllegalStateException("Context not available")
                File(context.filesDir, "historial_resultados.preferences_pb")
            }
        )
    }

    private val HISTORIAL_KEY = stringSetPreferencesKey("historial_resultados_key")
    private val _fallbackHistorial = MutableStateFlow<List<String>>(emptyList())

    override val historial: Flow<List<String>> = try {
        dataStore.data.map { preferences ->
            preferences[HISTORIAL_KEY]?.toList() ?: emptyList()
        }
    } catch (e: Exception) {
        _fallbackHistorial
    }

    override suspend fun guardarResultado(resultado: String) {
        try {
            dataStore.edit { preferences ->
                val actual = preferences[HISTORIAL_KEY]?.toMutableList() ?: mutableListOf()
                actual.add(0, resultado)
                if (actual.size > 5) {
                    actual.removeAt(actual.size - 1)
                }
                preferences[HISTORIAL_KEY] = actual.toSet()
            }
        } catch (e: Exception) {
            val actual = _fallbackHistorial.value.toMutableList()
            actual.add(0, resultado)
            if (actual.size > 5) actual.removeAt(actual.size - 1)
            _fallbackHistorial.value = actual
        }
    }

    override suspend fun contarPrimos(hasta: Int, onProgress: (Float) -> Unit): Int = withContext(Dispatchers.Default) {
        var cantidad = 0
        val total = hasta.toFloat()
        for (n in 2..hasta) {
            ensureActive()
            if (esPrimo(n)) {
                cantidad++
            }
            if (n % 2000 == 0 || n == hasta) {
                onProgress(n / total)
            }
        }
        cantidad
    }

    override suspend fun cargarPerfil(): String = withContext(Dispatchers.Default) {
        delay(2000)
        "Ana Torres (ficticia)"
    }

    override suspend fun cargarPedidos(conError: Boolean): List<String> = withContext(Dispatchers.Default) {
        delay(3000)
        if (conError) {
            throw java.io.IOException("Sin conexión")
        }
        listOf("Pedido 101", "Pedido 102", "Pedido 103")
    }

    private fun esPrimo(n: Int): Boolean {
        if (n < 2) return false
        var d = 2
        while (d.toLong() * d <= n) {
            if (n % d == 0) return false
            d++
        }
        return true
    }
}
