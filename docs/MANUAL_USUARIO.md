# Manual de usuario · IglesiaFlow

## 1. Primeros pasos

1. Instala `IglesiaFlow-release.apk` (permite "instalar apps de orígenes desconocidos" la primera vez).
2. Abre la app e inicia sesión con una de las cuentas de demostración:

   | Usuario | Contraseña | Rol |
   |---------|-----------|-----|
   | `admin@iglesia.org` | `admin123` | Administrador (acceso total) |
   | `pastor@iglesia.org` | `pastor123` | Pastor |
   | `tesorero@iglesia.org` | `tesoro123` | Tesorero |
   | `lider@iglesia.org` | `lider123` | Líder de ministerio |

3. **Cambia las contraseñas** en *Administración → Usuarios* antes de usarla de verdad.
4. Personaliza la iglesia en *Administración → Apariencia e idioma* (nombre, logo, colores, idioma, moneda).

La barra inferior da acceso a Inicio, Miembros, Finanzas y Eventos; el resto de módulos están en el
menú lateral (icono ☰).

## 2. Inicio (dashboard)

Muestra miembros activos, ingresos del mes, asistencia media, próximos eventos, cumpleaños del mes
y accesos rápidos. Toca cualquier tarjeta para ir al módulo correspondiente.

## 3. Miembros y familias

* **Buscar**: escribe nombre, teléfono o email en el buscador superior.
* **Nuevo miembro**: botón ➕ → completa datos personales, estado, familia y campos personalizados.
* **Ficha del miembro**: datos de contacto, familia, donaciones, grupos, notas pastorales (timeline)
  y botones para llamar, enviar email o SMS.
* **Familias**: pestaña *Familias*; agrupa miembros con un cabeza de familia y datos comunes.
* **Exportar**: menú ⋮ → CSV (hoja de cálculo) o PDF (directorio impreso).


## 5. Eventos y asistencia

* **Crear evento**: título, tipo, lugar, fecha, hora, duración y recurrencia
  (ninguna/diaria/semanal/quincenal/mensual). Con la recurrencia activada puedes generar de golpe
  las 8 próximas ocurrencias.
* **Ficha del evento**: marca la asistencia con las casillas; el contador se actualiza al momento y
  puedes exportar la lista a CSV.
* **Check-in infantil**: activa *Requiere check-in* en el evento. Desde *Eventos → Puesto de
  check-in* (modo kiosco):
  1. Selecciona el evento.
  2. Introduce el nombre del niño, tutor, teléfono, sala y alergias.
  3. La app genera un **código de seguridad de 4 dígitos** y muestra el ticket; opcionalmente envía
     un SMS al tutor.
  4. Para la salida, busca al niño en "Niños en sala" e introduce el código. Sin código correcto no
     se permite el check-out.

## 6. Grupos y ministerios

* Crea células, comités, equipos de alabanza o escuelas dominicales con líder, día y hora.
* En la ficha del grupo: añade o quita integrantes con su rol (líder, co-líder, miembro,
  participante) y publica mensajes en el muro interno del grupo.

## 7. Voluntariado

1. **Voluntarios** – registra habilidades (música, sonido, cocina, niños…), nivel y disponibilidad.
2. **Necesidades** – cada ministerio declara qué habilidad necesita, cuántas plazas y qué día.
3. **Matching** – la app propone automáticamente los voluntarios compatibles, ordenados por nivel.
4. **Horas** – registra horas de servicio; verás el total anual y el ranking de servidores.

## 8. Comunicación

* **Campañas**: crea un mensaje, elige canal (email, SMS o notificación push) y audiencia (toda la
  iglesia, solo activos o líderes). Al pulsar *Enviar* se abre tu app de correo/mensajería con los
  destinatarios ya cargados, y la campaña queda registrada con el número de destinatarios.
  Solo se incluyen los miembros que han dado su consentimiento de contacto.
* **Peticiones de oración**: cualquiera puede añadir una petición (pública o privada). Toca el
  corazón para indicar que has orado. Los moderadores pueden marcarla como respondida añadiendo
  el testimonio.

## 9. Reportes y analítica

* **Resumen**: métricas globales, tendencia de ingresos y de asistencia.
* **Directorio**: tabla completa de miembros.
* **Finanzas**: ingresos por fondo y mayores contribuyentes; exportable a PDF.
* **Asistencia**: asistencia por evento y evolución mensual; exportable a CSV.
* **Consultas**: constructor ad-hoc → elige entidad (miembros, donaciones, eventos), campo,
  condición (contiene, igual, mayor, menor) y valor. El resultado se muestra en tabla y se exporta
  a CSV.

## 10. Administración

| Sección | Para qué sirve |
|---------|----------------|
| Usuarios | Altas, roles, activación/desactivación, cambio de contraseña y 2FA (TOTP) |
| Roles y permisos | Matriz RBAC: marca qué puede hacer cada rol; "Restablecer" vuelve a los valores por defecto |
| Campos personalizados | Añade campos a miembros, familias, eventos o grupos sin actualizar la app |
| Apariencia e idioma | Nombre/lema/logo, color primario y secundario, Material You, tema claro/oscuro, idioma (es/en/pt), moneda y expiración de sesión |
| Backup e importación | Crea, comparte, importa y restaura copias de la base de datos; importa/exporta miembros en CSV |
| Auditoría | Registro de toda la actividad; exportable y purgable |

Desde la pantalla principal de Administración también se activan o desactivan **módulos visibles**,
las **notificaciones** por categoría y las opciones de **seguridad y privacidad**.

## 11. Preguntas frecuentes

**¿Funciona sin internet?** Sí. Todos los datos se guardan cifrados en el dispositivo; la nube es
opcional.

**¿Cómo activo la doble verificación?** *Administración → Usuarios → interruptor 2FA*. La app
muestra un secreto que debes escanear/copiar en Google Authenticator o similar.

**¿Dónde se guardan las exportaciones?** En el almacenamiento interno de la app y se comparten con
el selector de Android (correo, Drive, WhatsApp…).

**¿Puedo recuperar datos borrados?** Solo desde una copia de seguridad previa
(*Administración → Backup*). Se recomienda crear una copia semanal.

**¿Cómo cambio el idioma?** *Administración → Apariencia e idioma → Idioma*. El cambio es inmediato.

## Asistencia (módulo nuevo)

1. Abre **Asistencia** en la barra inferior.
2. Arriba verás el bloque **Seguimiento pastoral**: los miembros que han faltado **2 o más veces
   seguidas** a los últimos cultos. Toca a cualquiera para ver su historial.
3. Debajo aparece la lista de miembros con su porcentaje de asistencia a los últimos 8 eventos.
4. Al entrar en un miembro verás un **calendario del mes**: los días en verde son los eventos a los
   que asistió y los rojos aquellos a los que faltó. Usa las flechas para cambiar de mes.
5. La app avisa con una **notificación a los líderes** (pastor, administrador y líderes de
   ministerio) cuando alguien acumula 2 faltas seguidas. Se puede desactivar en
   *Administración → Notificaciones → Ausencias reiteradas*.

La asistencia se marca como siempre desde **Eventos → abrir el evento → lista de asistentes**.

## Seguridad de la sesión

La sesión se cierra sola tras **30 minutos sin usar la app**. Cualquier toque en la pantalla
reinicia la cuenta atrás, así que si sigues trabajando nunca te echará fuera. Al volver verás el
aviso "Tu sesión se cerró automáticamente" en la pantalla de acceso.

## Mensajes del muro de un grupo

En **Grupos → abrir un grupo**, cada mensaje del muro tiene una papelera a la derecha (solo para
quien puede editar grupos). Al borrarlo desaparece también en el resto de dispositivos.
