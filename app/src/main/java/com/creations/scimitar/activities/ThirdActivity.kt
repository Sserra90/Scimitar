package com.creations.scimitar.activities

import android.os.Bundle
import android.util.Log
import com.creations.scimitar.R
import com.creations.scimitar.entities.Repo
import com.creations.scimitar.entities.User
import com.creations.scimitar.vm.MyViewModel
import com.creations.scimitar.vm.ScimitarViewModelFactory
import com.creations.scimitar_annotations.BindViewModel
import com.creations.scimitar_annotations.ViewModelFactory
import com.creations.scimitar_annotations.OnError
import com.creations.scimitar_annotations.OnLoading
import com.creations.scimitar_annotations.OnSuccess
import com.creations.scimitar_annotations.ResourceObserver
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

    @ResourceObserver(id = "repos")
    lateinit var reposObserver: StateObserver<List<Repo>>

    override fun onCreate(savedInstanceState: Bundle?) {
        factory = ScimitarViewModelFactory()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "Vm injected: $vm")
        vm.liveData.observe(this, usersObserver)
        vm.getUsers()
    }

    @OnSuccess(id = "users")
    fun renderUsers(user: User) {
        Log.d(TAG, "Render user: $user")
    }

    @OnError(id = "users")
    fun renderError(t: Throwable) {
        Log.d(TAG, "Show error")
    }

    @OnLoading(id = "users")
    fun showLoading() {
        Log.d(TAG, "Show loading")
    }


    @OnSuccess(id = "repos")
    fun renderRepos(repos: List<Repo>) {
        Log.d(TAG, "Show repo: $repos")
    }

}