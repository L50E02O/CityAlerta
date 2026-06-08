package ec.cityalerta.app.view.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import ec.cityalerta.app.navigation.Routes

object AppNavigationRoutes {
    val primary = listOf(
        Routes.Home.route,
        Routes.Explore.route,
        Routes.Post.route,
        Routes.Map.route,
    )

    val extended = listOf(
        Routes.Home.route,
        Routes.Explore.route,
        Routes.Post.route,
        Routes.Map.route,
        Routes.Search.route,
        Routes.Profile.route,
        Routes.MyReports.route,
        Routes.Settings.route,
        Routes.Appearance.route,
        Routes.Accessibility.route,
        Routes.ReportDetail.route,
    )
}

data class AppNavigationContext(
    val currentRoute: String?,
    val startDestinationId: Int,
)

private fun shouldShowNavigation(currentRoute: String?, visibleRoutes: List<String>): Boolean {
    return currentRoute?.startsWith("map") == true || currentRoute in visibleRoutes
}

@Composable
fun rememberAppNavigationContext(
    navController: NavController,
    visibleRoutes: List<String>,
): AppNavigationContext? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    if (!shouldShowNavigation(currentRoute, visibleRoutes)) {
        return null
    }
    return AppNavigationContext(
        currentRoute = currentRoute,
        startDestinationId = navController.graph.findStartDestination().id,
    )
}
