package com.davidbugayov.financeanalyzer.presentation.transaction.edit.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LocalPlay
import androidx.compose.material.icons.filled.LocalPostOffice
import androidx.compose.material.icons.filled.LocalPrintshop
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory
import com.davidbugayov.financeanalyzer.presentation.transaction.base.BaseTransactionState
import java.util.Date

/**
 * Состояние экрана редактирования транзакции.
 */
data class EditTransactionState(
    override val title: String = "",
    override val amount: String = "",
    override val amountError: Boolean = false,
    override val note: String = "",
    override val selectedDate: Date = Date(),
    override val isExpense: Boolean = true,
    override val showDatePicker: Boolean = false,
    override val showCategoryPicker: Boolean = false,
    override val showCustomCategoryDialog: Boolean = false,
    override val showCancelConfirmation: Boolean = false,
    override val showSourcePicker: Boolean = false,
    override val showCustomSourceDialog: Boolean = false,
    override val customSource: String = "",
    override val source: String = "Сбер",
    override val sourceColor: Int = 0xFF21A038.toInt(),
    override val showColorPicker: Boolean = false,
    override val isLoading: Boolean = false,
    override val error: String? = null,
    override val isSuccess: Boolean = false,
    override val successMessage: String = "Операция выполнена успешно",
    override val expenseCategories: List<UiCategory> = emptyList(),
    override val incomeCategories: List<UiCategory> = emptyList(),
    override val sources: List<Source> = emptyList(),
    override val categoryToDelete: String? = null,
    override val sourceToDelete: String? = null,
    override val showDeleteCategoryConfirmDialog: Boolean = false,
    override val showDeleteSourceConfirmDialog: Boolean = false,
    override val editMode: Boolean = true,
    override val transactionToEdit: Transaction? = null,
    override val addToWallet: Boolean = false,
    override val selectedWallets: List<String> = emptyList(),
    override val showWalletSelector: Boolean = false,
    override val targetWalletId: String? = null,
    override val forceExpense: Boolean = false,
    override val customCategory: String = "",
    override val selectedExpenseCategory: String = "",
    override val selectedIncomeCategory: String = "",
    override val sourceError: Boolean = false,
    override val preventAutoSubmit: Boolean = false,
    override val category: String = "",
    override val categoryError: Boolean = false,
    override val selectedCategory: UiCategory? = null,
    override val categories: List<UiCategory> = emptyList(),
    override val availableCategoryIcons: List<ImageVector> = emptyList(),
    override val customCategoryIcon: ImageVector? = null
) : BaseTransactionState