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
import com.davidbugayov.financeanalyzer.data.local.dao.BudgetCategoryDao
import com.davidbugayov.financeanalyzer.data.local.entity.TransactionEntity
import com.davidbugayov.financeanalyzer.data.local.entity.BudgetCategoryEntity
import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.data.local.entity.CategoryEntity
import com.davidbugayov.financeanalyzer.data.local.entity.SourceEntity
import timber.log.Timber

/**
 * Класс базы данных Room для приложения.
 * Содержит таблицу транзакций.
 */
@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        SourceEntity::class,
        BudgetCategoryEntity::class
    ],
    version = 17,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Предоставляет доступ к DAO для работы с транзакциями
     */
    abstract fun transactionDao(): TransactionDao

    /**
     * Предоставляет доступ к DAO для работы с бюджетными категориями
     */
    abstract fun budgetCategoryDao(): BudgetCategoryDao

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
         * Оставляет пустую миграцию, т.к. таблица financial_goals больше не нужна
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

        /**
         * Миграция с версии 12 на версию 13
         * Исправляет схему таблицы budget_categories, добавляя правильные значения по умолчанию
         */
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создаем временную таблицу с правильной схемой
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS budget_categories_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        `limit` REAL NOT NULL,
                        spent REAL NOT NULL,
                        wallet_balance REAL NOT NULL DEFAULT 0.0,
                        period_duration INTEGER NOT NULL DEFAULT 14,
                        period_start_date INTEGER NOT NULL DEFAULT (strftime('%s', 'now') * 1000)
                    )
                """)

                // Копируем данные из старой таблицы в новую
                db.execSQL("""
                    INSERT INTO budget_categories_new (id, name, `limit`, spent, wallet_balance, period_duration, period_start_date)
                    SELECT id, name, `limit`, spent, 
                           CASE WHEN wallet_balance IS NULL THEN 0.0 ELSE wallet_balance END,
                           CASE WHEN period_duration IS NULL THEN 14 ELSE period_duration END,
                           CASE WHEN period_start_date IS NULL THEN (strftime('%s', 'now') * 1000) ELSE period_start_date END
                    FROM budget_categories
                """)

                // Удаляем старую таблицу
                db.execSQL("DROP TABLE budget_categories")

                // Переименовываем новую таблицу
                db.execSQL("ALTER TABLE budget_categories_new RENAME TO budget_categories")
            }
        }

        /**
         * Миграция с версии 13 на версию 14
         * Добавляет поле id_string в таблицу transactions
         */
        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создаем временную таблицу с новой схемой
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS transactions_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        id_string TEXT NOT NULL,
                        amount TEXT NOT NULL,
                        category TEXT NOT NULL,
                        isExpense INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        note TEXT,
                        source TEXT NOT NULL DEFAULT 'Наличные',
                        sourceColor INTEGER NOT NULL DEFAULT 0
                    )
                """)

                // Копируем данные из старой таблицы в новую, генерируя id_string
                db.execSQL("""
                    INSERT INTO transactions_new (id, id_string, amount, category, isExpense, date, note, source, sourceColor)
                    SELECT id, CAST(id AS TEXT), amount, category, isExpense, date, note, source, sourceColor FROM transactions
                """)

                // Удаляем старую таблицу
                db.execSQL("DROP TABLE transactions")

                // Переименовываем новую таблицу
                db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

                // Создаем индекс для id_string
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_transactions_id_string ON transactions (id_string)")
            }
        }

        /**
         * Проверяет состояние базы данных
         */
        private fun checkDatabaseState(db: SupportSQLiteDatabase) {
            // Проверяем таблицы
            val tablesCursor = db.query("SELECT name FROM sqlite_master WHERE type='table'")
            val tables = mutableListOf<String>()
            while (tablesCursor.moveToNext()) {
                tables.add(tablesCursor.getString(0))
            }
            tablesCursor.close()
            Timber.d("Database tables: $tables")

            // Проверяем содержимое таблиц
            tables.forEach { tableName ->
                val countCursor = db.query("SELECT COUNT(*) FROM $tableName")
                countCursor.moveToFirst()
                val count = countCursor.getInt(0)
                countCursor.close()
                Timber.d("Table $tableName has $count rows")
            }
        }

        /**
         * Миграция с версии 14 на версию 15
         * Исправляет структуру таблицы categories, добавляя правильные значения по умолчанию
         */
        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Проверяем состояние базы данных до миграции
                Timber.d("Checking database state before migration")
                checkDatabaseState(db)

                // Проверяем, существует ли таблица categories
                val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='categories'")
                val tableExists = cursor.moveToFirst()
                cursor.close()

                if (!tableExists) {
                    // Если таблица не существует, создаем её
                    db.execSQL("""
                        CREATE TABLE categories (
                            name TEXT NOT NULL PRIMARY KEY,
                            isExpense INTEGER NOT NULL,
                            color INTEGER NOT NULL,
                            icon TEXT NOT NULL
                        )
                    """)

                    // Вставляем стандартные категории с уникальными именами
                    db.execSQL("""
                        INSERT INTO categories (name, isExpense, color, icon)
                        VALUES 
                            ('Зарплата', 0, -16711936, 'work'),
                            ('Другое (Доход)', 0, -16776961, 'other'),
                            ('Продукты', 1, -65536, 'food'),
                            ('Транспорт', 1, -16711681, 'transport'),
                            ('Развлечения', 1, -65281, 'entertainment'),
                            ('Здоровье', 1, -16776961, 'health'),
                            ('Одежда', 1, -16711936, 'clothes'),
                            ('Жилье', 1, -65536, 'home'),
                            ('Образование', 1, -16711681, 'education'),
                            ('Другое (Расход)', 1, -65281, 'other')
                    """)
                } else {
                    // Если таблица существует, проверяем её структуру
                    val columnsCursor = db.query("PRAGMA table_info(categories)")
                    val columns = mutableListOf<String>()
                    while (columnsCursor.moveToNext()) {
                        columns.add(columnsCursor.getString(1))
                    }
                    columnsCursor.close()

                    // Если отсутствуют необходимые колонки, добавляем их
                    if (!columns.contains("color")) {
                        db.execSQL("ALTER TABLE categories ADD COLUMN color INTEGER NOT NULL DEFAULT -16776961")
                    }
                    if (!columns.contains("icon")) {
                        db.execSQL("ALTER TABLE categories ADD COLUMN icon TEXT NOT NULL DEFAULT 'other'")
                    }

                    // Создаем временную таблицу для стандартных категорий
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS temp_categories (
                            name TEXT NOT NULL PRIMARY KEY,
                            isExpense INTEGER NOT NULL,
                            color INTEGER NOT NULL,
                            icon TEXT NOT NULL
                        )
                    """)

                    // Вставляем стандартные категории во временную таблицу с уникальными именами
                    db.execSQL("""
                        INSERT OR IGNORE INTO temp_categories (name, isExpense, color, icon)
                        VALUES 
                            ('Зарплата', 0, -16711936, 'work'),
                            ('Другое (Доход)', 0, -16776961, 'other'),
                            ('Продукты', 1, -65536, 'food'),
                            ('Транспорт', 1, -16711681, 'transport'),
                            ('Развлечения', 1, -65281, 'entertainment'),
                            ('Здоровье', 1, -16776961, 'health'),
                            ('Одежда', 1, -16711936, 'clothes'),
                            ('Жилье', 1, -65536, 'home'),
                            ('Образование', 1, -16711681, 'education'),
                            ('Другое (Расход)', 1, -65281, 'other')
                    """)

                    // Обновляем существующие категории, добавляя цвета и иконки из временной таблицы
                    db.execSQL("""
                        UPDATE categories 
                        SET color = (
                            SELECT color 
                            FROM temp_categories 
                            WHERE temp_categories.name = categories.name
                        ),
                        icon = (
                            SELECT icon 
                            FROM temp_categories 
                            WHERE temp_categories.name = categories.name
                        )
                        WHERE EXISTS (
                            SELECT 1 
                            FROM temp_categories 
                            WHERE temp_categories.name = categories.name
                        )
                    """)

                    // Вставляем отсутствующие категории
                    db.execSQL("""
                        INSERT OR IGNORE INTO categories (name, isExpense, color, icon)
                        SELECT name, isExpense, color, icon
                        FROM temp_categories
                        WHERE NOT EXISTS (
                            SELECT 1 FROM categories WHERE categories.name = temp_categories.name
                        )
                    """)

                    // Удаляем временную таблицу
                    db.execSQL("DROP TABLE temp_categories")
                }

                // Проверяем состояние базы данных после миграции
                Timber.d("Checking database state after migration")
                checkDatabaseState(db)
            }
        }

        /**
         * Миграция с версии 15 на версию 16
         * Создает и заполняет таблицу sources
         */
        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Проверяем состояние базы данных до миграции
                Timber.d("Checking database state before migration")
                checkDatabaseState(db)

                // Удаляем существующую таблицу sources, если она есть
                db.execSQL("DROP TABLE IF EXISTS sources")

                // Создаем таблицу sources заново
                db.execSQL("""
                    CREATE TABLE sources (
                        name TEXT NOT NULL PRIMARY KEY,
                        icon TEXT NOT NULL DEFAULT 'wallet',
                        color INTEGER NOT NULL DEFAULT -16776961,
                        balance TEXT NOT NULL DEFAULT '0'
                    )
                """)

                // Вставляем стандартные источники
                db.execSQL("""
                    INSERT INTO sources (name, icon, color, balance)
                    VALUES 
                        ('Наличные', 'wallet', -16776961, '0'),
                        ('Сбер', 'bank', -16711936, '0'),
                        ('Т-Банк', 'bank', -16711681, '0'),
                        ('Альфа', 'bank', -65536, '0')
                """)

                // Проверяем состояние базы данных после миграции
                Timber.d("Checking database state after migration")
                checkDatabaseState(db)
            }
        }

        /**
         * Миграция с версии 16 на версию 17
         * Проверяет и исправляет состояние таблицы sources
         */
        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Проверяем состояние базы данных до миграции
                Timber.d("Checking database state before migration")
                checkDatabaseState(db)

                // Проверяем, существует ли таблица sources
                val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='sources'")
                val tableExists = cursor.moveToFirst()
                cursor.close()

                if (!tableExists) {
                    // Если таблица не существует, создаем её
                    db.execSQL("""
                        CREATE TABLE sources (
                            name TEXT NOT NULL PRIMARY KEY,
                            icon TEXT NOT NULL DEFAULT 'wallet',
                            color INTEGER NOT NULL DEFAULT -16776961,
                            balance TEXT NOT NULL DEFAULT '0'
                        )
                    """)

                    // Вставляем стандартные источники
                    db.execSQL("""
                        INSERT INTO sources (name, icon, color, balance)
                        VALUES 
                            ('Наличные', 'wallet', -16776961, '0'),
                            ('Сбер', 'bank', -16711936, '0'),
                            ('Т-Банк', 'bank', -16711681, '0'),
                            ('Альфа', 'bank', -65536, '0')
                    """)
                } else {
                    // Проверяем структуру таблицы
                    val columnsCursor = db.query("PRAGMA table_info(sources)")
                    val columns = mutableListOf<String>()
                    while (columnsCursor.moveToNext()) {
                        columns.add(columnsCursor.getString(1))
                    }
                    columnsCursor.close()

                    // Если отсутствуют необходимые колонки, пересоздаем таблицу
                    if (!columns.contains("name") || !columns.contains("icon") || 
                        !columns.contains("color") || !columns.contains("balance")) {
                        
                        // Создаем временную таблицу
                        db.execSQL("""
                            CREATE TABLE sources_temp (
                                name TEXT NOT NULL PRIMARY KEY,
                                icon TEXT NOT NULL DEFAULT 'wallet',
                                color INTEGER NOT NULL DEFAULT -16776961,
                                balance TEXT NOT NULL DEFAULT '0'
                            )
                        """)

                        // Копируем данные из старой таблицы
                        db.execSQL("""
                            INSERT INTO sources_temp (name, icon, color, balance)
                            SELECT 
                                name,
                                CASE WHEN icon IS NULL THEN 'wallet' ELSE icon END,
                                CASE WHEN color IS NULL THEN -16776961 ELSE color END,
                                CASE WHEN balance IS NULL THEN '0' ELSE balance END
                            FROM sources
                        """)

                        // Удаляем старую таблицу
                        db.execSQL("DROP TABLE sources")

                        // Переименовываем временную таблицу
                        db.execSQL("ALTER TABLE sources_temp RENAME TO sources")
                    }

                    // Проверяем наличие стандартных источников
                    val sourcesCursor = db.query("SELECT name FROM sources")
                    val sources = mutableListOf<String>()
                    while (sourcesCursor.moveToNext()) {
                        sources.add(sourcesCursor.getString(0))
                    }
                    sourcesCursor.close()

                    val defaultSources = listOf("Наличные", "Сбер", "Т-Банк", "Альфа")
                    defaultSources.forEach { source ->
                        if (!sources.contains(source)) {
                            db.execSQL("""
                                INSERT INTO sources (name, icon, color, balance)
                                VALUES (?, ?, ?, ?)
                            """, arrayOf(
                                source,
                                if (source == "Наличные") "wallet" else "bank",
                                when (source) {
                                    "Наличные" -> -16776961
                                    "Сбер" -> -16711936
                                    "Т-Банк" -> -16711681
                                    "Альфа" -> -65536
                                    else -> -16776961
                                },
                                "0"
                            ))
                        }
                    }
                }

                // Проверяем состояние базы данных после миграции
                Timber.d("Checking database state after migration")
                checkDatabaseState(db)
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
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_14_15,
                        MIGRATION_15_16,
                        MIGRATION_16_17
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Проверяем все миграции при создании базы данных
                            try {
                                for (i in 1 until 15) {
                                    val migration = when (i) {
                                        1 -> MIGRATION_1_2
                                        2 -> MIGRATION_2_3
                                        3 -> MIGRATION_3_4
                                        4 -> MIGRATION_4_5
                                        5 -> MIGRATION_5_6
                                        6 -> MIGRATION_6_7
                                        7 -> MIGRATION_7_8
                                        8 -> MIGRATION_8_9
                                        9 -> MIGRATION_9_10
                                        10 -> MIGRATION_10_11
                                        11 -> MIGRATION_11_12
                                        12 -> MIGRATION_12_13
                                        13 -> MIGRATION_13_14
                                        14 -> MIGRATION_14_15
                                        15 -> MIGRATION_15_16
                                        16 -> MIGRATION_16_17
                                        else -> null
                                    }
                                    migration?.migrate(db)
                                }
                            } catch (e: Exception) {
                                throw IllegalStateException("Migration check failed: ${e.message}", e)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 