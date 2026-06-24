package org.example.proserv.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import kotlinx.coroutines.launch
import org.example.proserv.data.repository.AuthRepository

/**
 * 📦 ESTADOS DE AUTENTICACIÓN: AuthUiState
 * Gestiona el flujo de sesión del usuario en Servitec.
 * El estado Success transporta el rol (admin, tecnico, cliente) para la navegación dinámica.
 */
sealed interface AuthUiState {
    object Idle : AuthUiState      // Estado inicial o sesión cerrada
    object Loading : AuthUiState   // Validando credenciales con Supabase
    data class Success(val rol: String) : AuthUiState // Acceso concedido con rol específico
    data class Error(val message: String) : AuthUiState // Fallo en login o conexión
}

/**
 * 🟢 CLASE VIEWMODEL: AuthViewModel
 * Responsable de la seguridad y el control de acceso a la aplicación.
 * Actúa como puente entre Supabase Auth y la lógica de navegación de la UI.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // 📍 ESTADO: Respalda la reactividad de la pantalla de Login
    var authUiState: AuthUiState by mutableStateOf(AuthUiState.Idle)
        private set

    init {
        // 🔑 CONTROL: Al arrancar la app, verificamos si hay un token válido guardado
        verificarSesionActiva()
    }

    /**
     * 🔑 CONTROL INTERNO: verificarSesionActiva
     * Intenta recuperar la sesión persistente para evitar que el usuario se loguee cada vez.
     */
    private fun verificarSesionActiva() {
        val email = authRepository.currentUserEmail
        if (email != null) {
            viewModelScope.launch {
                try {
                    // Si hay email en sesión, recuperamos el perfil completo para obtener el rol
                    val perfil = authRepository.obtenerPerfilUsuario(email)
                    authUiState = AuthUiState.Success(perfil.rol)
                } catch (e: Exception) {
                    // Si el token expiró o no hay red, forzamos re-login
                    authUiState = AuthUiState.Idle
                }
            }
        }
    }

    /**
     * 📝 ACCIÓN: login
     * Procesa las credenciales del usuario.
     * Realiza una doble validación: Autenticación (Auth) y Autorización (Perfil/Rol).
     */
    fun login(email: String, password: String) {
        // Validación local previa para ahorrar peticiones al servidor
        if (email.isBlank() || password.isBlank()) {
            authUiState = AuthUiState.Error("Los campos no pueden estar vacíos.")
            return
        }

        viewModelScope.launch {
            authUiState = AuthUiState.Loading
            try {
                // 🔑 PASO 1: Validación de identidad en Supabase Auth
                authRepository.loginWithEmail(email, password)

                // 🔑 PASO 2: Recuperación de permisos y rol desde la tabla 'perfiles'
                val perfil = authRepository.obtenerPerfilUsuario(email.trim())

                // 🔑 PASO 3: Emisión de éxito con el rol para redirigir al Dashboard correcto
                authUiState = AuthUiState.Success(perfil.rol)

            } catch (e: Exception) {
                // Captura errores de credenciales inválidas o fallos de red en Postgres
                authUiState = AuthUiState.Error(e.localizedMessage ?: "Error de autenticación desconocido.")
            }
        }
    }

    /**
     * 📝 ACCIÓN: logout
     * Termina la sesión actual y limpia los estados reactivos.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // 🔑 PASO ÚNICO: Notificar a Supabase y resetear estado local
                authRepository.logout()
                authUiState = AuthUiState.Idle
            } catch (e: Exception) {
                authUiState = AuthUiState.Error(e.localizedMessage ?: "Error al cerrar sesión.")
            }
        }
    }

    companion object {
        /**
         * 📦 PROVIDER: Factory
         * Inyecta el AuthRepository mediante el contenedor global de la aplicación.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as org.example.proserv.ProservApplication)
                AuthViewModel(authRepository = application.container.authRepository)
            }
        }
    }
}
