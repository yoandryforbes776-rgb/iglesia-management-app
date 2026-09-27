package com.iglesiaflow.gestion.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector
import com.iglesiaflow.gestion.domain.model.AppModule
import com.iglesiaflow.gestion.domain.model.Permission

/** Destinos principales de navegación, ligados a módulo y permiso. */
enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val module: AppModule,
    val permission: Permission?
) {
    DASHBOARD("dashboard", "Inicio", Icons.Filled.Home, AppModule.DASHBOARD, null),
    MEMBERS("members", "Miembros", Icons.Filled.People, AppModule.MEMBERS, Permission.MEMBERS_VIEW),
    FAMILIES("families", "Familias", Icons.Filled.Diversity3, AppModule.MEMBERS, Permission.MEMBERS_VIEW),
    FINANCE("finance", "Finanzas", Icons.Filled.Payments, AppModule.FINANCE, Permission.FINANCE_VIEW),
    EVENTS("events", "Eventos", Icons.Filled.CalendarMonth, AppModule.EVENTS, Permission.EVENTS_VIEW),
    GROUPS("groups", "Grupos", Icons.Filled.Groups, AppModule.GROUPS, Permission.GROUPS_VIEW),
    VOLUNTEERS("volunteers", "Voluntariado", Icons.Filled.VolunteerActivism, AppModule.VOLUNTEERS, Permission.VOLUNTEERS_VIEW),
    COMMUNICATION("communication", "Comunicación", Icons.Filled.Campaign, AppModule.COMMUNICATION, Permission.COMMUNICATION_SEND),
    REPORTS("reports", "Reportes", Icons.Filled.BarChart, AppModule.REPORTS, Permission.REPORTS_VIEW),
    ADMIN("admin", "Administración", Icons.Filled.AdminPanelSettings, AppModule.ADMIN, Permission.ADMIN_SETTINGS);

    companion object {
        val bottomBar = listOf(DASHBOARD, MEMBERS, FINANCE, EVENTS)
        fun fromRoute(route: String?): Destination? = entries.firstOrNull { it.route == route }
    }
}

object Routes {
    const val MEMBER_DETAIL = "member/{memberId}"
    const val EVENT_DETAIL = "event/{eventId}"
    const val GROUP_DETAIL = "group/{groupId}"
    const val CHECK_IN = "checkin"
    const val PRAYER = "prayer"
    const val ADMIN_USERS = "admin/users"
    const val ADMIN_ROLES = "admin/roles"
    const val ADMIN_FIELDS = "admin/fields"
    const val ADMIN_APPEARANCE = "admin/appearance"
    const val ADMIN_BACKUP = "admin/backup"
    const val ADMIN_AUDIT = "admin/audit"

    fun memberDetail(id: Long) = "member/$id"
    fun eventDetail(id: Long) = "event/$id"
    fun groupDetail(id: Long) = "group/$id"
}
