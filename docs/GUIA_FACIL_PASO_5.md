# Pasos 5 y 6 explicados muy fácil

Ya hiciste lo difícil (crear el proyecto, la base de datos y el acceso anónimo).
Ahora falta: **meter la llave dentro de la app** y **encender la sincronización**.

Piénsalo así: Firebase es una casa que ya construiste. El archivo `google-services.json` es la
**llave** de esa casa. La app todavía no tiene la llave en el bolsillo. Vamos a ponérsela.

---

> ⚠️ **Antes de empezar**: si descargaste `google-services.json` *antes* de crear la Realtime
> Database, ese archivo no trae la dirección de la base de datos. Descárgalo otra vez desde
> Firebase → ⚙️ **Configuración del proyecto** → abajo, en **Tus apps**, botón
> **google-services.json**. Con el archivo nuevo, repite el paso 5.2 (edita el secreto que ya
> creaste y pega el contenido nuevo).

## PASO 5 — Darle la llave a la app

### 5.1 Abre la llave y cópiala

1. Busca en tu computadora el archivo que descargaste: **`google-services.json`**
   (suele estar en la carpeta *Descargas*).
2. Haz **clic derecho** encima → **Abrir con** → **Bloc de notas**.
3. Verás un montón de texto raro con llaves `{ }`. No importa lo que diga.
4. Pulsa las teclas **Ctrl + E** (o Ctrl + A) para seleccionarlo todo, y luego **Ctrl + C** para copiarlo.

> Si lo abres y no se ve nada, es que abriste el archivo equivocado. Debe pesar unos 1-2 KB y
> dentro tiene que aparecer la palabra `com.iglesiaflow.gestion`.

### 5.2 Guarda la llave en GitHub (en la caja fuerte)

1. Entra en:
   <https://github.com/yoandryforbes776-rgb/iglesia-management-app>
2. Arriba, en la fila de pestañas (Code, Issues, Pull requests…), pulsa **Settings** (⚙️ Configuración).
   Es la última pestaña de la derecha.
3. En la lista de la izquierda baja hasta **Secrets and variables** y pulsa encima.
4. Se abre un submenú: pulsa **Actions**.
5. Botón verde arriba a la derecha: **New repository secret**.
6. Rellena solo dos cosas:
   - **Name**: escribe exactamente → `GOOGLE_SERVICES_JSON`
     (todo en mayúsculas y con guiones bajos, sin espacios)
   - **Secret**: pulsa dentro del recuadro grande y pega con **Ctrl + V** todo el texto que copiaste.
7. Pulsa **Add secret**.

Listo: la llave está guardada y nadie más puede verla.

### 5.3 Pide una app nueva con la llave dentro

1. Vuelve arriba y pulsa la pestaña **Actions**.
2. En la columna de la izquierda pulsa **Android CI**.
3. A la derecha aparece un botón gris: **Run workflow**. Pulsa ahí.
4. Se abre una cajita:
   - En **Use workflow from** elige la rama `arena/01a0e4e0-iglesia-management-app`.
   - Pulsa el botón verde **Run workflow**.
5. Espera unos **10 minutos**. Verás una bolita amarilla girando 🟡; cuando se ponga verde ✅ ya está.

### 5.4 Descarga la app nueva

1. Vuelve a la pestaña **Code** del repositorio.
2. A la derecha, en la columna, busca **Releases** y pulsa en la de arriba del todo
   (la que dice **Latest**, por ejemplo *IglesiaFlow 1.0.0 (build 9)*).
3. Abajo, en **Assets**, pulsa **`IglesiaFlow-release.apk`** para descargarla.

> Truco: puedes hacer todo esto **desde el propio teléfono** con el navegador, así la APK ya te
> queda descargada en el móvil y no tienes que pasarla por cable.

### 5.5 Instálala en el teléfono

1. Abre el archivo `IglesiaFlow-release.apk` en el teléfono (Archivos → Descargas).
2. Android te dirá *"Por seguridad no puedes instalar apps de este origen"* → pulsa
   **Configuración** → activa **Permitir de esta fuente** → vuelve atrás → **Instalar**.
3. Si ya tenías la app instalada, déjala actualizar encima (o desinstálala antes si te da error).

---

## PASO 6 — Encender la sincronización

Haz esto **en cada teléfono o tablet** que vaya a usar la iglesia:

1. Abre IglesiaFlow y entra con el usuario administrador
   (`admin@iglesia.org` / `admin123` si no lo has cambiado).
2. Menú → **Administración**.
3. Pulsa **Sincronización en la nube**.
4. Enciende el interruptor **Sincronización en la nube**.
5. En **Código de la iglesia** deja `principal` (o escribe otro nombre, pero tiene que ser
   **exactamente el mismo en todos los aparatos**) y pulsa **Guardar código**.
6. Pulsa **Sincronizar ahora**.

Arriba en la pantalla debe decir: *Firebase: configurado* y *Conexión: en línea*.

---

## Cómo saber que funciona

- En el teléfono A crea un miembro nuevo (por ejemplo "Prueba Uno").
- En el teléfono B, en 1 o 2 segundos, debe aparecer "Prueba Uno" solo, sin tocar nada.
- En Firebase → **Realtime Database → Datos** verás una carpetita
  `churches` → `principal` → `members` con la información dentro.

## Si algo sale mal

| Lo que ves | Qué pasa | Qué hacer |
|---|---|---|
| "Firebase: sin google-services.json" | Instalaste una APK vieja | Descarga la última release (la que dice *Latest*) y reinstala |
| "Error: permission denied" | Falta el acceso anónimo | Firebase → Authentication → Sign-in method → **Anónimo** activado |
| El otro móvil no ve nada | Códigos de iglesia distintos | Escribe el mismo código en los dos y pulsa Guardar |
| Dice "3 pendientes" y no baja | Sin internet | Conecta el wifi y pulsa Sincronizar ahora |
| "Conexión: sin conexión" aun con wifi | Al archivo le falta la dirección de la base de datos | Copia la URL que aparece arriba en Firebase → Realtime Database (algo como `https://iglesia-flow-default-rtdb.firebaseio.com`) y pégala en el campo **URL de la base de datos (opcional)** de la app → **Guardar URL** |
