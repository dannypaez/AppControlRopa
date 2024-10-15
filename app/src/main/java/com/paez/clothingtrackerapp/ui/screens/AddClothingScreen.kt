package com.paez.clothingtrackerapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.paez.clothingtrackerapp.viewmodel.ClothingViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@Composable
fun AddClothingScreen(
    clothingViewModel: ClothingViewModel,
    onClothingAdded: () -> Unit,
    onBackClick: () -> Unit
) {
    var clothingName by remember { mutableStateOf(TextFieldValue("")) }
    var selectedCategory by remember { mutableStateOf("Selecciona una categoría") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Predefinidas categorías
    val categories = listOf("Saco", "Chompa", "Camiseta", "Pantalón", "Otro")
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Verificar y solicitar permisos
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Lanzador para solicitar el permiso de cámara
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            errorMessage = "Es necesario el permiso de la cámara para tomar fotos."
        }
    }

    // Launcher para abrir la galería
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    // Launcher para tomar una foto con la cámara
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri = cameraImageUri
        }
    }

    // Crear un archivo temporal para la foto
    fun createImageFile(): File {
        val storageDir = context.cacheDir
        return File.createTempFile("temp_image", ".jpg", storageDir).apply {
            cameraImageUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", this)
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón de volver atrás
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver atrás")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre de la prenda
        OutlinedTextField(
            value = clothingName,
            onValueChange = { clothingName = it },
            label = { Text("Nombre de la prenda") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // DropdownMenu para seleccionar una categoría con ícono
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { expanded = true },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Icon(Icons.Filled.Category, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedCategory)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }

            // Botón para seleccionar una imagen desde la galería con ícono
            Button(
                onClick = { pickImageLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Galería")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para tomar una foto con la cámara con ícono
        Button(onClick = {
            if (hasCameraPermission) {
                createImageFile()
                takePictureLauncher.launch(cameraImageUri)
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }) {
            Icon(Icons.Filled.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tomar foto con la cámara")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar la imagen seleccionada o tomada si ya ha sido elegida
        Box(
            modifier = Modifier
                .size(150.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUri),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .size(150.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Aquí se mostrará la imagen cuando se cargue", textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para subir la prenda con la imagen a Firebase con ícono
        Button(
            onClick = {
                coroutineScope.launch {
                    if (clothingName.text.isNotEmpty() && selectedCategory != "Selecciona una categoría" && imageUri != null) {
                        isUploading = true
                        clothingViewModel.addClothingItemWithImage(
                            context,
                            clothingName.text,
                            selectedCategory,
                            imageUri!!,
                            onSuccess = {
                                isUploading = false
                                onClothingAdded()
                            },
                            onError = { error ->
                                isUploading = false
                                errorMessage = error
                            }
                        )
                    } else {
                        errorMessage = "Por favor, completa todos los campos y selecciona una imagen."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        ) {
            Icon(Icons.Filled.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isUploading) "Subiendo..." else "Añadir prenda")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mensaje de error en caso de fallo
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}