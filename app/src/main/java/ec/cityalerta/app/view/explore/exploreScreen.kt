package ec.cityalerta.app.view.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ec.cityalerta.app.viewmodel.ExploreViewModel
import ec.cityalerta.app.viewmodel.ProfileViewModel
import androidx.navigation.NavController
import ec.cityalerta.app.navigation.Routes
import androidx.compose.ui.res.stringResource
import ec.cityalerta.app.R

@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ExploreViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val profileState by profileViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
        profileViewModel.loadSummaryIfNeeded()
    }

    Scaffold(
        topBar = {
            ExploreHeader(
                city = state.ciudadNombre,
                profileImageUrl = profileState.profileImageUrl,
                onSearchClick = { navController.navigate(Routes.Search.route) },
                onProfileClick = { navController.navigate(Routes.Profile.route) }
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.explore_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B2633)
            )
            
            Text(
                text = stringResource(R.string.explore_subtitle),
                fontSize = 14.sp,
                color = Color(0xFF6C757D),
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1B2633))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(
                        items = state.reportes,
                        key = { it.id }
                    ) { reporte ->
                        ReporteCard(reporte = reporte)
                    }
                }
            }
        }
    }
}




