package ec.cityalerta.app.navigation

sealed class Routes(val route: String, val authRequired: Boolean = false) {
    object Login : Routes("login")
    object Register : Routes("register")
    object Home : Routes("home")
    object Explore : Routes("explore")
    object Post : Routes("post", authRequired = true)
    object ReporteForm : Routes("reporte_form", authRequired = true)
    object Map : Routes("map/{ciudadId}")
}