# Enfoque

Aplicación Android para planear y ejecutar tareas con dos paneles complementarios:
**Enfoque Diario** (lo que debe terminar antes de que acabe el día) y
**Planeación** (Mañana + Backlog). Incluye **Rutinas** reutilizables, **Pomodoro**
con alarmas por fase, **racha** de días completados, varios estilos de diseño,
persistencia local con Room, recordatorios con `AlarmManager` y bloques de
enfoque con temporizador en un servicio en primer plano.

Esta es la **fase 1**: base funcional, sin conexión a Google Calendar, pero con
los puntos de extensión preparados para integrarla sin reescribir la UI.

## Stack

- Kotlin 2.0 + Jetpack Compose (Material 3)
- MVVM: `PanelViewModel` + `StateFlow` + estado inmutable
- Room (KSP) con modelo de dominio separado de la entidad
- Repositorios como única puerta de entrada a los datos
- `AlarmManager` para recordatorios y alarmas exactas de cada fase
- Servicios en primer plano (`specialUse`) + notificaciones con acciones
- Preferencias persistentes de diseño y Pomodoro (`SharedPreferences` + `StateFlow`)
- Inyección de dependencias manual con `ContenedorApp` (fácil de migrar a Hilt)

## Requisitos

- Android Studio Ladybug o posterior
- JDK 17 o 21 para Gradle. Android Studio 2026.2 incluye JBR 25, que no es
  compatible con Gradle 8.9; selecciona un **Gradle JDK 21** en
  *Settings → Build, Execution, Deployment → Build Tools → Gradle*.
- Android SDK 35, `minSdk` 26
- Gradle 8.9 (incluido en `gradlew`)

Abre la carpeta del proyecto en Android Studio, deja que sincronice Gradle y
ejecuta la configuración `app`. Desde consola:

```bash
./gradlew assembleDebug
```

## Estructura

```
app/src/main/java/com/enfoque/app/
├── EnfoqueApplication.kt          # arranque + canales de notificación
├── di/ContenedorApp.kt            # composición de dependencias
├── domain/
│   ├── model/                     # Tarea, TareaRecurrente, Ajustes, Racha
│   ├── repository/TareaRepository.kt
│   ├── repository/RachaRepository.kt
│   ├── calendario/                # contrato futuro de Google Calendar
│   └── temporizador/Temporizador.kt
├── data/
│   ├── local/                     # Room: entidades, DAOs, convertidores, mapeadores
│   ├── preferencias/              # diseño, tema y tiempos Pomodoro
│   ├── repository/                # TareaRepositoryImpl, RachaRepositoryImpl
│   ├── recordatorio/              # programador, receivers y reprogramación al reiniciar
│   ├── temporizador/              # temporizador simple, Pomodoro, alarmas y notificaciones
│   ├── alarma/Alarmas.kt          # alarmas exactas con degradación segura
│   └── calendario/                # implementación no-op del contrato
└── ui/
    ├── MainActivity.kt            # edge-to-edge, tema y ruta de configuración
    ├── configuracion/             # tema, paletas y tiempos Pomodoro
    ├── panel/                     # pantalla, ViewModel, estado, formularios
    ├── panel/componentes/         # tarjeta de tarea reutilizable
    ├── theme/                     # paletas Material 3 y tema dinámico
    └── util/Fechas.kt
```

## Modelo de datos

Entidad `TareaEntity` (tabla `tareas`):

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `Long` | autogenerado |
| `titulo` | `String` | obligatorio en el formulario |
| `descripcion` | `String` | vacío por defecto |
| `fechaVencimiento` | `LocalDate` | se guarda como *epoch day* |
| `estado` | `PENDIENTE / EN_PROGRESO / COMPLETADA` | se guarda como texto |
| `categoria` | `HOY / MANANA / BACKLOG` | define el panel |
| `duracionMinutos` | `Int?` | opcional; habilita el botón «Iniciar» |
| `horaRecordatorio` | `Int?` | minutos desde medianoche; `null` desactiva el aviso |
| `usarPomodoro` | `Boolean` | usa la sesión Pomodoro en lugar del temporizador simple |
| `recurrenteId` | `Long?` | rutina que generó la tarea, si aplica |
| `creadaEn` | `Long` | epoch millis, para ordenar |

Entidad `TareaRecurrenteEntity` (tabla `tareas_recurrentes`): plantilla con
`titulo`, `descripcion`, `duracionMinutos`, `horaRecordatorio`, `usarPomodoro`
y `creadaEn`.

Entidad `DiaCompletadoEntity` (tabla `dias_completados`): una fila por día con
al menos una tarea completada. La fecha es la clave primaria.

El dominio usa `Tarea`, `TareaRecurrente`, `AjustesApp` y `Racha`; las entidades
nunca llegan a la UI. El DAO observa las tablas con `Flow` y el repositorio mapea
entidad → dominio. La base está en la **versión 3** con migraciones explícitas
(`MIGRACION_1_2` y `MIGRACION_2_3`).

## Paneles

- **Hoy**: tareas de categoría `HOY` que no están completadas, con contador de
  progreso (completadas / total de hoy). Las completadas se ocultan para mantener
  el foco, pero «Ver completadas» permite reabrirlas, moverlas o eliminarlas.
- **Rutinas**: plantillas que se repiten (por ejemplo, «Estudiar inglés»). Cada
  rutina tiene dos selectores, **Hoy** y **Mañana**; al activarlos se crea una
  tarea normal en el panel correspondiente. Al desactivarlos se retira esa
  ocurrencia. La rutina sigue disponible todos los días.
- **Mañana**: tareas preparadas para el día siguiente, con acción rápida «Hoy».
- **Backlog**: tareas generales, con acción rápida «Mañana».
- El menú de cada tarjeta permite mover a cualquier panel o eliminar.
- La cabecera muestra la **racha** y el acceso a **Configuración**.

Al mover una tarea a `HOY` o `MANANA`, el repositorio reancla su fecha de
vencimiento a hoy o mañana respectivamente. Mover al `BACKLOG` conserva la fecha.

**Cambio de día:** el ViewModel revisa la fecha cada medianoche y al volver a la
app. Las tareas de `MANANA` cuya fecha ya llegó se promueven a `HOY` en la base
de datos, de modo que dejan de mostrarse como «Mañana» aunque la app siguiera
abierta.

## Recordatorios locales

- Cada tarea pendiente programa un aviso a la **hora elegida en el formulario**
  (09:00 por defecto). El interruptor «Recordatorio» lo desactiva por tarea.
- Si la hora ya pasó, no se programa nada.
- Completar o eliminar una tarea cancela su alarma.
- `BootReceiver` reprograma todo después de reiniciar, actualizar la app o
  cambiar la hora/zona horaria.
- Se usa `setExactAndAllowWhileIdle` cuando el permiso de alarmas exactas está
  concedido; si no, se degrada a `setAndAllowWhileIdle` (puede sonar con minutos
  de retraso). La app muestra un aviso con el botón **Activar** cuando el permiso
  está desactivado, porque los recordatorios y Pomodoro dependen de alarmas
  exactas.
- En Android 13+ el usuario debe conceder notificaciones; la app las solicita al
  abrir. Para alarmas exactas: *Ajustes → Apps → Acceso especial → Alarmas y
  recordatorios*.

## Temporizador simple

1. El formulario acepta una duración opcional (1–720 min). La tarjeta muestra
   el botón «Iniciar» solo si la tarea tiene duración.
2. Al pulsar, `TemporizadorAndroid` arranca `TemporizadorService` en primer plano.
3. El servicio:
   - publica una notificación persistente con cuenta regresiva,
   - programa una **alarma de reloj exacta** (`setAlarmClock`, vía
     `AlarmaTemporizador`) como respaldo,
   - recalcula el tiempo restante al despertar y solo notifica al cumplirse,
   - al terminar, emite una notificación por el canal `Fin de temporizador` con
     sonido de alarma y vibración.
4. Si el sistema destruye el servicio, la alarma despierta a `AlarmaReceiver`,
   que publica el mismo aviso. `SesionesTemporizador` garantiza que el aviso se
   emita una sola vez.
5. Solo hay una sesión activa: iniciar otro temporizador o un Pomodoro detiene
   la sesión anterior y cancela su alarma.

La notificación de fin abre la app al tocarla. La tarea pasa a `EN_PROGRESO`
mientras corre el bloque y vuelve a `PENDIENTE` al terminar; si la completaste
durante el bloque, se respeta el estado `COMPLETADA`.

## Pomodoro

- Se activa por tarea al crearla (interruptor **Modo Pomodoro**) o al iniciarla
  desde el menú de la tarjeta.
- Tiempos por defecto: **25 min de enfoque, 5 min de descanso corto y 15 min de
  descanso largo cada 4 bloques**. Se pueden cambiar en Configuración con los
  presets Clásico, Profundo y Suave, o con los controles de cada valor.
- `PomodoroService` mantiene la sesión en primer plano, encadena enfoque y
  descanso, y programa una **alarma exacta al final de cada fase** con
  `PomodoroAlarmas`.
- La notificación permite **Pausar/Reanudar**, **Saltar** o **Detener**. Al
  terminar una fase suena el canal `Pomodoro`.
- Los descansos pueden iniciarse solos; el siguiente enfoque se puede iniciar
  solo o esperar a que pulses **Continuar**. Ambas opciones se configuran.
- Completar la tarea detiene su sesión Pomodoro automáticamente.
- Si el proceso muere, `PomodoroAlarmaReceiver` avisa del final de la fase y
  limpia la sesión.

## Racha

- La racha cuenta **días consecutivos con al menos una tarea completada**.
- Cada día ganado se guarda en `dias_completados`; si completas otra tarea el
  mismo día, el registro se ignora.
- La cabecera muestra la racha actual, la mejor racha y si hoy está pendiente.
- Desmarcar una tarea no borra el día ganado; se mantiene el logro.

## Diseño y configuración

La pantalla **Configuración** (engranaje de la cabecera) permite:

- Elegir tema **Sistema**, **Claro** u **Oscuro**.
- Elegir entre cinco paletas: **Minimalista**, **Bosque**, **Océano**,
  **Atardecer** y **Monocromo**.
- Ajustar los tiempos de Pomodoro y el comportamiento de los descansos.

Todo se guarda en `SharedPreferences` y se aplica al instante mediante
`StateFlow`; el tema y las barras del sistema se actualizan sin reiniciar.

## Punto de extensión: Google Calendar

La UI depende de `TareaRepository` y `ContenedorApp` entrega la implementación.
Para la fase 2:

1. Implementa `CalendarioRepository` con la Calendar API (o el Calendar Provider).
2. Crea un repositorio compuesto (`TareaRepository` que lea Room y calendario, o
   un caso de uso de sincronización que escriba ambos).
3. Cámbialo en `ContenedorAppImpl`; `PanelScreen`, `PanelViewModel` y las tarjetas
   no necesitan modificarse.

## Decisiones de diseño

- **Un solo modelo de fecha** por tarea: categoría y vencimiento se mantienen
  coherentes al mover entre paneles.
- **Repositorio que agenda alarmas**: la UI no conoce `AlarmManager`; así se evita
  olvidar cancelar recordatorios al completar o borrar.
- **Servicio + alarma**: la alarma garantiza el aviso aunque el proceso muera; el
  servicio da la cuenta regresiva y precisión.
- **Sin Hilt en fase 1**: menos configuración y tiempos de build; el contenedor
  manual mantiene el mismo punto de inyección.
- **Strings en español dentro de Compose**: se puede extraer a `strings.xml`
  cuando se aborde la internacionalización.

## Próximos pasos sugeridos

- Sincronización bidireccional con Google Calendar y resolución de conflictos.
- Notificaciones con acción «Completar» y posponer.
- Edición de rutinas y patrones automáticos (diario, días laborables, etc.).
- Pruebas unitarias del repositorio y de los mapeos; pruebas de UI con Compose.
- Migrar a Hilt y usar `WorkManager` para sincronización periódica.
- Widget de Enfoque Diario y vista semanal.

## Versionado y GitHub

El proyecto usa Git desde la primera versión. `.gitignore` excluye artefactos de
build, `local.properties`, `.idea/` y claves; los esquemas de Room sí se versionan
porque documentan las migraciones.

```bash
git log --oneline
git tag v0.3.0
```

Para publicar una nueva versión: sube `versionCode` y `versionName` en
`app/build.gradle.kts`, compila, haz commit y crea una etiqueta (`v0.3.0`). El APK
de depuración queda en `app/build/outputs/apk/debug/app-debug.apk`.
