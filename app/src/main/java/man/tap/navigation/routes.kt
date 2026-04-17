package man.tap.navigation

sealed class Routes(val route: String, val authRequired: Boolean = false){
    object Login: Routes("login")
    object Register: Routes("register")
    object Home: Routes("home")
    object Explore: Routes("explore")
    object Post: Routes("post", authRequired = true)
    object Map: Routes("map")

}