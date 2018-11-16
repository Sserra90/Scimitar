package com.creations.scimitar.vm

import android.os.Handler
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.creations.scimitar.entities.User
import com.creations.runtime.state.State
import com.creations.runtime.state.Status
import java.util.concurrent.TimeUnit

class MyViewModel : ViewModel() {

    val stateLive: MutableLiveData<State<User>> = MutableLiveData()

    fun getState(): LiveData<State<User>> = stateLive

    fun getUsers() {
        stateLive.value = State(status = Status.Loading)
        Handler().postDelayed(
                {
                    stateLive.value = State(User(1), Status.Success)
                },
                TimeUnit.SECONDS.toMillis(3)
        )
    }
}

class SecondViewModel : ViewModel()