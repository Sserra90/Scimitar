package com.creations.scimitar.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.creations.scimitar.User
import com.creations.scimitar_runtime.state.Resource

class MyViewModel : ViewModel() {

    val liveData: MutableLiveData<Resource<User>> = MutableLiveData()

    fun getUsers() {
        liveData.value = Resource.success(User(1))
    }
}

class SecondViewModel : ViewModel()