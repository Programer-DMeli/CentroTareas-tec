package com.carbajal.centrotareas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carbajal.centrotareas.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(viewModel: TareasViewModel = TareasViewModel()) {
    val contador by viewModel.contador.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val progreso by viewModel.progreso.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CentroTareas", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.incrementarContador() },
                        modifier = Modifier
                            .height(48.dp)
                            .wrapContentWidth(),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Tócame: $contador", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { viewModel.tareaLarga() },
                        modifier = Modifier
                            .height(48.dp)
                            .wrapContentWidth(),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Iniciar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    val porcentaje = (progreso * 100).toInt()
                    Text(
                        text = "$porcentaje %",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = mensaje,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dashed observation box for Step 3
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD))
                    .dashedBorder(
                        width = 1.5.dp,
                        color = Color(0xFF90CAF9),
                        cornerRadius = 8.dp,
                        dashLength = 6.dp,
                        gapLength = 4.dp
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "La barra avanza de 0 a 100 % en unos 4 segundos\ny el contador Tócame sigue sumando.",
                    color = Color(0xFF1565C0),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

fun Modifier.dashedBorder(
    width: Dp,
    color: Color,
    cornerRadius: Dp = 8.dp,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 4.dp
) = this.drawBehind {
    val stroke = Stroke(
        width = width.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength.toPx(), gapLength.toPx()), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    )
}
