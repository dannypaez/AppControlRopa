package com.paez.clothingtrackerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paez.clothingtrackerapp.data.model.ClothingItem
import com.paez.clothingtrackerapp.data.repository.ClothingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ClothingViewModel @Inject constructor(
    private val repository: ClothingRepository
) : ViewModel() {

    private val _clothingItems = MutableStateFlow<List<ClothingItem>>(emptyList())
    val clothingItems: StateFlow<List<ClothingItem>> get() = _clothingItems

    init {
        loadClothingItems() // Cargar las prendas cuando el ViewModel se inicializa
    }

    // Función para cargar las prendas desde el repositorio
    private fun loadClothingItems() {
        viewModelScope.launch {
            repository.getClothingItems().collect { items ->
                _clothingItems.value = items
            }
        }
    }

    // Función para añadir prenda
    fun addClothingItem(item: ClothingItem) {
        viewModelScope.launch {
            repository.addClothingItem(item)
            loadClothingItems()  // Actualizar la lista después de añadir
        }
    }

    // Función para obtener una prenda por ID como flujo de datos
    fun getClothingItemById(id: String): Flow<ClothingItem?> = flow {
        val item = repository.getClothingItemById(id)
        emit(item)
    }
}
