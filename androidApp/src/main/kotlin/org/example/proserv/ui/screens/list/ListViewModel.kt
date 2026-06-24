package org.example.proserv.ui.screens.list

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
import org.example.proserv.data.model.ServicioDto
import org.example.proserv.data.repository.RecordRepository
import org.example.proserv.ui.ModuloTipo

sealed interface ListUiState {
    object Loading : ListUiState
    data class Success(
        val equipos: List<EquipoDto> = emptyList(),
        val servicios: List<ServicioDto> = emptyList(),
        val perfiles: List<org.example.proserv.data.model.PerfilDto> = emptyList()
    ) : ListUiState
    data class Error(val message: String) : ListUiState
}

class ListViewModel(
    private val recordRepository: RecordRepository,
    private val authRepository: org.example.proserv.data.repository.AuthRepository
) : ViewModel() {

    var uiState: ListUiState by mutableStateOf(ListUiState.Loading)
        private set

    fun cargarDatos(tipo: ModuloTipo) {
        viewModelScope.launch {
            uiState = ListUiState.Loading
            try {
                // Cargamos perfiles siempre para poder resolver nombres por ID
                val todosLosPerfiles = authRepository.obtenerTodosLosPerfiles()

                when (tipo) {
                    ModuloTipo.EQUIPOS -> {
                        val lista = recordRepository.obtenerEquipos()
                        uiState = ListUiState.Success(equipos = lista, perfiles = todosLosPerfiles)
                    }
                    ModuloTipo.SERVICIOS -> {
                        val listaServicios = recordRepository.obtenerServicios()
                        val listaEquipos = recordRepository.obtenerEquipos()
                        uiState = ListUiState.Success(
                            servicios = listaServicios, 
                            equipos = listaEquipos, 
                            perfiles = todosLosPerfiles
                        )
                    }
                    ModuloTipo.PERFILES -> {
                        uiState = ListUiState.Success(perfiles = todosLosPerfiles)
                    }
                }
            } catch (e: Exception) {
                uiState = ListUiState.Error("Error al cargar datos: ${e.message}")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ProservApplication)
                ListViewModel(
                    application.container.recordRepository,
                    application.container.authRepository
                )
            }
        }
    }
}
