# Esquema de base de datos · IglesiaFlow

Motor: **Room 2.6.1 sobre SQLCipher** (`iglesiaflow.db`, versión 1, `exportSchema = true` →
`app/schemas/`). Todas las claves primarias son `INTEGER` autoincrementales. Los enums se
persisten como texto mediante `Converters`.

## 1. Mapa de tablas (25)

| Módulo | Tablas |
|--------|--------|
| Miembros | `members`, `families`, `custom_field_defs`, `custom_field_values`, `notes` |
| Finanzas | `funds`, `donations`, `pledges`, `envelopes`, `deposits`, `expenses` |
| Eventos | `events`, `attendance`, `check_ins` |
| Grupos | `church_groups`, `group_members`, `group_messages` |
| Voluntariado | `volunteer_skills`, `ministry_needs`, `service_records` |
| Comunicación | `campaigns`, `prayer_requests` |
| Administración | `users`, `role_permissions`, `audit_log` |

> La tabla de grupos se llama `church_groups` porque `GROUPS` es palabra reservada en SQL.

## 2. Detalle por tabla

### 2.1 Miembros y familias

**`members`**

| Campo | Tipo | Notas |
|-------|------|-------|
| `id` | INTEGER PK | |
| `firstName`, `lastName` | TEXT | índice de búsqueda |
| `email`, `phone` | TEXT | |
| `birthDate` | INTEGER? | epoch millis |
| `gender`, `maritalStatus`, `status`, `familyRole` | TEXT (enum) | |
| `churchRole` | TEXT | cargo/ministerio |
| `familyId` | INTEGER? | → `families.id` |
| `photoUri`, `address`, `city` | TEXT | |
| `baptized`, `baptismDate` | BOOLEAN / INTEGER? | |
| `joinedAt`, `createdAt`, `updatedAt` | INTEGER | |
| `propertiesCsv` | TEXT | etiquetas libres |
| `isChild`, `guardianPhone` | BOOLEAN / TEXT | usado por el check-in infantil |
| `allowsContact` | BOOLEAN | consentimiento para campañas |
| `remoteId`, `pendingSync` | TEXT? / BOOLEAN | sincronización con Firestore |

**`families`** — `id`, `name`, `address`, `city`, `phone`, `email`, `photoUri`, `notes`, `createdAt`.

**`custom_field_defs`** — `id`, `entityType` (MIEMBRO/FAMILIA/EVENTO/GRUPO), `label`,
`fieldType` (TEXTO/NUMERO/FECHA/BOOLEANO/SELECCION), `optionsCsv`, `required`, `position`, `active`.

**`custom_field_values`** — `id`, `fieldId`, `entityType`, `entityId`, `value`. Índice único
(`fieldId`, `entityId`).

**`notes`** — `id`, `entityType`, `entityId`, `title`, `body`, `author`, `createdAt`. Timeline
pastoral por miembro/familia.

### 2.2 Finanzas

**`funds`** — `id`, `name`, `description`, `active`, `goal`.

**`donations`** — `id`, `memberId?`, `fundId`, `amount`, `date`, `type` (DIEZMO/OFRENDA/…),
`method` (EFECTIVO/TRANSFERENCIA/…), `envelopeNumber?`, `depositId?`, `reference`, `note`,
`createdBy`, `createdAt`, `remoteId`, `pendingSync`. Índices en `memberId`, `fundId`, `date`.

**`pledges`** — `id`, `memberId`, `fundId`, `amount`, `frequency`, `startDate`, `endDate?`, `note`.
El progreso se calcula con `SUM(donations.amount)` del mismo miembro y fondo.

**`envelopes`** — `id`, `number`, `memberId`, `year`, `active`. Índice único (`number`, `year`).

**`deposits`** — `id`, `date`, `bank`, `reference`, `note`, `createdBy`. Las donaciones se enlazan
mediante `donations.depositId`.

**`expenses`** — `id`, `category`, `amount`, `date`, `description`, `vendor`, `createdBy`.

### 2.3 Eventos y asistencia

**`events`** — `id`, `title`, `description`, `type`, `location`, `startAt`, `endAt`,
`recurrence` (NONE/DAILY/WEEKLY/BIWEEKLY/MONTHLY), `requiresCheckIn`, `groupId?`, `capacity`,
`reminderMinutesBefore`, `timezoneId`, `remoteId`, `pendingSync`.

**`attendance`** — `id`, `eventId`, `memberId`, `present`, `checkedAt`, `note`. Índice único
(`eventId`, `memberId`).

**`check_ins`** — `id`, `eventId`, `childMemberId?`, `childName`, `guardianName`, `guardianPhone`,
`securityCode`, `room`, `allergies`, `checkInAt`, `checkOutAt?`, `notified`. El código de seguridad
de 4 dígitos es obligatorio para retirar al menor.

### 2.4 Grupos

**`church_groups`** — `id`, `name`, `type`, `description`, `leaderId?`, `meetingDay`,
`meetingTime`, `location`, `active`, `createdAt`.

**`group_members`** — `id`, `groupId`, `memberId`, `role` (LIDER/COLIDER/MIEMBRO/PARTICIPANTE),
`joinedAt`. Índice único (`groupId`, `memberId`).

**`group_messages`** — `id`, `groupId`, `authorId?`, `authorName`, `content`, `createdAt`.

### 2.5 Voluntariado

**`volunteer_skills`** — `id`, `memberId`, `skill`, `level` (BASICO/INTERMEDIO/AVANZADO),
`availability`, `notes`, `active`.

**`ministry_needs`** — `id`, `ministry`, `skillRequired`, `slots`, `dayOfWeek`, `note`, `active`.

**`service_records`** — `id`, `memberId`, `ministry`, `eventId?`, `hours`, `date`, `note`.

### 2.6 Comunicación

**`campaigns`** — `id`, `subject`, `body`, `channel` (EMAIL/SMS/PUSH), `audience`
(`ALL`/`ACTIVE`/`LEADERS`/`GROUP:{id}`), `status`, `scheduledAt?`, `sentAt?`, `recipientCount`,
`createdBy`, `createdAt`.

**`prayer_requests`** — `id`, `title`, `detail`, `requesterId?`, `requesterName`, `isPrivate`,
`status`, `prayerCount`, `createdAt`, `answeredAt?`, `answerNote`.

### 2.7 Administración

**`users`** — `id`, `displayName`, `email` (único), `passwordHash`, `salt`, `role`, `memberId?`,
`active`, `twoFactorEnabled`, `totpSecret?`, `lastLoginAt?`, `createdAt`.

**`role_permissions`** — PK compuesta (`role`, `permission`), `granted`. Solo almacena las
*anulaciones* sobre la matriz por defecto.

**`audit_log`** — `id`, `userId?`, `userName`, `action`, `entityType`, `entityId?`, `detail`,
`timestamp`.

## 3. Consultas de analítica

Las agregaciones temporales usan SQLite puro, por ejemplo:

```sql
SELECT strftime('%Y-%m', date / 1000, 'unixepoch') AS period,
       SUM(amount) AS total
FROM donations
WHERE date BETWEEN :from AND :to
GROUP BY period
ORDER BY period;
```

Proyecciones (POJOs) devueltas por los DAOs: `PeriodTotal(period,total)`,
`LabeledTotal(label,total)`, `StatusCount(status,total)`, `AttendanceRow(memberId,firstName,lastName,present)`,
`GroupMemberRow(...)`, `GroupSizeRow(groupId,total)`, `VolunteerHours(memberId,firstName,lastName,hours)`.

## 4. Datos de demostración

`DatabaseSeeder` crea en el primer arranque: 4 usuarios, ~20 miembros y familias, fondos,
donaciones de varios meses, promesas, sobres, eventos con asistencia, grupos con mensajes,
habilidades/necesidades de voluntariado, campañas y peticiones de oración.

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin@iglesia.org` | `admin123` | Administrador |
| `pastor@iglesia.org` | `pastor123` | Pastor |
| `tesorero@iglesia.org` | `tesoro123` | Tesorero |
| `lider@iglesia.org` | `lider123` | Líder de ministerio |

## 5. Migraciones

La versión actual es la 1. Para evolucionar el esquema: incrementar `version`, añadir una
`Migration` en `di/DatabaseModule.kt` y versionar el JSON generado en `app/schemas/`.
Los backups (`BackupManager`) copian el fichero `.db` completo, por lo que una restauración
requiere la misma versión de esquema o superior compatible.
