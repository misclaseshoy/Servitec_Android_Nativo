package org.example.proserv.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 📦 IMPORTACIÓN DE ENUMS Y RECURSOS
import org.example.proserv.ui.ModuloTipo
import org.example.proserv.ui.FormularioAccion
import org.example.proserv.R
import org.example.proserv.ui.theme.AzulEquipos
import org.example.proserv.ui.theme.VerdeClaro

/**
 * 🟢 FUNCIÓN COMPLEJA: HomeScreen
 * Esta es la pantalla principal que actúa como panel de control (Dashboard). 
 * Su estructura se divide en cuatro secciones clave: Cabecera, Cuerpo, Gestión y Navegación.
 * Implementa el diseño "Servitec Servicio" con bordes redondeados y paleta crema/azul.
 * 
 * @param rol El rol del usuario obtenido de Supabase (ej.: "administrador", "tecnico").
 * @param onNavigateToList Callback para navegar a pantallas de listado (Equipos, Usuarios, etc).
 * @param onNavigateToRecord Callback para abrir formularios (Registrar equipo, nueva orden).
 * @param onLogout Función para cerrar la sesión y retornar al flujo de Login.
 */
@Composable
fun HomeScreen(
    rol: String,
    onNavigateToList: (ModuloTipo) -> Unit,
    onNavigateToRecord: (FormularioAccion) -> Unit,
    onLogout: () -> Unit
) {
    // 🔑 PALETA: Colores corporativos extraídos del esquema de MaterialTheme
    val azulPetroleo = MaterialTheme.colorScheme.primary
    val fondoCrema = MaterialTheme.colorScheme.background
    val verdeAdmin = MaterialTheme.colorScheme.secondary
    val azulEquipos = AzulEquipos
    val azulServicios = MaterialTheme.colorScheme.tertiary

    // 🔑 LÓGICA: Evitar que la app se cierre accidentalmente desde el Home
    BackHandler(enabled = true) {
        // Al estar en Home, el botón atrás no hace nada (evita cierre)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(azulPetroleo)
    ) {
        // --- SECCIÓN 1: CABECERA ---
        // 🟢 BLOQUE: BrandingHeader - Renderiza el logo y branding principal.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 45.dp, bottom = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logser),
                contentDescription = stringResource(R.string.logo_desc),
                modifier = Modifier.size(85.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = stringResource(R.string.brand_main),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = stringResource(R.string.brand_sub),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 18.sp,
                letterSpacing = 8.sp
            )
        }

        // --- SECCIÓN 2: CUERPO PRINCIPAL ---
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = fondoCrema,
            shape = RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 25.dp, vertical = 35.dp)
                    .fillMaxSize()
            ) {
                // 🟢 BLOQUE: Mensaje de Bienvenida y Estado Operativo
                Text(
                    text = stringResource(R.string.welcome),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.ops_status),
                    fontSize = 18.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 15.dp)
                )

                // 🔑 LOGICA: RoleBadge - Indicador dinámico basado en el rol de Supabase
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 35.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = VerdeClaro,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✓", color = azulPetroleo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = rol.uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = azulPetroleo
                    )
                }

                // --- SECCIÓN 3: FILAS DE GESTIÓN ---
                // 📝 VISIBILIDAD TOTAL: Todos los roles ven las categorías principales
                
                // Módulo de Usuarios (Gestión de perfiles)
                FilaGestion(
                    label = stringResource(R.string.label_users),
                    iconId = R.drawable.usu,
                    colorBotones = verdeAdmin,
                    onEditClick = { /* Futuro: Registro manual de usuarios */ },
                    onListClick = { onNavigateToList(ModuloTipo.PERFILES) }
                )
                
                Spacer(modifier = Modifier.height(25.dp))
                
                // Módulo de Equipos (Inventario)
                FilaGestion(
                    label = stringResource(R.string.label_equipments),
                    iconId = R.drawable.equi,
                    colorBotones = azulEquipos,
                    onEditClick = if (rol.lowercase().contains("admin")) {
                        { onNavigateToRecord(FormularioAccion.REGISTRAR_EQUIPO) }
                    } else null, // 🚫 Cliente y Técnico: Sin acción en lápiz
                    onListClick = if (rol.lowercase().contains("tecnico")) null 
                                 else { { onNavigateToList(ModuloTipo.EQUIPOS) } } // 🚫 Técnico: Sin acción en ojo
                )
                
                Spacer(modifier = Modifier.height(25.dp))
                
                // Módulo de Servicios (Órdenes de trabajo)
                FilaGestion(
                    label = stringResource(R.string.label_services),
                    iconId = R.drawable.servi,
                    colorBotones = azulServicios,
                    onEditClick = null, // 🔴 BORRADO: Botón lápiz eliminado para todos los perfiles
                    onListClick = { onNavigateToList(ModuloTipo.SERVICIOS) }
                )

                Spacer(modifier = Modifier.weight(1f))

                // --- SECCIÓN 4: NAVEGACIÓN INFERIOR ---
                // 🟢 BLOQUE: DashboardNav - Barra de acciones rápidas con iconos locales
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 15.dp),
                        thickness = 1.dp,
                        color = Color.Gray.copy(alpha = 0.4f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 📍 ACCIÓN: Ver Notificaciones
                        Icon(
                            painter = painterResource(id = R.drawable.noti),
                            contentDescription = stringResource(R.string.noti_desc),
                            tint = azulPetroleo,
                            modifier = Modifier
                                .weight(1f)
                                .size(40.dp)
                                .clickable { /* 🟢 Acción: notificaciones */ }
                        )
                        // 📍 ACCIÓN: Refrescar Home
                        Icon(
                            painter = painterResource(id = R.drawable.hom),
                            contentDescription = stringResource(R.string.home_desc),
                            tint = azulPetroleo,
                            modifier = Modifier
                                .weight(1f)
                                .size(40.dp)
                                .clickable { /* 🟢 Acción: Recarga de datos del Dashboard */ }
                        )
                        // 📍 ACCIÓN: Salida Segura
                        Icon(
                            painter = painterResource(id = R.drawable.sali),
                            contentDescription = stringResource(R.string.logout_desc),
                            tint = azulPetroleo,
                            modifier = Modifier
                                .weight(1f)
                                .size(40.dp)
                                .clickable { onLogout() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 🟢 FUNCIÓN COMPLEJA: FilaGestion
 * Encapsula la lógica visual de una categoría administrativa. 
 * Combina un identificador (Icono + Label) con dos botones de acción con diseño de botones físicos.
 * 
 * @param label Título de la fila (ej: "Usuarios").
 * @param iconId Recurso drawable del icono descriptivo.
 * @param colorBotones Color de fondo para los botones de acción.
 * @param onEditClick Acción para el botón del Lápiz (Formularios).
 * @param onListClick Acción para el botón del Ojo (Listados).
 */
@Composable
fun FilaGestion(
    label: String,
    iconId: Int,
    colorBotones: Color,
    onEditClick: (() -> Unit)? = null,
    onListClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // --- BLOQUE IZQUIERDO: Branding de la categoría ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(90.dp)
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        // --- BLOQUE DERECHO: Acciones rápidas (Lápiz y Ojo) ---
        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            // 📍 ACCIÓN: Editar / Nuevo Registro
            if (onEditClick != null) {
                Surface(
                    modifier = Modifier
                        .size(width = 90.dp, height = 65.dp)
                        .clickable { onEditClick() },
                    color = colorBotones,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = stringResource(R.string.edit_action_desc),
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            // 📍 ACCIÓN: Ver Listado Completo
            if (onListClick != null) {
                Surface(
                    modifier = Modifier
                        .size(width = 90.dp, height = 65.dp)
                        .clickable { onListClick() },
                    color = colorBotones,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.view),
                            contentDescription = stringResource(R.string.view_list_action_desc),
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        rol = "administrador",
        onNavigateToList = {},
        onNavigateToRecord = {},
        onLogout = {}
    )
}
