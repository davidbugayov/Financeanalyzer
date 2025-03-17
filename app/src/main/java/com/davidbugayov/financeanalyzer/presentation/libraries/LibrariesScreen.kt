package com.davidbugayov.financeanalyzer.presentation.libraries

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R

/**
 * Экран для отображения списка используемых библиотек.
 * @param onNavigateBack Обработчик нажатия на кнопку "Назад".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.libraries_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.libraries_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            items(getLibraries()) { library ->
                LibraryItem(
                    name = library.name,
                    version = library.version,
                    description = library.description,
                    license = library.license
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.licenses_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Элемент списка библиотек.
 * @param name Название библиотеки.
 * @param version Версия библиотеки.
 * @param description Описание библиотеки.
 * @param license Лицензия библиотеки.
 */
@Composable
private fun LibraryItem(
    name: String,
    version: String,
    description: String,
    license: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Версия: $version",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Лицензия: $license",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Модель данных для библиотеки.
 * @param name Название библиотеки.
 * @param version Версия библиотеки.
 * @param description Описание библиотеки.
 * @param license Лицензия библиотеки.
 */
data class Library(
    val name: String,
    val version: String,
    val description: String,
    val license: String
)

/**
 * Возвращает список используемых библиотек.
 * @return Список библиотек.
 */
private fun getLibraries(): List<Library> {
    return listOf(
        Library(
            name = "Jetpack Compose",
            version = "1.5.0",
            description = "Современный инструментарий для создания нативного UI на Android",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Kotlin Coroutines",
            version = "1.7.3",
            description = "Библиотека для асинхронного программирования в Kotlin",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Koin",
            version = "3.4.3",
            description = "Легковесная библиотека для внедрения зависимостей в Kotlin",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Room",
            version = "2.6.0",
            description = "Библиотека для работы с базами данных SQLite",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Firebase Analytics",
            version = "21.3.0",
            description = "Библиотека для аналитики и отслеживания пользовательских событий",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Firebase Crashlytics",
            version = "18.4.3",
            description = "Библиотека для отслеживания сбоев и ошибок в приложении",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Material Components",
            version = "1.9.0",
            description = "Компоненты Material Design для Android",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Accompanist",
            version = "0.30.1",
            description = "Набор библиотек для расширения возможностей Jetpack Compose",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Timber",
            version = "5.0.1",
            description = "Библиотека для логирования в Android",
            license = "Apache License 2.0"
        )
    )
} 