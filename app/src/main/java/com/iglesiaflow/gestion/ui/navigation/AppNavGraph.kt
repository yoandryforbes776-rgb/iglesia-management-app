package com.iglesiaflow.gestion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.iglesiaflow.gestion.ui.screens.admin.AdminHomeScreen
import com.iglesiaflow.gestion.ui.screens.admin.AppearanceScreen
import com.iglesiaflow.gestion.ui.screens.admin.AuditScreen
import com.iglesiaflow.gestion.ui.screens.admin.BackupScreen
import com.iglesiaflow.gestion.ui.screens.admin.CustomFieldsScreen
import com.iglesiaflow.gestion.ui.screens.admin.RolesScreen
import com.iglesiaflow.gestion.ui.screens.admin.SyncScreen
import com.iglesiaflow.gestion.ui.screens.admin.UsersScreen
import com.iglesiaflow.gestion.ui.screens.attendance.AttendanceScreen
import com.iglesiaflow.gestion.ui.screens.attendance.MemberAttendanceScreen
import com.iglesiaflow.gestion.ui.screens.communication.CommunicationScreen
import com.iglesiaflow.gestion.ui.screens.communication.PrayerScreen
import com.iglesiaflow.gestion.ui.screens.dashboard.DashboardScreen
import com.iglesiaflow.gestion.ui.screens.events.CheckInScreen
import com.iglesiaflow.gestion.ui.screens.events.EventDetailScreen
import com.iglesiaflow.gestion.ui.screens.events.EventsScreen
import com.iglesiaflow.gestion.ui.screens.groups.GroupDetailScreen
import com.iglesiaflow.gestion.ui.screens.groups.GroupsScreen
import com.iglesiaflow.gestion.ui.screens.members.FamiliesScreen
import com.iglesiaflow.gestion.ui.screens.members.MemberDetailScreen
import com.iglesiaflow.gestion.ui.screens.members.MembersScreen
import com.iglesiaflow.gestion.ui.screens.reports.ReportsScreen
import com.iglesiaflow.gestion.ui.screens.volunteers.VolunteersScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destination.DASHBOARD.route,
        modifier = modifier
    ) {
        composable(Destination.DASHBOARD.route) {
            DashboardScreen(
                onOpenMembers = { navController.navigate(Destination.MEMBERS.route) },
                onOpenAttendance = { navController.navigate(Destination.ATTENDANCE.route) },
                onOpenEvents = { navController.navigate(Destination.EVENTS.route) },
                onOpenEvent = { id -> navController.navigate(Routes.eventDetail(id)) },
                onOpenMember = { id -> navController.navigate(Routes.memberDetail(id)) }
            )
        }
        composable(Destination.MEMBERS.route) {
            MembersScreen(onOpenMember = { id -> navController.navigate(Routes.memberDetail(id)) })
        }
        composable(Destination.FAMILIES.route) {
            FamiliesScreen(onOpenMember = { id -> navController.navigate(Routes.memberDetail(id)) })
        }
        composable(
            route = Routes.MEMBER_DETAIL,
            arguments = listOf(navArgument("memberId") { type = NavType.LongType })
        ) {
            MemberDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenAttendance = { id -> navController.navigate(Routes.memberAttendance(id)) }
            )
        }
        composable(Destination.ATTENDANCE.route) {
            AttendanceScreen(
                onOpenMemberAttendance = { id -> navController.navigate(Routes.memberAttendance(id)) }
            )
        }
        composable(
            route = Routes.MEMBER_ATTENDANCE,
            arguments = listOf(navArgument("memberId") { type = NavType.LongType })
        ) {
            MemberAttendanceScreen(onBack = { navController.popBackStack() })
        }
        composable(Destination.EVENTS.route) {
            EventsScreen(
                onOpenEvent = { id -> navController.navigate(Routes.eventDetail(id)) },
                onOpenCheckIn = { navController.navigate(Routes.CHECK_IN) }
            )
        }
        composable(
            route = Routes.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) {
            EventDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CHECK_IN) { CheckInScreen(onBack = { navController.popBackStack() }) }
        composable(Destination.GROUPS.route) {
            GroupsScreen(onOpenGroup = { id -> navController.navigate(Routes.groupDetail(id)) })
        }
        composable(
            route = Routes.GROUP_DETAIL,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) {
            GroupDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(Destination.VOLUNTEERS.route) { VolunteersScreen() }
        composable(Destination.COMMUNICATION.route) {
            CommunicationScreen(onOpenPrayer = { navController.navigate(Routes.PRAYER) })
        }
        composable(Routes.PRAYER) { PrayerScreen(onBack = { navController.popBackStack() }) }
        composable(Destination.REPORTS.route) { ReportsScreen() }
        composable(Destination.ADMIN.route) {
            AdminHomeScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable(Routes.ADMIN_USERS) { UsersScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.ADMIN_ROLES) { RolesScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.ADMIN_FIELDS) { CustomFieldsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.ADMIN_APPEARANCE) { AppearanceScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.ADMIN_BACKUP) { BackupScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.ADMIN_AUDIT) { AuditScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.ADMIN_SYNC) { SyncScreen(onBack = { navController.popBackStack() }) }
    }
}
