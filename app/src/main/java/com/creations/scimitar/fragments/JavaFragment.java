package com.creations.scimitar.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.creations.annotations.OnSuccess;
import com.creations.annotations.ResourceObserver;
import com.creations.runtime.ScimitarKt;
import com.creations.runtime.state.State;
import com.creations.runtime.state.StateObserver;
import com.creations.scimitar.entities.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.creations.runtime.state.StateKt.success;

public class JavaFragment extends AbsJavaFragment {

    public static JavaFragment newInstance() {
        Bundle args = new Bundle();
        JavaFragment fragment = new JavaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @ResourceObserver(id = "user")
    protected StateObserver<User> observer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScimitarKt.scimitar(this);
        observer.onSuccess(new User(1));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @OnSuccess(id = "user")
    public void renderUser(User user) { }
}
