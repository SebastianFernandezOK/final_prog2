package com.example.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.shared.Event
import com.example.myapplication.shared.EventsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventsViewModel : ViewModel() {

    // Usamos el objeto Singleton directamente
    private val eventsRepository = EventsRepository

    private val _eventsState = MutableStateFlow<EventsState>(EventsState.Loading)
    val eventsState: StateFlow<EventsState> = _eventsState

    init {
        getEvents()
    }

    private fun getEvents() {
        viewModelScope.launch {
            Log.d(TAG, "Attempting to fetch events...")
            _eventsState.value = EventsState.Loading
            try {
                val events = eventsRepository.getEvents()
                _eventsState.value = EventsState.Success(events)
                Log.d(TAG, "Successfully fetched ${events.size} events.")
            } catch (e: Exception) {
                _eventsState.value = EventsState.Error(e.message ?: "An unexpected error occurred")
                Log.e(TAG, "Failed to fetch events", e)
            }
        }
    }

    companion object {
        private const val TAG = "EventsViewModel"
    }
}

sealed class EventsState {
    object Loading : EventsState()
    data class Success(val events: List<Event>) : EventsState()
    data class Error(val message: String) : EventsState()
}

class EventsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
