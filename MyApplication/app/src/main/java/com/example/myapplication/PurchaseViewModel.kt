package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.EventDetail
import com.example.myapplication.shared.EventsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// States for the Purchase Screen UI
data class PurchaseUiState(
    val eventDetail: EventDetail? = null,
    val personName: String = "",
    val isLoading: Boolean = true,
    val purchaseState: PurchaseResult = PurchaseResult.Idle
)

enum class PurchaseResult {
    Idle,
    InProgress,
    Success,
    Error
}

class PurchaseViewModel(
    private val eventId: Int,
    private val seatRow: Int,
    private val seatCol: Int
) : ViewModel() {

    // CORRECCIÓN: Usamos el objeto Singleton directamente, sin paréntesis ()
    private val repository = EventsRepository

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    init {
        fetchEventDetails()
    }

    private fun fetchEventDetails() {
        viewModelScope.launch {
            try {
                val event = repository.getEventDetail(eventId)
                _uiState.update { it.copy(eventDetail = event, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, purchaseState = PurchaseResult.Error) }
            }
        }
    }

    fun onPersonNameChange(name: String) {
        _uiState.update { it.copy(personName = name) }
    }

    fun onConfirmPurchase() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val event = currentState.eventDetail ?: return@launch
            val personName = currentState.personName

            if (personName.isBlank()) {
                // Podríamos mostrar un error, pero por ahora el botón estará desactivado
                return@launch
            }

            _uiState.update { it.copy(purchaseState = PurchaseResult.InProgress) }

            try {
                repository.sellSeat(
                    eventId = event.id,
                    price = event.ticketPrice,
                    seatRow = seatRow,
                    seatCol = seatCol,
                    personName = personName
                )
                _uiState.update { it.copy(purchaseState = PurchaseResult.Success) }
            } catch (e: Exception) {
                _uiState.update { it.copy(purchaseState = PurchaseResult.Error) }
            }
        }
    }
}

class PurchaseViewModelFactory(
    private val eventId: Int,
    private val seatRow: Int,
    private val seatCol: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PurchaseViewModel(eventId, seatRow, seatCol) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
