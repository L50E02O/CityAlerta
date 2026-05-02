package ec.cityalerta.app.view.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ec.cityalerta.app.model.data.ReportType

@Composable
fun CategoryFilter(
    categories: List<ReportType>,
    modifier: Modifier = Modifier,
    selectedCategory: ReportType? = null,
    onCategoryClick: (ReportType) -> Unit
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            CategoryCard(
                category = category,
                isSelected = category == selectedCategory,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
fun CategoryCard(category: ReportType, isSelected: Boolean, onClick: () -> Unit) {
    val (icon, label, color) = when (category) {
        ReportType.RISK_ZONE -> Triple(Icons.Default.Warning, "Risk\nZones", Color(0xFFD32F2F))
        ReportType.POTHOLE -> Triple(Icons.Default.Build, "Pothole", Color(0xFF1976D2))
        ReportType.WATER -> Triple(Icons.Default.WaterDrop, "Water", Color(0xFF0288D1))
        ReportType.LIGHT -> Triple(Icons.Default.Lightbulb, "Light", Color(0xFFFBC02D))
    }

    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 85.dp, height = 95.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.1f) else Color.White
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, color) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF1B2633)
            )
        }
    }
}