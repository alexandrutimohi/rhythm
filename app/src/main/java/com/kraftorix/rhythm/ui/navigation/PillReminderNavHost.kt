package com.kraftorix.rhythm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kraftorix.rhythm.ui.PillAlarmViewModel
import com.kraftorix.rhythm.ui.screens.AddEditAlarmScreen
import com.kraftorix.rhythm.ui.screens.DashboardScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AddEditAlarm : Screen("add_edit_alarm?alarmId={alarmId}") {
        fun createRoute(alarmId: Long? = null) = if (alarmId != null) "add_edit_alarm?alarmId=$alarmId" else "add_edit_alarm"
    }
}

@Composable
fun PillReminderNavHost(
    navController: NavHostController,
    viewModel: PillAlarmViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onAddAlarm = { navController.navigate(Screen.AddEditAlarm.createRoute()) },
                onEditAlarm = { alarmId -> navController.navigate(Screen.AddEditAlarm.createRoute(alarmId)) }
            )
        }
        composable(
            route = Screen.AddEditAlarm.route,
            arguments = listOf(
                navArgument("alarmId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: -1L
            AddEditAlarmScreen(
                viewModel = viewModel,
                alarmId = if (alarmId == -1L) null else alarmId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
