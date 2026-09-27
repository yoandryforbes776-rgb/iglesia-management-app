package com.iglesiaflow.gestion.domain.model

/** Roles del sistema (RBAC). Se pueden reconfigurar desde Administración. */
enum class UserRole(val label: String) {
    PASTOR("Pastor"),
    ADMINISTRADOR("Administrador"),
    TESORERO("Tesorero"),
    LIDER_MINISTERIO("Líder de ministerio"),
    SECRETARIO("Secretario"),
    MIEMBRO("Miembro");

    companion object {
        fun fromName(value: String?): UserRole =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MIEMBRO
    }
}

/** Permisos granulares verificados en ViewModels y UI. */
enum class Permission(val label: String) {
    MEMBERS_VIEW("Ver miembros"),
    MEMBERS_EDIT("Editar miembros"),
    MEMBERS_DELETE("Eliminar miembros"),
    FINANCE_VIEW("Ver finanzas"),
    FINANCE_EDIT("Registrar donaciones"),
    FINANCE_DEPOSIT("Gestionar depósitos"),
    EVENTS_VIEW("Ver eventos"),
    EVENTS_EDIT("Editar eventos"),
    ATTENDANCE_MANAGE("Gestionar asistencia y check-in"),
    GROUPS_VIEW("Ver grupos"),
    GROUPS_EDIT("Editar grupos"),
    VOLUNTEERS_VIEW("Ver voluntariado"),
    VOLUNTEERS_EDIT("Editar voluntariado"),
    COMMUNICATION_SEND("Enviar comunicaciones"),
    PRAYER_MODERATE("Moderar peticiones de oración"),
    REPORTS_VIEW("Ver reportes"),
    REPORTS_EXPORT("Exportar reportes"),
    ADMIN_USERS("Gestionar usuarios"),
    ADMIN_SETTINGS("Configurar la aplicación"),
    ADMIN_BACKUP("Copias de seguridad"),
    ADMIN_AUDIT("Ver auditoría")
}

enum class Gender(val label: String) { MASCULINO("Masculino"), FEMENINO("Femenino"), OTRO("Otro"), NO_ESPECIFICA("Prefiere no decirlo") }

enum class MaritalStatus(val label: String) { SOLTERO("Soltero/a"), CASADO("Casado/a"), VIUDO("Viudo/a"), DIVORCIADO("Divorciado/a"), OTRO("Otro") }

enum class MemberStatus(val label: String) { ACTIVO("Activo"), INACTIVO("Inactivo"), VISITANTE("Visitante"), NUEVO("Nuevo convertido"), TRASLADADO("Trasladado") }

enum class FamilyRole(val label: String) { CABEZA("Cabeza de familia"), CONYUGE("Cónyuge"), HIJO("Hijo/a"), OTRO("Otro familiar") }

enum class CustomFieldType(val label: String) { TEXTO("Texto"), NUMERO("Número"), FECHA("Fecha"), BOOLEANO("Sí/No"), SELECCION("Selección") }

enum class CustomFieldEntity(val label: String) { MIEMBRO("Miembro"), FAMILIA("Familia"), EVENTO("Evento"), GRUPO("Grupo") }

enum class DonationMethod(val label: String) { EFECTIVO("Efectivo"), TRANSFERENCIA("Transferencia"), TARJETA("Tarjeta"), BIZUM("Bizum/Móvil"), CHEQUE("Cheque"), ESPECIE("En especie") }

enum class DonationType(val label: String) { DIEZMO("Diezmo"), OFRENDA("Ofrenda"), MISIONES("Misiones"), CONSTRUCCION("Construcción"), DONACION_ESPECIAL("Donación especial") }

enum class PledgeFrequency(val label: String) { SEMANAL("Semanal"), MENSUAL("Mensual"), TRIMESTRAL("Trimestral"), ANUAL("Anual") }

enum class EventType(val label: String) { CULTO("Culto"), ESTUDIO("Estudio bíblico"), REUNION("Reunión"), INFANTIL("Ministerio infantil"), JUVENIL("Jóvenes"), ESPECIAL("Evento especial"), RETIRO("Retiro") }

enum class GroupType(val label: String) { CELULA("Célula"), COMITE("Comité"), ESTUDIO_BIBLICO("Estudio bíblico"), ALABANZA("Equipo de alabanza"), MINISTERIO("Ministerio"), ESCUELA_DOMINICAL("Escuela dominical") }

enum class GroupRole(val label: String) { LIDER("Líder"), COLIDER("Co-líder"), MIEMBRO("Miembro"), PARTICIPANTE("Participante") }

enum class SkillLevel(val label: String) { BASICO("Básico"), INTERMEDIO("Intermedio"), AVANZADO("Avanzado") }

enum class CampaignChannel(val label: String) { EMAIL("Email"), SMS("SMS"), PUSH("Notificación push") }

enum class CampaignStatus(val label: String) { BORRADOR("Borrador"), PROGRAMADA("Programada"), ENVIADA("Enviada") }

enum class PrayerStatus(val label: String) { ABIERTA("Abierta"), EN_ORACION("En oración"), RESPONDIDA("Respondida"), CERRADA("Cerrada") }

enum class AppModule(val key: String, val label: String) {
    DASHBOARD("dashboard", "Inicio"),
    MEMBERS("members", "Miembros y familias"),
    FINANCE("finance", "Finanzas y donaciones"),
    EVENTS("events", "Eventos y asistencia"),
    GROUPS("groups", "Grupos y ministerios"),
    VOLUNTEERS("volunteers", "Voluntariado"),
    COMMUNICATION("communication", "Comunicación"),
    REPORTS("reports", "Reportes y analítica"),
    ADMIN("admin", "Administración");

    companion object {
        /** Módulos que nunca pueden ocultarse. */
        val alwaysOn = setOf(DASHBOARD, ADMIN)
        fun fromKey(key: String): AppModule? = entries.firstOrNull { it.key == key }
    }
}

enum class ThemeMode(val label: String) { SISTEMA("Según el sistema"), CLARO("Claro"), OSCURO("Oscuro") }
