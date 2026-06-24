package org.example.proserv.ui

/**
 * 📍 NAVEGACIÓN: AppDestinos
 * Define las rutas principales de la aplicación.
 * Se utiliza en el NavHost para gestionar el cambio de pantallas.
 */
enum class AppDestinos {
    LOGIN,   // Pantalla de acceso inicial
    HOME,    // Dashboard principal según rol
    LIST,    // Listados de servicios o equipos
    RECORD   // Formularios de registro y edición
}

/**
 * 📦 MÓDULOS: ModuloTipo
 * Categorización de las áreas funcionales de la app.
 * Determina qué iconos y colores se muestran en el Dashboard.
 */
enum class ModuloTipo {
    SERVICIOS, // Gestión de órdenes de trabajo
    EQUIPOS,   // Inventario de activos de clientes
    PERFILES,  // Administración de usuarios (Admin)
}

/**
 * 📝 ACCIONES: FormularioAccion
 * Define el propósito de la pantalla RecordScreen.
 * Permite reutilizar un mismo Composable para múltiples funciones lógicas.
 */
enum class FormularioAccion {
    CREAR_SERVICIO,    // Nueva orden de reparación
    EDITAR_SERVICIO,   // Modificación de orden existente
    REGISTRAR_EQUIPO,  // Alta de nuevo equipo en el sistema
    ASIGNAR_TECNICO,   // Vinculación de técnico a servicio (Solo Admin)
    CALIFICAR_SERVICIO // Feedback del cliente tras finalizar
}
