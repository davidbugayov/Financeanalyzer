package com.davidbugayov.financeanalyzer.presentation.profile

data class Time(val hours: Int, val minutes: Int) {

    val formattedString: String
        get() = "%02d:%02d".format(hours, minutes)
} 