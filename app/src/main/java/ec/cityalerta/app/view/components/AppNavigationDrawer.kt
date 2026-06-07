package ec.cityalerta.app.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ec.cityalerta.app.R
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.theme.DarkOnSurface
import ec.cityalerta.app.theme.DarkSurface

@Composable
fun AppNavigationDrawer(navController: NavController, ciudadId: String = "Sin ciudad") {
    val navigation = rememberAppNavigationContext(navController, AppNavigationRoutes.extended)
    if (navigation != null) {
        val currentRoute = navigation.currentRoute
        val startDestinationId = navigation.startDestinationId
        PermanentDrawerSheet(
            modifier = Modifier.width(240.dp),
            drawerContainerColor = DarkSurface,
            drawerContentColor = DarkOnSurface
        ) {
            Box(Modifier.padding(16.dp)) {
                Text("City Alerta", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_explore)) },
                icon = {
                    Icon(Icons.Default.Explore, contentDescription = "EXPLORE", modifier = Modifier.size(24.dp))
                },
                selected = currentRoute == Routes.Explore.route || currentRoute == Routes.Home.route,
                onClick = {
                    navController.navigate(Routes.Explore.route) {
                        popUpTo(startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = DarkOnSurface.copy(alpha = 0.6f),
                    unselectedTextColor = DarkOnSurface.copy(alpha = 0.6f),
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_report)) },
                icon = {
                    Icon(Icons.Default.Add, contentDescription = "REPORT", modifier = Modifier.size(24.dp))
                },
                selected = currentRoute == Routes.Post.route,
                onClick = {
                    navController.navigate(Routes.Post.route) {
                        popUpTo(startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = DarkOnSurface.copy(alpha = 0.6f),
                    unselectedTextColor = DarkOnSurface.copy(alpha = 0.6f),
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_map)) },
                icon = {
                    Icon(Icons.Default.Map, contentDescription = "MAP", modifier = Modifier.size(24.dp))
                },
                selected = currentRoute?.startsWith("map") == true,
                onClick = {
                    navController.navigate("map/Sin ciudad") {
                        popUpTo(startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = DarkOnSurface.copy(alpha = 0.6f),
                    unselectedTextColor = DarkOnSurface.copy(alpha = 0.6f),
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}
