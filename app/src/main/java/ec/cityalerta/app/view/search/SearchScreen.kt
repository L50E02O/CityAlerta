package ec.cityalerta.app.view.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.view.components.ProfileAvatar
import ec.cityalerta.app.view.explore.ReporteCard
import ec.cityalerta.app.viewmodel.ProfileViewModel
import ec.cityalerta.app.viewmodel.SearchReportViewModel

// Pantalla de busqueda de reportes con filtros por categoria y nombre de barrio
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchReportViewModel = viewModel(),
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
            SearchHeader(
                profileImageUrl = profileState.profileImageUrl,
                onBackClick = { navController.popBackStack() },
                onProfileClick = { navController.navigate("profile") }
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

            // Campo de busqueda por nombre de barrio
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Busca por barrio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Etiqueta de filtros
            Text(
                text = "Categoria",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B2633)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtros por categoria con opcion Todos
            CategoryFilterRow(
                selectedCategory = state.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Resultados de busqueda
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1B2633))
                }
            } else if (state.reportes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No se encontraron reportes",
                        fontSize = 14.sp,
                        color = Color(0xFF6C757D)
                    )
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

            state.error?.let {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = Color(0xFFE74C3C)
                    )
                }
            }
        }
    }
}

// Header de la pantalla de busqueda con boton de regreso y perfil
@Composable
fun SearchHeader(
    profileImageUrl: String? = null,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atras",
                    tint = Color(0xFF1B2633)
                )
            }

            Text(
                text = "Buscar",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1B2633),
                modifier = Modifier.weight(1f)
            )

            ProfileAvatar(
                imageUrl = profileImageUrl,
                onClick = onProfileClick
            )
        }
    }
}

// Componente de filtros de categoria en forma de botones toggleables
@Composable
fun CategoryFilterRow(
    selectedCategory: ReportType?,
    onCategorySelected: (ReportType?) -> Unit
) {
    val categories = listOf(null) + ReportType.entries.toList()
    val categoryLabels = mapOf(
        null to "Todos",
        ReportType.ZONA_DE_RIESGO to "Seguridad",
        ReportType.BACHE to "Bache",
        ReportType.AGUA to "Agua",
        ReportType.LUZ to "Luz"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategory == category
            val label = categoryLabels[category] ?: "Desconocido"

            // Boton de categoria que se resalta cuando esta seleccionado
            Surface(
                modifier = Modifier
                    .height(40.dp)
                    .padding(end = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) Color(0xFF1B2633) else Color.White,
                onClick = { onCategorySelected(category) }
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFF1B2633)
                    )
                }
             }
         }
     }
 }
