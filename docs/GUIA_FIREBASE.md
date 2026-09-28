# Guía paso a paso: crear el proyecto Firebase de IglesiaFlow

Esta guía es para hacerla **desde cero, sin saber programar**. Al terminar, todos los móviles y
tablets de la iglesia verán los mismos datos actualizados en 1-2 segundos.

Todo lo que usamos está en el **plan Spark (gratis, sin tarjeta de crédito)**.

Tiempo estimado: 20 minutos.

---

## Parte 1 — Crear el proyecto (5 min)

1. Entra en <https://console.firebase.google.com> con una cuenta de Google de la iglesia
   (recomendado crear una cuenta nueva tipo `iglesia.tuiglesia@gmail.com`, no la personal).
2. Pulsa **Crear un proyecto**.
3. Nombre del proyecto: `IglesiaFlow` → **Continuar**.
4. Google Analytics: puedes **desactivarlo** (no hace falta) → **Crear proyecto**.
5. Espera a que termine y pulsa **Continuar**.

## Parte 2 — Registrar la aplicación Android (5 min)

1. En la pantalla inicial del proyecto, pulsa el icono de **Android** (“Agrega una app”).
2. Rellena:
   - **Nombre del paquete de Android**: `com.iglesiaflow.gestion`
     ⚠️ Tiene que ser exactamente así, letra por letra, o la app no reconocerá el archivo.
   - **Sobrenombre de la app**: `IglesiaFlow` (opcional).
   - **Certificado SHA-1**: déjalo vacío (no hace falta para esto).
3. Pulsa **Registrar app**.
4. Pulsa **Descargar google-services.json**. Guarda ese archivo, es la “llave” de tu proyecto.
5. En los pasos siguientes (“Agrega el SDK de Firebase”, “Verificación”) pulsa **Siguiente** y al
   final **Continuar a la consola**. Esa parte ya está hecha en el código.

## Parte 3 — Activar la base de datos en tiempo real (5 min)

1. Menú izquierdo → **Compilación → Realtime Database** (⚠️ *Realtime Database*, **no** Firestore).
2. **Crear una base de datos**.
3. Ubicación: **Bélgica (europe-west1)** si estás en Europa, o **Estados Unidos (us-central1)**
   si estás en América. Cualquiera funciona.
4. Reglas de seguridad: elige **Iniciar en modo bloqueado** → **Habilitar**.
5. Abre la pestaña **Reglas**, borra lo que haya y pega esto exactamente:

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

6. Pulsa **Publicar**.

## Parte 4 — Activar el acceso anónimo (2 min)

Las reglas de arriba exigen que la app esté “identificada”. IglesiaFlow lo hace sola, en silencio,
con una sesión anónima (tus usuarios siguen entrando con el correo y contraseña de la propia app).

1. Menú izquierdo → **Compilación → Authentication** → **Comenzar**.
2. Pestaña **Sign-in method** → elige **Anónimo** → activa el interruptor → **Guardar**.

> Si te saltas este paso, la app no podrá guardar nada en la nube y verás un error de permisos en
> *Administración → Sincronización en la nube*.

## Parte 5 — Meter el archivo en la APK (5 min)

La APK necesita incluir tu `google-services.json`. Como la compilación se hace en GitHub, tienes
dos caminos. **El más fácil es el A.**

### Opción A — Pegar el archivo como “secreto” de GitHub (recomendado)

1. Abre el archivo `google-services.json` con el Bloc de notas y **copia todo su contenido**.
2. Ve al repositorio en GitHub → pestaña **Settings** → menú **Secrets and variables → Actions**.
3. Botón **New repository secret**:
   - **Name**: `GOOGLE_SERVICES_JSON`
   - **Secret**: pega el contenido completo del archivo.
   - **Add secret**.
4. Ve a la pestaña **Actions** → workflow **Android CI** → botón **Run workflow** → elige la rama
   `arena/01a0e4e0-iglesia-management-app` → **Run workflow**.
5. En ~10 minutos tendrás una **nueva release** con la APK ya conectada a tu Firebase
   (en el resumen del job verás “Firebase: **configurado**”).

### Opción B — Subir el archivo al repositorio

1. En GitHub, entra en la carpeta `app/` de la rama del proyecto.
2. **Add file → Upload files** y suelta ahí `google-services.json` (debe quedar como
   `app/google-services.json`).
3. **Commit changes**. La compilación arranca sola.

> Nota: el archivo no contiene contraseñas, pero sí identifica tu proyecto. Con las reglas de la
> Parte 3 solo pueden leer/escribir las apps autenticadas.

## Parte 6 — Encender la sincronización en los dispositivos (3 min)

1. Instala la APK nueva en **todos** los móviles/tablets.
2. En cada uno: entra como administrador → **Administración → Sincronización en la nube**.
3. Activa el interruptor **Sincronización en la nube**.
4. Escribe el mismo **código de iglesia** en todos (por defecto `principal`; puedes poner
   `betel-centro`, por ejemplo, pero tiene que ser idéntico en todos los aparatos).
5. Pulsa **Guardar código** y luego **Sincronizar ahora**.

El icono de nube de la barra superior te dirá el estado: sin conexión, sincronizando, al día, y el
número de cambios que faltan por subir.

---

## Comprobar que funciona

- En el móvil A crea un miembro nuevo. En el móvil B debe aparecer en 1-2 segundos.
- En la consola de Firebase → **Realtime Database → Datos** verás el árbol
  `churches / principal / members / ...`.
- Si no aparece nada, mira la pantalla de sincronización: muestra el error exacto.

| Síntoma | Causa habitual | Solución |
|---|---|---|
| “Firebase: sin google-services.json” | La APK se compiló sin el archivo | Repite la Parte 5 y reinstala la APK nueva |
| Error de permisos (*permission denied*) | Falta activar el acceso anónimo | Parte 4 |
| No se ven los datos del otro móvil | Código de iglesia distinto | Ponlo idéntico en todos |
| Todo “pendiente” y nada sube | Sin internet o reglas sin publicar | Revisa la conexión y pulsa **Publicar** en las reglas |

## ¿Cuánto me va a costar?

Nada. Con el plan Spark tienes 1 GB de datos guardados, 10 GB de descarga al mes y 100 conexiones
simultáneas. Una iglesia con 2.000 miembros, 15 dispositivos y 100 cambios al día usa alrededor del
3 % de esa cuota, porque la app solo descarga lo que cambió desde la última vez. Además el plan
Spark **no puede generar factura**: si algún día te pasaras del límite, el servicio se pausa hasta
el día siguiente, pero nunca te cobran.

## Datos que NO salen del teléfono

Usuarios, contraseñas, códigos 2FA y el registro de auditoría se quedan siempre en cada dispositivo.
A la nube solo viajan: familias, miembros, fondos, donaciones, eventos, asistencia, grupos,
integrantes, mensajes de grupo y peticiones de oración.
