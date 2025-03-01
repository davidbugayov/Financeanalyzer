package com.davidbugayov.financeanalyzer.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File

object FileHelper {
    // Создание CSV-файла в папке Documents
    fun createCsvFile(context: Context, fileName: String): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Для Android 10 и выше используем MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                }

                context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            } else {
                // Для старых версий Android используем прямой доступ к файловой системе
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                if (!documentsDir.exists()) {
                    documentsDir.mkdirs()
                }
                val file = File(documentsDir, fileName)
                if (!file.exists()) {
                    file.createNewFile()
                }
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            Log.e("FileHelper", "Error creating CSV file", e)
            null
        }
    }

    // Получение URI файла по имени
    fun getCsvFileUri(context: Context, fileName: String): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val projection = arrayOf(MediaStore.Files.FileColumns._ID)
                val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ?"
                val selectionArgs = arrayOf(fileName)

                context.contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                        MediaStore.Files.getContentUri("external", id)
                    } else {
                        null
                    }
                }
            } else {
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val file = File(documentsDir, fileName)
                if (file.exists()) {
                    Uri.fromFile(file)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FileHelper", "Error getting CSV file URI", e)
            null
        }
    }
}