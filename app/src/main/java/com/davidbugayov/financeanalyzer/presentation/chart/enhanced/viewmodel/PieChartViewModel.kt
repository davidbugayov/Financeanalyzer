package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.viewmodel


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.usecase.GetCategoriesWithAmountUseCase
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.CategoryColorProvider
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.PieChartData
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.PieChartContract
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class PieChartViewModel(
    private val getCategoriesWithAmountUseCase: GetCategoriesWithAmountUseCase,
    private val colorProvider: CategoryColorProvider
) : ViewModel() {
    
    private val _state = MutableStateFlow(PieChartContract.State())
    val state: StateFlow<PieChartContract.State> = _state.asStateFlow()
    
    private val _event = MutableSharedFlow<PieChartContract.Event>()
    val event: SharedFlow<PieChartContract.Event> = _event.asSharedFlow()

    init {
        loadData(true)
    }
    
    private fun loadData(isIncome: Boolean) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                val categoriesWithAmount = getCategoriesWithAmountUseCase(isIncome)
                val chartData = categoriesWithAmount.map { (category, amountValue) ->
                    val color = colorProvider.getColorForCategory(category.id.toString(), isIncome)
                    PieChartData(
                        id = category.id.toString(),
                        name = category.name,
                        amount = amountValue.toFloat(),
                        percentage = 0f,
                        color = color.toArgb(),
                        category = category
                    )
                }
                
                if (isIncome) {
                    _state.update { it.copy(
                        incomePieChartData = chartData,
                        isLoading = false
                    ) }
                } else {
                    _state.update { it.copy(
                        expensePieChartData = chartData,
                        isLoading = false
                    ) }
                }
                
                _event.emit(PieChartContract.Event.DataLoaded)
            } catch (e: Exception) {
                Timber.e(e, "Error loading pie chart data")
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                ) }
                _event.emit(PieChartContract.Event.Error(e.message ?: "Unknown error occurred"))
            }
        }
    }

}

