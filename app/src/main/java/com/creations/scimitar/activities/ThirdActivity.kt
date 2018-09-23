package com.creations.scimitar.activities

import android.os.Bundle
import android.util.Log
import com.creations.scimitar.vm.MyViewModel
import com.creations.scimitar.R
import com.creations.scimitar.vm.ScimitarViewModelFactory
import com.creations.scimitar.User
import com.creations.scimitar_annotations.BindViewModel
import com.creations.scimitar_annotations.ViewModelFactory
import com.creations.scimitar_annotations.state.OnError
import com.creations.scimitar_annotations.state.OnLoading
import com.creations.scimitar_annotations.state.OnSuccess
import com.creations.scimitar_annotations.state.ResourceObserver
import com.creations.scimitar_runtime.state.Resource
import com.creations.scimitar_runtime.state.StateObserver

class ThirdActivity : SecondActivity() {

    companion object {
        const val TAG = "ThirdActivity"
    }

    @BindViewModel
    lateinit var thirdVm: MyViewModel

    @ViewModelFactory
    lateinit var factory: ScimitarViewModelFactory

    @ResourceObserver(id = "users")
    lateinit var usersObserver: StateObserver<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        factory = ScimitarViewModelFactory()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "Vm injected: $vm")
    }

    @OnSuccess(id = "users")
    public fun renderUsers(user: User) {
        Log.d(TAG, "Render user: $user")
    }

    @OnError(id = "users")
    public fun renderError(t: Throwable) {
        Log.d(TAG, "Show error")
    }

    @OnLoading(id = "users")
    public fun showLoading() {
        Log.d(TAG, "Show loading")
    }
}