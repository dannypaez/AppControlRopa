package com.paez.clothingtrackerapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(auth: FirebaseAuth, onAuthSuccess: () -> Unit) {
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var isLoginMode by remember { mutableStateOf(true) } // Modo Login/Registro
    var errorMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo de la app o imagen decorativa
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Reemplaza con tu logo
            contentDescription = "Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Texto de Bienvenida
        Text(
            text = if (isLoginMode) "Bienvenido" else "Crea tu cuenta",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de correo electrónico con ícono
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de contraseña con ícono
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar mensaje de error si lo hay
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Login o Registro con ícono
        Button(
            onClick = {
                loading = true
                coroutineScope.launch {
                    if (isLoginMode) {
                        loginUser(auth, email.text, password.text, onSuccess = {
                            loading = false
                            onAuthSuccess()
                        }, onError = {
                            loading = false
                            errorMessage = it
                        })
                    } else {
                        registerUser(auth, email.text, password.text, onSuccess = {
                            loading = false
                            onAuthSuccess()
                        }, onError = {
                            loading = false
                            errorMessage = it
                        })
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading,
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text(text = if (isLoginMode) "Iniciar sesión" else "Registrarse", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de alternar entre Login y Registro
        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(
                text = if (isLoginMode) "¿No tienes una cuenta? Regístrate" else "¿Ya tienes una cuenta? Inicia sesión",
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Indicador de carga mientras se realiza una operación
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}
