# Interfaces y "endpoints" · IglesiaFlow

La aplicación no expone un servidor HTTP propio: su "API" son (1) los repositorios internos que
consume la UI, (2) las colecciones de Firestore utilizadas para la sincronización opcional y
(3) los intents e integraciones con el sistema operativo. Este documento describe las tres.

## 1. API interna (repositorios)

Todos los repositorios son `@Singleton` inyectados con Hilt. Las lecturas devuelven `Flow` (para
observación reactiva) y las escrituras son `suspend`.

### MemberRepository
| Operación | Firma | Descripción |
|-----------|-------|-------------|
| Listar | `members(): Flow<List<MemberEntity>>` | Directorio completo |
| Buscar | `search(query): Flow<List<MemberEntity>>` | Nombre, email o teléfono |
| Detalle | `member(id): Flow<MemberEntity?>` | |
| Niños | `children(): Flow<List<MemberEntity>>` | Para el check-in infantil |
| Familias | `families()`, `familyMembers(familyId)` | |
| Cumpleaños | `birthdays(month)` | |
| Métricas | `totalMembers()`, `activeMembers()`, `newMembersSince(ts)`, `membersByStatus()` | |
| Escritura | `save(member)`, `saveAll(list)`, `delete(member)`, `saveFamily`, `deleteFamily` | Marca `pendingSync` y audita |
| Campos personalizados | `customFields(type)`, `allCustomFields()`, `customValues(type,id)`, `saveCustomField`, `deleteCustomField`, `setCustomValue` | |
| Notas | `notes(type,id)`, `addNote`, `deleteNote` | |

### FinanceRepository
`donations()`, `donationsBetween(from,to)`, `donationsByMember(id)`, `totalBetween`, `byMonth`,
`byFund`, `byType`, `topDonors(from,to,limit)`, `funds()`, `pledges()`, `pledgeProgress(pledge)`,
`envelopes(year)`, `envelopeTotal(number)`, `deposits()`, `depositTotal(id)`,
`assignToDeposit(depositId, donationIds)`, `expenses()`, `expensesBetween`, `expensesByCategory()`,
y los `save*`/`delete*` correspondientes.

### EventRepository
`events()`, `upcoming(limit)`, `between(from,to)`, `event(id)`, `attendanceRows(eventId)`,
`presentCount(eventId)`, `attendanceByMonth`, `attendanceByEvent(limit)`, `checkIns(eventId)`,
`activeCheckIns()`, `activeCheckInCount()`, `save(event)`, `generateRecurrences(event, times)`,
`delete(id)`, `toggleAttendance(eventId, memberId, present)`,
`checkIn(eventId, childName, childMemberId, guardianName, guardianPhone, room, allergies)` → devuelve
el código de seguridad, `checkOut(checkInId, code): Boolean`.

### GroupRepository
`groups()`, `group(id)`, `sizes()`, `members(groupId)`, `messages(groupId)`, `save`, `delete`,
`addMember(groupId, memberId, role)`, `removeMember(membershipId)`, `postMessage(groupId, content)`,
`deleteMessage(id)`.

### VolunteerRepository
`skills()`, `skillsFor(memberId)`, `needs()`, `serviceRecords()`, `topVolunteers(since, limit)`,
`totalHours(since)`, `volunteerCount()`, `saveSkill`, `saveNeed`, `saveServiceRecord`, los
`delete*` y `match(needs, skills): List<VolunteerMatch>` (algoritmo de emparejamiento por
habilidad + disponibilidad, ordenado por nivel).

### CommunicationRepository
`campaigns()`, `prayerRequests()`, `publicPrayerRequests()`, `openPrayerCount()`, `saveCampaign`,
`markSent(campaignId, recipients)`, `deleteCampaign`, `savePrayer`, `prayFor(id)`,
`answerPrayer(id, note)`, `deletePrayer`.

### AdminRepository
`users()`, `auditLog(limit)`, `permissionMatrix(): Flow<Map<UserRole, Set<Permission>>>`,
`setPermission(role, permission, granted)`, `resetPermissions()`, `createUser(...)`, `updateUser`,
`changePassword`, `toggleTwoFactor(user, enabled): String?` (devuelve el secreto TOTP),
`deleteUser`, `findByEmail`, `allAudit()`, `purgeAuditBefore(ts)`.

### Servicios transversales
* `SessionManager`: `login(email, password)`, `verifyTwoFactor(code)`, `logout()`, `has(permission)`,
  `isSessionExpired(minutes)`.
* `ExportManager`: `writeCsv(baseName, headers, rows)`, `writePdf(baseName, title, subtitle, headers, rows)`,
  `share(file)`; todo se comparte mediante `FileProvider` (`${applicationId}.fileprovider`).
* `BackupManager`: `createBackup()`, `listBackups()`, `restoreBackup(file)`, `importBackupFrom(uri)`, `deleteBackup(file)`.
* `CsvImporter`: `parseMembers(uri)` acepta separador `;` o `,` y cabeceras
  `nombre, apellidos, email, telefono, nacimiento, estado, rol, direccion, ciudad`.
* `NotificationHelper`: canales `general`, `events`, `checkin`, `birthdays`, `prayer`.

## 2. Firestore (sincronización opcional)

Solo se activa si existe `app/google-services.json` **y** el ajuste *Sincronización en la nube*
está habilitado. `SyncManager` sube los registros con `pendingSync = true`.

| Colección | Documento | Campos principales |
|-----------|-----------|--------------------|
| `members` | `remoteId` (auto) | `firstName`, `lastName`, `email`, `phone`, `status`, `updatedAt` |
| `donations` | `remoteId` (auto) | `memberId`, `fundId`, `amount`, `date`, `type`, `method` |
| `events` | `remoteId` (auto) | `title`, `type`, `startAt`, `endAt`, `location` |

Reglas de seguridad recomendadas (ejemplo mínimo):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{db}/documents {
    match /{collection}/{doc} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### Firebase Cloud Messaging
`IglesiaMessagingService` recibe los push y los publica en el canal correspondiente. Formato de
payload sugerido:

```json
{
  "to": "/topics/iglesia",
  "notification": { "title": "Culto de oración", "body": "Hoy a las 19:30" },
  "data": { "channel": "iglesiaflow_events" }
}
```

## 3. Integraciones con el sistema

| Acción | Mecanismo |
|--------|-----------|
| Email masivo | `Intent.ACTION_SENDTO` con `mailto:` y destinatarios en BCC |
| SMS | `Intent.ACTION_SENDTO` con `smsto:` y `sms_body` |
| Compartir CSV/PDF | `Intent.ACTION_SEND` + `FileProvider` (`xml/file_paths.xml`) |
| Selección de logo / CSV / backup | `ActivityResultContracts.GetContent()` |
| Notificaciones locales | `NotificationManagerCompat` con canales por categoría |

## 4. Panel web opcional

`web-admin/` contiene una SPA React + TypeScript + Vite + MUI que puede desplegarse como panel de
consulta. Se conecta al mismo proyecto de Firebase (Auth + Firestore) cuando la sincronización en
la nube está habilitada.
