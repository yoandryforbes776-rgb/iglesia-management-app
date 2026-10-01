# IglesiaFlow · Gestión y control para iglesias

Aplicación **Android nativa** (Kotlin + Jetpack Compose, Material Design 3) para la administración
integral de una iglesia local, inspirada en ChurchCRM. Funciona **offline-first** sobre una base de
datos cifrada y, opcionalmente, se sincroniza con Firebase.

<p align="center">
  <b>8 módulos · 25 tablas · RBAC granular · Material You · Español / English / Português</b>
</p>

## ✨ Módulos

| # | Módulo | Qué incluye |
|---|--------|-------------|
| 1 | **Miembros y familias** | Perfiles completos, familias, campos personalizados, notas y timeline pastoral, export CSV/PDF |
| 2 | **Finanzas** | Diezmos y ofrendas, fondos, promesas de fe con progreso, sobres numerados, depósitos bancarios, gastos y reportes |
| 3 | **Eventos y asistencia** | Calendario con recurrencias, lista de asistencia, check-in/out infantil con código de seguridad y modo kiosco |
| 4 | **Grupos y ministerios** | Células y equipos con líder, integrantes por rol y muro de mensajes interno |
| 5 | **Voluntariado** | Habilidades y disponibilidad, necesidades por ministerio, *matching* automático y registro de horas |
| 6 | **Comunicación** | Campañas de email/SMS/push segmentadas y peticiones de oración con moderación |
| 7 | **Reportes y analítica** | Directorio, tendencias de ingresos y asistencia, top donantes y constructor de consultas ad-hoc |
| 8 | **Administración** | Usuarios, RBAC granular, 2FA (TOTP), campos personalizados, apariencia/idioma, backup/restauración, import/export CSV y auditoría |

## 🎨 Configurable sin recompilar

Nombre, lema y logo de la iglesia · colores primario y secundario · Material You (color dinámico) ·
tema claro/oscuro/sistema · idioma (es/en/pt) · moneda · módulos visibles · notificaciones por
categoría · campos personalizados · matriz de permisos · expiración de sesión · modo kiosco.

## 🔐 Seguridad

* Contraseñas con PBKDF2 + sal por usuario.
* **2FA TOTP** (RFC 6238) para administradores.
* Base de datos **Room cifrada con SQLCipher**; passphrase en `EncryptedSharedPreferences`.
* **RBAC** con 21 permisos × 6 roles, reconfigurable en caliente.
* **Auditoría** completa de acciones y consentimiento de contacto por miembro (RGPD).
* TLS obligatorio; sin tráfico en claro.

## 🏗️ Stack

Kotlin 2.0.20 · Jetpack Compose (BOM 2024.09.02) · Material 3 · Hilt 2.52 · Room 2.6.1 + SQLCipher ·
DataStore · WorkManager · Navigation Compose · Coil · Firebase (Auth, Firestore, FCM) *opcional* ·
AGP 8.5.2 / Gradle 8.9 · minSdk 26, targetSdk 34.

## 📦 Descargar la APK

Las APKs firmadas se generan automáticamente en GitHub Actions y se publican en
**[Releases](../../releases)** (`IglesiaFlow-release.apk`). También quedan disponibles como
artefacto de cada ejecución del workflow *Android CI*.

La app abre directamente como **Administrador**: no pide usuario ni contraseña
(ver [docs/MANUAL_USUARIO.md](docs/MANUAL_USUARIO.md)).

## 🛠️ Compilar en local

```bash
git clone https://github.com/yoandryforbes776-rgb/iglesia-management-app.git
cd iglesia-management-app

# Requisitos: JDK 17 y Android SDK 34
gradle wrapper --gradle-version 8.9     # solo la primera vez
./gradlew :app:assembleDebug            # APK de depuración
./gradlew :app:assembleRelease          # APK firmada (ver más abajo)
```

Firma de la release: crea `keystore.properties` en la raíz

```properties
storeFile=/ruta/al/keystore.jks
storePassword=****
keyAlias=iglesiaflow
keyPassword=****
```

o define las variables de entorno `SIGNING_STORE_FILE`, `SIGNING_STORE_PASSWORD`,
`SIGNING_KEY_ALIAS` y `SIGNING_KEY_PASSWORD` (es lo que hace el workflow de CI).

## ☁️ Firebase (opcional)

Coloca tu `google-services.json` en `app/` y activa *Sincronización en la nube* en Administración.
Sin ese archivo la app compila y funciona 100 % offline. Guía completa en
[docs/FIREBASE.md](docs/FIREBASE.md).

## 📚 Documentación

| Documento | Contenido |
|-----------|-----------|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Arquitectura MVVM + Clean, capas, seguridad y build |
| [docs/DATABASE.md](docs/DATABASE.md) | Esquema completo de las 25 tablas y consultas analíticas |
| [docs/API.md](docs/API.md) | API interna (repositorios), colecciones Firestore e integraciones |
| [docs/FIREBASE.md](docs/FIREBASE.md) | Configuración paso a paso de Firebase |
| [docs/MANUAL_USUARIO.md](docs/MANUAL_USUARIO.md) | Manual de usuario de los 8 módulos |

## 🗂️ Estructura del repositorio

```
app/                     # aplicación Android (Kotlin + Compose)
docs/                    # documentación técnica y manual de usuario
web-admin/               # panel de administración web opcional (React + TS + Vite + MUI)
gradle/libs.versions.toml# catálogo de versiones
.github/workflows/       # compilación y publicación automática de la APK
```

## 📄 Licencia

Proyecto desarrollado a medida para la gestión de iglesias. Úsalo y adáptalo libremente en tu
congregación.
