# CentroTareas

## Observación del Paso 1
- **¿Qué pasa con la pantalla?**: Durante 4 segundos la pantalla se congela por completo (no responde a toques ni redibuja).
- **¿Por qué no se ve "Trabajando..."?**: Porque `Thread.sleep(4000)` bloquea el hilo principal (UI thread). Como el hilo principal está bloqueado, Jetpack Compose no puede procesar la recomposición para pintar el texto "Trabajando..." en la UI antes de que el hilo entre en pausa. El cambio de estado ocurre, pero el hilo se bloquea inmediatamente impidiendo que se renderice en pantalla.
- **¿Qué crees que mostraría Android si la espera fuera de 10 segundos?**: Si la espera fuera de 10 segundos en el hilo principal, Android detectaría que la aplicación no responde a los eventos táctiles o de red durante más de 5 segundos, lanzando un diálogo de **ANR (Application Not Responding)** y cerrando o forzando el cierre de la aplicación.
