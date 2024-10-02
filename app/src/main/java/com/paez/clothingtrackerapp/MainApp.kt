package com.paez.clothingtrackerapp

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paez.clothingtrackerapp.ui.screens.AddClothingScreen
import com.paez.clothingtrackerapp.ui.screens.ClothingDetailScreen
import com.paez.clothingtrackerapp.ui.screens.HomeScreen
import com.paez.clothingtrackerapp.viewmodel.AuthViewModel
import com.paez.clothingtrackerapp.viewmodel.ClothingViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.paez.clothingtrackerapp.ui.AuthScreen

@Composable
fun MainApp() {
    val navController = rememberNavController()

    // Inyecta los ViewModel usando Hilt
    val authViewModel: AuthViewModel = hiltViewModel()
    val clothingViewModel: ClothingViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "auth") {

        // Pantalla de autenticación
        composable("auth") {
            AuthScreen(authViewModel = authViewModel, onAuthSuccess = {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }

        // Pantalla principal
        composable("home") {
            HomeScreen(
                clothingViewModel = clothingViewModel, // Pasar el ClothingViewModel
                onAddClothingClick = { navController.navigate("add_clothing") },
                onLogoutClick = {
                    authViewModel.logoutUser() // Usa el ViewModel para cerrar sesión
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onClothingSelected = { selectedItem ->
                    navController.navigate("details/${selectedItem.id}")
                }
            )
        }

        // Pantalla de añadir prenda
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

        // Pantalla de detalles de la prenda
        composable("details/{clothingId}") { backStackEntry ->
            val clothingId = backStackEntry.arguments?.getString("clothingId")

            if (clothingId == null) {
                Log.e("MainApp", "El clothingId es nulo")
                return@composable
            }

            // Usa el ClothingViewModel para obtener el ítem
            val selectedClothingItem by clothingViewModel.getClothingItemById(clothingId).collectAsState(initial = null)

            if (selectedClothingItem == null) {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize())
            } else {
                ClothingDetailScreen(clothingItem = selectedClothingItem!!, onBackClick = { navController.popBackStack() })
            }
        }
    }
}
