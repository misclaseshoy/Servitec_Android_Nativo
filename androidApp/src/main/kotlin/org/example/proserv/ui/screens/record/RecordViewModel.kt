package org.example.proserv.ui.screens.record


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import org.example.proserv.ProservApplication
import org.example.proserv.data.model.EquipoDto
import org.example.proserv.data.model.PerfilDto
import org.example.proserv.data.model.ServicioDto
import org.example.proserv.data.repository.AuthRepository
import org.example.proserv.data.repository.RecordRepository

/**
 * 🔑 ESTADOS DE INTERFAZ: RecordUiState
 * Define la máquina de estados para la reactividad de los formularios.
 */
sealed interface RecordUiState {
    object Idle : RecordUiState      // Estado inicial: en espera de interacción
    object Loading : RecordUiState   // Procesando: comunicación activa con Supabase
    object Success : RecordUiState   // Éxito: operación confirmada en base de datos
    data class Error(val message: String) : RecordUiState // Error: fallo en validación o red
}

/**
 * 🟢 CLASE VIEWMODEL: RecordViewModel
 * Gestiona la persistencia de equipos y servicios bajo las reglas RLS de Supabase.
 * Centraliza la lógica de negocio para la creación y edición de registros.
 */
class RecordViewModel(
    private val recordRepository: RecordRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // 🔑 ESTADO REACTIVO: Controla el feedback visual de la pantalla
    var uiState: RecordUiState by mutableStateOf(RecordUiState.Idle)
        private set

    // 🔑 ESTADO: Lista de clientes para el selector en formularios
    var clientes by mutableStateOf<List<PerfilDto>>(emptyList())
        private set

    // 🔑 ESTADO: Lista de técnicos para la asignación
    var tecnicos by mutableStateOf<List<PerfilDto>>(emptyList())
        private set

    // 🔑 ESTADO: Lista de equipos para elegir al crear servicio
    var equipos by mutableStateOf<List<EquipoDto>>(emptyList())
        private set

    /**
     * 🟢 FUNCIÓN: cargarClientes
     * Recupera la lista de usuarios con rol 'cliente' desde Supabase.
     */
    fun cargarClientes() {
        viewModelScope.launch {
            try {
                clientes = authRepository.obtenerClientes()
            } catch (e: Exception) {
                // Manejo de error silencioso para la lista
            }
        }
    }

    /**
     * 🟢 FUNCIÓN: cargarTecnicos
     * Recupera la lista de usuarios con rol 'tecnico' desde Supabase.
     */
    fun cargarTecnicos() {
        viewModelScope.launch {
            try {
                tecnicos = authRepository.obtenerTecnicos()
            } catch (e: Exception) {
                // Manejo de error silencioso
            }
        }
    }

    /**
     * 🟢 FUNCIÓN: cargarEquipos
     * Recupera los equipos (RLS filtrará por cliente o permitirá todos al admin).
     */
    fun cargarEquipos() {
        viewModelScope.launch {
            try {
                equipos = recordRepository.obtenerEquipos()
            } catch (e: Exception) {
                // Manejo de error
            }
        }
    }

    /**
     * 🟢 FUNCIÓN: registrarEquipo
     * Permite a un 'administrador' vincular un nuevo equipo a un cliente existente.
     * 🔑 LÓGICA CLAVE: Resuelve el correo del cliente -> Verifica permisos -> Persiste en DB.
     */
    fun registrarEquipo(marca: String, modelo: String, serie: String, emailCliente: String?) {
        viewModelScope.launch {
            uiState = RecordUiState.Loading
            
            // 🔑 VALIDACIÓN: El correo es indispensable para la búsqueda de la FK
            if (emailCliente.isNullOrBlank()) {
                uiState = RecordUiState.Error("Se requiere el correo del propietario")
                return@launch
            }

            try {
                // 🔑 LÓGICA CLAVE: Resolución de identidad del cliente mediante su correo único
                val perfilCliente = try {
                    authRepository.obtenerPerfilUsuario(emailCliente.trim())
                } catch (e: Exception) {
                    null
                }

                if (perfilCliente == null) {
                    uiState = RecordUiState.Error("El cliente '${emailCliente.trim()}' no existe")
                    return@launch
                }

                // 🔑 LÓGICA CLAVE: Inserción sujeta a política RLS 'administrador'
                val equipo = EquipoDto(
                    marca = marca,
                    modelo = modelo,
                    serie = serie,
                    id_cliente = perfilCliente.id // Vinculación mediante UUID
                )
                
                recordRepository.registrarEquipo(equipo)
                uiState = RecordUiState.Success

            } catch (e: Exception) {
                // 🛑 NOTA: Si falla aquí, verificar que el usuario logueado tenga rol 'administrador' en Supabase
                uiState = RecordUiState.Error("Error: No tienes permisos o los datos son inválidos")
            }
        }
    }

    /**
     * 🟢 FUNCIÓN: crearServicio
     * Registra una nueva orden de soporte técnico en el sistema.
     * 🔑 LÓGICA CLAVE: Validación de técnico (opcional) -> Registro de falla -> Persistencia.
     */
    fun crearServicio(titulo: String, descripcion: String, idEquipo: Long, idTecnico: String?) {
        viewModelScope.launch {
            uiState = RecordUiState.Loading
            try {
                // 🔑 LÓGICA CLAVE: Buscar UUID localmente si ya está cargado para evitar fallos de RLS
                val tecnicoUuid = tecnicos.find { it.correo.equals(idTecnico?.trim(), ignoreCase = true) }?.id 
                    ?: idTecnico?.takeIf { it.isNotBlank() }?.let { correo ->
                        authRepository.obtenerPerfilUsuario(correo.trim()).id
                    }

                // 🔑 LÓGICA CLAVE: Mapeo de datos al DTO de servicio
                val servicio = ServicioDto(
                    id_equipo = idEquipo,
                    falla = "$titulo: $descripcion",
                    fechaIni = getIsoTimestamp(),
                    id_tecnico = tecnicoUuid,
                    estado = "pendiente" // El estado inicial por defecto según regla
                )
                recordRepository.crearServicio(servicio)
                
                // ✅ ÉXITO: Orden creada correctamente
                uiState = RecordUiState.Success
            } catch (e: Exception) {
                uiState = RecordUiState.Error("Error: ${e.message ?: "Verifique los datos o permisos"}")
            }
        }
    }

    /**
     * 🟢 FUNCIÓN: asignarTecnico
     * RESERVADO PARA ADMIN: Vincula un especialista a una orden de servicio existente.
     * 🔑 LÓGICA CLAVE: Búsqueda de técnico por correo -> Actualización parcial (Patch).
     */
    fun asignarTecnico(idServicio: Long, emailTecnico: String) {
        viewModelScope.launch {
            uiState = RecordUiState.Loading
            try {
                // 🔑 RESOLUCIÓN LOCAL: Buscamos el UUID en la lista que ya tenemos cargada.
                val tecnicoUuid = tecnicos.find { it.correo.trim().equals(emailTecnico.trim(), ignoreCase = true) }?.id

                if (tecnicoUuid == null) {
                    uiState = RecordUiState.Error("No se encontró el técnico. Verifique el correo.")
                    return@launch
                }

                // 🔑 ACTUALIZACIÓN: Se asigna técnico y el estado permanece en pendiente hasta que el técnico lo inicie.
                recordRepository.actualizarServicio(idServicio, mapOf("id_tecnico" to tecnicoUuid, "estado" to "pendiente"))
                uiState = RecordUiState.Success
            } catch (e: Exception) {
                uiState = RecordUiState.Error("Error DB: ${e.message ?: "Fallo al asignar"}")
            }
        }
    }

    /**
     * 🟢 FUNCIÓN: calificarServicio
     * PERMITIDO PARA CLIENTE: El cliente califica y se cierra definitivamente.
     */
    fun calificarServicio(idServicio: Long, calificacion: Double) {
        viewModelScope.launch {
            uiState = RecordUiState.Loading
            try {
                // Según regla: Solo cliente califica. El estado final es 'finalizado'.
                recordRepository.actualizarServicio(idServicio, mapOf("califica" to calificacion))
                uiState = RecordUiState.Success
            } catch (e: Exception) {
                uiState = RecordUiState.Error("Error al calificar el servicio")
            }
        }
    }

    /**
     * 🟢 FUNCIÓN: iniciarServicio
     * RESERVADO PARA TÉCNICO: Cambia el estado de la orden a 'iniciado' y marca la fecha de inicio real.
     */
    fun iniciarServicio(idServicio: Long) {
        viewModelScope.launch {
            uiState = RecordUiState.Loading
            try {
                // Al iniciar, actualizamos la fechaIni para que el cálculo de tiempo_sol sea preciso (de iniciado a finalizado)
                val now = getIsoTimestamp()
                recordRepository.actualizarServicio(idServicio, mapOf("estado" to "iniciado", "fechaIni" to now))
                uiState = RecordUiState.Success
            } catch (e: Exception) {
                uiState = RecordUiState.Error("Error al iniciar servicio")
            }
        }
    }

    /**
     * 🟢 FUNCIÓN: finalizarServicio
     * RESERVADO PARA TÉCNICO: Cierra la orden con un reporte y cálculo de tiempo.
     * 🔑 LÓGICA CLAVE: Calcula tiempoSol (fechaFin - fechaIni) automáticamente.
     */
    fun finalizarServicio(idServicio: Long, comentario: String, fechaIniStr: String) {
        viewModelScope.launch {
            uiState = RecordUiState.Loading
            try {
                val now = java.util.Date()
                val instantFin = getIsoTimestamp(now)
                
                // Parseamos la fecha de inicio para calcular el tiempo de solución en minutos
                val start = parseIsoTimestamp(fechaIniStr)
                val diffMs = now.time - (start?.time ?: now.time)
                
                // Calculamos minutos con decimales (ej: 2.85 min)
                val minutos = diffMs / 60000.0

                val updates = mapOf(
                    "comentaRep" to comentario,
                    "fechaFin" to instantFin,
                    "tiempoSol" to minutos,
                    "estado" to "finalizado"
                )

                recordRepository.actualizarServicio(idServicio, updates)
                uiState = RecordUiState.Success
            } catch (e: Exception) {
                uiState = RecordUiState.Error("Error al finalizar servicio: ${e.message}")
            }
        }
    }

    /**
     * 🟢 FUNCIÓN PRIVADA: getIsoTimestamp
     * Genera un timestamp compatible con el tipo 'timestamp' de PostgreSQL.
     * Usa UTC y milisegundos para cumplir con el formato ISO 8601 requerido.
     */
    private fun getIsoTimestamp(date: java.util.Date = java.util.Date()): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    /**
     * 🟢 FUNCIÓN PRIVADA: parseIsoTimestamp
     * Maneja múltiples formatos de fecha para evitar errores de cálculo (0 min).
     */
    private fun parseIsoTimestamp(isoString: String): java.util.Date? {
        // Limpieza de caracteres de zona horaria de Postgres para el parser de Java
        val cleanIso = isoString.replace("Z", "").replace("+00", "").replace("T", " ")
        
        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        )
        for (format in formats) {
            try {
                val sdf = java.text.SimpleDateFormat(format, java.util.Locale.US)
                if (format.contains("'Z'")) sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                return sdf.parse(cleanIso)
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    /**
     * 🟢 FUNCIÓN: resetState
     * Limpia el estado de la UI tras una operación exitosa o error.
     */
    fun resetState() {
        uiState = RecordUiState.Idle
    }

    companion object {
        /**
         * 🟢 FACTORY: Creación del ViewModel con inyección de dependencias
         * Inyecta los repositorios necesarios desde el contenedor global de la aplicación.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ProservApplication)
                RecordViewModel(
                    application.container.recordRepository,
                    application.container.authRepository
                )
            }
        }
    }
}
