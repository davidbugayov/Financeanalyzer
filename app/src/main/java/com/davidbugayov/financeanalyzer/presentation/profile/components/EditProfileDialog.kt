package com.davidbugayov.financeanalyzer.presentation.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Диалог редактирования профиля пользователя.
 * @param userName Текущее имя пользователя.
 * @param userEmail Текущий email пользователя.
 * @param userPhone Текущий телефон пользователя.
 * @param onSave Обработчик сохранения изменений.
 * @param onDismiss Обработчик закрытия диалога.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    userName: String,
    userEmail: String,
    userPhone: String? = null,
    onSave: (name: String, email: String, phone: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(userName) }
    var email by remember { mutableStateOf(userEmail) }
    var phone by remember { mutableStateOf(userPhone ?: "") }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Редактирование профиля",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Поле имени
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = if (it.isBlank()) "Имя не может быть пустым" else null
                    },
                    label = { Text("Имя") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Поле email
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        emailError = if (!isValidEmail(it)) "Некорректный email" else null
                    },
                    label = { Text("Email") },
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Поле телефона
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Телефон (опционально)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Кнопки действий
                Button(
                    onClick = {
                        // Проверяем валидность данных
                        nameError = if (name.isBlank()) "Имя не может быть пустым" else null
                        emailError = if (!isValidEmail(email)) "Некорректный email" else null
                        
                        if (nameError == null && emailError == null) {
                            onSave(name, email, if (phone.isBlank()) null else phone)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Сохранить")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Отмена")
                }
            }
        }
    }
}

/**
 * Проверяет валидность email.
 * @param email Email для проверки.
 * @return true, если email валиден.
 */
private fun isValidEmail(email: String): Boolean {
    val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
    return email.matches(emailRegex)
} 