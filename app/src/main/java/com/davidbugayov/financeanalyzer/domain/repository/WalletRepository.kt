package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Wallet

/**
 * Интерфейс репозитория для работы с кошельками
 */
interface WalletRepository {

    /**
     * Получает все кошельки
     * @return Список всех кошельков
     */
    suspend fun getAllWallets(): List<Wallet>

    /**
     * Получает кошелек по ID
     * @param id ID кошелька
     * @return Кошелек с указанным ID или null, если не найден
     */
    suspend fun getWalletById(id: String): Wallet?

    /**
     * Добавляет новый кошелек
     * @param wallet Кошелек для добавления
     */
    suspend fun addWallet(wallet: Wallet)

    /**
     * Обновляет существующий кошелек
     * @param wallet Обновленный кошелек
     */
    suspend fun updateWallet(wallet: Wallet)

    /**
     * Удаляет кошелек
     * @param wallet Кошелек для удаления
     */
    suspend fun deleteWallet(wallet: Wallet)

    /**
     * Удаляет кошелек по ID
     * @param id ID кошелька для удаления
     */
    suspend fun deleteWalletById(id: String)

    /**
     * Удаляет все кошельки
     */
    suspend fun deleteAllWallets()

    /**
     * Обновляет потраченную сумму для кошелька
     * @param id ID кошелька
     * @param spent Новая потраченная сумма
     */
    suspend fun updateSpentAmount(id: String, spent: Money)

    /**
     * Проверяет, есть ли кошельки в репозитории
     * @return true, если есть хотя бы один кошелек
     */
    suspend fun hasWallets(): Boolean

    /**
     * Получает кошельки по списку ID
     * @param ids Список ID кошельков для получения
     * @return Список кошельков с указанными ID
     */
    suspend fun getWalletsByIds(ids: List<String>): List<Wallet>

    /**
     * Получает кошельки, связанные с транзакцией
     * @param transactionId ID транзакции
     * @return Список кошельков, связанных с данной транзакцией
     */
    suspend fun getWalletsForTransaction(transactionId: String): List<Wallet>
}
