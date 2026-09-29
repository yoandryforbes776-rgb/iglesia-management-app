package com.iglesiaflow.gestion.core.security

import com.iglesiaflow.gestion.domain.model.Permission
import com.iglesiaflow.gestion.domain.model.UserRole

/**
 * Matriz de permisos por defecto. El administrador puede sobreescribirla en
 * tiempo de ejecución (tabla role_permissions) sin recompilar la APK.
 */
object Rbac {

    private val all: Set<Permission> = Permission.entries.toSet()

    val defaults: Map<UserRole, Set<Permission>> = mapOf(
        UserRole.PASTOR to all,
        UserRole.ADMINISTRADOR to all,
        UserRole.TESORERO to setOf(
            Permission.MEMBERS_VIEW,
            Permission.REPORTS_VIEW, Permission.REPORTS_EXPORT,
            Permission.EVENTS_VIEW, Permission.ATTENDANCE_VIEW
        ),
        UserRole.LIDER_MINISTERIO to setOf(
            Permission.MEMBERS_VIEW, Permission.MEMBERS_EDIT,
            Permission.EVENTS_VIEW, Permission.EVENTS_EDIT,
            Permission.ATTENDANCE_VIEW, Permission.ATTENDANCE_MANAGE,
            Permission.GROUPS_VIEW, Permission.GROUPS_EDIT,
            Permission.VOLUNTEERS_VIEW, Permission.VOLUNTEERS_EDIT,
            Permission.COMMUNICATION_SEND, Permission.PRAYER_MODERATE,
            Permission.REPORTS_VIEW
        ),
        UserRole.SECRETARIO to setOf(
            Permission.MEMBERS_VIEW, Permission.MEMBERS_EDIT,
            Permission.EVENTS_VIEW, Permission.EVENTS_EDIT,
            Permission.ATTENDANCE_VIEW, Permission.ATTENDANCE_MANAGE,
            Permission.GROUPS_VIEW,
            Permission.COMMUNICATION_SEND,
            Permission.REPORTS_VIEW, Permission.REPORTS_EXPORT
        ),
        UserRole.MIEMBRO to setOf(
            Permission.MEMBERS_VIEW,
            Permission.EVENTS_VIEW, Permission.ATTENDANCE_VIEW,
            Permission.GROUPS_VIEW,
            Permission.VOLUNTEERS_VIEW
        )
    )

    fun defaultsFor(role: UserRole): Set<Permission> = defaults[role].orEmpty()
}
