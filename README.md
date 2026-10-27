# Enfoque

Aplicación Android para planear y ejecutar tareas con dos paneles complementarios:
**Enfoque Diario** (lo que debe terminar antes de que acabe el día) y
**Planeación** (Mañana + Backlog). Incluye **Rutinas** reutilizables, persistencia
local con Room, recordatorios con `AlarmManager` y bloques de enfoque con
temporizador en un servicio en primer plano.

Esta es la **fase 1**: base funcional, sin conexión a Google Calendar, pero con
los puntos de extensión preparados para integrarla sin reescribir la UI.

## Stack

- Kotlin 2.0 + Jetpack Compose (Material 3)
- MVVM: `PanelViewModel` + `StateFlow` + estado inmutable
- Room (KSP) con modelo de dominio separado de la entidad
- Repositorios como única puerta de entrada a los datos
- `AlarmManager` para recordatorios y alarma de respaldo del temporizador
- Servicio en primer plano (`specialUse`) + notificaciones con canal de alarma
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
│   ├── model/                     # Tarea, TareaRecurrente, CategoriaTarea, EstadoTarea
│   ├── repository/TareaRepository.kt
│   ├── calendario/                # contrato futuro de Google Calendar
│   └── temporizador/Temporizador.kt
├── data/
│   ├── local/                     # Room: entidad, DAO, convertidores, mapeadores
│   ├── repository/                # TareaRepositoryImpl (Room + recordatorios)
│   ├── recordatorio/              # programador, receivers y reprogramación al reiniciar
│   ├── temporizador/              # servicio, alarma de respaldo, notificaciones
│   ├── alarma/Alarmas.kt          # alarmas exactas con degradación segura
│   └── calendario/                # implementación no-op del contrato
└── ui/
    ├── MainActivity.kt            # edge-to-edge + permiso de notificaciones
    ├── panel/                     # pantalla, ViewModel, estado, formularios
    ├── panel/componentes/         # tarjeta de tarea reutilizable
    ├── theme/                     # tema minimalista Material 3 claro/oscuro
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
| `recurrenteId` | `Long?` | rutina que generó la tarea, si aplica |
| `creadaEn` | `Long` | epoch millis, para ordenar |

Entidad `TareaRecurrenteEntity` (tabla `tareas_recurrentes`): plantilla con
`titulo`, `descripcion`, `duracionMinutos`, `horaRecordatorio` y `creadaEn`.

El dominio usa `Tarea` y `TareaRecurrente`; las entidades nunca llegan a la UI.
El DAO observa las tablas con `Flow` y el repositorio mapea entidad → dominio.
La base está en la **versión 2** con una migración explícita (`MIGRACION_1_2`).

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
  de retraso). La app muestra un aviso con el botón **Activar** cuando hay
  recordatorios pendientes y el permiso está desactivado.
- En Android 13+ el usuario debe conceder notificaciones; la app las solicita al
  abrir. Para alarmas exactas: *Ajustes → Apps → Acceso especial → Alarmas y
  recordatorios*.

## Temporizador de enfoque

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
5. Solo hay un temporizador activo: iniciar otro reemplaza al anterior y cancela
   su alarma.

La notificación de fin abre la app al tocarla. La tarea pasa a `EN_PROGRESO`
mientras corre el bloque y vuelve a `PENDIENTE` al terminar; si la completaste
durante el bloque, se respeta el estado `COMPLETADA`.

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
git tag v0.2.0
```

Para publicar una nueva versión: sube `versionCode` y `versionName` en
`app/build.gradle.kts`, compila, haz commit y crea una etiqueta (`v0.2.0`). El APK
de depuración queda en `app/build/outputs/apk/debug/app-debug.apk`.
