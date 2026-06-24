package org.example.proserv.data

import org.example.proserv.data.network.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest
import org.example.proserv.data.repository.AuthRepository
import org.example.proserv.data.repository.NetworkAuthRepository
import org.example.proserv.data.repository.RecordRepository
import org.example.proserv.data.repository.NetworkRecordRepository

/**
 * 🟢 INTERFAZ: AppContainer
 * Define el contrato para la inyección de dependencias manual.
 * Garantiza que los repositorios necesarios estén disponibles en toda la app.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val recordRepository: RecordRepository
}

/**
 * 🟢 CLASE: AppDataContainer
 * Implementación concreta del contenedor de dependencias.
 * Centraliza la creación de repositorios inyectando el cliente de Supabase.
 */
class AppDataContainer : AppContainer {
    
    // 🔑 REPOSITORIO: Auth (Gestión de Identidad y Roles)
    override val authRepository: AuthRepository by lazy {
        NetworkAuthRepository(SupabaseProvider.client)
    }

    // 🔑 REPOSITORIO: Record (Gestión de Inventario y Órdenes)
    override val recordRepository: RecordRepository by lazy {
        NetworkRecordRepository(SupabaseProvider.client.postgrest)
    }
}
