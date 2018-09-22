package com.creations.scimitar

import android.os.Bundle
import android.util.Log
import com.creations.scimitar_annotations.BindViewModel

open class SecondActivity : FirstActivity() {

    companion object {
        const val TAG = "SecondActivity"
    }

    @BindViewModel(MyViewModel::class)
    lateinit var firstVm: MyViewModel

    @BindViewModel(SecondViewModel::class)
    lateinit var secondVm: SecondViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Scimitar.bind(this)

        Log.d(TAG,"Vm injected: $vm")
    }

}