package com.lucy.ungukosthub.presentation.navigation

/**
 * Definisi rute navigasi lengkap untuk aplikasi Ungu Kost.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login_route")
    object Dashboard : Screen("dashboard_route")
    object RoomList : Screen("room_list_route")
    object RoomDetail : Screen("room_detail_route/{roomId}") {
        fun createRoute(roomId: String) = "room_detail_route/$roomId"
    }
    object AddRoom : Screen("add_room_route")
    object TenantList : Screen("tenant_list_route")
    object TenantDetail : Screen("tenant_detail_route/{tenantId}") {
        fun createRoute(tenantId: String) = "tenant_detail_route/$tenantId"
    }
    object FinanceSummary : Screen("finance_summary_route")
    object AddIncome : Screen("add_income_route")
    object AddExpense : Screen("add_expense_route")
    object ReportsMenu : Screen("reports_menu_route")
    object FinanceReport : Screen("finance_report_route")
    object Settings : Screen("settings_route")
}
