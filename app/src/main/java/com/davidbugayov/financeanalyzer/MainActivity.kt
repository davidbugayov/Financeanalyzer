package com.davidbugayov.financeanalyzer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Стартовая активность приложения.
 * Просто перенаправляет в FinanceActivity и завершается.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Запускаем основную активность
        startActivity(Intent(this, FinanceActivity::class.java))
        
        // Завершаем эту активность
        finish()
    }
} 