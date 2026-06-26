package org.example.proserv.ui.screens.login

// 📦 IMPORTACIÓN DE COMPOSABLES Y RECURSOS
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import org.example.proserv.R

/**
 * 🟢 FUNCIÓN COMPLEJA: LoginScreen
 * Punto de entrada de la aplicación que gestiona la autenticación con Supabase.
 * Se encarga de observar el estado del ViewModel y disparar la navegación al éxito.
 * 
 * @param viewModel Instancia del AuthViewModel inyectada.
 * @param onLoginSuccess Callback que se ejecuta cuando el rol es validado correctamente.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit
) {
    val uiState = viewModel.authUiState

    // 🔑 LÓGICA DE NAVEGACIÓN: Escucha cambios en el estado de autenticación
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess(uiState.rol)
        }
    }

    LoginContent(
        uiState = uiState,
        onLoginClick = { email, password ->
            viewModel.login(email, password)
        }
    )
}

/**
 * 🟢 FUNCIÓN COMPLEJA: LoginContent
 * Contiene la estructura visual de la pantalla de acceso.
 * Implementa el branding corporativo con el logo y la paleta de colores Servitec.
 * 
 * @param uiState Estado actual de la UI (Idle, Loading, Success, Error).
 * @param onLoginClick Acción para intentar el inicio de sesión.
 */
@Composable
fun LoginContent(
    uiState: AuthUiState,
    onLoginClick: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // 🔑 PALETA DE COLORES SERVITEC (Desde MaterialTheme)
    val azulPetroleo = MaterialTheme.colorScheme.primary
    val fondoCrema = MaterialTheme.colorScheme.background
    val verdeAdmin = MaterialTheme.colorScheme.secondary
    val textoOscuro = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(azulPetroleo)
            .padding(horizontal = 35.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- SECCIÓN BRANDING ---
        Image(
            painter = painterResource(id = R.drawable.logser),
            contentDescription = stringResource(R.string.logo_desc),
            modifier = Modifier.size(160.dp)
        )
        Text(
            text = stringResource(R.string.brand_main),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            text = stringResource(R.string.brand_sub),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 18.sp,
            letterSpacing = 8.sp,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // --- SECCIÓN FORMULARIO ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(R.string.welcome),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.login_subtitle),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }

        // Campo: Usuario
        TextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text(text = stringResource(R.string.user_email_placeholder), color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = fondoCrema,
                unfocusedContainerColor = fondoCrema,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = textoOscuro
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Campo: Contraseña
        TextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text(text = stringResource(R.string.password_placeholder), color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                }
            },
            shape = RoundedCornerShape(15.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = fondoCrema,
                unfocusedContainerColor = fondoCrema,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = textoOscuro
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (email.isNotBlank() && password.isNotBlank()) {
                        onLoginClick(email.trim(), password.trim())
                    }
                }
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        // --- ACCIÓN: Botón Iniciar Sesión / Cargando ---
        if (uiState is AuthUiState.Loading) {
            CircularProgressIndicator(color = verdeAdmin)
        } else {
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        onLoginClick(email.trim(), password.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = verdeAdmin)
            ) {
                Text(
                    text = stringResource(R.string.login_button),
                    color = azulPetroleo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Manejo de errores
        if (uiState is AuthUiState.Error) {
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginContent(
        uiState = AuthUiState.Idle,
        onLoginClick = { _, _ -> }
    )
}
