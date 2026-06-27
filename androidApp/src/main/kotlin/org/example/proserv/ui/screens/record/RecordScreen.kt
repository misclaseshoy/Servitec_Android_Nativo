package org.example.proserv.ui.screens.record

// 📦 IMPORTACIÓN DE DEPENDENCIAS Y MODELOS

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import org.example.proserv.ui.FormularioAccion
import org.example.proserv.data.model.ServicioDto
import org.example.proserv.R

/**
 * 🟢 FUNCIÓN COMPLEJA: RecordScreen
 * Pantalla multipropósito para el ingreso de datos en Servitec.
 * Se adapta dinámicamente para registrar equipos, crear servicios o asignar técnicos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    accion: FormularioAccion,
    rol: String,
    idEntidad: Long? = null,
    extraData: Any? = null,
    onBack: () -> Unit,
    onSave: () -> Unit,
    viewModel: RecordViewModel = viewModel(factory = RecordViewModel.Factory)
) {
    // 🔑 PALETA: Colores corporativos
    val azulPetroleo = MaterialTheme.colorScheme.primary
    val verdeAdmin = MaterialTheme.colorScheme.secondary
    val fondoOscuro = MaterialTheme.colorScheme.background

    // 🔑 ESTADOS DE FORMULARIO
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }
    var text3 by remember { mutableStateOf("") }
    var text4 by remember { mutableStateOf(idEntidad?.toString() ?: "") }
    var text5 by remember { mutableStateOf("") }
    var text6 by remember { mutableStateOf("") }

    val state = viewModel.uiState
    val successMsg = stringResource(R.string.msg_operation_success)
    var showSuccessOverlay by remember { mutableStateOf(false) }
    val servicio = extraData as? ServicioDto
    val focusManager = LocalFocusManager.current

    // 🔑 LÓGICA: Navegación nativa
    BackHandler { onBack() }

    LaunchedEffect(state) {
        if (state is RecordUiState.Success) {
            showSuccessOverlay = true
            delay(1600)
            showSuccessOverlay = false
            onSave() 
            viewModel.resetState()
        }
    }

    LaunchedEffect(accion) {
        if (accion == FormularioAccion.REGISTRAR_EQUIPO) viewModel.cargarClientes()
        if (accion == FormularioAccion.ASIGNAR_TECNICO || (accion == FormularioAccion.CREAR_SERVICIO && rol.lowercase().contains("admin"))) {
            viewModel.cargarTecnicos()
        }
        if (accion == FormularioAccion.CREAR_SERVICIO) viewModel.cargarEquipos()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (accion) {
                            FormularioAccion.REGISTRAR_EQUIPO -> stringResource(R.string.title_register_equipment)
                            FormularioAccion.ASIGNAR_TECNICO -> stringResource(R.string.title_assign_technician)
                            FormularioAccion.CALIFICAR_SERVICIO -> stringResource(R.string.title_rate_service)
                            FormularioAccion.EDITAR_SERVICIO -> {
                                val estadoActual = servicio?.estado?.lowercase()?.trim()?.replace("'", "")?.replace("\"", "") ?: ""
                                if (estadoActual == "pendiente") stringResource(R.string.title_start_service, idEntidad ?: 0)
                                else stringResource(R.string.title_finish_order_simple)
                            }
                            else -> stringResource(R.string.title_new_service)
                        },
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back_desc), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = azulPetroleo)
            )
        },
        containerColor = fondoOscuro
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        when (accion) {
                            FormularioAccion.REGISTRAR_EQUIPO -> {
                                FormField(
                                    label = stringResource(R.string.label_brand),
                                    placeholder = stringResource(R.string.placeholder_brand),
                                    value = text2,
                                    onValueChange = { text2 = it },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                FormField(
                                    label = stringResource(R.string.label_model),
                                    placeholder = stringResource(R.string.placeholder_model),
                                    value = text1,
                                    onValueChange = { text1 = it },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                FormField(
                                    label = stringResource(R.string.label_serial),
                                    placeholder = stringResource(R.string.placeholder_serial),
                                    value = text3,
                                    onValueChange = { text3 = it },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(stringResource(R.string.label_client_email), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = azulPetroleo.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                                    TextField(
                                        value = text5, onValueChange = {}, readOnly = true,
                                        placeholder = { Text(stringResource(R.string.placeholder_client_email), color = Color.Gray) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                                    )
                                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        viewModel.clientes.forEach { cliente ->
                                            DropdownMenuItem(text = { Text("${cliente.nombre ?: ""} (${cliente.correo})") }, onClick = { text5 = cliente.correo; expanded = false })
                                        }
                                    }
                                }
                            }
                            FormularioAccion.ASIGNAR_TECNICO -> {
                                Text(stringResource(R.string.assigning_tech_to_service, idEntidad ?: 0), fontWeight = FontWeight.Bold, color = azulPetroleo)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(stringResource(R.string.label_tech_email), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = azulPetroleo.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                                var expandedTech by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(expanded = expandedTech, onExpandedChange = { expandedTech = !expandedTech }) {
                                    TextField(
                                        value = text6, onValueChange = {}, readOnly = true,
                                        placeholder = { Text(stringResource(R.string.placeholder_select_tech), color = Color.Gray) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTech) },
                                        modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                                    )
                                    ExposedDropdownMenu(expanded = expandedTech, onDismissRequest = { expandedTech = false }) {
                                        viewModel.tecnicos.forEach { tech ->
                                            DropdownMenuItem(text = { Text("${tech.nombre ?: ""} (${tech.correo})") }, onClick = { text6 = tech.correo; expandedTech = false })
                                        }
                                    }
                                }
                            }
                            FormularioAccion.EDITAR_SERVICIO -> {
                                val estadoActual = servicio?.estado?.lowercase()?.trim()?.replace("'", "")?.replace("\"", "") ?: ""
                                when (estadoActual) {
                                    "pendiente" -> {
                                        Text("Presione iniciar servicio", fontWeight = FontWeight.ExtraBold, color = azulPetroleo, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(stringResource(R.string.msg_start_service_confirm, idEntidad ?: 0), color = Color.Gray)
                                    }
                                    "iniciado" -> {
                                        Text(stringResource(R.string.title_finish_order, idEntidad ?: 0), fontWeight = FontWeight.Bold, color = azulPetroleo, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        FormField(
                                            label = stringResource(R.string.label_tech_report),
                                            placeholder = stringResource(R.string.placeholder_tech_report),
                                            isLong = true,
                                            value = text2,
                                            onValueChange = { text2 = it },
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                                        )
                                    }
                                    "finalizado" -> {
                                        Text("Orden #$idEntidad Finalizada", fontWeight = FontWeight.Bold, color = azulPetroleo, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        if (servicio?.tiempoSol != null) {
                                            Text("Tiempo de solución: ${servicio.tiempoSol} min", fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        FormField(
                                            label = stringResource(R.string.label_tech_report),
                                            placeholder = "",
                                            isLong = true,
                                            value = servicio?.comentaRep ?: "",
                                            onValueChange = {},
                                            readOnly = true
                                        )
                                    }
                                    else -> {
                                        // Caso de seguridad: Si no hay estado o es desconocido
                                        Text("Estado del servicio: ${servicio?.estado ?: "Desconocido"}", fontWeight = FontWeight.Bold, color = azulPetroleo)
                                    }
                                }
                            }
                            FormularioAccion.CALIFICAR_SERVICIO -> {
                                Text(stringResource(R.string.rate_service_id, idEntidad ?: 0), fontWeight = FontWeight.Bold, color = azulPetroleo)
                                Spacer(modifier = Modifier.height(16.dp))
                                FormField(
                                    label = stringResource(R.string.label_rating),
                                    placeholder = stringResource(R.string.placeholder_rating),
                                    value = text3,
                                    onValueChange = { text3 = it },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                                )
                            }
                            else -> {
                                FormField(
                                    label = stringResource(R.string.label_service_title),
                                    placeholder = stringResource(R.string.placeholder_service_title),
                                    value = text1,
                                    onValueChange = { text1 = it },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                FormField(
                                    label = stringResource(R.string.label_description),
                                    placeholder = stringResource(R.string.placeholder_description),
                                    isLong = true,
                                    value = text2,
                                    onValueChange = { text2 = it },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(stringResource(R.string.label_equipment_id), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = azulPetroleo.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                                var expandedEq by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(expanded = expandedEq, onExpandedChange = { expandedEq = !expandedEq }) {
                                    TextField(
                                        value = if (text4.isBlank()) "" else "ID: $text4", onValueChange = {}, readOnly = true,
                                        placeholder = { Text(stringResource(R.string.placeholder_select_equipment), color = Color.Gray) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEq) },
                                        modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                                    )
                                    ExposedDropdownMenu(expanded = expandedEq, onDismissRequest = { expandedEq = false }) {
                                        viewModel.equipos.forEach { eq ->
                                            DropdownMenuItem(text = { Text("${eq.marca} ${eq.modelo} (S/N: ${eq.serie})") }, onClick = { text4 = eq.id.toString(); expandedEq = false })
                                        }
                                    }
                                }
                                if (rol.lowercase().contains("admin")) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(stringResource(R.string.label_tech_optional), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = azulPetroleo.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                                    var expandedTechCreate by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(expanded = expandedTechCreate, onExpandedChange = { expandedTechCreate = !expandedTechCreate }) {
                                        TextField(
                                            value = text6, onValueChange = {}, readOnly = true,
                                            placeholder = { Text(stringResource(R.string.placeholder_optional), color = Color.Gray) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTechCreate) },
                                            modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                                        )
                                        ExposedDropdownMenu(expanded = expandedTechCreate, onDismissRequest = { expandedTechCreate = false }) {
                                            viewModel.tecnicos.forEach { tech ->
                                                DropdownMenuItem(text = { Text("${tech.nombre ?: ""} (${tech.correo})") }, onClick = { text6 = tech.correo; expandedTechCreate = false })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (state is RecordUiState.Error) {
                    Text(text = state.message, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
                }

                Button(
                    onClick = {
                        val estadoActual = servicio?.estado?.lowercase()?.trim()?.replace("'", "")?.replace("\"", "") ?: ""
                        when (accion) {
                            FormularioAccion.REGISTRAR_EQUIPO -> viewModel.registrarEquipo(text2, text1, text3, text5)
                            FormularioAccion.ASIGNAR_TECNICO -> viewModel.asignarTecnico(idEntidad ?: 0L, text6)
                            FormularioAccion.CALIFICAR_SERVICIO -> viewModel.calificarServicio(idEntidad ?: 0L, text3.toDoubleOrNull() ?: 0.0)
                            FormularioAccion.EDITAR_SERVICIO -> {
                                when (estadoActual) {
                                    "pendiente" -> viewModel.iniciarServicio(idEntidad ?: 0L)
                                    "iniciado" -> viewModel.finalizarServicio(idEntidad ?: 0L, text2, servicio?.fechaIni ?: "")
                                    else -> onBack()
                                }
                            }
                            else -> viewModel.crearServicio(text1, text2, text4.toLongOrNull() ?: 0L, text6)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (accion == FormularioAccion.REGISTRAR_EQUIPO) azulPetroleo else verdeAdmin),
                    enabled = state !is RecordUiState.Loading
                ) {
                    if (state is RecordUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        val estadoActual = servicio?.estado?.lowercase()?.trim()?.replace("'", "")?.replace("\"", "") ?: ""
                        val label = when {
                            accion == FormularioAccion.EDITAR_SERVICIO && estadoActual == "pendiente" -> stringResource(R.string.btn_start_service)
                            accion == FormularioAccion.EDITAR_SERVICIO && estadoActual == "iniciado" -> stringResource(R.string.title_finish_order_simple)
                            accion == FormularioAccion.EDITAR_SERVICIO && estadoActual == "finalizado" -> "VOLVER"
                            else -> stringResource(R.string.save)
                        }
                        Text(text = label, color = if (accion == FormularioAccion.REGISTRAR_EQUIPO) Color.White else azulPetroleo, fontWeight = FontWeight.Bold)
                    }
                }
            }

            AnimatedVisibility(visible = showSuccessOverlay, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.Center)) {
                Surface(color = Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(32.dp)) {
                    Box(modifier = Modifier.padding(40.dp), contentAlignment = Alignment.Center) {
                        Text(text = successMsg.uppercase(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FormField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isLong: Boolean = false,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        TextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().heightIn(min = if (isLong) 120.dp else 56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            readOnly = readOnly
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecordScreenPreview() {
    RecordScreen(accion = FormularioAccion.CREAR_SERVICIO, rol = "admin", onBack = {}, onSave = {})
}
