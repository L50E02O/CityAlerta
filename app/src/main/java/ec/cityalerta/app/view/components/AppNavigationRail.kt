package ec.cityalerta.app.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import ec.cityalerta.app.R
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.theme.DarkOnSurface
import ec.cityalerta.app.theme.DarkSurface

@Composable
fun AppNavigationRail(navController: NavController, ciudadId: String = "Sin ciudad") {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
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
        Routes.ReportDetail.route   
    )

    if (currentRoute?.startsWith("map") == true || currentRoute in bottomBarRoutes) {
        val startDestinationId = navController.graph.findStartDestination().id
        Surface(
            color = DarkSurface,
            modifier = Modifier.fillMaxHeight(),
            tonalElevation = 0.dp
        ) {
            NavigationRail(
                containerColor = Color.Transparent,
                contentColor = DarkOnSurface,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                NavigationRailItem(
                    selected = currentRoute == Routes.Explore.route || currentRoute == Routes.Home.route,
                    onClick = {
                        navController.navigate(Routes.Explore.route) {
                            popUpTo(startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Explore,
                            contentDescription = "EXPLORE",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = DarkOnSurface.copy(alpha = 0.6f),
                        unselectedTextColor = DarkOnSurface.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationRailItem(
                    selected = currentRoute == Routes.Post.route,
                    onClick = {
                        navController.navigate(Routes.Post.route) {
                            popUpTo(startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "REPORT",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = DarkOnSurface.copy(alpha = 0.6f),
                        unselectedTextColor = DarkOnSurface.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationRailItem(
                    selected = currentRoute?.startsWith("map") == true,
                    onClick = {
                        navController.navigate("map/Sin ciudad") {
                            popUpTo(startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "MAP",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = DarkOnSurface.copy(alpha = 0.6f),
                        unselectedTextColor = DarkOnSurface.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
