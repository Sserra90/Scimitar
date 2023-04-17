package com.creations.scimitar.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.creations.annotations.ViewModel;
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

    @ViewModel
    SecondViewModel secondViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
