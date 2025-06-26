package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle

interface IAnalytics {
    fun logEvent(eventName: String)
    fun logEvent(eventName: String, params: Bundle)
    fun setUserProperty(name: String, value: String)
    fun setUserId(userId: String)
}
