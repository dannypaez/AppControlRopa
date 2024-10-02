package com.paez.clothingtrackerapp

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ClothingDetailScreen(
    clothingItem: ClothingItem,
    onBackClick: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    // Estado observable para las veces que se ha puesto la prenda
    var vecesPuesto by remember { mutableStateOf(clothingItem.vecesPuesto) }

    // Función para incrementar el contador de veces usado
    fun incrementarUso() {
        val newVecesPuesto = vecesPuesto + 1
        db.collection("ropa").document(clothingItem.id)
            .update("vecesPuesto", newVecesPuesto)
            .addOnSuccessListener {
                vecesPuesto = newVecesPuesto
            }
            .addOnFailureListener { e ->
                Log.e("ClothingDetail", "Error al actualizar veces puesto", e)
            }
    }

    // Función para resetear el contador cuando se pone a lavar
    fun ponerALavar() {
        db.collection("ropa").document(clothingItem.id)
            .update("vecesPuesto", 0)
            .addOnSuccessListener {
                vecesPuesto = 0
            }
            .addOnFailureListener { e ->
                Log.e("ClothingDetail", "Error al poner a lavar", e)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mostrar la imagen de la prenda con mayor tamaño
        Image(
            painter = rememberAsyncImagePainter(model = clothingItem.imagenUrl),
            contentDescription = "Imagen de ${clothingItem.nombre}",
            modifier = Modifier
                .fillMaxWidth() // Asegurar que ocupe el ancho completo
                .aspectRatio(1f) // Mantener la proporción de la imagen cuadrada
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar el nombre, veces puesta y categoría de la prenda
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = clothingItem.nombre,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Veces puesta: $vecesPuesto",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Categoría: ${clothingItem.categoría}",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botones de "Usar" y "Poner a lavar"
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón para incrementar el contador de uso
            Button(
                onClick = { incrementarUso() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Usar")
            }

            // Botón para poner a lavar
            OutlinedButton(
                onClick = { ponerALavar() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Poner a lavar")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón para volver atrás
        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}
