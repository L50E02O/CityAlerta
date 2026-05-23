package ec.cityalerta.app.view.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.view.components.AppTopBar
import ec.cityalerta.app.view.utils.readBytesFromUri
import ec.cityalerta.app.viewmodel.ProfileViewModel
import ec.cityalerta.app.viewmodel.UserReportUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val state by viewModel.state.collectAsState()
    var editingReport by remember { mutableStateOf<UserReportUi?>(null) }
    var reportToDelete by remember { mutableStateOf<UserReportUi?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadMyReports()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Mis reportes",
                showBack = true,
                showProfile = false,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF6F7F9))
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.myReports.isEmpty() -> {
                    Text(
                        text = "No hay reportes para mostrar",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF6C757D)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)
                    ) {
                        items(state.myReports, key = { it.id }) { report ->
                            MyReportCard(
                                report = report,
                                onEdit = { editingReport = report },
                                onDelete = { reportToDelete = report }
                            )
                        }
                    }
                }
            }
        }
    }

    editingReport?.let { report ->
        EditReportDialog(
            report = report,
            onDismiss = { editingReport = null },
            onSave = { descripcion, categoria, newImageBytes ->
                viewModel.updateReport(report, descripcion, categoria, newImageBytes)
                editingReport = null
            }
        )
    }

    reportToDelete?.let { report ->
        AlertDialog(
            onDismissRequest = { reportToDelete = null },
            title = { Text("Eliminar reporte") },
            text = { Text("Esta accion eliminara tu reporte de forma permanente.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReport(report)
                        reportToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE64B4B))
                ) { Text("Eliminar") }
            },
            dismissButton = {
                Button(onClick = { reportToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun MyReportCard(
    report: UserReportUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (report.imageUrl != null) {
                    AsyncImage(
                        model = report.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE9ECEF)),
                        contentAlignment = Alignment.Center
                    ) { Text("Imagen no disponible", color = Color(0xFFADB5BD)) }
                }

                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportTag(report.categoria.toDisplayName(), Color(0xFFE64B4B))
                    ReportTag(report.estado.name.replace("_", " "), Color.Black.copy(alpha = 0.45f))
                }
            }

            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = report.barrio,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B2633)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF3B5B7A), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(report.direccion, color = Color(0xFF3B5B7A), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(report.descripcion, color = Color(0xFF495057))

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar")
                    }
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE64B4B))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditReportDialog(
    report: UserReportUi,
    onDismiss: () -> Unit,
    onSave: (String, ReportType, ByteArray?) -> Unit
) {
    val context = LocalContext.current
    var description by remember(report.id) { mutableStateOf(report.descripcion) }
    var category by remember(report.id) { mutableStateOf(report.categoria) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var previewUri by remember(report.id) { mutableStateOf<Uri?>(null) }
    var newImageBytes by remember(report.id) { mutableStateOf<ByteArray?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            previewUri = it
            newImageBytes = readBytesFromUri(context, it)
        }
    }

    val imageModel = previewUri ?: report.imageUrl

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar reporte") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE9ECEF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Imagen del reporte",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("Sin imagen", color = Color(0xFFADB5BD))
                    }
                }

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (imageModel == null) "Agregar imagen" else "Cambiar imagen")
                }

                OutlinedTextField(
                    value = report.estado.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Estado") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color(0xFF1B1B1B),
                        disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                        disabledLabelColor = Color.Gray
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category.toDisplayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1B1B1B),
                            unfocusedTextColor = Color(0xFF1B1B1B)
                        )
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        ReportType.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.toDisplayName()) },
                                onClick = {
                                    category = item
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripcion") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1B1B1B),
                        unfocusedTextColor = Color(0xFF1B1B1B)
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(description, category, newImageBytes) }) { Text("Guardar") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ReportTag(text: String, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text.uppercase(), color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}