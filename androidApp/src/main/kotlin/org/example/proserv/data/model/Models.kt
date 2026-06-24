package org.example.proserv.data.model

import kotlinx.serialization.Serializable

/**
 * 📦 DTO: PerfilDto
 * Representa a un usuario en el sistema (Admin, Técnico o Cliente).
 * Vinculado directamente a la tabla 'perfiles' de Supabase.
 */
@Serializable
data class PerfilDto(
    val id: String,       // UUID único generado por Supabase Auth
    val correo: String,   // Email de acceso (Clave única de búsqueda)
    val rol: String,      // Tipo de usuario: 'admin', 'tecnico', 'cliente'
    val nombre: String?,  // Nombre completo del titular
    val telefono: String? // Contacto directo
)

/**
 * 📦 DTO: EquipoDto
 * Representa un activo físico registrado por un cliente.
 * Vinculado a la tabla 'equipos'.
 */
@Serializable
data class EquipoDto(
    val id: Long? = null,           // ID autoincremental de Postgres
    val id_cliente: String? = null, // UUID del propietario (Relación con Perfiles)
    val marca: String?,             // Fabricante (Samsung, HP, etc.)
    val modelo: String?,            // Identificador del modelo
    val serie: String?              // Número de serie único del fabricante
)

/**
 * 📦 DTO: ServicioDto
 * Representa una orden de trabajo o mantenimiento.
 * Es la entidad central del flujo operativo de Servitec.
 */
@Serializable
data class ServicioDto(
    val id: Long? = null,           // ID autoincremental de la orden
    val id_equipo: Long,            // ID del equipo vinculado
    val estado: String = "pendiente", // 'pendiente', 'iniciado', 'finalizado'
    val fechaIni: String,           // Timestamp de apertura (ISO 8601)
    val fechaFin: String? = null,   // Timestamp de cierre
    val tiempoSol: Long? = null,    // Duración total en minutos (Calculado)
    val califica: Double? = null,   // Puntuación del cliente (1-5)
    val falla: String,              // Descripción detallada del problema
    val comentaRep: String? = null, // Reporte técnico final
    val id_tecnico: String? = null  // UUID del especialista asignado
)
