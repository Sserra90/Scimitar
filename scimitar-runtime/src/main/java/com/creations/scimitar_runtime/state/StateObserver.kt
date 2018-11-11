package com.creations.scimitar_runtime.state

import androidx.annotation.Nullable
import androidx.lifecycle.Observer

abstract class StateObserver<D> : Observer<Resource<D>> {

    open fun onSuccess(data: D) {}
    open fun onError(throwable: Throwable) {}
    open fun onLoading() {}

    override fun onChanged(@Nullable res: Resource<D>?) {
        if (res != null) {
            when {
                res.success() -> onSuccess(res.data!!)
                res.error() -> onError(res.error)
                else -> onLoading()
            }
        }
    }
}
