package com.carbajal.centrotareas.data

import kotlinx.coroutines.flow.Flow

interface TareasRepository {
    suspend fun contarPrimos(hasta: Int, onProgress: (Float) -> Unit = {}): Int
    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(conError: Boolean): List<String>
    val historial: Flow<List<String>>
    suspend fun guardarResultado(resultado: String)
}
