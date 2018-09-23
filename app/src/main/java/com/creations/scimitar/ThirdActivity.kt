package com.creations.scimitar

import android.os.Bundle
import android.util.Log
import com.creations.scimitar_annotations.BindViewModel
import com.creations.scimitar_annotations.ViewModelFactory

class ThirdActivity : SecondActivity() {

    companion object {
        const val TAG = "ThirdActivity"
    }

    @BindViewModel
    lateinit var thirdVm: MyViewModel

    @ViewModelFactory
    lateinit var factory: ScimitarViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        factory = ScimitarViewModelFactory()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG,"Vm injected: $vm")
    }

}