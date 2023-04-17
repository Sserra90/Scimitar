package com.creations.scimitar.activities

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.creations.annotations.BindObserver
import com.creations.annotations.OnError
import com.creations.annotations.OnLoading
import com.creations.annotations.OnNoResults
import com.creations.annotations.OnSuccess
import com.creations.annotations.ViewModel
import com.creations.annotations.ViewModelFactory
import com.creations.runtime.state.StateError
import com.creations.runtime.state.StateObserver
import com.creations.runtime.state.error
import com.creations.runtime.state.loading
import com.creations.runtime.state.noResults
import com.creations.runtime.state.success
import com.creations.scimitar.R
import com.creations.scimitar.databinding.ActivityMainBinding
import com.creations.scimitar.entities.Repo
import com.creations.scimitar.entities.User
import com.creations.scimitar.vm.MyViewModel
import com.creations.scimitar.vm.ScimitarViewModelFactory

class ThirdActivity : SecondActivity() {

    companion object {
        const val TAG = "ThirdActivity"
    }

    @ViewModel
    lateinit var thirdVm: MyViewModel

    @ViewModelFactory
    lateinit var factory: ScimitarViewModelFactory

    @BindObserver(id = "users")
    lateinit var usersObserver: StateObserver<User>

    @BindObserver(id = "repos")
    lateinit var reposObserver: StateObserver<List<Repo>>

    private lateinit var db: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        factory = ScimitarViewModelFactory()
        super.onCreate(savedInstanceState)

        /*supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, JavaFragment.newInstance())
                .commit()*/

        db = DataBindingUtil.setContentView(this, R.layout.activity_main)
        db.setLifecycleOwner(this)
        db.vm = vm


        /*
        stateView.apply {
            loadingExitAnim = { v, _ ->
                v.alpha = 1F
                v.animate().alpha(0F).duration = 320
            }
            contentEnterAnim = { v, _ ->
                v.alpha = 0F
                v.visibility = View.VISIBLE
                v.animate().alpha(1F).duration = 320
            }
        }*/

        Log.d(TAG, "Vm injected: $vm")
        //vm.stateLive.observe(this, usersObserver)
        vm.getUsers()
    }

    @OnSuccess(id = "users")
    fun renderUsers(user: User) {
        Log.d(TAG, "Render user: $user")
        db.stateView.state = success(user)
    }

    @OnError(id = "users")
    fun renderError(error: StateError) {
        Log.d(TAG, "Show error $error")
        db.stateView.state = error(error)
    }

    @OnLoading(id = "users")
    fun showLoading() {
        Log.d(TAG, "Show loading")
        db.stateView.state = loading()
    }

    @OnNoResults(id = "users")
    fun showNoResults() {
        Log.d(TAG, "Show no results")
        db.stateView.state = noResults()
    }

    @OnSuccess(id = "repos")
    fun renderRepos(repos: List<Repo>) {
        Log.d(TAG, "Show repo: $repos")
    }

}