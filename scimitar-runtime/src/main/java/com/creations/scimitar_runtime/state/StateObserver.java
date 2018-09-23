package com.creations.scimitar_runtime.state;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

/**
 * @author Sérgio Serra
 */
public abstract class StateObserver<D> implements Observer<Resource<D>> {

    public void onSuccess(D data){}
    public void onError(String error){}
    public void onLoading(){}

    @Override
    public void onChanged(@Nullable Resource<D> res) {
        if (res != null) {
            if (res.success()) {
                onSuccess(res.data);
            } else if (res.error()) {
                onError(res.getMessage());
            } else {
                onLoading();
            }
        }
    }
}
