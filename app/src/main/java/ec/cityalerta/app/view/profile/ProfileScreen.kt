package ec.cityalerta.app.view.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import ec.cityalerta.app.view.components.ProfileAvatar
import ec.cityalerta.app.navigation.Routes

@Composable
fun ProfileScreen(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF6F7F9)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(28.dp))
            ProfileAvatar(initials = "US")
            Spacer(modifier = Modifier.height(12.dp))
            Text("User", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B2633))
            Text("@Manta, Ec", fontSize = 14.sp, color = Color(0xFF6C757D))

            Spacer(modifier = Modifier.height(20.dp))
            // Stats cards
            Row(modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)) {
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("12", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("REPORTES", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("08", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("RESULTADOS", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { /* Configuracion */ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                    Text("Configuracion", color = Color(0xFF1B2633))
                }
                Button(onClick = { /* Apariencia */ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                    Text("Apariencia", color = Color(0xFF1B2633))
                }
                Button(onClick = { /* Accesibilidad */ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                    Text("Accesibilidad", color = Color(0xFF1B2633))
                }
                Button(onClick = { navController.navigate(Routes.Login.route) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))) {
                    Text("Cerrar Sesion", color = Color.White)
                }
            }
        }
    }
}
