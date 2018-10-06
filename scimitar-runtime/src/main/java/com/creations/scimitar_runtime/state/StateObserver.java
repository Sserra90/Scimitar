package com.creations.scimitar_runtime.state;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

public abstract class StateObserver<D> implements Observer<Resource<D>> {

    public void onSuccess(D data){}
    public void onError(Throwable throwable){}
    public void onLoading(){}

    @Override
    public final void onChanged(@Nullable Resource<D> res) {
        if (res != null) {
            if (res.success()) {
                onSuccess(res.data);
            } else if (res.error()) {
                onError(res.getError());
            } else {
                onLoading();
            }
        }
    }
}
