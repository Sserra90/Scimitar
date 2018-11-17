package com.creations.scimitar.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.creations.scimitar.activities.ThirdActivity
import com.creations.scimitar.entities.User
import com.creations.scimitar.vm.MyViewModel
import com.creations.scimitar.vm.ScimitarViewModelFactory
import com.creations.annotations.*
import com.creations.runtime.scimitar
import com.creations.runtime.state.StateError
import com.creations.runtime.state.StateObserver

/**
 * @author Sérgio Serra on 06/10/2018.
 * Criations
 * sergioserra99@gmail.com
 */
open class FirstFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scimitar()
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
    fun renderError(error: StateError) {
        Log.d(ThirdActivity.TAG, "Show error: $error")
    }

    @OnLoading(id = "users")
    fun showLoading() {
        Log.d(ThirdActivity.TAG, "Show loading")
    }

    @OnNoResults(id = "users")
    fun showNoResults() {
        Log.d(ThirdActivity.TAG, "Show no results")
    }
}