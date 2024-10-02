package com.paez.clothingtrackerapp

import android.util.Log
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize



@Composable
fun MainApp(auth: FirebaseAuth) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(auth = auth, onAuthSuccess = {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }

        composable("home") {
            HomeScreen(
                onAddClothingClick = { navController.navigate("add_clothing") },
                onLogoutClick = {
                    auth.signOut()
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onClothingSelected = { selectedItem ->
                    navController.navigate("details/${selectedItem.id}")
                }
            )
        }

        composable("add_clothing") {
            AddClothingScreen(
                onClothingAdded = {
                    navController.popBackStack() // Navegar de regreso cuando se añada la prenda
                },
                onBackClick = {
                    navController.popBackStack() // Volver a la pantalla anterior
                }
            )
        }

        composable("details/{clothingId}") { backStackEntry ->
            val clothingId = backStackEntry.arguments?.getString("clothingId")

            // Verificar si el clothingId es nulo o incorrecto
            if (clothingId == null) {
                Log.e("MainApp", "El clothingId es nulo")
                return@composable
            }

            // Usamos un estado observable para `selectedClothingItem`
            var selectedClothingItem by remember { mutableStateOf<ClothingItem?>(null) }
            var isLoading by remember { mutableStateOf(true) }

            // Cargar la prenda usando LaunchedEffect
            LaunchedEffect(clothingId) {
                try {
                    // Obtener la prenda seleccionada
                    selectedClothingItem = withContext(Dispatchers.IO) {
                        getClothingItemById(clothingId)
                    }
                    isLoading = false
                } catch (e: Exception) {
                    Log.e("MainApp", "Error al obtener la prenda: ${e.message}")
                    isLoading = false
                }
            }

            // Mostrar un indicador de carga mientras se obtienen los datos
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize())
            } else {
                // Verificar si se obtuvo la prenda seleccionada
                selectedClothingItem?.let {
                    ClothingDetailScreen(clothingItem = it, onBackClick = { navController.popBackStack() })
                } ?: run {
                    // Mostrar un mensaje de error si no se encontró la prenda
                    Text(
                        text = "No se pudo obtener la prenda con ID: $clothingId",
                        modifier = Modifier.fillMaxSize(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
