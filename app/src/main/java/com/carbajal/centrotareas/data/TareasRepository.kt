package com.carbajal.centrotareas.data

interface TareasRepository {
    suspend fun contarPrimos(hasta: Int): Int
    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(conError: Boolean): List<String>
}
