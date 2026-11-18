package com.carbajal.centrotareas.data

interface TareasRepository {
    suspend fun contarPrimos(hasta: Int): Int
}
