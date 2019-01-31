package com.creations.scimitar.activities

import android.os.Bundle
import android.util.Log
import com.creations.annotations.BindObserver
import com.creations.scimitar.vm.MyViewModel
import com.creations.scimitar.R
import com.creations.scimitar.vm.SecondViewModel
import com.creations.annotations.ViewModel
import com.creations.runtime.state.StateObserver
import com.creations.scimitar.entities.Repo

open class SecondActivity : FirstActivity() {

    companion object {
        const val TAG = "SecondActivity"
    }

    @ViewModel
    lateinit var firstVm: MyViewModel

    @ViewModel
    lateinit var secondVm: SecondViewModel

    @BindObserver(id = "getRepos")
    lateinit var reposObs: StateObserver<List<Repo>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Scimitar.bind(this)
        reposObs.onLoading()

        Log.d(TAG,"Vm injected: $vm")
    }

}