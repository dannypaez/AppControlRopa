package com.paez.clothingtrackerapp.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.paez.clothingtrackerapp.data.model.ClothingItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

import javax.inject.Inject

class ClothingRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    // Función para obtener todas las prendas como flujo reactivo
    fun getClothingItems(): Flow<List<ClothingItem>> = callbackFlow {
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
            val document = db.collection("ropa").document(id).get().await()
            if (document.exists()) {
                document.toObject(ClothingItem::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ClothingRepository", "Error al obtener el documento: ${e.message}")
            null
        }
    }

    // Función para agregar una nueva prenda
    suspend fun addClothingItem(clothingItem: ClothingItem): String? {
        return try {
            val docRef = db.collection("ropa").add(clothingItem).await()
            docRef.id // Devolver el ID generado por Firestore
        } catch (e: Exception) {
            Log.e("ClothingRepository", "Error al agregar la prenda: ${e.message}")
            null
        }
    }

    // Función para subir la imagen y añadir la prenda a Firestore
    suspend fun uploadImageAndAddClothingItem(
        context: Context,
        nombre: String,
        categoria: String,
        imageUri: Uri
    ): String? {
        return try {
            // Redimensionar la imagen antes de subirla
            val resizedBitmap = resizeImage(context, imageUri)
            val compressedImageUri = resizedBitmap?.let { bitmapToFile(context, it) }

            if (compressedImageUri != null) {
                // Subir la imagen a Firebase Storage
                val imageRef = storage.reference.child("clothing_images/${UUID.randomUUID()}.jpg")
                imageRef.putFile(compressedImageUri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()

                // Crear un objeto de datos para la prenda
                val clothingData = hashMapOf(
                    "nombre" to nombre,
                    "categoría" to categoria,
                    "imagenUrl" to downloadUrl,
                    "vecesPuesto" to 0
                )

                // Añadir la prenda a Firestore
                val docRef = db.collection("ropa").add(clothingData).await()
                docRef.id // Devolver el ID generado por Firestore
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ClothingRepository", "Error al subir la imagen o añadir la prenda: ${e.message}")
            null
        }
    }


    // Funciones auxiliares para redimensionar la imagen y convertir Bitmap a archivo
    private fun resizeImage(context: Context, imageUri: Uri): Bitmap? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        return originalBitmap?.let {
            Bitmap.createScaledBitmap(it, 800, 800, true)
        }
    }

    private fun bitmapToFile(context: Context, bitmap: Bitmap): Uri? {
        val file = File(context.cacheDir, "resized_image.jpg")
        val outputStream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        outputStream.flush()
        outputStream.close()

        return Uri.fromFile(file)
    }

    // Función para actualizar una prenda
    suspend fun updateClothingItem(clothingItem: ClothingItem): Boolean {
        return try {
            db.collection("ropa").document(clothingItem.id).set(clothingItem).await()
            true
        } catch (e: Exception) {
            Log.e("ClothingRepository", "Error al actualizar la prenda con ID: ${clothingItem.id}: ${e.message}")
            false
        }
    }

    // Función para eliminar una prenda por su ID
    // Verificar la existencia antes de eliminar
    suspend fun deleteClothingItem(clothingItemId: String): Boolean {
        return try {
            db.collection("ropa").document(clothingItemId).delete().await()
            Log.d("ClothingRepository", "Prenda eliminada con ID: $clothingItemId")
            true
        } catch (e: Exception) {
            Log.e("ClothingRepository", "Error al eliminar la prenda con ID: $clothingItemId: ${e.message}")
            false
        }
    }

}
