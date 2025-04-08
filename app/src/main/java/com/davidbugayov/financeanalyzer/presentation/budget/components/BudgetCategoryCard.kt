package com.davidbugayov.financeanalyzer.presentation.budget.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import kotlin.math.min
import java.math.BigDecimal

// Перечисление действий для меню категории
enum class CategoryAction {
    ADD_FUNDS,
    SPEND,
    TRANSFER,
    RESET_PERIOD,
    DELETE
}

@Composable
fun BudgetCategoryCard(
    category: BudgetCategory,
    onCategoryClick: (String) -> Unit,
    onMenuClick: (BudgetCategory, CategoryAction) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onCategoryClick(category.id) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Box {
                    IconButton(
                        onClick = { 
                            showMenu = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Действия для категории ${category.name}"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Добавить средства") },
                            onClick = {
                                showMenu = false
                                onMenuClick(category, CategoryAction.ADD_FUNDS)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Потратить") },
                            onClick = {
                                showMenu = false
                                onMenuClick(category, CategoryAction.SPEND)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Перевести в другую категорию") },
                            onClick = {
                                showMenu = false
                                onMenuClick(category, CategoryAction.TRANSFER)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Сбросить период") },
                            onClick = {
                                showMenu = false
                                onMenuClick(category, CategoryAction.RESET_PERIOD)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить категорию") },
                            onClick = {
                                showMenu = false
                                onMenuClick(category, CategoryAction.DELETE)
                            }
                        )
                    }
                }
            }

            // Прогресс
            val progressValue = min(
                category.spent.amount.toDouble() / if (category.limit.amount > BigDecimal.ZERO) category.limit.amount.toDouble() else 1.0, 
                1.0
            ).toFloat()
            LinearProgressIndicator(
                progress = progressValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(6.dp),
                color = if (category.spent.amount >= category.limit.amount) Color.Red else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Потрачено: ${category.spent.amount.toInt()} ₽",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Лимит: ${category.limit.amount.toInt()} ₽",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Баланс кошелька
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Баланс кошелька",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Кошелёк: ${category.walletBalance.amount.toInt()} ₽",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Подробнее",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
} 