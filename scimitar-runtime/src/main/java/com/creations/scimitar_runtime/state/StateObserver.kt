package com.creations.scimitar_runtime.state

import androidx.annotation.Nullable
import androidx.lifecycle.Observer

abstract class StateObserver<D> : Observer<State<D>> {

    open fun onSuccess(data: D) {}
    open fun onError(error: StateError) {}
    open fun onLoading() {}
    open fun onNoResults() {}

    override fun onChanged(@Nullable state: State<D>?) {
        if (state != null) {
            when {
                state.success -> onSuccess(state.data)
                state.hasError -> onError(state.error!!)
                state.noResults -> onNoResults()
                state.loading -> onLoading()
            }
        }
    }
}
