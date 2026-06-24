package org.example.proserv.ui.screens.list

// 📦 IMPORTACIÓN DE COMPOSABLES Y RECURSOS
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.proserv.ui.ModuloTipo
import org.example.proserv.R

/**
 * 🟢 FUNCIÓN COMPLEJA: ListScreen
 * Pantalla genérica para visualizar listados de Equipos, Servicios o Usuarios.
 * Implementa una barra de búsqueda y una lista escroleable con el estilo visual Servitec.
 * 
 * @param tipo El tipo de módulo a listar (SERVICIOS, EQUIPOS, PERFILES).
 * @param onBack Callback para regresar a la pantalla anterior.
 * @param onItemClick Callback para manejar la selección de un elemento de la lista.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    tipo: ModuloTipo,
    onBack: () -> Unit,
    onItemClick: (Long, Any?) -> Unit,
    viewModel: ListViewModel = viewModel(factory = ListViewModel.Factory)
) {
    val azulPetroleo = MaterialTheme.colorScheme.primary
    val fondoCrema = MaterialTheme.colorScheme.background
    
    var searchQuery by remember { mutableStateOf("") }
    val uiState = viewModel.uiState

    // 🔑 CARGA INICIAL: Sincroniza con Supabase al entrar
    LaunchedEffect(tipo) {
        viewModel.cargarDatos(tipo)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (tipo) {
                            ModuloTipo.SERVICIOS -> stringResource(R.string.title_history_services)
                            ModuloTipo.EQUIPOS -> stringResource(R.string.title_inventory_equipments)
                            ModuloTipo.PERFILES -> stringResource(R.string.title_management_users)
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_desc),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = azulPetroleo,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = fondoCrema
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // --- SECCIÓN: ESTADOS DE CARGA Y ERROR ---
            when (uiState) {
                is ListUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = azulPetroleo)
                    }
                }
                is ListUiState.Error -> {
                    Text(uiState.message, color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is ListUiState.Success -> {
                    // --- FILTRO DE BÚSQUEDA ---
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    // --- LISTADO REACTIVO ---
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (tipo == ModuloTipo.EQUIPOS) {
                            items(uiState.equipos.filter { 
                                it.modelo?.contains(searchQuery, true) == true || it.marca?.contains(searchQuery, true) == true 
                            }) { equipo ->
                                val nombreCliente = uiState.perfiles.find { it.id == equipo.id_cliente }?.nombre ?: "Sin asignar"
                                ItemCard(
                                    titulo = "${equipo.marca} ${equipo.modelo}",
                                    subtitulo = "ID: ${equipo.id} • S/N: ${equipo.serie}\nCliente: $nombreCliente",
                                    tipo = tipo,
                                    onClick = { onItemClick(equipo.id ?: 0, equipo) }
                                )
                            }
                        } else if (tipo == ModuloTipo.SERVICIOS) {
                            items(uiState.servicios.filter { 
                                it.falla.contains(searchQuery, true) || it.estado.contains(searchQuery, true)
                            }) { servicio ->
                                val tiempoInfo = if (servicio.estado == "finalizado" && servicio.tiempoSol != null) {
                                    " • ${servicio.tiempoSol} min"
                                } else ""
                                
                                // Resolvemos el nombre del cliente a través del equipo
                                val equipo = uiState.equipos.find { it.id == servicio.id_equipo }
                                val nombreCliente = uiState.perfiles.find { it.id == equipo?.id_cliente }?.nombre ?: "---"
                                val nombreTecnico = uiState.perfiles.find { it.id == servicio.id_tecnico }?.nombre ?: "No asignado"

                                ItemCard(
                                    titulo = "Orden #${servicio.id}",
                                    subtitulo = "Cliente: $nombreCliente • Técnico: $nombreTecnico\nEquipo: ${equipo?.marca ?: ""} ${equipo?.modelo ?: ""}\n[${servicio.estado.uppercase()}]$tiempoInfo",
                                    tipo = tipo,
                                    onClick = { onItemClick(servicio.id ?: 0, servicio) }
                                )
                            }
                        } else if (tipo == ModuloTipo.PERFILES) {
                            items(uiState.perfiles.filter { 
                                it.nombre?.contains(searchQuery, true) == true || it.correo.contains(searchQuery, true)
                            }) { perfil ->
                                ItemCard(
                                    titulo = perfil.nombre ?: "Sin nombre",
                                    subtitulo = "${perfil.correo} • Tel: ${perfil.telefono ?: "S/T"}\n[${perfil.rol.uppercase()}]",
                                    tipo = tipo,
                                    onClick = { /* Acción futura: ver detalle de usuario */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🟢 COMPONENTE INTERMEDIO: ItemCard Refactorizado
 */
@Composable
fun ItemCard(titulo: String, subtitulo: String, tipo: ModuloTipo, onClick: () -> Unit) {
    val azulPetroleo = MaterialTheme.colorScheme.primary
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = azulPetroleo.copy(alpha = 0.1f),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(
                            id = when (tipo) {
                                ModuloTipo.SERVICIOS -> R.drawable.servi
                                ModuloTipo.EQUIPOS -> R.drawable.equi
                                ModuloTipo.PERFILES -> R.drawable.usu
                            }
                        ),
                        contentDescription = null,
                        tint = azulPetroleo,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = titulo, fontWeight = FontWeight.Bold, color = azulPetroleo, fontSize = 16.sp)
                Text(text = subtitulo, color = Color(0xFF424242), fontSize = 14.sp)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ListScreenPreview() {
    ListScreen(tipo = ModuloTipo.SERVICIOS, onBack = {}, onItemClick = { _, _ -> })
}
