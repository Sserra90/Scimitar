package com.creations.scimitar.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.creations.annotations.BindViewModel;
import com.creations.annotations.OnSuccess;
import com.creations.scimitar.entities.User;
import com.creations.scimitar.vm.SecondViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class JavaFragment extends AbsJavaFragment {

    public static JavaFragment newInstance() {
        Bundle args = new Bundle();
        JavaFragment fragment = new JavaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindViewModel
    SecondViewModel secondViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        observer.onSuccess(new User(1));
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
