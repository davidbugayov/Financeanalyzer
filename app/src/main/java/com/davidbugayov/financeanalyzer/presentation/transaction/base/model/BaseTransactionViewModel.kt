package com.davidbugayov.financeanalyzer.presentation.transaction.base.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope

abstract class BaseTransactionViewModel<S : BaseTransactionState, E : BaseTransactionEvent> : ViewModel(), TransactionScreenViewModel<S, E> {
    protected abstract val _state: MutableStateFlow<S>
    override val state: StateFlow<S> get() = _state.asStateFlow()
    override val wallets: List<Wallet> = emptyList()

    // Добавляю protected поля для работы с категориями и источниками
    protected abstract val categoriesViewModel: com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
    protected abstract val sourcePreferences: com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences

    // Вся обработка событий теперь только в наследниках
    abstract override fun onEvent(event: E, context: android.content.Context)
    abstract override fun resetFields()
    abstract override fun updateCategoryPositions()
    abstract override fun submitTransaction(context: android.content.Context)

    open fun updateWidget(context: android.content.Context) {
        val widgetManager = android.appwidget.AppWidgetManager.getInstance(context)
        val widgetComponent = android.content.ComponentName(context, "com.davidbugayov.financeanalyzer.widget.BalanceWidget")
        val widgetIds = widgetManager.getAppWidgetIds(widgetComponent)

        if (widgetIds.isNotEmpty()) {
            val intent = android.content.Intent(context, Class.forName("com.davidbugayov.financeanalyzer.widget.BalanceWidget"))
            intent.action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            context.sendBroadcast(intent)
        }

        // Обновляем малый виджет баланса
        val smallWidgetComponent = android.content.ComponentName(context, "com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget")
        val smallWidgetIds = widgetManager.getAppWidgetIds(smallWidgetComponent)

        if (smallWidgetIds.isNotEmpty()) {
            val intent = android.content.Intent(context, Class.forName("com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget"))
            intent.action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, smallWidgetIds)
            context.sendBroadcast(intent)
        }
    }

    /**
     * Универсальный метод для обновления баланса кошельков после транзакции (доход/расход)
     */
    protected suspend fun updateWalletsAfterTransaction(
        walletRepository: WalletRepository,
        walletIds: List<String>,
        totalAmount: Money,
        isExpense: Boolean
    ) {
        if (walletIds.isEmpty()) return
        withContext(Dispatchers.IO) {
            try {
                val walletsList = walletRepository.getWalletsByIds(walletIds)
                if (walletsList.isEmpty()) return@withContext
                val amountPerWallet = totalAmount / walletsList.size
                for (wallet in walletsList) {
                    val updatedWallet = wallet.copy(
                        balance = if (isExpense) wallet.balance.minus(amountPerWallet)
                                   else wallet.balance.plus(amountPerWallet)
                    )
                    walletRepository.updateWallet(updatedWallet)
                }
            } catch (e: Exception) {
                // Можно добавить Timber.e(e) для логирования
            }
        }
    }

    /**
     * Универсальная валидация обязательных полей (amount, category, source)
     * Вызывать из наследников для обновления ошибок
     */
    protected fun validateBaseFields(
        amount: String,
        category: String,
        source: String,
        updateState: (amountError: Boolean, categoryError: Boolean, sourceError: Boolean, errorMsg: String?) -> Unit
    ): Boolean {
        var isValid = true
        val amountError = amount.isBlank()
        val categoryError = category.isBlank()
        val sourceError = source.isBlank()
        var errorMsg: String? = null
        if (amountError) isValid = false
        if (categoryError) isValid = false
        if (sourceError) isValid = false
        errorMsg = when {
            amountError && categoryError && sourceError -> "Заполните сумму, категорию и источник"
            amountError && categoryError -> "Заполните сумму и категорию"
            amountError && sourceError -> "Заполните сумму и источник"
            categoryError && sourceError -> "Заполните категорию и источник"
            amountError -> "Введите сумму транзакции"
            categoryError -> "Выберите категорию"
            sourceError -> "Выберите источник"
            else -> null
        }
        updateState(amountError, categoryError, sourceError, errorMsg)
        return isValid
    }

    /**
     * Добавляет пользовательскую категорию в соответствующий список
     */
    protected abstract fun addCustomCategory(category: String)

    /**
     * Удаляет категорию из списка категорий
     */
    protected abstract fun deleteCategory(category: String)

    /**
     * Добавляет пользовательский источник в список источников
     */
    protected abstract fun addCustomSource(source: String, color: Int)

    /**
     * Удаляет источник из списка источников
     */
    protected abstract fun deleteSource(source: String)
} 