package com.carbajal.centrotareas.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class TareasRepositoryLocal : TareasRepository {
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
