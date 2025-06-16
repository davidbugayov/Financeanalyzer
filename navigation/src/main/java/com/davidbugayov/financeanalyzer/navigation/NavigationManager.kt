package com.davidbugayov.financeanalyzer.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.Serializable

class NavigationManager {

    private val _commands = MutableSharedFlow<Command>(extraBufferCapacity = 1)
    val commands = _commands.asSharedFlow()

    fun navigate(command: Command) {
        _commands.tryEmit(command)
    }

    sealed class Command : Serializable {
        data class Navigate(val destination: String) : Command()
        data object NavigateUp : Command() {
            private fun readResolve(): Any = NavigateUp
        }
        data class PopUpTo(val destination: String, val inclusive: Boolean) : Command()
    }
}
