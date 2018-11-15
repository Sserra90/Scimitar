package com.creations.scimitar_runtime.state

import androidx.annotation.Nullable
import androidx.lifecycle.Observer
import com.creations.scimitar_runtime.state.Status.*

abstract class StateObserver<D> : Observer<State<D>> {

    open fun onSuccess(data: D) {}
    open fun onError(error: StateError) {}
    open fun onLoading() {}
    open fun onNoResults() {}

    override fun onChanged(@Nullable state: State<D>?) {
        if (state != null) {
            when (state.status) {
                Success -> onSuccess(state.data)
                Error -> onError(state.error!!)
                NoResults -> onNoResults()
                Loading -> onLoading()
            }
        }
    }
}
