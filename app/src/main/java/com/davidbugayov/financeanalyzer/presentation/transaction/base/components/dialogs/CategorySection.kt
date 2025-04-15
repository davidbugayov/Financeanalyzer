package com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem

/**
 * Улучшенный компонент секции выбора категории с визуальными акцентами
 * 
 * @param categories список доступных категорий
 * @param selectedCategory выбранная категория
 * @param onCategorySelected обработчик выбора категории
 * @param onAddCategoryClick обработчик нажатия на кнопку добавления категории
 * @param isError флаг ошибки валидации
 * @param modifier модификатор компонента
 */
@Composable
fun CategorySection(
    categories: List<CategoryItem>,
    selectedCategory: String,
    onCategorySelected: (CategoryItem) -> Unit,
    onAddCategoryClick: () -> Unit,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp) // Уменьшенный отступ для более компактного вида
        ) {
            // Заголовок секции с индикатором обязательного поля
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Категория",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = " *",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isError) MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (isError) {
                    Text(
                        text = "Обязательное поле",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Сетка категорий (показываем только первые 8 популярных)
            val displayCategories = categories.take(8)
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(6.dp), // Уменьшенные отступы
                verticalArrangement = Arrangement.spacedBy(6.dp),   // Уменьшенные отступы
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.height(160.dp) // Уменьшенная высота
            ) {
                items(displayCategories) { category ->
                    CategoryGridItem(
                        category = category,
                        isSelected = category.name == selectedCategory,
                        onClick = { onCategorySelected(category) }
                    )
                }
                
                // Кнопка добавления категории
                item {
                    AddCategoryButton(onClick = onAddCategoryClick)
                }
            }
            
            // Кнопка "Еще категории" если есть еще
            if (categories.size > 8) {
                Spacer(modifier = Modifier.height(6.dp)) // Уменьшенный отступ
                androidx.compose.material3.OutlinedButton(
                    onClick = onAddCategoryClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Больше категорий (${categories.size - 8})")
                }
            }
        }
    }
}

/**
 * Элемент сетки категорий
 */
@Composable
private fun CategoryGridItem(
    category: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Используем голубой цвет для всех иконок категорий, как на скриншоте
    val backgroundColor = Color(0xFFE3F2FD) // Светло-голубой
    val contentColor = Color(0xFF2196F3)   // Голубой
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = backgroundColor,
            contentColor = contentColor,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                // Выбираем иконку в зависимости от названия категории, если она не задана
                val icon = if (category.image != null) {
                    category.image
                } else {
                    getCategoryIconByName(category.name)
                }
                
                // Отображаем иконку
                Icon(
                    imageVector = icon,
                    contentDescription = category.name,
                    modifier = Modifier.size(22.dp),
                    tint = contentColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = Color.Black // Черный текст для лучшей видимости
        )
    }
}

/**
 * Получает иконку для категории по ее имени
 */
@Composable
private fun getCategoryIconByName(categoryName: String) = when (categoryName.lowercase()) {
    "продукты" -> Icons.Default.ShoppingCart
    "рестораны" -> Icons.Default.Restaurant
    "транспорт" -> Icons.Default.LocalTaxi
    "развлечения" -> Icons.Default.Movie
    "зарплата" -> Icons.Default.AttachMoney
    else -> Icons.Default.MoreHoriz
}

/**
 * Кнопка добавления новой категории
 */
@Composable
private fun AddCategoryButton(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFEEEEEE), // Светло-серый
            contentColor = Color.Gray,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить категорию",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = "Добавить",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = Color.Black
        )
    }
} 