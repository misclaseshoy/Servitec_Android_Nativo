package org.example.proserv.data.repository

import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.example.proserv.data.model.EquipoDto
import org.example.proserv.data.model.ServicioDto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 🟢 INTERFAZ: RecordRepository
 * Define las operaciones permitidas sobre activos y órdenes de servicio.
 * Centraliza la lógica de persistencia para el flujo operativo de Servitec.
 */
interface RecordRepository {
    suspend fun registrarEquipo(equipo: EquipoDto)
    suspend fun crearServicio(servicio: ServicioDto)
    suspend fun actualizarServicio(id: Long, updates: Map<String, Any?>)
    suspend fun obtenerEquipos(): List<EquipoDto>
    suspend fun obtenerServicios(): List<ServicioDto>
}

/**
 * 🟢 CLASE: NetworkRecordRepository
 * Implementación de RecordRepository usando Supabase Postgrest.
 * Realiza operaciones directas sobre las tablas 'equipos' y 'servicios'.
 */
class NetworkRecordRepository(
    private val postgrest: Postgrest
) : RecordRepository {

    override suspend fun registrarEquipo(equipo: EquipoDto) {
        withContext(Dispatchers.IO) {
            postgrest.from("equipos").insert(equipo)
        }
    }

    override suspend fun crearServicio(servicio: ServicioDto) {
        withContext(Dispatchers.IO) {
            postgrest.from("servicios").insert(servicio)
        }
    }

    override suspend fun actualizarServicio(id: Long, updates: Map<String, Any?>) {
        withContext(Dispatchers.IO) {
            postgrest.from("servicios").update(
                buildJsonObject {
                    updates.forEach { (key, value) ->
                        when (value) {
                            is String -> put(key, value)
                            is Number -> put(key, value)
                            is Boolean -> put(key, value)
                            null -> put(key, JsonNull)
                        }
                    }
                }
            ) {
                filter {
                    eq("id", id)
                }
            }
        }
    }

    override suspend fun obtenerEquipos(): List<EquipoDto> = withContext(Dispatchers.IO) {
        postgrest.from("equipos").select().decodeList<EquipoDto>()
    }

    override suspend fun obtenerServicios(): List<ServicioDto> = withContext(Dispatchers.IO) {
        postgrest.from("servicios").select().decodeList<ServicioDto>()
    }
}
