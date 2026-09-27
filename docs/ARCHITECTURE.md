# Arquitectura técnica · IglesiaFlow

## 1. Visión general

IglesiaFlow es una aplicación **Android nativa** (Kotlin + Jetpack Compose, Material 3) para la
gestión integral de una iglesia local, inspirada en ChurchCRM. Está diseñada como
**offline-first**: todo funciona sin conexión sobre una base de datos local cifrada y, de forma
opcional, replica los datos en Firebase cuando la iglesia configura sus credenciales.

```
┌──────────────────────────────────────────────────────────────┐
│                        UI (Compose)                          │
│  Screens + Components + Navigation + Theme (Material You)    │
└───────────────▲──────────────────────────────┬───────────────┘
                │ StateFlow<UiState>           │ eventos de usuario
┌───────────────┴──────────────────────────────▼───────────────┐
│                        ViewModels (MVVM)                     │
│    combinan flujos, aplican permisos (RBAC) y exportan       │
└───────────────▲──────────────────────────────┬───────────────┘
                │ Flow                          │ suspend fun
┌───────────────┴──────────────────────────────▼───────────────┐
│                        Repositorios                          │
│  Member · Finance · Event · Group · Volunteer · Communication │
│  Admin · Backup · CsvImporter                                │
└───────▲────────────────────────────┬─────────────────────────┘
        │                            │
┌───────┴─────────────┐   ┌──────────▼───────────────┐
│ Room + SQLCipher    │   │ Firebase (opcional)      │
│ 25 entidades, 7 DAO │   │ Auth · Firestore · FCM   │
└─────────────────────┘   └──────────────────────────┘
```

## 2. Capas

| Capa | Paquete | Responsabilidad |
|------|---------|-----------------|
| Presentación | `ui/screens/**`, `ui/components/**`, `ui/navigation/**`, `ui/theme/**` | Composables sin lógica de negocio; reciben un `UiState` inmutable y emiten acciones. |
| Presentación (estado) | `ui/screens/**/*ViewModel.kt` | `@HiltViewModel`, combinan `Flow`s de repositorios en un `StateFlow<UiState>` con `SharingStarted.WhileSubscribed(5s)`. Comprueban permisos con `SessionManager`. |
| Dominio | `domain/model/**` | Enumerados del negocio (roles, permisos, tipos de evento, estados…) y agregados de presentación. |
| Datos | `data/repository/**` | Orquestan DAOs, auditoría, sellado de campos (`createdBy`, `updatedAt`, `pendingSync`) y reglas de negocio (matching de voluntarios, recurrencias, check-in). |
| Persistencia | `data/local/**` | Room: entidades, DAOs, `Converters`, `AppDatabase`, `DatabaseSeeder`. |
| Remoto | `data/remote/**` | `FirebaseGateway`, `SyncManager` (push de pendientes) y `IglesiaMessagingService` (FCM). |
| Transversal | `core/**` | Seguridad (hash de contraseñas, TOTP, RBAC, sesión, SQLCipher), configuración en caliente, auditoría, utilidades (fechas, formatos, export CSV/PDF, notificaciones), idioma. |
| Inyección | `di/DatabaseModule.kt`, `IglesiaFlowApp` | Hilt: base de datos, DAOs y singletons. |

> Se ha optado por una **Clean Architecture pragmática**: las entidades Room se usan también como
> modelo de dominio para evitar una capa de mappers que, en una app CRUD de este tamaño,
> multiplicaría el código sin aportar valor. Las reglas de negocio viven en los repositorios.

## 3. Módulos funcionales

1. **Miembros y familias** – perfiles completos, familias, campos personalizados, notas/timeline, fotos, export CSV/PDF.
2. **Finanzas** – donaciones (diezmos/ofrendas), fondos, promesas de fe con progreso, sobres numerados, depósitos bancarios, gastos, reportes.
3. **Eventos y asistencia** – calendario, recurrencias (diaria/semanal/quincenal/mensual), lista de asistencia, check-in/out infantil con código de seguridad y modo kiosco.
4. **Grupos y ministerios** – grupos con líder, integrantes con rol y muro de mensajes interno.
5. **Voluntariado** – habilidades y disponibilidad, necesidades por ministerio, *matching* automático y registro de horas de servicio.
6. **Comunicación** – campañas de email/SMS/push, audiencias segmentadas, peticiones de oración con moderación.
7. **Reportes y analítica** – directorio, tendencias de ingresos y asistencia, mayores contribuyentes y **constructor de consultas ad-hoc** exportable.
8. **Administración** – usuarios, RBAC granular, 2FA (TOTP), campos personalizados, apariencia/idioma, backup/restauración, import/export CSV y auditoría.

## 4. Configuración en caliente (sin recompilar)

`core/config/SettingsRepository` persiste `AppSettings` en **DataStore** y lo expone como `Flow`.
Se puede cambiar en runtime desde *Administración*:

* nombre, lema y logo de la iglesia;
* color primario/secundario y Material You (color dinámico), modo claro/oscuro/sistema;
* idioma (es/en/pt o el del sistema) mediante `AppCompatDelegate.setApplicationLocales`;
* moneda, módulos visibles, notificaciones por categoría;
* exigencia de 2FA, tiempo de expiración de sesión, sincronización en la nube, modo kiosco;
* campos personalizados por entidad (miembro, familia, evento, grupo);
* matriz de permisos por rol.

## 5. Seguridad

| Control | Implementación |
|---------|----------------|
| Contraseñas | PBKDF2 con sal aleatoria por usuario (`core/security/PasswordHasher`). |
| 2FA | TOTP RFC 6238 con `javax.crypto` (`TotpGenerator`), secreto por usuario. |
| Cifrado en reposo | Room sobre **SQLCipher**; la passphrase se genera al vuelo y se guarda en `EncryptedSharedPreferences` (`CryptoManager`). |
| RBAC | `Permission` (21 permisos) × `UserRole` (6 roles) con matriz por defecto (`Rbac`) y anulaciones persistidas en `role_permissions`. |
| Auditoría | `AuditLogger` escribe en `audit_log` cada acción relevante (quién, qué, cuándo, detalle). |
| Sesión | `SessionManager` con expiración configurable por inactividad. |
| Transporte | TLS obligatorio (`usesCleartextTraffic=false`); Firebase usa HTTPS/gRPC cifrado. |
| Privacidad | Consentimiento de contacto por miembro (`allowsContact`), peticiones de oración privadas, backups locales y exportaciones compartidas vía `FileProvider`. |

## 6. Offline-first y sincronización

* Toda escritura marca `pendingSync = true`.
* `SyncManager` (WorkManager) empuja a Firestore las colecciones `members`, `donations` y `events`
  cuando `cloudSyncEnabled` está activo y Firebase está inicializado.
* Si no hay `google-services.json`, `FirebaseGateway.isAvailable` es `false` y la app degrada a
  modo 100 % local sin errores.

## 7. Compilación y entrega

* Gradle 8.9, AGP 8.5.2, Kotlin 2.0.20, KSP, Hilt 2.52, Compose BOM 2024.09.02.
* `minSdk 26`, `targetSdk 34`, Java 17 con *core library desugaring*.
* El workflow `.github/workflows/android.yml` genera el wrapper, crea un keystore, compila
  `assembleDebug` y `assembleRelease` (R8 + shrink) y publica las APKs como artefacto y como
  *GitHub Release*.

## 8. Estructura de carpetas

```
app/src/main/java/com/iglesiaflow/gestion/
├── core/            # seguridad, configuración, auditoría, utilidades, idioma
├── data/
│   ├── local/       # entidades, DAOs, AppDatabase, seeder, converters
│   ├── remote/      # Firebase gateway, sync, FCM
│   └── repository/  # 9 repositorios + backup + import CSV
├── di/              # módulos Hilt
├── domain/model/    # enums y modelos de dominio
├── ui/
│   ├── components/  # tarjetas, formularios, gráficos, tablas
│   ├── navigation/  # destinos, NavGraph y shell (bottom bar + drawer)
│   ├── screens/     # 8 módulos funcionales
│   └── theme/       # Material You + esquemas propios
├── IglesiaFlowApp.kt
└── MainActivity.kt
```
