package com.davidbugayov.financeanalyzer.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.davidbugayov.financeanalyzer.data.local.converter.DateConverter
import com.davidbugayov.financeanalyzer.data.local.dao.FinancialGoalDao
import com.davidbugayov.financeanalyzer.data.local.dao.TransactionDao
import com.davidbugayov.financeanalyzer.data.local.entity.FinancialGoalEntity
import com.davidbugayov.financeanalyzer.data.local.entity.TransactionEntity
import com.davidbugayov.financeanalyzer.domain.model.Currency

/**
 * Класс базы данных Room для приложения.
 * Содержит таблицы транзакций и финансовых целей.
 */
@Database(
    entities = [TransactionEntity::class, FinancialGoalEntity::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Предоставляет доступ к DAO для работы с транзакциями
     */
    abstract fun transactionDao(): TransactionDao
    
    /**
     * Предоставляет доступ к DAO для работы с финансовыми целями
     */
    abstract fun financialGoalDao(): FinancialGoalDao

    companion object {

        private const val DATABASE_NAME = "finance_analyzer.db"

        /**
         * Миграция с версии 1 на версию 2
         * Добавляет поле currencyCode в таблицу transactions
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Добавляем колонку currencyCode с значением по умолчанию RUB
                db.execSQL("ALTER TABLE transactions ADD COLUMN currencyCode TEXT NOT NULL DEFAULT '${Currency.RUB.code}'")
            }
        }

        /**
         * Миграция с версии 2 на версию 3
         * Делает поле title необязательным
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создаем временную таблицу с новой схемой
                db.execSQL(
                    """
                    CREATE TABLE transactions_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT,
                        amount REAL NOT NULL,
                        currencyCode TEXT NOT NULL DEFAULT '${Currency.RUB.code}',
                        category TEXT NOT NULL,
                        isExpense INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        note TEXT
                    )
                """
                )

                // Копируем данные из старой таблицы в новую
                db.execSQL(
                    """
                    INSERT INTO transactions_temp (id, title, amount, currencyCode, category, isExpense, date, note)
                    SELECT id, title, amount, currencyCode, category, isExpense, date, note FROM transactions
                """
                )

                // Удаляем старую таблицу
                db.execSQL("DROP TABLE transactions")

                // Переименовываем временную таблицу
                db.execSQL("ALTER TABLE transactions_temp RENAME TO transactions")
            }
        }

        /**
         * Миграция с версии 3 на версию 4
         * Добавляет поле source в таблицу transactions
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Добавляем колонку source с значением по умолчанию "Сбер"
                db.execSQL("ALTER TABLE transactions ADD COLUMN source TEXT NOT NULL DEFAULT 'Сбер'")
            }
        }

        /**
         * Миграция с версии 4 на версию 5
         * Добавляет поле destination в таблицу transactions
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Добавляем колонку destination с значением по умолчанию "Наличные"
                db.execSQL("ALTER TABLE transactions ADD COLUMN destination TEXT NOT NULL DEFAULT 'Наличные'")
            }
        }

        /**
         * Миграция с версии 5 на версию 6
         * Изменяет тип поля amount с REAL на TEXT для более точного хранения денежных значений
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создаем временную таблицу с новой схемой
                db.execSQL(
                    """
                    CREATE TABLE transactions_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT,
                        amount TEXT NOT NULL,
                        currencyCode TEXT NOT NULL DEFAULT '${Currency.RUB.code}',
                        category TEXT NOT NULL,
                        isExpense INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        note TEXT,
                        source TEXT NOT NULL DEFAULT 'Сбер',
                        destination TEXT NOT NULL DEFAULT 'Наличные'
                    )
                """
                )

                // Копируем данные из старой таблицы в новую, преобразуя amount в TEXT
                db.execSQL(
                    """
                    INSERT INTO transactions_temp (id, title, amount, currencyCode, category, isExpense, date, note, source, destination)
                    SELECT id, title, CAST(amount AS TEXT), currencyCode, category, isExpense, date, note, source, destination FROM transactions
                """
                )

                // Удаляем старую таблицу
                db.execSQL("DROP TABLE transactions")

                // Переименовываем временную таблицу
                db.execSQL("ALTER TABLE transactions_temp RENAME TO transactions")
            }
        }

        /**
         * Миграция с версии 6 на версию 7
         * Добавляет таблицу financial_goals
         */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создаем таблицу для финансовых целей
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS financial_goals (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        target_amount REAL NOT NULL,
                        current_amount REAL NOT NULL,
                        deadline INTEGER,
                        category TEXT,
                        description TEXT,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        is_completed INTEGER NOT NULL
                    )
                    """
                )
            }
        }

        /**
         * Миграция с версии 7 на версию 8
         * Исправляет структуру таблицы transactions, приводя ее в соответствие с TransactionEntity
         */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создаем временную таблицу с правильной схемой
                db.execSQL(
                    """
                    CREATE TABLE transactions_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount TEXT NOT NULL,
                        category TEXT NOT NULL,
                        isExpense INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        note TEXT,
                        source TEXT NOT NULL DEFAULT 'Сбер'
                    )
                    """
                )

                // Копируем данные из старой таблицы в новую, игнорируя лишние колонки
                db.execSQL(
                    """
                    INSERT INTO transactions_temp (id, amount, category, isExpense, date, note, source)
                    SELECT id, amount, category, isExpense, date, note, source FROM transactions
                    """
                )

                // Удаляем старую таблицу
                db.execSQL("DROP TABLE transactions")

                // Переименовываем временную таблицу
                db.execSQL("ALTER TABLE transactions_temp RENAME TO transactions")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Получает экземпляр базы данных.
         * Создает базу данных, если она еще не создана.
         * @param context Контекст приложения
         * @return Экземпляр базы данных
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 