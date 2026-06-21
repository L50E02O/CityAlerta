package ec.cityalerta.app.navigation

sealed class Routes(val route: String, val authRequired: Boolean = false) {
    object Splash : Routes("splash")
    object Login : Routes("login")
    object Register : Routes("register")
    object RecoverPassword : Routes("recover_password")
    object Home : Routes("home")
    object Explore : Routes("explore")
    object Search : Routes("search", authRequired = true)
    object Post : Routes("post", authRequired = true)
    object ReporteForm : Routes("reporte_form", authRequired = true)

    object Map : Routes("map/{ciudadId}?reportId={reportId}")
    object Profile : Routes("profile", authRequired = true)
    object MyReports : Routes("my_reports?resolved={resolved}", authRequired = true)
    object ReportDetail : Routes("report_detail/{reportId}")
    object Settings : Routes("settings", authRequired = true)
    object Appearance : Routes("appearance", authRequired = true)
    object Accessibility : Routes("accessibility", authRequired = true)
    object Help : Routes("help", authRequired = true)

}