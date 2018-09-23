package com.creations.scimitar.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ScimitarViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyViewModel::class.java)) {
            return MyViewModel() as T
        }
        return SecondViewModel() as T
    }
}
