package com.davidbugayov.financeanalyzer.presentation.transaction.base.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem

@Composable
fun CategorySection(
    categories: List<CategoryItem>,
    selectedCategory: String,
    onCategorySelected: (CategoryItem) -> Unit,
    onAddCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Заголовок секции
        Text(
            text = "Категория",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Горизонтальный список категорий
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            // Отображаем доступные категории
            items(categories) { category ->
                CategoryItemComponent(
                    category = category,
                    isSelected = category.name == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }
            
            // Добавляем кнопку "Добавить категорию"
            item {
                AddCategoryButton(onClick = onAddCategoryClick)
            }
        }
    }
}

@Composable
private fun CategoryItemComponent(
    category: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(65.dp)
            .clickable(onClick = onClick)
    ) {
        // Голубой круг с иконкой категории
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFF90CAF9)) // Светло-голубой цвет
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Иконка или первая буква названия категории
            if (category.image != null) {
                // TODO: Здесь должна быть загрузка иконки из ресурсов
                // Временно используем первую букву
                Text(
                    text = category.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Text(
                    text = category.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Название категории
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = Color.Black
        )
    }
}

@Composable
private fun AddCategoryButton(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(65.dp)
            .clickable(onClick = onClick)
    ) {
        // Серый круг с иконкой плюса
        Surface(
            shape = CircleShape,
            color = Color(0xFFEEEEEE), // Светло-серый
            contentColor = Color.Gray,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить категорию",
                    tint = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Добавить",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = Color.Black
        )
    }
} 