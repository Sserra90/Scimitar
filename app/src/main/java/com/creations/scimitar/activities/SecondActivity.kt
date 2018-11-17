package com.creations.scimitar.activities

import android.os.Bundle
import android.util.Log
import com.creations.scimitar.vm.MyViewModel
import com.creations.scimitar.R
import com.creations.scimitar.vm.SecondViewModel
import com.creations.annotations.BindViewModel

open class SecondActivity : FirstActivity() {

    companion object {
        const val TAG = "SecondActivity"
    }

    @BindViewModel
    lateinit var firstVm: MyViewModel

    @BindViewModel
    lateinit var secondVm: SecondViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Scimitar.bind(this)

        Log.d(TAG,"Vm injected: $vm")
    }

}