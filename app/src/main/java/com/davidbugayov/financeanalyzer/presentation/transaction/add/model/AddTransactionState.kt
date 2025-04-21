package com.davidbugayov.financeanalyzer.presentation.transaction.add.model

import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.transaction.base.BaseTransactionState
import java.util.Date
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp

/**
 * Состояние экрана добавления транзакции.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
data class AddTransactionState(
    override val title: String = "",
    override val amount: String = "",
    override val amountError: Boolean = false,
    override val category: String = "",
    override val categoryError: Boolean = false,
    override val note: String = "",
    override val selectedDate: Date = Date(),
    override val isExpense: Boolean = true,
    override val showDatePicker: Boolean = false,
    override val showCategoryPicker: Boolean = false,
    override val showCustomCategoryDialog: Boolean = false,
    override val showCancelConfirmation: Boolean = false,
    override val customCategory: String = "",
    override val showSourcePicker: Boolean = false,
    override val showCustomSourceDialog: Boolean = false,
    override val customSource: String = "",
    override val source: String = "Сбер",
    override val sourceColor: Int = 0xFF21A038.toInt(), // Цвет Сбера
    override val showColorPicker: Boolean = false,
    override val isLoading: Boolean = false,
    override val error: String? = null,
    override val isSuccess: Boolean = false,
    override val successMessage: String = "Операция выполнена успешно",
    override val expenseCategories: List<CategoryItem> = emptyList(),
    override val incomeCategories: List<CategoryItem> = emptyList(),
    override val sources: List<Source> = emptyList(),
    override val categoryToDelete: String? = null,
    override val sourceToDelete: String? = null,
    override val showDeleteCategoryConfirmDialog: Boolean = false,
    override val showDeleteSourceConfirmDialog: Boolean = false,
    override val addToWallet: Boolean = false, // Добавлять ли доход в кошелек (по умолчанию - нет)
    override val selectedWallets: List<String> = emptyList(), // Список ID выбранных кошельков
    override val showWalletSelector: Boolean = false, // Показывать ли диалог выбора кошельков
    override val targetWalletId: String? = null, // ID целевого кошелька для добавления дохода
    override val forceExpense: Boolean = false, // Принудительно использовать тип "Расход"
    override val editMode: Boolean = false, // Режим редактирования (по умолчанию выключен)
    override val transactionToEdit: Transaction? = null, // Транзакция для редактирования
    override val sourceError: Boolean = false,
    override val preventAutoSubmit: Boolean = false, // Предотвращать автоматическую отправку формы
    val walletError: Boolean = false,
    val dateError: Boolean = false,
    override val selectedExpenseCategory: String = "",
    override val selectedIncomeCategory: String = "",
    override val customCategoryIcon: ImageVector = Icons.Default.MoreHoriz,
    override val availableCategoryIcons: List<ImageVector> = listOf(
        Icons.Default.MoreHoriz,
        Icons.Default.Add,
        Icons.Default.ShoppingCart,
        Icons.Default.Fastfood,
        Icons.Default.Home,
        Icons.Default.DirectionsCar,
        Icons.Default.Movie,
        Icons.Default.Restaurant,
        Icons.Default.LocalHospital,
        Icons.Default.Work,
        Icons.Default.School,
        Icons.Default.Flight,
        Icons.Default.Pets,
        Icons.Default.CardGiftcard,
        Icons.Default.SportsSoccer,
        Icons.Default.Phone,
        Icons.Default.Computer,
        Icons.Default.CreditCard,
        Icons.Default.AttachMoney,
        Icons.Default.Savings,
        Icons.Default.EmojiEvents,
        Icons.Default.LocalCafe,
        Icons.Default.LocalAtm,
        Icons.Default.ChildCare,
        Icons.Default.LocalBar,
        Icons.Default.LocalGasStation,
        Icons.Default.LocalLaundryService,
        Icons.Default.LocalLibrary,
        Icons.Default.LocalMall,
        Icons.Default.LocalPharmacy,
        Icons.Default.LocalPizza,
        Icons.Default.LocalPlay,
        Icons.Default.LocalPostOffice,
        Icons.Default.LocalPrintshop,
        Icons.Default.LocalTaxi,
        Icons.Default.LocalFlorist,
        Icons.Default.LocalGroceryStore,
        Icons.Default.MonetizationOn,
        Icons.Default.Receipt,
        Icons.Default.SportsBasketball,
        Icons.Default.SportsTennis,
        Icons.Default.Train,
        Icons.Default.Wifi,
        Icons.Default.Watch,
        Icons.Default.WbSunny,
        Icons.Default.Star,
        Icons.Default.Favorite,
        Icons.Default.DirectionsBus,
        Icons.Default.StarBorder
    )
) : BaseTransactionState