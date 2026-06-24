package org.example.proserv.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.proserv.data.model.PerfilDto

/**
 * 🟢 INTERFAZ: AuthRepository
 * Define el contrato para la gestión de identidad en Servitec.
 * Separa la lógica de negocio de la implementación técnica (Supabase).
 */
interface AuthRepository {
    suspend fun loginWithEmail(email: String, password: String)
    suspend fun obtenerPerfilUsuario(correo: String): PerfilDto
    suspend fun obtenerClientes(): List<PerfilDto>
    suspend fun obtenerTecnicos(): List<PerfilDto>
    suspend fun obtenerTodosLosPerfiles(): List<PerfilDto>
    suspend fun logout()
    val currentUserEmail: String?
    val currentUserId: String?
}

/**
 * 🟢 CLASE: NetworkAuthRepository
 * Implementación de AuthRepository utilizando el SDK de Supabase.
 * Gestiona sesiones y perfiles mediante Auth y Postgrest.
 */
class NetworkAuthRepository(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    /**
     * 📝 MÉTODO: loginWithEmail
     * Inicia sesión de forma asíncrona en el hilo IO.
     */
    override suspend fun loginWithEmail(email: String, password: String) {
        withContext(Dispatchers.IO) {
            supabaseClient.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password.trim()
            }
        }
    }

    /**
     * 📝 MÉTODO: obtenerPerfilUsuario
     * Recupera el rol y datos del usuario desde la tabla 'perfiles'.
     * 🔑 CLAVE: Se filtra por el correo único del usuario.
     */
    override suspend fun obtenerPerfilUsuario(correo: String): PerfilDto {
        return withContext(Dispatchers.IO) {
            supabaseClient.postgrest["perfiles"]
                .select {
                    filter {
                        eq("correo", correo.trim())
                    }
                }
                .decodeSingle<PerfilDto>()
        }
    }

    /**
     * 📝 MÉTODO: obtenerClientes
     * Recupera todos los perfiles con rol 'cliente'.
     */
    override suspend fun obtenerClientes(): List<PerfilDto> {
        return withContext(Dispatchers.IO) {
            supabaseClient.postgrest["perfiles"]
                .select {
                    filter {
                        eq("rol", "cliente")
                    }
                }
                .decodeList<PerfilDto>()
        }
    }

    /**
     * 📝 MÉTODO: obtenerTecnicos
     * Recupera todos los perfiles con rol 'tecnico'.
     */
    override suspend fun obtenerTecnicos(): List<PerfilDto> {
        return withContext(Dispatchers.IO) {
            supabaseClient.postgrest["perfiles"]
                .select {
                    filter {
                        eq("rol", "tecnico")
                    }
                }
                .decodeList<PerfilDto>()
        }
    }

    /**
     * 📝 MÉTODO: obtenerTodosLosPerfiles
     * Recupera todos los usuarios registrados (solo para Admin).
     */
    override suspend fun obtenerTodosLosPerfiles(): List<PerfilDto> {
        return withContext(Dispatchers.IO) {
            supabaseClient.postgrest["perfiles"]
                .select()
                .decodeList<PerfilDto>()
        }
    }

    /**
     * 📝 MÉTODO: logout
     * Cierra la sesión activa y limpia los tokens locales.
     */
    override suspend fun logout() {
        withContext(Dispatchers.IO) {
            supabaseClient.auth.signOut()
        }
    }

    // 🔑 PROPIEDAD: Email del usuario en la sesión actual
    override val currentUserEmail: String?
        get() = supabaseClient.auth.currentSessionOrNull()?.user?.email

    // 🔑 PROPIEDAD: UUID del usuario en la sesión actual
    override val currentUserId: String?
        get() = supabaseClient.auth.currentSessionOrNull()?.user?.id
}
