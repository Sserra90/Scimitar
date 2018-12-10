package com.creations.scimitar.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.creations.annotations.OnSuccess;
import com.creations.annotations.ResourceObserver;
import com.creations.runtime.ScimitarKt;
import com.creations.runtime.state.StateObserver;
import com.creations.scimitar.entities.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class AbsJavaFragment extends Fragment {

    @ResourceObserver(id = "getUser")
    StateObserver<User> observer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScimitarKt.scimitar(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @OnSuccess(id = "getUser")
    public void renderUser(User user) {

    }
}
