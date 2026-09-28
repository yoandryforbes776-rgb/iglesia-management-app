# Configuración de Firebase · IglesiaFlow

Firebase es **opcional**. Sin credenciales la app funciona al 100 % en local (Room cifrado). Al
añadir `google-services.json` se habilitan: autenticación en la nube, réplica de datos en Firestore
y notificaciones push (FCM).

## 1. Crear el proyecto

1. Entra en <https://console.firebase.google.com> → **Agregar proyecto** (ej. `iglesiaflow`).
2. Desactiva Google Analytics si no lo necesitas.
3. En el proyecto: **Agregar app → Android**.
   * Nombre del paquete: `com.iglesiaflow.gestion`
     *(para la variante de depuración también `com.iglesiaflow.gestion.debug`)*.
   * Apodo: `IglesiaFlow`.
   * SHA-1: opcional, necesario solo para inicio de sesión con Google.
4. Descarga `google-services.json` y colócalo en `app/google-services.json`.

El `build.gradle.kts` aplica el plugin de Google Services **solo si ese archivo existe**, y expone
`BuildConfig.FIREBASE_CONFIGURED` para que el código sepa en qué modo está.

## 2. Servicios a habilitar

| Servicio | Uso en la app | Configuración |
|----------|---------------|---------------|
| **Authentication** | Inicio de sesión en la nube (además del local) | Habilita el proveedor *Correo electrónico/contraseña* |
| **Cloud Firestore** | Réplica de `members`, `donations`, `events` | Crea la base en *modo producción* y aplica las reglas de abajo |
| **Cloud Messaging** | Avisos de eventos, cumpleaños, check-in y oración | Nada extra; usa el token del dispositivo o el topic `iglesia` |
| **Storage** (opcional) | Fotos de miembros | Solo si decides externalizar las imágenes |

## 2 bis. Realtime Database: sincronización en tiempo real (recomendado)

> ¿Primera vez? Sigue la versión con capturas de pantalla mentales y clic a clic:
> [docs/GUIA_FIREBASE.md](GUIA_FIREBASE.md).

La app replica los datos entre dispositivos con **Realtime Database**, que en el plan Spark
incluye 1 GB almacenado, 10 GB/mes de descarga y 100 conexiones simultáneas: de sobra para una
iglesia. Pasos:

1. Firebase Console → **Realtime Database → Crear base de datos** (modo bloqueado).
2. Pega estas reglas (pestaña *Reglas*) y publica:

```json
{
  "rules": {
    "churches": {
      "$churchId": {
        ".read": "auth != null",
        ".write": "auth != null",
        "$collection": {
          ".indexOn": ["updatedAt"]
        }
      }
    }
  }
}
```

   Estas reglas exigen sesión iniciada: activa **Authentication → Sign-in method → Anónimo**.
   La app abre esa sesión anónima por su cuenta (el login de usuarios sigue siendo local).

   Si quieres probar rápido sin Authentication, puedes usar
   `".read": true, ".write": true` **solo durante las pruebas**: cualquiera con la URL podría leer
   los datos de la congregación.

3. En la app: *Administración → Sincronización en la nube* → activa el interruptor y escribe el
   mismo **código de iglesia** en todos los dispositivos (por defecto `principal`).

### Cómo funciona

| Aspecto | Comportamiento |
|---------|----------------|
| Bajada | Un listener por colección aplica los cambios en Room en 1-2 s; la UI se refresca sola |
| Subida | Los registros con `pendingSync = true` se envían en orden de dependencia |
| Identidad | Cada registro tiene un `remoteId` (UUID) que es su clave en la nube: dos móviles nunca se pisan |
| Borrados | Viajan como lápidas (`deleted: true`) para propagarse al resto |
| Conflictos | Gana la escritura con `updatedAt` mayor |
| Sin conexión | La caché en disco de RTDB + la cola local permiten trabajar offline y subir al reconectar |
| Coste | Solo se descargan los nodos con `updatedAt` posterior a la última sincronización |

Colecciones replicadas: `families`, `members`, `funds`, `groups`, `events`, `donations`,
`attendance`, `group_members`, `group_messages`, `prayers`. **No** se replican usuarios,
contraseñas, 2FA ni auditoría: son locales a cada dispositivo por seguridad.

## 3. Reglas de Firestore sugeridas

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function esUsuario() { return request.auth != null; }

    match /members/{doc}   { allow read, write: if esUsuario(); }
    match /donations/{doc} { allow read, write: if esUsuario(); }
    match /events/{doc}    { allow read: if esUsuario();
                             allow write: if esUsuario(); }
    match /{document=**}   { allow read, write: if false; }
  }
}
```

Recomendación: crea un usuario por cada persona con acceso y usa *custom claims* si necesitas
diferenciar roles también en la nube (los roles internos de la app ya se gestionan con RBAC local).

## 4. Activar la sincronización en la app

1. Instala la APK y entra como administrador.
2. **Administración → Seguridad y privacidad → Sincronización en la nube**: actívala.
3. La app sube en segundo plano (WorkManager) todos los registros con `pendingSync = true`.
4. Si Firebase no está inicializado, el interruptor no tiene efecto y todo sigue funcionando en local.

## 5. Notificaciones push

* Canal por defecto: `iglesiaflow_general`. El campo `data.channel` del mensaje permite dirigir el
  aviso a `iglesiaflow_events`, `iglesiaflow_checkin`, `iglesiaflow_birthdays` o `iglesiaflow_prayer`.
* Desde Firebase Console → *Messaging* puedes enviar a todos los dispositivos o al topic `iglesia`.
* En Android 13+ la app solicita `POST_NOTIFICATIONS` en el primer arranque.

## 6. Coste y privacidad

El plan *Spark* (gratuito) es suficiente para una iglesia media: 50 000 lecturas/día y 20 000
escrituras/día en Firestore. Ten en cuenta que los datos de la congregación son **datos personales**:
informa a los miembros, activa solo los servicios necesarios y revisa periódicamente la auditoría
(*Administración → Auditoría*).
