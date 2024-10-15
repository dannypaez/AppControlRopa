package com.paez.clothingtrackerapp.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paez.clothingtrackerapp.data.model.ClothingItem
import com.paez.clothingtrackerapp.data.repository.ClothingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClothingViewModel @Inject constructor(
    private val repository: ClothingRepository
) : ViewModel() {

    private val _clothingItems = MutableStateFlow<List<ClothingItem>>(emptyList())
    val clothingItems: StateFlow<List<ClothingItem>> get() = _clothingItems

    private val _selectedClothingItem = MutableStateFlow<ClothingItem?>(null)
    val selectedClothingItem: StateFlow<ClothingItem?> get() = _selectedClothingItem

    init {
        loadClothingItems()
    }

    // Función para cargar las prendas desde el repositorio
    private fun loadClothingItems() {
        viewModelScope.launch {
            repository.getClothingItems().collect { items ->
                _clothingItems.value = items
            }
        }
    }

    // Nueva función para obtener una prenda por ID
    fun loadClothingItemById(id: String) {
        viewModelScope.launch {
            val item = repository.getClothingItemById(id)
            _selectedClothingItem.value = item
        }
    }

    // Función para añadir una prenda con imagen (con callbacks para manejar éxito y error)
    fun addClothingItemWithImage(
        context: Context,
        nombre: String,
        categoria: String,
        imageUri: Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val success = repository.uploadImageAndAddClothingItem(context, nombre, categoria, imageUri)
            if (success != null) {  // Si success no es nulo, la prenda fue añadida correctamente
                loadClothingItems()  // Actualizar la lista después de añadir
                onSuccess()  // Notificar éxito
            } else {
                onError("Error al subir la imagen o añadir la prenda.")  // Notificar error
            }
        }
    }

    // Función para actualizar una prenda con un callback de éxito o error
    fun updateClothingItem(item: ClothingItem, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.updateClothingItem(item)
            if (success) {
                loadClothingItems()  // Actualizar la lista después de la operación
            }
            onComplete(success)  // Llamar al callback con el éxito o fracaso
        }
    }

    // Función para eliminar una prenda con un callback de éxito o error
    fun deleteClothingItem(itemId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.deleteClothingItem(itemId)
            if (success) {
                loadClothingItems()  // Actualizar la lista después de eliminar
            }
            onComplete(success)  // Llamar al callback con el éxito o fracaso
        }
    }

    // Función suspendida para obtener una prenda por ID
    suspend fun getClothingItemById(id: String): ClothingItem? {
        return repository.getClothingItemById(id)
    }
}
