package com.paez.clothingtrackerapp.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.paez.clothingtrackerapp.data.model.ClothingItem
import com.paez.clothingtrackerapp.viewmodel.ClothingViewModel
import kotlinx.coroutines.launch

@Composable
fun ClothingDetailScreen(
    clothingId: String,
    viewModel: ClothingViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    // Observar la prenda seleccionada desde el ViewModel
    val clothingItem by viewModel.selectedClothingItem.collectAsState()

    // Cargar la prenda cuando se abre la pantalla
    LaunchedEffect(clothingId) {
        viewModel.loadClothingItemById(clothingId)
    }

    // Mostrar indicador de carga mientras no se carga la prenda
    if (clothingItem == null) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else {
        // Mostrar detalles de la prenda
        val item = clothingItem!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = item.imagenUrl),
                contentDescription = "Imagen de ${item.nombre}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = item.nombre, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Veces puesta: ${item.vecesPuesto}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Categoría: ${item.categoría}", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acciones
            Button(onClick = {
                // Aumenta el contador de veces puesta
                viewModel.updateClothingItem(item.copy(vecesPuesto = item.vecesPuesto + 1)) { success ->
                    if (!success) {
                        Log.e("ClothingDetail", "Error al actualizar veces puesto para la prenda con ID: ${item.id}")
                    } else {
                        viewModel.loadClothingItemById(clothingId) // Recarga los datos después de la actualización
                    }
                }
            }) {
                Text("Usar")
            }

            OutlinedButton(onClick = {
                // Poner a lavar (resetear el contador)
                viewModel.updateClothingItem(item.copy(vecesPuesto = 0)) { success ->
                    if (!success) {
                        Log.e("ClothingDetail", "Error al poner a lavar")
                    } else {
                        viewModel.loadClothingItemById(clothingId) // Recarga los datos después de la actualización
                    }
                }
            }) {
                Text("Poner a lavar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para eliminar la prenda
            Button(
                onClick = {
                    viewModel.deleteClothingItem(item.id) { success ->
                        if (success) {
                            onBackClick()  // Navegar de regreso después de eliminar
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar Prenda")
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(onClick = onBackClick) {
                Text("Volver")
            }
        }
    }
}
