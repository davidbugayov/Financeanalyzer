package com.davidbugayov.financeanalyzer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber

/**
 * Класс для управления кошельками через SharedPreferences.
 * Содержит кошельки для отображения на экране WalletScreen.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
class WalletPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )
    private val gson = Gson()

    companion object {

        private const val PREFERENCES_NAME = "wallet_prefs"
        private const val KEY_WALLETS = "wallets"

        @Volatile
        private var instance: WalletPreferences? = null

        fun getInstance(context: Context): WalletPreferences {
            return instance ?: synchronized(this) {
                instance ?: WalletPreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Сохраняет кошельки
     */
    fun saveWallets(wallets: List<Wallet>) {
        try {
            val walletsJson = gson.toJson(wallets)
            prefs.edit {
                putString(KEY_WALLETS, walletsJson)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving wallets")
        }
    }

    /**
     * Загружает кошельки
     */
    fun getWallets(): List<Wallet> {
        return try {
            val walletsJson = prefs.getString(KEY_WALLETS, null)
            if (walletsJson == null) {
                emptyList()
            } else {
                val type = object : TypeToken<List<Wallet>>() {}.type
                gson.fromJson(walletsJson, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading wallets")
            emptyList()
        }
    }

    /**
     * Добавляет новый кошелек
     */
    fun addWallet(wallet: Wallet) {
        val currentWallets = getWallets().toMutableList()
        currentWallets.add(wallet)
        saveWallets(currentWallets)
    }

    /**
     * Удаляет кошелек
     */
    fun removeWallet(walletId: String) {
        val currentWallets = getWallets().toMutableList()
        currentWallets.removeIf { it.id == walletId }
        saveWallets(currentWallets)
    }

    /**
     * Обновляет существующий кошелек
     */
    fun updateWallet(wallet: Wallet) {
        val currentWallets = getWallets().toMutableList()
        val index = currentWallets.indexOfFirst { it.id == wallet.id }
        if (index != -1) {
            currentWallets[index] = wallet
            saveWallets(currentWallets)
        }
    }
}
