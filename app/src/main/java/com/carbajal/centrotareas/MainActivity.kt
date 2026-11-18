package com.carbajal.centrotareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.carbajal.centrotareas.ui.PantallaTareas
import com.carbajal.centrotareas.ui.theme.CentrotareasTheme
import com.carbajal.centrotareas.viewmodel.TareasViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CentrotareasTheme {
                val viewModel = TareasViewModel()
                PantallaTareas(viewModel = viewModel)
            }
        }
    }
}
