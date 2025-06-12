package com.davidbugayov.financeanalyzer.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Базовый интерфейс для всех репозиториев
 * Следует принципу Interface Segregation Principle (ISP)
 * @param T тип сущности, с которой работает репозиторий
 * @param ID тип идентификатора сущности
 */
interface BaseRepository<T, ID> {
    /**
     * Получает сущность по идентификатору
     * @param id идентификатор сущности
     * @return сущность или null, если не найдена
     */
    suspend fun getById(id: ID): T?

    /**
     * Получает поток всех сущностей
     * @return Flow со списком всех сущностей
     */
    fun getAll(): Flow<List<T>>

    /**
     * Добавляет новую сущность
     * @param item сущность для добавления
     * @return идентификатор добавленной сущности
     */
    suspend fun add(item: T): ID

    /**
     * Обновляет существующую сущность
     * @param item сущность для обновления
     */
    suspend fun update(item: T)

    /**
     * Удаляет сущность по идентификатору
     * @param id идентификатор сущности для удаления
     * @return true если сущность была удалена, false если сущность не найдена
     */
    suspend fun delete(id: ID): Boolean
}
