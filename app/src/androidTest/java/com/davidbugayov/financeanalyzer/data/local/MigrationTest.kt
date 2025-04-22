package com.davidbugayov.financeanalyzer.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.davidbugayov.financeanalyzer.data.local.database.AppDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    /**
     * Тест для проверки миграции с версии 15 на 16
     * (Добавление полей walletIds, categoryId и title)
     */
    @Test
    @Throws(IOException::class)
    fun migrate15To16() {
        // Создаем базу данных с версией 15
        helper.createDatabase(TEST_DB, 15).apply {
            // Добавляем тестовую транзакцию в базу данных версии 15
            execSQL(
                """
                INSERT INTO transactions (id, id_string, amount, category, isExpense, date, note, source, sourceColor, isTransfer)
                VALUES (1, 'test_id', '100.0,RUB', 'Test Category', 1, 1622548800000, 'Test Note', 'Test Source', 0, 0)
                """
            )
            close()
        }

        // Открываем базу данных с миграцией на версию 16
        helper.runMigrationsAndValidate(TEST_DB, 16, true).apply {
            // Проверяем, что в базе данных появились новые колонки
            val cursor = query("PRAGMA table_info(transactions)")
            
            // Создаем список имен столбцов
            val columnNames = mutableListOf<String>()
            while (cursor.moveToNext()) {
                val columnNameIndex = cursor.getColumnIndex("name")
                if (columnNameIndex != -1) {
                    columnNames.add(cursor.getString(columnNameIndex))
                }
            }
            cursor.close()
            
            // Проверяем наличие новых колонок
            assert(columnNames.contains("wallet_ids")) { "wallet_ids column should exist" }
            assert(columnNames.contains("categoryId")) { "categoryId column should exist" }
            assert(columnNames.contains("title")) { "title column should exist" }
            
            close()
        }
    }
} 