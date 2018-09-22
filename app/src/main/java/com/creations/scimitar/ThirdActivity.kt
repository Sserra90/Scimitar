package com.creations.scimitar

import android.os.Bundle
import android.util.Log
import com.creations.scimitar_annotations.BindViewModel

class ThirdActivity : SecondActivity() {

    companion object {
        const val TAG = "SecondActivity"
    }

    @BindViewModel(MyViewModel::class)
    lateinit var thirdVm: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG,"Vm injected: $vm")
    }

}