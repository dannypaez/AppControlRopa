package com.paez.clothingtrackerapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.paez.clothingtrackerapp.data.model.ClothingItem
import com.paez.clothingtrackerapp.viewmodel.ClothingViewModel

@Composable
fun HomeScreen(
    clothingViewModel: ClothingViewModel, // Pasar ClothingViewModel
    onAddClothingClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onClothingSelected: (ClothingItem) -> Unit
) {
    val clothingItems by clothingViewModel.clothingItems.collectAsState() // Observa el flujo de prendas
    var selectedCategory by remember { mutableStateOf("Todas") }
    val categories = listOf("Todas", "Saco", "Chompa", "Camiseta", "Pantalón", "Chaleco","Zapato","Otro")
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Centrar horizontalmente el contenido
    ) {
        // DropdownMenu para seleccionar la categoría a filtrar
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center // Centrar el Dropdown horizontalmente
        ) {
            Button(onClick = { expanded = true }) {
                Text("Filtrar según categoría: $selectedCategory")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        // Texto y logo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mis Prendas",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                imageVector = Icons.Filled.Checkroom,
                contentDescription = "Icono de Ropa",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filtrar las prendas según la categoría seleccionada
        val filteredClothingItems = if (selectedCategory == "Todas") {
            clothingItems
        } else {
            clothingItems.filter { it.categoría == selectedCategory }
        }

        // Mostrar lista de prendas o mensaje si no hay prendas
        when {
            clothingItems.isEmpty() -> {
                // Mostrar mensaje si no hay prendas
                Text(
                    text = "No tienes prendas registradas.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            filteredClothingItems.isNotEmpty() -> {
                // Mostrar lista de prendas
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                ) {
                    items(filteredClothingItems) { item ->
                        ClothingItemRow(item, onClick = { onClothingSelected(item) })
                    }
                }
            }
            else -> {
                // Mostrar mensaje si no hay prendas en la categoría seleccionada
                Text(
                    text = "No tienes prendas registradas en la categoría $selectedCategory.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para añadir prenda
        Button(
            onClick = onAddClothingClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Añadir Prenda", fontSize = 18.sp)
        }

        // Botón de cerrar sesión
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cerrar Sesión", fontSize = 18.sp)
        }
    }
}

@Composable
fun ClothingItemRow(item: ClothingItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mostrar la imagen de la prenda
        Image(
            painter = rememberAsyncImagePainter(model = item.imagenUrl),
            contentDescription = "Imagen de ${item.nombre}",
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Mostrar los detalles de la prenda
        Column {
            Text(text = item.nombre, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Veces Puesta: ${item.vecesPuesto}", style = MaterialTheme.typography.bodyMedium)
            Text(text = item.categoría, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
