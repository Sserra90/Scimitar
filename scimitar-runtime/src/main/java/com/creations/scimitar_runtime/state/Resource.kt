package com.creations.scimitar_runtime.state

sealed class Status {
    object Success : Status()
    object Error : Status()
    object Loading : Status()
    object NoResults : Status()
}

data class StateError(val error: Throwable)
data class State<T>(val data: T, val status: Status = Status.Loading, val error: StateError? = null) {
    val loading: Boolean
        get() = status == Status.Loading
    val hasError: Boolean
        get() = status == Status.Error && error != null
    val noResults: Boolean
        get() = status == Status.NoResults
    val success: Boolean
        get() = status == Status.Success
}

/*
class Resource<T> private constructor(@param:NonNull @field:NonNull
                                      private val status: Status, @param:Nullable @field:Nullable
                                      val data: T, @param:Nullable @field:Nullable
                                      val error: Throwable) {

    val isLoading: Boolean
        get() = status == LOADING

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING,
        NO_RESULTS
    }

    fun success(): Boolean {
        return status == SUCCESS
    }

    fun error(): Boolean {
        return status == ERROR
    }

    fun noResults(): Boolean {
        return status == NO_RESULTS
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val resource = o as Resource<*>?
        return status == resource!!.status &&
                equals(error, resource.error) &&
                equals(data, resource.data)
    }

    override fun hashCode(): Int {
        return hash(status, error, data)
    }

    companion object {

        fun <T> success(@Nullable data: T): Resource<T> {
            return Resource(SUCCESS, data, null)
        }

        fun <T> error(error: Throwable, @Nullable data: T): Resource<T> {
            return Resource(ERROR, data, error)
        }

        fun <T> error(error: Throwable): Resource<T> {
            return Resource<T>(ERROR, null, error)
        }

        fun <T> loading(@Nullable data: T): Resource<T> {
            return Resource(LOADING, data, null)
        }

        fun hash(vararg values: Any): Int {
            return Arrays.hashCode(values)
        }

        fun equals(a: Any?, b: Any): Boolean {
            return a === b || a != null && a == b
        }
    }
}*/