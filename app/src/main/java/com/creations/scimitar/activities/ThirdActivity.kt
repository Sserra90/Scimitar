package com.creations.scimitar.activities

import android.os.Bundle
import android.util.Log
import com.creations.scimitar.R
import com.creations.scimitar.R.id.stateView
import com.creations.scimitar.entities.Repo
import com.creations.scimitar.entities.User
import com.creations.scimitar.vm.MyViewModel
import com.creations.scimitar.vm.ScimitarViewModelFactory
import com.creations.scimitar_annotations.*
import com.creations.scimitar_runtime.state.State
import com.creations.scimitar_runtime.state.StateError
import com.creations.scimitar_runtime.state.StateObserver
import com.creations.scimitar_runtime.state.Status
import kotlinx.android.synthetic.main.activity_main.*

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
        stateView.state = State(status = Status.Success)
    }

    @OnError(id = "users")
    fun renderError(error: StateError) {
        Log.d(TAG, "Show error $error")
        stateView.state = State(status = Status.Error)
    }

    @OnLoading(id = "users")
    fun showLoading() {
        Log.d(TAG, "Show loading")
    }

    @OnNoResults(id = "users")
    fun showNoResults() {
        Log.d(TAG, "Show no results")
        stateView.state = State(status = Status.NoResults)
    }

    @OnSuccess(id = "repos")
    fun renderRepos(repos: List<Repo>) {
        Log.d(TAG, "Show repo: $repos")
    }

}