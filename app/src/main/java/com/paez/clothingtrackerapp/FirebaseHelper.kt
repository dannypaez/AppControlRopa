package com.paez.clothingtrackerapp

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Función para obtener todas las prendas
suspend fun getClothingItems(): List<ClothingItem> {
    val db = FirebaseFirestore.getInstance()
    val clothingList = mutableListOf<ClothingItem>()

    try {
        val snapshot = db.collection("ropa").get().await() // Obtener la colección de ropa
        for (document in snapshot.documents) {
            val clothingItem = document.toObject(ClothingItem::class.java)
            clothingItem?.let {
                clothingList.add(it.copy(id = document.id)) // Añadimos el ID del documento
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return clothingList
}

suspend fun getClothingItemById(id: String): ClothingItem? {
    val db = FirebaseFirestore.getInstance()

    return try {
        Log.d("FirebaseHelper", "Intentando obtener prenda con ID: $id")
        val document = db.collection("ropa").document(id).get().await()

        if (document.exists()) {
            Log.d("FirebaseHelper", "Documento encontrado: ${document.id}")
            // Aquí agregamos más depuración para ver el contenido del documento
            val data = document.data
            Log.d("FirebaseHelper", "Datos del documento: $data")

            val clothingItem = document.toObject(ClothingItem::class.java)?.copy(id = document.id)
            Log.d("FirebaseHelper", "Objeto ClothingItem: $clothingItem")
            clothingItem
        } else {
            Log.e("FirebaseHelper", "No se encontró ningún documento con ID: $id")
            null
        }
    } catch (e: Exception) {
        Log.e("FirebaseHelper", "Error al obtener el documento: ${e.message}")
        null
    }
}
