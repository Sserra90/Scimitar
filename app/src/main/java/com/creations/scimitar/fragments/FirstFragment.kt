package com.creations.scimitar.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.creations.scimitar.activities.ThirdActivity
import com.creations.scimitar.entities.User
import com.creations.scimitar.vm.MyViewModel
import com.creations.scimitar.vm.ScimitarViewModelFactory
import com.creations.scimitar_annotations.BindViewModel
import com.creations.scimitar_annotations.ViewModelFactory
import com.creations.scimitar_annotations.state.OnError
import com.creations.scimitar_annotations.state.OnLoading
import com.creations.scimitar_annotations.state.OnSuccess
import com.creations.scimitar_annotations.state.ResourceObserver
import com.creations.scimitar_runtime.Scimitar
import com.creations.scimitar_runtime.state.Resource
import com.creations.scimitar_runtime.state.StateObserver


/**
 * @author Sérgio Serra on 06/10/2018.
 * Criations
 * sergioserra99@gmail.com
 */
open class FirstFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Scimitar.bind(this)
    }
}

class SecondFragment : FirstFragment() {

    @BindViewModel
    lateinit var thirdVm: MyViewModel

    @ViewModelFactory
    lateinit var factory: ScimitarViewModelFactory

    @ResourceObserver(id = "users")
    lateinit var usersObserver: StateObserver<User>

    @OnSuccess(id = "users")
    fun renderUsers(user: User) {
        Log.d(ThirdActivity.TAG, "Render user: $user")
    }

    @OnError(id = "users")
    fun renderError(t: Throwable) {
        Log.d(ThirdActivity.TAG, "Show error")
    }

    @OnLoading(id = "users")
    fun showLoading() {
        Log.d(ThirdActivity.TAG, "Show loading")
    }
}