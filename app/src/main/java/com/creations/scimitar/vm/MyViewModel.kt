package com.creations.scimitar.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.creations.scimitar.entities.User
import com.creations.scimitar_runtime.state.State

class MyViewModel : ViewModel() {

    val liveData: MutableLiveData<State<User>> = MutableLiveData()

    fun getUsers() {
        liveData.value = State(User(1))
    }
}

class SecondViewModel : ViewModel()