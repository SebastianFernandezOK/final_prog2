package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.shared.EventsRepository
import com.example.myapplication.shared.Sale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel para manejar la lógica de la pantalla de ventas
class SalesViewModel : ViewModel() {
    private val repository = EventsRepository
    
    private val _salesState = MutableStateFlow<SalesState>(SalesState.Loading)
    val salesState: StateFlow<SalesState> = _salesState

    init {
        loadSales()
    }

    fun loadSales() {
        viewModelScope.launch {
            _salesState.value = SalesState.Loading
            try {
                // Obtenemos la lista, aseguramos que sea única por ID, y la ordenamos
                val sales = repository.getSales()
                    .distinctBy { it.ventaId } // PREVENCIÓN DE CRASH: Evita duplicados que rompan LazyColumn
                    .sortedByDescending { it.fechaVenta }
                _salesState.value = SalesState.Success(sales)
            } catch (e: Exception) {
                _salesState.value = SalesState.Error("Error al cargar el historial: ${e.message}")
            }
        }
    }
}

// Estados posibles de la pantalla
sealed class SalesState {
    object Loading : SalesState()
    data class Success(val sales: List<Sale>) : SalesState()
    data class Error(val message: String) : SalesState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(navController: NavController) {
    val viewModel: SalesViewModel = viewModel()
    val state by viewModel.salesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Compras") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is SalesState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SalesState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = currentState.message, color = Color.Red)
                    }
                }
                is SalesState.Success -> {
                    if (currentState.sales.isEmpty()) {
                        Text(
                            text = "No hay compras registradas.",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = currentState.sales,
                                key = { sale -> sale.ventaId } // Proporcionar una clave única
                            ) { sale ->
                                SaleItem(sale)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaleItem(sale: Sale) {
    val cardColor = if (sale.resultado) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val borderColor = if (sale.resultado) Color(0xFF43A047) else Color(0xFFE53935)
    val icon = if (sale.resultado) Icons.Default.CheckCircle else Icons.Default.Warning
    val statusText = if (sale.resultado) "Exitosa" else "Rechazada"
    val statusColor = if (sale.resultado) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = statusColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Venta #${sale.ventaId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelLarge,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Evento ID: ${sale.eventoId}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = sale.fechaVenta.substringBefore(".").replace("T", " "), 
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            if (sale.resultado) {
                Text(
                    text = "Precio: $${String.format("%.2f", sale.precioVenta)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("Asientos: ${sale.cantidadAsientos}", style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sale.descripcion,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
