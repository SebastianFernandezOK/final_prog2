package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.shared.EventDetail
import com.example.myapplication.shared.Integrante
import com.example.myapplication.shared.Sale
import com.example.myapplication.shared.Seat
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "EventDetailScreen"

@Composable
fun EventDetailScreen(eventId: Int, navController: NavController) {
    val viewModel: EventDetailViewModel = viewModel(factory = EventDetailViewModelFactory(eventId))
    val state by viewModel.eventDetailState.collectAsState()
    var selectedCoords by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshDetails()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is NavigationEvent.ToPurchaseConfirmation -> {
                    navController.navigate("purchase/${event.eventDetail.id}/${event.seat.row}/${event.seat.column}")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val currentState = state) {
            is EventDetailState.Loading -> CircularProgressIndicator()
            is EventDetailState.Success -> {
                // Si seleccionamos unas coordenadas pero el asiento no existe en la lista (es virtual),
                // lo creamos al vuelo con estado "libre".
                val selectedSeat = selectedCoords?.let { (row, col) ->
                    currentState.seats.find { it.row == row && it.column == col }
                        ?: Seat(row = row, column = col, status = "libre")
                }

                EventDetailContent(
                    eventDetail = currentState.eventDetail,
                    seats = currentState.seats,
                    sales = currentState.sales, // Pasamos las ventas a la vista
                    selectedCoords = selectedCoords,
                    onSeatSelected = {
                        val newCoords = it.row to it.column
                        selectedCoords = if (selectedCoords == newCoords) null else newCoords
                    },
                    onBlockSeatClicked = {
                        selectedSeat?.let { viewModel.onBlockSeatClicked(it) }
                    }
                )
            }
            is EventDetailState.Error -> Text(text = "Error: ${currentState.message}")
        }
    }
}

@Composable
fun EventDetailContent(
    eventDetail: EventDetail,
    seats: List<Seat>,
    sales: List<Sale>,
    selectedCoords: Pair<Int, Int>?,
    onSeatSelected: (Seat) -> Unit,
    onBlockSeatClicked: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { EventoInfo(eventDetail) }

        if (eventDetail.members.isNotEmpty()) {
            item {
                Text("Integrantes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            }
            itemsIndexed(eventDetail.members, key = { index, member ->
                if (member.identification.isNotBlank()) member.identification else "member-$index"
            }) { _, member ->
                IntegranteItem(member, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
        }

        item {
            Divider(modifier = Modifier.padding(top = 16.dp))
            SeatSelection(eventDetail, seats, selectedCoords, onSeatSelected, onBlockSeatClicked)
        }

        // Nueva sección: Historial de Compras del Evento
        if (sales.isNotEmpty()) {
            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    text = "Historial de Ventas del Evento",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
            // Reutilizamos el SaleItem que definimos en SalesScreen.kt
            items(sales) { sale ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SaleItem(sale)
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun EventoInfo(eventDetail: EventDetail) {
    eventDetail.imageUrl?.let {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(it).crossfade(true).build(),
            contentDescription = "Event Image",
            modifier = Modifier.fillMaxWidth().height(250.dp), contentScale = ContentScale.Crop
        )
    }
    Column(modifier = Modifier.padding(16.dp)) {
        Text(eventDetail.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(eventDetail.summary, style = MaterialTheme.typography.titleMedium, fontStyle = FontStyle.Italic, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        eventDetail.eventType?.name?.let {
            Text(it.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(Modifier.height(16.dp))
        Text(eventDetail.description, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        Divider()
        InfoRow("Dirección", eventDetail.address)
        InfoRow("Fecha", eventDetail.date.substringBefore("T"))
        InfoRow("Capacidad", "${eventDetail.seatRows} x ${eventDetail.seatColumns} asientos")
        InfoRow("Precio", "$${String.format("%.2f", eventDetail.ticketPrice)}")
    }
}

@Composable
fun SeatSelection(
    eventDetail: EventDetail,
    seats: List<Seat>,
    selectedCoords: Pair<Int, Int>?,
    onSeatSelected: (Seat) -> Unit,
    onBlockSeatClicked: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Selecciona tu asiento", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        ColorLegend()
        Spacer(modifier = Modifier.height(8.dp))
        SeatGrid(eventDetail, seats, selectedCoords, onSeatSelected)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onBlockSeatClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedCoords != null
        ) {
            Text(if (selectedCoords != null) "Comprar Asiento ${selectedCoords.first}-${selectedCoords.second}" else "Selecciona un asiento")
        }
    }
}

@Composable
fun ColorLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = Color.Green.copy(alpha = 0.5f), text = "Libre")
        LegendItem(color = Color.Red.copy(alpha = 0.5f), text = "Vendido")
        LegendItem(color = Color.DarkGray.copy(alpha = 0.5f), text = "Bloqueado")
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SeatGrid(
    eventDetail: EventDetail,
    seats: List<Seat>,
    selCoords: Pair<Int, Int>?,
    onSeatClick: (Seat) -> Unit
) {
    val seatMap = seats.associateBy { it.row to it.column }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        (1..eventDetail.seatRows).forEach { row ->
            Row(horizontalArrangement = Arrangement.Center) {
                (1..eventDetail.seatColumns).forEach { col ->
                    val seatFromApi = seatMap[row to col]
                    val seatToDisplay = seatFromApi ?: Seat(row = row, column = col, status = "libre")
                    
                    val isSelected = selCoords?.let { it.first == row && it.second == col } ?: false
                    
                    SeatItem(seatToDisplay, isSelected) { onSeatClick(seatToDisplay) }
                }
            }
        }
    }
}

@Composable
fun SeatItem(seat: Seat, isSelected: Boolean, onSeatClick: () -> Unit) {
    val trimmedStatus = seat.status.trim()
    val isAvailable = trimmedStatus.equals("libre", ignoreCase = true)

    val color = when {
        isSelected && isAvailable -> MaterialTheme.colorScheme.primary
        isAvailable -> Color.Green.copy(alpha = 0.5f)
        trimmedStatus.equals("Bloqueado", ignoreCase = true) -> Color.DarkGray.copy(alpha = 0.5f)
        else -> Color.Red.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(24.dp)
            .background(color, RoundedCornerShape(4.dp))
            .border(2.dp, if (isSelected && isAvailable) MaterialTheme.colorScheme.onPrimary else Color.Transparent, RoundedCornerShape(4.dp))
            .clickable(enabled = isAvailable, onClick = onSeatClick)
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("$label:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun IntegranteItem(member: Integrante, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${member.identification} ${member.firstName} ${member.lastName}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
