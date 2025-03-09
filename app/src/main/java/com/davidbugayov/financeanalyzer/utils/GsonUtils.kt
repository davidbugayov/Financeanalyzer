package com.davidbugayov.financeanalyzer.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Вспомогательные функции для работы с Gson
 */
object GsonUtils {

    /**
     * Создает Type для списка строк, который устойчив к R8
     */
    fun getStringListType(): Type {
        return object : TypeToken<ArrayList<String>>() {}.type
    }

    /**
     * Преобразует JSON в список строк
     */
    fun fromJsonToStringList(gson: Gson, json: String): List<String> {
        return try {
            gson.fromJson<ArrayList<String>>(json, getStringListType())
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Преобразует список строк в JSON
     */
    fun toJsonFromStringList(gson: Gson, list: List<String>): String {
        return gson.toJson(list, getStringListType())
    }
} 