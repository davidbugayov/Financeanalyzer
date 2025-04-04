package com.davidbugayov.financeanalyzer.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.davidbugayov.financeanalyzer.data.local.converter.DateConverter
import com.davidbugayov.financeanalyzer.data.local.dao.TransactionDao
import com.davidbugayov.financeanalyzer.data.local.entity.TransactionEntity
import com.davidbugayov.financeanalyzer.domain.model.Currency

/**
 * Класс базы данных Room для приложения.
 * Содержит таблицу транзакций.
 */
@Database(
    entities = [TransactionEntity::class],
    version = 12,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Предоставляет доступ к DAO для работы с транзакциями
     */
    abstract fun transactionDao(): TransactionDao

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
                // Добавляем колонку source с значением по умолчанию "Наличные"
                db.execSQL("ALTER TABLE transactions ADD COLUMN source TEXT NOT NULL DEFAULT 'Наличные'")
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
         * Оставляем пустую миграцию, т.к. таблица financial_goals больше не нужна
         */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Миграция не требуется
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

        /**
         * Миграция с версии 8 на версию 9
         * Обновляет схему таблицы после изменения значений по умолчанию для source
         */
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Эта миграция не изменяет структуру таблицы,
                // а только синхронизирует схему Room с фактической схемой базы данных
                // Можно добавить пустой блок или выполнить SQL для обновления данных
                // Например, обновить источник "Сбер" на "Наличные" если нужно
                db.execSQL("UPDATE transactions SET source = 'Наличные' WHERE source = 'Сбер'")
            }
        }

        /**
         * Миграция с версии 9 на версию 10
         * Добавляет поле sourceColor в таблицу transactions
         */
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Добавляем колонку sourceColor с NULL значением по умолчанию
                db.execSQL("ALTER TABLE transactions ADD COLUMN sourceColor INTEGER")
                
                // Обновляем цвета для стандартных источников
                db.execSQL("UPDATE transactions SET sourceColor = 0xFF21A038 WHERE source = 'Сбер'")
                db.execSQL("UPDATE transactions SET sourceColor = 0xFFFFDD2D WHERE source = 'Т-Банк'")
                db.execSQL("UPDATE transactions SET sourceColor = 0xFFEF3124 WHERE source = 'Альфа'")
            }
        }

        /**
         * Миграция с версии 10 на версию 11
         * Устанавливает значение для sourceColor по умолчанию, чтобы использовался метод getEffectiveSourceColor()
         */
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Устанавливаем значение по умолчанию 0 для всех записей
                // Это заставит использовать логику метода getEffectiveSourceColor()
                db.execSQL("UPDATE transactions SET sourceColor = 0 WHERE sourceColor IS NULL")
            }
        }

        /**
         * Миграция с версии 11 на версию 12
         * Исправляет структуру таблицы transactions для решения проблемы с миграцией
         */
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создаем новую таблицу с правильной структурой
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS transactions_new (
                        id INTEGER PRIMARY KEY NOT NULL,
                        amount TEXT NOT NULL,
                        category TEXT NOT NULL,
                        isExpense INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        note TEXT,
                        source TEXT NOT NULL DEFAULT 'Наличные',
                        sourceColor INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                // Копируем данные из старой таблицы в новую
                db.execSQL("""
                    INSERT OR IGNORE INTO transactions_new (id, amount, category, isExpense, date, note, source, sourceColor)
                    SELECT id, amount, category, isExpense, date, note, source, sourceColor FROM transactions
                """)
                
                // Удаляем старую таблицу
                db.execSQL("DROP TABLE IF EXISTS transactions")
                
                // Переименовываем новую таблицу
                db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 