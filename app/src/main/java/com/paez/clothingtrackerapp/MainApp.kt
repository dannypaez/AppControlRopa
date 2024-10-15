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
import com.paez.clothingtrackerapp.data.model.ClothingItem
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
                clothingViewModel = clothingViewModel,
                onAddClothingClick = { navController.navigate("add_clothing") },
                onLogoutClick = {
                    authViewModel.logoutUser()
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onClothingSelected = { selectedItem ->
                    // Navegar con el ID de la prenda seleccionada
                    navController.navigate("details/${selectedItem.id}")
                }
            )
        }

        // Pantalla de añadir prenda
        composable("add_clothing") {
            AddClothingScreen(
                clothingViewModel = clothingViewModel, // Pasar el ClothingViewModel
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

            // Cargar la prenda utilizando la nueva función del ViewModel
            LaunchedEffect(clothingId) {
                clothingViewModel.loadClothingItemById(clothingId)
            }

            val selectedClothingItem by clothingViewModel.selectedClothingItem.collectAsState()

            if (selectedClothingItem == null) {
                // Muestra un indicador de carga mientras se busca la prenda
                CircularProgressIndicator(modifier = Modifier.fillMaxSize())
            } else {
                // Si la prenda fue encontrada, mostramos la pantalla de detalles
                ClothingDetailScreen(
                    clothingId = clothingId,  // Pasar el ID de la prenda
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
