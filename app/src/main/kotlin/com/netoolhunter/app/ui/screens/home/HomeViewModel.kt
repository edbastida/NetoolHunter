package com.netoolhunter.app.ui.screens.home

import androidx.lifecycle.ViewModel
import com.netoolhunter.app.data.ToolsCatalog
import com.netoolhunter.app.domain.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val countsByCategory: Map<Category, Int>
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        HomeUiState(
            countsByCategory = Category.entries.associateWith { cat ->
                ToolsCatalog.byCategory[cat]?.size ?: 0
            }
        )
    )
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
}
