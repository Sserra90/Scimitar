package com.creations.scimitar_runtime.state


/**
 * @author Sérgio Serra.
 * sergioserra99@gmail.com
 *
 * Representation of all possible states.
 */
sealed class State {
    data class Success<T>(val data: T) : State()
    data class Error(val throwable: Throwable) : State()
    object NoResults : State()
    object Loading : State()
}

class A {
    init {
        val state = State.Success(listOf(""))
    }
}

