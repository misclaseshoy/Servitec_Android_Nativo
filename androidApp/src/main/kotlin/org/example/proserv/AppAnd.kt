package org.example.proserv

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.proserv.ui.*
import org.example.proserv.ui.screens.home.HomeScreen
import org.example.proserv.ui.screens.login.LoginScreen
import org.example.proserv.ui.screens.list.ListScreen
import org.example.proserv.ui.screens.login.AuthUiState
import org.example.proserv.ui.screens.login.AuthViewModel
import org.example.proserv.ui.screens.record.RecordScreen

/**
 * 🟢 FUNCIÓN COMP: AppAnd
 * Enrutador principal de la aplicación Servitec.
 * Gestiona el flujo entre pantallas basándose en el estado de autenticación y navegación manual.
 */
@Composable
fun AppAnd(
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    // 🔑 ESTADOS DE NAVEGACIÓN LOCAL
    var pantallaActual by remember { mutableStateOf(AppDestinos.LOGIN) }
    var rolUsuarioActivo by remember { mutableStateOf("") }
    var moduloSeleccionado by remember { mutableStateOf<ModuloTipo?>(null) }
    var accionSeleccionada by remember { mutableStateOf<FormularioAccion?>(null) }
    var idEntidadSeleccionada by remember { mutableStateOf<Long?>(null) } 
    var objetoSeleccionado by remember { mutableStateOf<Any?>(null) } // Para pasar DTOs completos

    val uiState = viewModel.authUiState

    // 🔑 LÓGICA DE SINCRONIZACIÓN: 
    // Forzamos que la UI responda a cambios globales en el estado de Auth.
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Idle -> {
                pantallaActual = AppDestinos.LOGIN
            }
            is AuthUiState.Success -> {
                rolUsuarioActivo = uiState.rol
                pantallaActual = AppDestinos.HOME
            }
            else -> { /* Loading o Error se manejan dentro de las pantallas */ }
        }
    }

    when (pantallaActual) {
        AppDestinos.LOGIN -> {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { rolObtenido ->
                    rolUsuarioActivo = rolObtenido
                    pantallaActual = AppDestinos.HOME
                }
            )
        }

        AppDestinos.HOME -> {
            HomeScreen(
                rol = rolUsuarioActivo,
                onNavigateToList = { modulo ->
                    moduloSeleccionado = modulo
                    pantallaActual = AppDestinos.LIST
                },
                onNavigateToRecord = { accion ->
                    accionSeleccionada = accion
                    pantallaActual = AppDestinos.RECORD
                },
                onLogout = {
                    viewModel.logout()
                }
            )
        }

        AppDestinos.LIST -> {
            moduloSeleccionado?.let { tipo ->
                ListScreen(
                    tipo = tipo,
                    onBack = { pantallaActual = AppDestinos.HOME },
                    onItemClick = { id, objeto ->
                        idEntidadSeleccionada = id
                        objetoSeleccionado = objeto
                        
                        when (tipo) {
                            ModuloTipo.EQUIPOS -> {
                                accionSeleccionada = FormularioAccion.CREAR_SERVICIO
                                pantallaActual = AppDestinos.RECORD
                            }
                            ModuloTipo.SERVICIOS -> {
                                val servicio = objeto as? org.example.proserv.data.model.ServicioDto
                                when {
                                    rolUsuarioActivo.contains("admin", true) -> {
                                        accionSeleccionada = FormularioAccion.ASIGNAR_TECNICO
                                    }
                                    rolUsuarioActivo.contains("tecnico", true) -> {
                                        accionSeleccionada = FormularioAccion.EDITAR_SERVICIO
                                    }
                                    rolUsuarioActivo.contains("cliente", true) && servicio?.estado == "finalizado" -> {
                                        accionSeleccionada = FormularioAccion.CALIFICAR_SERVICIO
                                    }
                                }
                                pantallaActual = AppDestinos.RECORD
                            }
                            ModuloTipo.PERFILES -> {}
                        }
                    }
                )
            }
        }

        AppDestinos.RECORD -> {
            accionSeleccionada?.let { accion ->
                RecordScreen(
                    accion = accion,
                    rol = rolUsuarioActivo,
                    idEntidad = idEntidadSeleccionada,
                    extraData = objetoSeleccionado,
                    onBack = { 
                        pantallaActual = AppDestinos.HOME 
                        idEntidadSeleccionada = null
                        objetoSeleccionado = null
                    },
                    onSave = { 
                        // Regresamos a la lista si estábamos en una, para forzar la recarga de datos
                        pantallaActual = if (moduloSeleccionado != null) AppDestinos.LIST else AppDestinos.HOME
                        idEntidadSeleccionada = null
                        objetoSeleccionado = null
                    }
                )
            }
        }
    }
}
