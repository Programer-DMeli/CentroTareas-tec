# CentroTareas

## Observación del Paso 1
- **¿Qué pasa con la pantalla?**: Durante 4 segundos la pantalla se congela por completo (no responde a toques ni redibuja).
- **¿Por qué no se ve "Trabajando..."?**: Porque `Thread.sleep(4000)` bloquea el hilo principal (UI thread). Como el hilo principal está bloqueado, Jetpack Compose no puede procesar la recomposición para pintar el texto "Trabajando..." en la UI antes de que el hilo entre en pausa. El cambio de estado ocurre, pero el hilo se bloquea inmediatamente impidiendo que se renderice en pantalla.
- **¿Qué crees que mostraría Android si la espera fuera de 10 segundos?**: Si la espera fuera de 10 segundos en el hilo principal, Android detectaría que la aplicación no responde a los eventos táctiles o de red durante más de 5 segundos, lanzando un diálogo de **ANR (Application Not Responding)** y cerrando o forzando el cierre de la aplicación.

## Observación del Paso 2
- **¿Qué pasa con la pantalla y el contador?**: Al usar `viewModelScope.launch` y `withContext(Dispatchers.Default) { delay(4000) }`, la tarea pesada corre en segundo plano. La interfaz principal (UI thread) no se bloquea, por lo que el contador sigue sumando al tocar el botón mientras el texto muestra "Trabajando...".
- **Logs en Logcat (tag `HILO`)**:
  - `Antes: main` (hilo principal antes de entrar al cambio de contexto).
  - `Dentro: DefaultDispatcher-worker-...` (hilo de segundo plano donde se ejecuta la demora).
  - `Despues: main` (retorno al hilo principal para actualizar el estado a "Terminó").

## Observación del Paso 3
- **Progreso en tiempo real**: Se implementó `LinearProgressIndicator` enlazado a un `StateFlow<Float>` de `0f` a `1f` en el ViewModel. Durante las 100 iteraciones con `delay(40)` dentro de `Dispatchers.Default`, el progreso avanza progresivamente hasta el 100% (~4 segundos) mientras la UI se mantiene 100% responsiva (permitiendo seguir incrementando el contador con el botón "Tócame").

## Observación del Paso 4
- **Cancelación y Control de Concurrencia**: Se gestionó la referencia al `Job` devuelto por `viewModelScope.launch` y un estado booleano `corriendo` expuesto como `StateFlow<Boolean>`. Al pulsar **Iniciar**, si ya está corriendo, se previene un doble inicio. Al pulsar **Cancelar**, se invoca `trabajo?.cancel()`, deteniendo el `delay` cooperativo en curso y actualizando el mensaje a **Cancelado** mientras la barra mantiene el porcentaje alcanzado. El bloque `finally` garantiza que `corriendo` vuelva a `false` permitiendo reiniciar la tarea.
