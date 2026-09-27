package com.iglesiaflow.gestion.data.local

import com.iglesiaflow.gestion.core.security.PasswordHasher
import com.iglesiaflow.gestion.core.util.DateTimeUtils
import com.iglesiaflow.gestion.data.local.dao.AdminDao
import com.iglesiaflow.gestion.data.local.dao.CommunicationDao
import com.iglesiaflow.gestion.data.local.dao.EventDao
import com.iglesiaflow.gestion.data.local.dao.FinanceDao
import com.iglesiaflow.gestion.data.local.dao.GroupDao
import com.iglesiaflow.gestion.data.local.dao.MemberDao
import com.iglesiaflow.gestion.data.local.dao.VolunteerDao
import com.iglesiaflow.gestion.data.local.entity.AttendanceEntity
import com.iglesiaflow.gestion.data.local.entity.CampaignEntity
import com.iglesiaflow.gestion.data.local.entity.CustomFieldDefEntity
import com.iglesiaflow.gestion.data.local.entity.DonationEntity
import com.iglesiaflow.gestion.data.local.entity.EnvelopeEntity
import com.iglesiaflow.gestion.data.local.entity.EventEntity
import com.iglesiaflow.gestion.data.local.entity.ExpenseEntity
import com.iglesiaflow.gestion.data.local.entity.FamilyEntity
import com.iglesiaflow.gestion.data.local.entity.FundEntity
import com.iglesiaflow.gestion.data.local.entity.GroupEntity
import com.iglesiaflow.gestion.data.local.entity.GroupMemberEntity
import com.iglesiaflow.gestion.data.local.entity.GroupMessageEntity
import com.iglesiaflow.gestion.data.local.entity.MemberEntity
import com.iglesiaflow.gestion.data.local.entity.MinistryNeedEntity
import com.iglesiaflow.gestion.data.local.entity.NoteEntity
import com.iglesiaflow.gestion.data.local.entity.PledgeEntity
import com.iglesiaflow.gestion.data.local.entity.PrayerRequestEntity
import com.iglesiaflow.gestion.data.local.entity.ServiceRecordEntity
import com.iglesiaflow.gestion.data.local.entity.UserEntity
import com.iglesiaflow.gestion.data.local.entity.VolunteerSkillEntity
import com.iglesiaflow.gestion.domain.model.CampaignChannel
import com.iglesiaflow.gestion.domain.model.CampaignStatus
import com.iglesiaflow.gestion.domain.model.CustomFieldEntity
import com.iglesiaflow.gestion.domain.model.CustomFieldType
import com.iglesiaflow.gestion.domain.model.DonationMethod
import com.iglesiaflow.gestion.domain.model.DonationType
import com.iglesiaflow.gestion.domain.model.EventType
import com.iglesiaflow.gestion.domain.model.FamilyRole
import com.iglesiaflow.gestion.domain.model.Gender
import com.iglesiaflow.gestion.domain.model.GroupRole
import com.iglesiaflow.gestion.domain.model.GroupType
import com.iglesiaflow.gestion.domain.model.MaritalStatus
import com.iglesiaflow.gestion.domain.model.MemberStatus
import com.iglesiaflow.gestion.domain.model.PledgeFrequency
import com.iglesiaflow.gestion.domain.model.PrayerStatus
import com.iglesiaflow.gestion.domain.model.SkillLevel
import com.iglesiaflow.gestion.domain.model.UserRole
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Datos iniciales: usuarios de acceso y un juego de datos de demostración para
 * que la iglesia pueda evaluar todos los módulos desde el primer arranque.
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val adminDao: AdminDao,
    private val memberDao: MemberDao,
    private val financeDao: FinanceDao,
    private val eventDao: EventDao,
    private val groupDao: GroupDao,
    private val volunteerDao: VolunteerDao,
    private val communicationDao: CommunicationDao
) {

    suspend fun seedIfNeeded() {
        if (adminDao.countUsers() > 0) return
        seedUsers()
        val familyIds = seedFamilies()
        val memberIds = seedMembers(familyIds)
        seedCustomFields()
        seedFinance(memberIds)
        val eventIds = seedEvents(memberIds)
        seedGroups(memberIds)
        seedVolunteers(memberIds, eventIds)
        seedCommunication(memberIds)
    }

    private suspend fun seedUsers() {
        fun user(name: String, email: String, password: String, role: UserRole): UserEntity {
            val salt = PasswordHasher.newSalt()
            return UserEntity(
                displayName = name,
                email = email,
                passwordHash = PasswordHasher.hash(password, salt),
                salt = salt,
                role = role
            )
        }
        adminDao.insertUser(user("Administrador", "admin@iglesia.org", "admin123", UserRole.ADMINISTRADOR))
        adminDao.insertUser(user("Pastor Daniel Ruiz", "pastor@iglesia.org", "pastor123", UserRole.PASTOR))
        adminDao.insertUser(user("Tesorería", "tesorero@iglesia.org", "tesoro123", UserRole.TESORERO))
        adminDao.insertUser(user("Líder de Jóvenes", "lider@iglesia.org", "lider123", UserRole.LIDER_MINISTERIO))
    }

    private suspend fun seedFamilies(): List<Long> = listOf(
        FamilyEntity(name = "Familia Rodríguez", address = "C/ Mayor 12", city = "Madrid", phone = "600111222"),
        FamilyEntity(name = "Familia Pérez", address = "Av. Libertad 5", city = "Madrid", phone = "600333444"),
        FamilyEntity(name = "Familia Gómez", address = "C/ Sol 30", city = "Getafe", phone = "600555666"),
        FamilyEntity(name = "Familia Santos", address = "C/ Luna 8", city = "Leganés", phone = "600777888")
    ).map { memberDao.insertFamily(it) }

    private suspend fun seedMembers(familyIds: List<Long>): List<Long> {
        val today = LocalDate.now()
        val people = listOf(
            MemberEntity(
                firstName = "Daniel", lastName = "Rodríguez", email = "daniel@iglesia.org", phone = "611000001",
                birthDate = DateTimeUtils.toMillis(today.minusYears(45).withDayOfMonth(12)),
                gender = Gender.MASCULINO, maritalStatus = MaritalStatus.CASADO, status = MemberStatus.ACTIVO,
                churchRole = "Pastor", familyId = familyIds[0], familyRole = FamilyRole.CABEZA,
                baptized = true, propertiesCsv = "Pastor,Predicador", city = "Madrid"
            ),
            MemberEntity(
                firstName = "María", lastName = "Rodríguez", email = "maria@iglesia.org", phone = "611000002",
                birthDate = DateTimeUtils.toMillis(today.minusYears(43).withDayOfMonth(3)),
                gender = Gender.FEMENINO, maritalStatus = MaritalStatus.CASADO, status = MemberStatus.ACTIVO,
                churchRole = "Coordinadora", familyId = familyIds[0], familyRole = FamilyRole.CONYUGE,
                baptized = true, propertiesCsv = "Miembro de coro,Voluntario", city = "Madrid"
            ),
            MemberEntity(
                firstName = "Lucas", lastName = "Rodríguez", phone = "611000003",
                birthDate = DateTimeUtils.toMillis(today.minusYears(8).withDayOfMonth(21)),
                gender = Gender.MASCULINO, status = MemberStatus.ACTIVO, familyId = familyIds[0],
                familyRole = FamilyRole.HIJO, isChild = true, guardianPhone = "611000001", city = "Madrid"
            ),
            MemberEntity(
                firstName = "Carlos", lastName = "Pérez", email = "carlos@correo.com", phone = "611000004",
                birthDate = DateTimeUtils.toMillis(today.minusYears(34).withDayOfMonth(9)),
                gender = Gender.MASCULINO, maritalStatus = MaritalStatus.CASADO, status = MemberStatus.ACTIVO,
                churchRole = "Diácono", familyId = familyIds[1], familyRole = FamilyRole.CABEZA,
                baptized = true, propertiesCsv = "Voluntario,Sonido", city = "Madrid"
            ),
            MemberEntity(
                firstName = "Ana", lastName = "Pérez", email = "ana@correo.com", phone = "611000005",
                birthDate = DateTimeUtils.toMillis(today.minusYears(31).withDayOfMonth(25)),
                gender = Gender.FEMENINO, maritalStatus = MaritalStatus.CASADO, status = MemberStatus.ACTIVO,
                familyId = familyIds[1], familyRole = FamilyRole.CONYUGE, baptized = true,
                propertiesCsv = "Escuela dominical", city = "Madrid"
            ),
            MemberEntity(
                firstName = "Sofía", lastName = "Pérez",
                birthDate = DateTimeUtils.toMillis(today.minusYears(5).withDayOfMonth(14)),
                gender = Gender.FEMENINO, status = MemberStatus.ACTIVO, familyId = familyIds[1],
                familyRole = FamilyRole.HIJO, isChild = true, guardianPhone = "611000004", city = "Madrid"
            ),
            MemberEntity(
                firstName = "José", lastName = "Gómez", email = "jose@correo.com", phone = "611000006",
                birthDate = DateTimeUtils.toMillis(today.minusYears(58).withDayOfMonth(2)),
                gender = Gender.MASCULINO, maritalStatus = MaritalStatus.CASADO, status = MemberStatus.ACTIVO,
                churchRole = "Anciano", familyId = familyIds[2], familyRole = FamilyRole.CABEZA,
                baptized = true, city = "Getafe"
            ),
            MemberEntity(
                firstName = "Elena", lastName = "Gómez", phone = "611000007",
                birthDate = DateTimeUtils.toMillis(today.minusYears(55).withDayOfMonth(18)),
                gender = Gender.FEMENINO, maritalStatus = MaritalStatus.CASADO, status = MemberStatus.ACTIVO,
                familyId = familyIds[2], familyRole = FamilyRole.CONYUGE, baptized = true,
                propertiesCsv = "Intercesión", city = "Getafe"
            ),
            MemberEntity(
                firstName = "Pablo", lastName = "Santos", email = "pablo@correo.com", phone = "611000008",
                birthDate = DateTimeUtils.toMillis(today.minusYears(22).withDayOfMonth(7)),
                gender = Gender.MASCULINO, maritalStatus = MaritalStatus.SOLTERO, status = MemberStatus.ACTIVO,
                churchRole = "Líder de jóvenes", familyId = familyIds[3], familyRole = FamilyRole.CABEZA,
                baptized = true, propertiesCsv = "Alabanza,Guitarra", city = "Leganés"
            ),
            MemberEntity(
                firstName = "Laura", lastName = "Martín", email = "laura@correo.com", phone = "611000009",
                birthDate = DateTimeUtils.toMillis(today.minusYears(27).withDayOfMonth(29)),
                gender = Gender.FEMENINO, status = MemberStatus.VISITANTE, city = "Madrid"
            ),
            MemberEntity(
                firstName = "Miguel", lastName = "Torres", phone = "611000010",
                birthDate = DateTimeUtils.toMillis(today.minusYears(38).withDayOfMonth(16)),
                gender = Gender.MASCULINO, status = MemberStatus.NUEVO,
                joinedAt = System.currentTimeMillis() - 15L * 24 * 3600 * 1000, city = "Madrid"
            ),
            MemberEntity(
                firstName = "Raquel", lastName = "Díaz", email = "raquel@correo.com", phone = "611000011",
                birthDate = DateTimeUtils.toMillis(today.minusYears(19).withDayOfMonth(4)),
                gender = Gender.FEMENINO, status = MemberStatus.ACTIVO, propertiesCsv = "Miembro de coro",
                city = "Madrid"
            )
        )
        val ids = memberDao.insertMembers(people)
        memberDao.insertNote(
            NoteEntity(
                entityType = CustomFieldEntity.MIEMBRO, entityId = ids[9], authorName = "Secretaría",
                title = "Primera visita", content = "Visitó el culto dominical, interesada en el grupo de jóvenes."
            )
        )
        memberDao.insertNote(
            NoteEntity(
                entityType = CustomFieldEntity.MIEMBRO, entityId = ids[10], authorName = "Pastor Daniel Ruiz",
                title = "Seguimiento", content = "Aceptó a Cristo el 3 del mes pasado. Iniciar discipulado.",
                isPrivate = true
            )
        )
        return ids
    }

    private suspend fun seedCustomFields() {
        memberDao.insertCustomField(
            CustomFieldDefEntity(
                entityType = CustomFieldEntity.MIEMBRO, label = "Talla de camiseta",
                fieldType = CustomFieldType.SELECCION, optionsCsv = "S,M,L,XL", position = 1
            )
        )
        memberDao.insertCustomField(
            CustomFieldDefEntity(
                entityType = CustomFieldEntity.MIEMBRO, label = "Curso de discipulado",
                fieldType = CustomFieldType.BOOLEANO, position = 2
            )
        )
        memberDao.insertCustomField(
            CustomFieldDefEntity(
                entityType = CustomFieldEntity.FAMILIA, label = "Aniversario de boda",
                fieldType = CustomFieldType.FECHA, position = 1
            )
        )
    }

    private suspend fun seedFinance(memberIds: List<Long>) {
        val fundIds = listOf(
            FundEntity(name = "Fondo general", description = "Gastos ordinarios de la iglesia"),
            FundEntity(name = "Misiones", description = "Apoyo a misioneros"),
            FundEntity(name = "Construcción", description = "Ampliación del templo"),
            FundEntity(name = "Benevolencia", description = "Ayuda social")
        ).map { financeDao.insertFund(it) }

        val random = Random(7)
        val now = LocalDateTime.now()
        repeat(60) { index ->
            val date = DateTimeUtils.toMillis(now.minusDays((index * 3L) % 180))
            financeDao.insertDonation(
                DonationEntity(
                    memberId = memberIds.random(random),
                    amount = listOf(10.0, 20.0, 25.0, 50.0, 75.0, 100.0, 150.0).random(random),
                    type = DonationType.entries.random(random),
                    method = DonationMethod.entries.random(random),
                    fundId = fundIds.random(random),
                    envelopeNumber = if (random.nextBoolean()) random.nextInt(1, 25) else null,
                    date = date,
                    createdBy = "Tesorería"
                )
            )
        }
        repeat(20) { index ->
            financeDao.insertEnvelope(
                EnvelopeEntity(
                    number = index + 1,
                    memberId = memberIds.getOrNull(index % memberIds.size),
                    year = DateTimeUtils.currentYear()
                )
            )
        }
        financeDao.insertPledge(
            PledgeEntity(
                memberId = memberIds[0], fundId = fundIds[2], campaign = "Ampliación del templo 2026",
                totalAmount = 3000.0, frequency = PledgeFrequency.MENSUAL,
                startDate = DateTimeUtils.startOfYear()
            )
        )
        financeDao.insertPledge(
            PledgeEntity(
                memberId = memberIds[3], fundId = fundIds[1], campaign = "Misiones 2026",
                totalAmount = 1200.0, frequency = PledgeFrequency.MENSUAL,
                startDate = DateTimeUtils.startOfYear()
            )
        )
        listOf(
            ExpenseEntity(category = "Alquiler", amount = 950.0, description = "Alquiler del local", supplier = "Inmobiliaria Sur"),
            ExpenseEntity(category = "Suministros", amount = 180.5, description = "Luz y agua"),
            ExpenseEntity(category = "Misiones", amount = 400.0, description = "Envío mensual misionero", fundId = fundIds[1]),
            ExpenseEntity(category = "Material", amount = 120.0, description = "Material escuela dominical")
        ).forEach { financeDao.insertExpense(it) }
    }

    private suspend fun seedEvents(memberIds: List<Long>): List<Long> {
        val base = LocalDateTime.now().withHour(11).withMinute(0).withSecond(0).withNano(0)
        val events = listOf(
            EventEntity(
                title = "Culto dominical", description = "Servicio principal de adoración",
                type = EventType.CULTO, location = "Templo principal",
                startAt = DateTimeUtils.toMillis(base.plusDays(((7 - base.dayOfWeek.value) % 7).toLong())),
                endAt = DateTimeUtils.toMillis(base.plusDays(((7 - base.dayOfWeek.value) % 7).toLong()).plusHours(2)),
                recurrence = "WEEKLY", timezoneId = java.time.ZoneId.systemDefault().id
            ),
            EventEntity(
                title = "Estudio bíblico de mitad de semana", type = EventType.ESTUDIO, location = "Sala 2",
                startAt = DateTimeUtils.toMillis(base.plusDays(3).withHour(19)),
                endAt = DateTimeUtils.toMillis(base.plusDays(3).withHour(21)),
                recurrence = "WEEKLY", timezoneId = java.time.ZoneId.systemDefault().id
            ),
            EventEntity(
                title = "Ministerio infantil", type = EventType.INFANTIL, location = "Aula infantil",
                startAt = DateTimeUtils.toMillis(base.plusDays(((7 - base.dayOfWeek.value) % 7).toLong())),
                endAt = DateTimeUtils.toMillis(base.plusDays(((7 - base.dayOfWeek.value) % 7).toLong()).plusHours(2)),
                requiresCheckIn = true, recurrence = "WEEKLY", timezoneId = java.time.ZoneId.systemDefault().id
            ),
            EventEntity(
                title = "Retiro de jóvenes", type = EventType.RETIRO, location = "Campamento El Encuentro",
                startAt = DateTimeUtils.toMillis(base.plusDays(21)),
                endAt = DateTimeUtils.toMillis(base.plusDays(23)),
                timezoneId = java.time.ZoneId.systemDefault().id
            ),
            EventEntity(
                title = "Culto dominical (semana pasada)", type = EventType.CULTO, location = "Templo principal",
                startAt = DateTimeUtils.toMillis(base.minusDays(7)),
                endAt = DateTimeUtils.toMillis(base.minusDays(7).plusHours(2)),
                timezoneId = java.time.ZoneId.systemDefault().id
            )
        )
        val ids = events.map { eventDao.insertEvent(it) }
        memberIds.take(8).forEach { memberId ->
            eventDao.insertAttendance(AttendanceEntity(eventId = ids[4], memberId = memberId, present = true))
        }
        return ids
    }

    private suspend fun seedGroups(memberIds: List<Long>) {
        val groups = listOf(
            GroupEntity(
                name = "Célula Centro", type = GroupType.CELULA, description = "Grupo de casa del centro",
                leaderId = memberIds[3], meetingDay = "Martes", meetingTime = "19:30", location = "C/ Mayor 12"
            ),
            GroupEntity(
                name = "Equipo de alabanza", type = GroupType.ALABANZA, description = "Músicos y cantantes",
                leaderId = memberIds[8], meetingDay = "Sábado", meetingTime = "17:00", location = "Templo"
            ),
            GroupEntity(
                name = "Escuela dominical", type = GroupType.ESCUELA_DOMINICAL, description = "Maestros de niños",
                leaderId = memberIds[4], meetingDay = "Domingo", meetingTime = "10:00", location = "Aula infantil"
            )
        )
        val ids = groups.map { groupDao.insertGroup(it) }
        groupDao.insertGroupMember(GroupMemberEntity(groupId = ids[0], memberId = memberIds[3], role = GroupRole.LIDER))
        groupDao.insertGroupMember(GroupMemberEntity(groupId = ids[0], memberId = memberIds[4], role = GroupRole.MIEMBRO))
        groupDao.insertGroupMember(GroupMemberEntity(groupId = ids[0], memberId = memberIds[6], role = GroupRole.MIEMBRO))
        groupDao.insertGroupMember(GroupMemberEntity(groupId = ids[1], memberId = memberIds[8], role = GroupRole.LIDER))
        groupDao.insertGroupMember(GroupMemberEntity(groupId = ids[1], memberId = memberIds[11], role = GroupRole.MIEMBRO))
        groupDao.insertGroupMember(GroupMemberEntity(groupId = ids[2], memberId = memberIds[4], role = GroupRole.LIDER))
        groupDao.insertGroupMessage(
            GroupMessageEntity(groupId = ids[0], authorName = "Carlos Pérez", content = "¡Nos vemos el martes a las 19:30!")
        )
        groupDao.insertGroupMessage(
            GroupMessageEntity(groupId = ids[1], authorName = "Pablo Santos", content = "Ensayo el sábado, repasamos 3 cantos nuevos.")
        )
    }

    private suspend fun seedVolunteers(memberIds: List<Long>, eventIds: List<Long>) {
        listOf(
            VolunteerSkillEntity(memberId = memberIds[3], skill = "Sonido", level = SkillLevel.AVANZADO, availability = "Domingo,Sábado"),
            VolunteerSkillEntity(memberId = memberIds[8], skill = "Guitarra", level = SkillLevel.AVANZADO, availability = "Sábado,Domingo"),
            VolunteerSkillEntity(memberId = memberIds[11], skill = "Voz", level = SkillLevel.INTERMEDIO, availability = "Domingo"),
            VolunteerSkillEntity(memberId = memberIds[4], skill = "Enseñanza infantil", level = SkillLevel.AVANZADO, availability = "Domingo"),
            VolunteerSkillEntity(memberId = memberIds[7], skill = "Intercesión", level = SkillLevel.INTERMEDIO, availability = "Miércoles")
        ).forEach { volunteerDao.insertSkill(it) }

        listOf(
            MinistryNeedEntity(ministry = "Alabanza", skillRequired = "Guitarra", slots = 1, dayOfWeek = "Domingo"),
            MinistryNeedEntity(ministry = "Multimedia", skillRequired = "Sonido", slots = 2, dayOfWeek = "Domingo"),
            MinistryNeedEntity(ministry = "Escuela dominical", skillRequired = "Enseñanza infantil", slots = 3, dayOfWeek = "Domingo")
        ).forEach { volunteerDao.insertNeed(it) }

        listOf(
            ServiceRecordEntity(memberId = memberIds[3], ministry = "Multimedia", eventId = eventIds.getOrNull(4), hours = 3.0),
            ServiceRecordEntity(memberId = memberIds[8], ministry = "Alabanza", eventId = eventIds.getOrNull(4), hours = 4.0),
            ServiceRecordEntity(memberId = memberIds[4], ministry = "Escuela dominical", hours = 2.5),
            ServiceRecordEntity(memberId = memberIds[11], ministry = "Alabanza", hours = 2.0)
        ).forEach { volunteerDao.insertServiceRecord(it) }
    }

    private suspend fun seedCommunication(memberIds: List<Long>) {
        communicationDao.insertCampaign(
            CampaignEntity(
                subject = "Boletín semanal", body = "Recordatorio del culto dominical a las 11:00.",
                channel = CampaignChannel.EMAIL, audience = "ALL", status = CampaignStatus.ENVIADA,
                sentAt = System.currentTimeMillis() - 86_400_000, recipientCount = 12, createdBy = "Secretaría"
            )
        )
        communicationDao.insertCampaign(
            CampaignEntity(
                subject = "Retiro de jóvenes", body = "Inscripciones abiertas hasta el viernes.",
                channel = CampaignChannel.PUSH, audience = "GROUP", status = CampaignStatus.BORRADOR,
                createdBy = "Líder de Jóvenes"
            )
        )
        listOf(
            PrayerRequestEntity(
                title = "Salud de Elena", detail = "Operación programada la próxima semana.",
                requesterId = memberIds[7], requesterName = "Elena Gómez", prayerCount = 6
            ),
            PrayerRequestEntity(
                title = "Trabajo para Miguel", detail = "Buscando empleo estable.",
                requesterId = memberIds[10], requesterName = "Miguel Torres", prayerCount = 3
            ),
            PrayerRequestEntity(
                title = "Familia en crisis", detail = "Petición confidencial de acompañamiento pastoral.",
                isPrivate = true, requesterName = "Anónimo", status = PrayerStatus.EN_ORACION
            )
        ).forEach { communicationDao.insertPrayerRequest(it) }
    }
}
