package ec.cityalerta.app.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import ec.cityalerta.app.R
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.theme.DarkOnSurface
import ec.cityalerta.app.theme.DarkSurface

@Composable
fun AppBottomBar(navController: NavController, ciudadId: String = "Sin ciudad") {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Routes.Home.route,
        Routes.Explore.route,
        Routes.Post.route,
        Routes.Map.route
    )

    if (currentRoute?.startsWith("map") == true || currentRoute in bottomBarRoutes) {
        Surface(
            color = DarkSurface,
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 0.dp
        ) {
            NavigationBar(
                modifier = Modifier.height(84.dp),
                containerColor = Color.Transparent,
                contentColor = DarkOnSurface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = currentRoute == Routes.Explore.route || currentRoute == Routes.Home.route,
                    onClick = {
                        navController.navigate(Routes.Explore.route) {
                            popUpTo(Routes.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Explore,
                            contentDescription = "EXPLORE",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.nav_explore), fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = DarkOnSurface.copy(alpha = 0.6f),
                        unselectedTextColor = DarkOnSurface.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.Post.route,
                    onClick = {
                        navController.navigate(Routes.Post.route) {
                            popUpTo(Routes.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "REPORT",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    label = { Text(stringResource(R.string.nav_report), fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = DarkOnSurface.copy(alpha = 0.6f),
                        unselectedTextColor = DarkOnSurface.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = currentRoute?.startsWith("map") == true,
                    onClick = {
                        navController.navigate("map/Sin ciudad") {
                            popUpTo(Routes.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "MAP",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.nav_map), fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
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
