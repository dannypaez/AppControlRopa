package com.paez.clothingtrackerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paez.clothingtrackerapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.mutableStateOf


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var loading = mutableStateOf(false)
    var errorMessage = mutableStateOf("")

    fun loginUser(email: String, password: String, onSuccess: () -> Unit) {
        loading.value = true
        viewModelScope.launch {
            authRepository.loginUser(email, password, onSuccess = {
                loading.value = false
                onSuccess()
            }, onError = {
                loading.value = false
                errorMessage.value = it
            })
        }
    }

    fun registerUser(email: String, password: String, onSuccess: () -> Unit) {
        loading.value = true
        viewModelScope.launch {
            authRepository.registerUser(email, password, onSuccess = {
                loading.value = false
                onSuccess()
            }, onError = {
                loading.value = false
                errorMessage.value = it
            })
        }
    }

    // Nueva función para cerrar sesión
    fun logoutUser() {
        authRepository.logoutUser()
    }
}
