package com.davidbugayov.financeanalyzer.domain.usecase.debt

import com.davidbugayov.financeanalyzer.domain.model.Debt
import com.davidbugayov.financeanalyzer.domain.repository.DebtRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * UseCase: экспорт долгов в CSV файл.
 */
class ExportDebtsToCSVUseCase(
    private val repository: DebtRepository,
) {
    suspend operator fun invoke(
        debts: List<Debt>,
        outputDir: File,
    ): File {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val fileName = "debts_export_$timestamp.csv"
        val outputFile = File(outputDir, fileName)

        val csvContent = buildString {
            // Заголовки
            appendLine("ID,Title,Counterparty,Type,Status,Principal,Remaining,Created At,Due At,Note")

            // Данные
            debts.forEach { debt ->
                val formattedCreatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(debt.createdAt))

                val formattedDueAt = debt.dueAt?.let { dueAt ->
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(dueAt))
                } ?: ""

                appendLine(
                    "\"${debt.id}\"," +
                    "\"${debt.title}\"," +
                    "\"${debt.counterparty}\"," +
                    "\"${debt.type}\"," +
                    "\"${debt.status}\"," +
                    "\"${debt.principal.amount} ${debt.principal.currency}\"," +
                    "\"${debt.remaining.amount} ${debt.remaining.currency}\"," +
                    "\"$formattedCreatedAt\"," +
                    "\"$formattedDueAt\"," +
                    "\"${debt.note ?: ""}\""
                )
            }
        }

        outputFile.writeText(csvContent, Charsets.UTF_8)
        return outputFile
    }
}
