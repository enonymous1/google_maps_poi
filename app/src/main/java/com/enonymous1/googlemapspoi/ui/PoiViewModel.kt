package com.enonymous1.googlemapspoi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enonymous1.googlemapspoi.data.Poi
import com.enonymous1.googlemapspoi.data.PoiRepository
import com.enonymous1.googlemapspoi.data.PoiSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PoiViewModel(private val repository: PoiRepository) : ViewModel() {

    private val query = MutableStateFlow("")

    /** All POIs, filtered by the current search query (title, description, category). */
    val pois: StateFlow<List<Poi>> = combine(repository.observeAll(), query) { all, q ->
        PoiSearch.filter(all, q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            // Best effort: a failed legacy import must never block the map.
            runCatching { repository.migrateLegacyIfNeeded() }
        }
    }

    fun setQuery(newQuery: String) {
        query.value = newQuery
    }

    suspend fun getPoi(id: Long): Poi? = repository.getById(id)

    fun savePoi(poi: Poi, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { repository.upsert(poi) }.onSuccess { onSaved() }
        }
    }

    fun deletePoi(poi: Poi, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { repository.delete(poi) }.onSuccess { onDeleted() }
        }
    }
}

class PoiViewModelFactory(private val repository: PoiRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PoiViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return PoiViewModel(repository) as T
    }
}
