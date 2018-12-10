package com.creations.scimitar.activities;

import android.os.Bundle;

import com.creations.scimitar.fragments.JavaFragment;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

public class JavaActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, JavaFragment.newInstance())
                .commit();
    }
}