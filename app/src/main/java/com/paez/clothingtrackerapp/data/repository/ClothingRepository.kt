package com.paez.clothingtrackerapp.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.paez.clothingtrackerapp.data.model.ClothingItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

import javax.inject.Inject

class ClothingRepository @Inject constructor( // Anotación @Inject
    private val db: FirebaseFirestore // Dependencia que puede ser inyectada
) {

    // Función para obtener todas las prendas como flujo reactivo
    fun getClothingItems(): Flow<List<ClothingItem>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("ropa")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                } else if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { document ->
                        document.toObject(ClothingItem::class.java)?.copy(id = document.id)
                    }
                    trySend(items).isSuccess
                }
            }

        awaitClose { listenerRegistration.remove() }
    }


    // Obtener una prenda por ID como función suspendida
    suspend fun getClothingItemById(id: String): ClothingItem? {
        return try {
            Log.d("ClothingRepository", "Intentando obtener prenda con ID: $id")
            val document = db.collection("ropa").document(id).get().await()

            if (document.exists()) {
                Log.d("ClothingRepository", "Documento encontrado: ${document.id}")
                val clothingItem = document.toObject(ClothingItem::class.java)?.copy(id = document.id)
                Log.d("ClothingRepository", "Objeto ClothingItem: $clothingItem")
                clothingItem
            } else {
                Log.e("ClothingRepository", "No se encontró ningún documento con ID: $id")
                null
            }
        } catch (e: Exception) {
            Log.e("ClothingRepository", "Error al obtener el documento: ${e.message}")
            null
        }
    }

    // Función para agregar una nueva prenda
    suspend fun addClothingItem(clothingItem: ClothingItem): Boolean {
        return try {
            db.collection("ropa").add(clothingItem).await()
            true
        } catch (e: Exception) {
            Log.e("ClothingRepository", "Error al agregar la prenda: ${e.message}")
            false
        }
    }

    // Función para actualizar una prenda
    suspend fun updateClothingItem(clothingItem: ClothingItem): Boolean {
        return try {
            db.collection("ropa").document(clothingItem.id).set(clothingItem).await()
            true
        } catch (e: Exception) {
            Log.e("ClothingRepository", "Error al actualizar la prenda: ${e.message}")
            false
        }
    }
}
