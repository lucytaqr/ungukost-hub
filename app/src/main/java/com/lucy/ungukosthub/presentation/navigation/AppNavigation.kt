package com.lucy.ungukosthub.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lucy.ungukosthub.domain.repository.AuthRepository
import com.lucy.ungukosthub.presentation.dashboard.DashboardScreen
import com.lucy.ungukosthub.presentation.finance.AddExpenseScreen
import com.lucy.ungukosthub.presentation.finance.AddIncomeScreen
import com.lucy.ungukosthub.presentation.finance.FinanceReportScreen
import com.lucy.ungukosthub.presentation.finance.FinanceSummaryScreen
import com.lucy.ungukosthub.presentation.finance.ReportsMenuScreen
import com.lucy.ungukosthub.presentation.login.LoginScreen
import com.lucy.ungukosthub.presentation.room.AddRoomScreen
import com.lucy.ungukosthub.presentation.room.EditRoomScreen
import com.lucy.ungukosthub.presentation.room.RoomDetailScreen
import com.lucy.ungukosthub.presentation.room.RoomListScreen
import com.lucy.ungukosthub.presentation.settings.SettingsScreen
import com.lucy.ungukosthub.presentation.tenant.AddTenantScreen
import com.lucy.ungukosthub.presentation.tenant.EditTenantScreen
import com.lucy.ungukosthub.presentation.tenant.TenantDetailScreen
import com.lucy.ungukosthub.presentation.tenant.TenantListScreen

/**
 * Komponen NavHost utama aplikasi Ungu Kost memuat seluruh 12 layar.
 */
@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    navController: NavHostController = rememberNavController()
) {
    val currentUser = authRepository.getCurrentUser()

    val startDestination = if (currentUser != null) {
        Screen.Dashboard.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 1. Login Route
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Dashboard Route (01_dashboard.png)
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onLogoutClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToRooms = { navController.navigate(Screen.RoomList.route) },
                onNavigateToTenants = { navController.navigate(Screen.TenantList.route) },
                onNavigateToFinance = { navController.navigate(Screen.FinanceSummary.route) },
                onNavigateToReports = { navController.navigate(Screen.ReportsMenu.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // 3. Rooms List Route (02_rooms_list.png)
        composable(route = Screen.RoomList.route) {
            RoomListScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddRoomClick = { navController.navigate(Screen.AddRoom.route) },
                onRoomClick = { roomId -> navController.navigate(Screen.RoomDetail.createRoute(roomId)) },
                onNavigateToDashboard = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToTenants = { navController.navigate(Screen.TenantList.route) },
                onNavigateToFinance = { navController.navigate(Screen.FinanceSummary.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // 4. Room Detail Route (03_room_detail.png)
        composable(
            route = Screen.RoomDetail.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: "101"
            RoomDetailScreen(
                roomId = roomId,
                onNavigateBack = { navController.popBackStack() },
                onEditRoomClick = { navController.navigate(Screen.EditRoom.createRoute(roomId)) }
            )
        }

        // 5. Add Room Route (04_room_add.png)
        composable(route = Screen.AddRoom.route) {
            AddRoomScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5b. Edit Room Route
        composable(
            route = Screen.EditRoom.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: "101"
            EditRoomScreen(
                roomId = roomId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. Tenant List Route (05_tenants_list.png)
        composable(route = Screen.TenantList.route) {
            TenantListScreen(
                onNavigateBack = { navController.popBackStack() },
                onTenantClick = { tenantId ->
                    navController.navigate(Screen.TenantDetail.createRoute(tenantId))
                },
                onAddTenantClick = { navController.navigate(Screen.AddTenant.route) },
                onNavigateToDashboard = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToRooms = { navController.navigate(Screen.RoomList.route) },
                onNavigateToFinance = { navController.navigate(Screen.FinanceSummary.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // 7. Add Tenant Route
        composable(route = Screen.AddTenant.route) {
            AddTenantScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 8. Tenant Detail Route (06_tenant_detail.png)
        composable(
            route = Screen.TenantDetail.route,
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getString("tenantId") ?: "1"
            TenantDetailScreen(
                tenantId = tenantId,
                onNavigateBack = { navController.popBackStack() },
                onEditTenantClick = { id ->
                    navController.navigate(Screen.EditTenant.createRoute(id))
                }
            )
        }

        // 9. Edit Tenant Route
        composable(
            route = Screen.EditTenant.route,
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getString("tenantId") ?: "1"
            EditTenantScreen(
                tenantId = tenantId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 8. Finance Summary Route (07_finance_summary.png)
        composable(route = Screen.FinanceSummary.route) {
            FinanceSummaryScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddIncomeClick = { navController.navigate(Screen.AddIncome.route) },
                onAddExpenseClick = { navController.navigate(Screen.AddExpense.route) },
                onNavigateToDashboard = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToRooms = { navController.navigate(Screen.RoomList.route) },
                onNavigateToTenants = { navController.navigate(Screen.TenantList.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // 9. Add Income Route (08_income_add.png)
        composable(route = Screen.AddIncome.route) {
            AddIncomeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 10. Add Expense Route (09_expense_add.png)
        composable(route = Screen.AddExpense.route) {
            AddExpenseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 11. Reports Menu Route (10_reports_menu.png)
        composable(route = Screen.ReportsMenu.route) {
            ReportsMenuScreen(
                onFinanceReportClick = { navController.navigate(Screen.FinanceReport.route) },
                onNavigateToDashboard = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToRooms = { navController.navigate(Screen.RoomList.route) },
                onNavigateToTenants = { navController.navigate(Screen.TenantList.route) },
                onNavigateToFinance = { navController.navigate(Screen.FinanceSummary.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // 12. Finance Report Route (11_finance_report.png)
        composable(route = Screen.FinanceReport.route) {
            FinanceReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 13. Settings Route (12_settings.png)
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onLogoutClick = {
                    authRepository.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToRooms = { navController.navigate(Screen.RoomList.route) },
                onNavigateToTenants = { navController.navigate(Screen.TenantList.route) },
                onNavigateToFinance = { navController.navigate(Screen.FinanceSummary.route) }
            )
        }
    }
}
