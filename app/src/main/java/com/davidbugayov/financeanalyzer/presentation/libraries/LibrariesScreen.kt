package com.davidbugayov.financeanalyzer.presentation.libraries

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_light_primary
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_light_primaryContainer
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_light_secondary
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_light_secondaryContainer
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_dark_primary
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_dark_primaryContainer

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
            AppTopBar(
                title = stringResource(id = R.string.libraries_title),
                showBackButton = true,
                onBackClick = onNavigateBack,
                titleFontSize = dimensionResource(R.dimen.text_size_normal).value.toInt()
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_normal))
        ) {
            item {
                Text(
                    text = stringResource(R.string.libraries_description),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_normal))
                )
            }
            
            items(getLibraries()) { library ->
                LibraryItem(
                    name = library.name,
                    version = library.version,
                    description = library.description,
                    license = library.license,
                    index = getLibraries().indexOf(library)
                )
                
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            }
            
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_normal)))
                
                Text(
                    text = stringResource(R.string.licenses_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xxlarge)))
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
 * @param index Индекс элемента в списке, используется для чередования оттенков.
 */
@Composable
private fun LibraryItem(
    name: String,
    version: String,
    description: String,
    license: String,
    index: Int
) {
    // Определяем градиент цветов в зависимости от четности индекса
    val gradientColors = if (index % 2 == 0) {
        listOf(
            md_theme_light_primary.copy(alpha = 0.9f),
            md_theme_light_primaryContainer.copy(alpha = 0.3f)
        )
    } else {
        listOf(
            md_theme_light_secondary.copy(alpha = 0.8f),
            md_theme_light_secondaryContainer.copy(alpha = 0.3f)
        )
    }

    // Определяем цвет текста для заголовка на цветном фоне
    val textColor = Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(R.dimen.spacing_medium)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation).div(2)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        // Заголовок с градиентом
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(gradientColors)
                )
                .padding(dimensionResource(R.dimen.spacing_normal))
        ) {
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            
                Text(
                    text = "Версия: $version",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.9f)
                )
            }
        }
        
        // Основное содержимое карточки
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_normal))
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_normal)))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            
            Text(
                text = stringResource(R.string.license_colon, license),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
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
            version = "3.5.3",
            description = "Легковесная библиотека для внедрения зависимостей в Kotlin",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Room",
            version = "2.6.1",
            description = "Библиотека для работы с базами данных SQLite",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Firebase Analytics",
            version = "32.7.4",
            description = "Библиотека для аналитики и отслеживания пользовательских событий",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Firebase Crashlytics",
            version = "2.9.9",
            description = "Библиотека для отслеживания сбоев и ошибок в приложении",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Material Components",
            version = "1.11.0",
            description = "Компоненты Material Design для Android",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Timber",
            version = "5.0.1",
            description = "Библиотека для логирования в Android",
            license = "Apache License 2.0"
        ),
        Library(
            name = "PDFBox Android",
            version = "2.0.27.0",
            description = "Библиотека для работы с PDF-файлами в Android",
            license = "Apache License 2.0"
        ),
        Library(
            name = "Apache POI",
            version = "5.2.3",
            description = "Библиотека для работы с документами Microsoft Office",
            license = "Apache License 2.0"
        )
    )
} 

/**
 * Горизонтальный разделитель
 */
@Composable
private fun HorizontalDivider() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.height_divider)),
        color = MaterialTheme.colorScheme.outlineVariant
    ) {}
}