package com.creations.scimitar.activities

import android.os.Bundle
import android.util.Log
import com.creations.scimitar.vm.MyViewModel
import com.creations.scimitar.R
import com.creations.scimitar.vm.SecondViewModel
import com.creations.annotations.BindViewModel
import com.creations.annotations.ResourceObserver
import com.creations.runtime.state.StateObserver
import com.creations.scimitar.entities.Repo

open class SecondActivity : FirstActivity() {

    companion object {
        const val TAG = "SecondActivity"
    }

    @BindViewModel
    lateinit var firstVm: MyViewModel

    @BindViewModel
    lateinit var secondVm: SecondViewModel

    @ResourceObserver(id = "getRepos")
    lateinit var reposObs: StateObserver<List<Repo>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Scimitar.bind(this)
        reposObs.onLoading()

        Log.d(TAG,"Vm injected: $vm")
    }

}