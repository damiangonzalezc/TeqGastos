# TeqGastos – Rendición de Gastos en Terreno

App Android para **TEQMED SpA** (servicio técnico de equipamiento médico, Región del Biobío).
Los técnicos registran sus gastos de terreno en el momento, con la foto del comprobante;
la jefatura los aprueba, observa o rechaza desde su teléfono, y Finanzas registra el pago y exporta al sistema contable.

**Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles · Evaluación Parcial 2

**Integrantes:**
- _(Nombre integrante 1)_
- _(Nombre integrante 2)_

## Funcionalidades implementadas

| Funcionalidad | Detalle |
|---|---|
| Login con rol | Correo corporativo `@teqmed.cl`, contraseña (mín. 6, con un número) y rol: Técnico, Jefatura o Finanzas. La sesión queda guardada. |
| Bandeja por rol | El técnico ve sus gastos, la jefatura los del equipo y Finanzas los aprobados/pagados. Resumen de montos y filtros por estado con contador. |
| Registro de gasto | Categoría, medio de pago, monto, fecha (con calendario), proveedor, N° de boleta, **foto del comprobante** y **ubicación GPS**. Barra de avance del formulario. |
| Reglas de negocio | Tope por categoría, plazo máximo de 30 días, fecha no futura, foto obligatoria y **bloqueo de comprobante duplicado**. |
| Flujo de aprobación | Pendiente → Aprobado / Observado / Rechazado → Pagado. Observar o rechazar exige un motivo; rechazar y pagar piden confirmación. |
| Corrección de observados | El técnico corrige su gasto observado y lo reenvía a revisión. |
| Historial (auditoría) | Cada cambio de estado queda registrado con usuario, fecha y comentario. |
| Exportación ERP | Finanzas exporta los gastos aprobados/pagados a CSV y lo comparte (correo, Drive, etc.). |
| Ajustes | Perfil, tema **Claro / Oscuro / Sistema** y leyenda de estados. |

Topes referenciales por comprobante: Combustible $60.000 · Peaje $15.000 · Estacionamiento $10.000 · Alimentación $12.000 · Alojamiento $70.000 · Insumos $50.000.

## Diseño visual

- **Tema claro y oscuro** completos con roles de Material 3, elegibles desde Ajustes.
- **Paleta por estado**, con versión clara y oscura que cumple contraste WCAG AA:

| Estado | Color | Significado |
|---|---|---|
| Pendiente | Azul | Esperando revisión |
| Aprobado | Verde | Aprobado, pendiente de pago |
| Observado | Ámbar | Requiere corrección |
| Rechazado | Rojo | No se reembolsa |
| Pagado | Violeta | Pago registrado |

Cada estado tiene además su propio ícono, para que el color no sea la única señal.

## Tecnologías

Kotlin · Jetpack Compose (Material 3) · Navigation Compose · ViewModel + StateFlow · Room · Coil · JUnit

## Arquitectura (MVVM)

```
app/src/main/java/cl/duoc/teqgastos/
├── domain/        Modelos y reglas puras: GastoValidator, LoginValidator,
│                  FlujoAprobacion (máquina de estados) y ErpExporter
├── data/
│   ├── local/     Room: entidades (gastos, historial), DAO y base de datos
│   ├── repository/GastoRepository (única fuente de datos para los ViewModels)
│   ├── session/   SessionManager (sesión)
│   └── settings/  SettingsRepository (tema)
├── ui/
│   ├── components/Campos validados, chips y banners de estado, animación de sacudida
│   ├── navigation/Rutas y NavHost con transiciones
│   ├── screens/   login · home · form · detail · settings (Screen + ViewModel)
│   └── theme/     Colores claro/oscuro, paleta de estados y tema
└── util/          Cámara, ubicación y compartir archivo
```

- **Validaciones desacopladas:** las reglas viven en `domain/` y las usa el ViewModel; las pantallas solo muestran el estado. Las acciones permitidas a cada rol salen de `FlujoAprobacion`, no de la UI.
- **Persistencia local:** Room guarda gastos e historial sin conexión. Un índice único (proveedor + N° comprobante) impide duplicados también a nivel de base de datos. Gasto e historial se guardan en una misma transacción.
- **Tests unitarios:** `GastoValidatorTest` y `FlujoAprobacionTest`.

## Recursos nativos

1. **Cámara:** foto del comprobante, con permiso en tiempo de ejecución. Se guarda en el almacenamiento privado de la app.
2. **GPS:** ubicación del gasto, con permiso en tiempo de ejecución. Se puede ver luego en la app de mapas.
3. Extras: calendario del sistema, vibración háptica al fallar una validación y compartir el CSV de exportación.

## Animaciones

Transiciones deslizantes entre pantallas · logo con rebote en el login · mensajes de error animados · botón que se sacude si hay errores · barra de avance del formulario · lista con animación de ítems · montos que cuentan hasta su valor · cambio de color animado del estado · banners y paneles que aparecen/desaparecen.

## Cómo ejecutar

1. Abrir la carpeta del proyecto en **Android Studio** (Ladybug o superior) y esperar la sincronización de Gradle.
2. Ejecutar en un emulador o teléfono con **Android 8.0 (API 26)** o superior.
3. Ingresar con cualquier correo `@teqmed.cl` y una contraseña con número (prototipo sin servidor). Recorrido sugerido:
   1. **Técnico** (`tecnico@teqmed.cl`): registrar un gasto con foto y ubicación.
   2. **Jefatura** (`jefe@teqmed.cl`): observarlo con un motivo.
   3. **Técnico**: corregirlo y reenviarlo.
   4. **Jefatura**: aprobarlo.
   5. **Finanzas** (`finanzas@teqmed.cl`): registrar el pago y exportar a ERP.
4. En el emulador, la ubicación se simula desde *Extended controls → Location*.

Tests unitarios: `./gradlew test`

## Pendiente para la EFT

Sincronización con servidor, conversión de moneda/UF con API de indicadores e importación de traslados desde TeqRuta.
