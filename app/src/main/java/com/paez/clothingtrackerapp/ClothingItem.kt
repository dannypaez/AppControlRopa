package com.paez.clothingtrackerapp

data class ClothingItem(
    val id: String = "",
    val nombre: String = "",
    val categoría: String = "",
    val imagenUrl: String = "",
    val vecesPuesto: Int = 0
)
