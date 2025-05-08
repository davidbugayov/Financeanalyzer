package com.davidbugayov.financeanalyzer.domain

import kotlinx.coroutines.flow.flowOf

class UserPreferences(
    val userName: String = "",
    val currency: String = ""
) {
    // TODO: Реализовать методы и свойства по необходимости

    fun getUserPreferences() = flowOf(this)
} 