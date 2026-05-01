package com.netoolhunter.app.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netoolhunter.app.NetoolHunterApp
import com.netoolhunter.app.domain.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val countsByCategory: Map<Category, Int> = Category.entries.associateWith { 0 }
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as NetoolHunterApp
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            app.catalog.tools.collect { tools ->
                _state.update {
                    it.copy(
                        countsByCategory = Category.entries.associateWith { cat ->
                            tools.count { tool -> tool.category == cat }
                        }
                    )
                }
            }
        }
    }
}
