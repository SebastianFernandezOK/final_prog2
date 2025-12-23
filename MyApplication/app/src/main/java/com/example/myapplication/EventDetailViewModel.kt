package com.example.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.EventDetail
import com.example.myapplication.shared.EventsRepository
import com.example.myapplication.shared.Sale
import com.example.myapplication.shared.Seat
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Evento para comunicar navegaciones de un solo uso a la UI.
sealed class NavigationEvent {
    data class ToPurchaseConfirmation(val eventDetail: EventDetail, val seat: Seat) : NavigationEvent()
}

class EventDetailViewModel(private val eventId: Int) : ViewModel() {

    // Usamos el objeto Singleton directamente
    private val eventsRepository = EventsRepository

    private val _eventDetailState = MutableStateFlow<EventDetailState>(EventDetailState.Loading)
    val eventDetailState: StateFlow<EventDetailState> = _eventDetailState

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent

    init {
        refreshDetails() // Carga inicial de datos
    }

    fun onBlockSeatClicked(seat: Seat) {
        viewModelScope.launch {
            try {
                val currentState = _eventDetailState.value
                if (currentState is EventDetailState.Success) {
                    Log.d(TAG, "Attempting to block seat ${seat.row}-${seat.column} for event $eventId")
                    eventsRepository.blockSeat(eventId, seat)
                    Log.d(TAG, "Seat successfully blocked. Emitting navigation event.")
                    _navigationEvent.emit(NavigationEvent.ToPurchaseConfirmation(currentState.eventDetail, seat))
                } else {
                    Log.e(TAG, "onBlockSeatClicked called from an invalid state: $currentState")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to block seat", e)
                _eventDetailState.value = EventDetailState.Error("Error al bloquear el asiento. Inténtelo de nuevo.")
            }
        }
    }

    // Función pública para refrescar los datos desde la UI
    fun refreshDetails() {
        viewModelScope.launch {
            try {
                // 1. Obtener detalles del evento
                val eventDetail = eventsRepository.getEventDetail(eventId)
                
                // 2. Obtener asientos
                val seats = try {
                    Log.d(TAG, "Attempting to fetch seats with standard structure for event $eventId")
                    eventsRepository.getSeatsForEvent(eventId)
                } catch (e: JsonConvertException) {
                    Log.w(TAG, "Could not parse seats for event $eventId. API format is likely inconsistent. Defaulting to empty seat list.", e)
                    emptyList<Seat>()
                }

                // 3. Obtener historial de ventas de este evento
                val sales = try {
                    eventsRepository.getSalesForEvent(eventId).sortedByDescending { it.fechaVenta }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch sales history for event $eventId", e)
                    emptyList<Sale>()
                }

                _eventDetailState.value = EventDetailState.Success(eventDetail, seats, sales)
                Log.d(TAG, "Successfully refreshed details for: ${eventDetail.name} with ${seats.size} seats and ${sales.size} sales.")

            } catch (e: Exception) {
                _eventDetailState.value = EventDetailState.Error(e.message ?: "An unexpected error occurred")
                Log.e(TAG, "Failed to fetch critical event details for event $eventId", e)
            }
        }
    }

    companion object {
        private const val TAG = "EventDetailViewModel"
    }
}

sealed class EventDetailState {
    object Loading : EventDetailState()
    data class Success(
        val eventDetail: EventDetail, 
        val seats: List<Seat>,
        val sales: List<Sale> // Nueva propiedad: lista de ventas
    ) : EventDetailState()
    data class Error(val message: String) : EventDetailState()
}

class EventDetailViewModelFactory(private val eventId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventDetailViewModel(eventId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
