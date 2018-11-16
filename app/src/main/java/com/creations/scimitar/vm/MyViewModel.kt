package com.creations.scimitar.vm

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.creations.scimitar.entities.User
import com.creations.scimitar_runtime.state.State
import com.creations.scimitar_runtime.state.Status
import java.util.concurrent.TimeUnit

class MyViewModel : ViewModel() {

    val liveData: MutableLiveData<State<User>> = MutableLiveData()

    fun getUsers() {
        Handler().postDelayed(
                { liveData.value = State(User(1), Status.Success) },
                TimeUnit.SECONDS.toMillis(3)
        )
    }
}

class SecondViewModel : ViewModel()