package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun PurchaseScreen(eventId: Int, seatRow: Int, seatCol: Int, navController: NavController) {
    val viewModel: PurchaseViewModel = viewModel(
        factory = PurchaseViewModelFactory(eventId, seatRow, seatCol)
    )
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Cargando detalles del evento...")
            }
        }

        uiState.purchaseState == PurchaseResult.Success -> {
            PurchaseSuccessView(
                personName = uiState.personName,
                navController = navController
            )
        }

        else -> {
            PurchaseContentView(uiState, viewModel, seatRow, seatCol)
        }
    }
}

@Composable
fun PurchaseContentView(
    uiState: PurchaseUiState,
    viewModel: PurchaseViewModel,
    seatRow: Int,
    seatCol: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Confirmar Compra", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(32.dp))

        uiState.eventDetail?.let {
            Text(it.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Asiento: Fila $seatRow, Columna $seatCol", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Precio: $${String.format("%.2f", it.ticketPrice)}", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(48.dp))

        OutlinedTextField(
            value = uiState.personName,
            onValueChange = { viewModel.onPersonNameChange(it) },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth(0.9f),
            singleLine = true
        )
        Spacer(Modifier.height(24.dp))

        if (uiState.purchaseState == PurchaseResult.InProgress) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.onConfirmPurchase() },
                modifier = Modifier.fillMaxWidth(0.8f),
                enabled = uiState.personName.isNotBlank()
            ) {
                Text("Confirmar y Pagar")
            }
        }

        if (uiState.purchaseState == PurchaseResult.Error) {
            Spacer(Modifier.height(16.dp))
            Text("Error al procesar la compra. Por favor, inténtelo de nuevo.", color = Color.Red)
        }
    }
}

@Composable
fun PurchaseSuccessView(personName: String, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Felicidades, $personName!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Tu compra ha sido realizada con éxito.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = {
            // Navegamos de vuelta a la lista de eventos, limpiando la pila de navegación.
            navController.navigate("events") {
                popUpTo("events") { inclusive = true }
            }
        }) {
            Text("Volver a Eventos")
        }
    }
}
