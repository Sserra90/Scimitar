package com.creations.scimitar

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.creations.scimitar_annotations.BindViewModel
import com.creations.scimitar_runtime.Scimitar

class SecondActivity : MainActivity() {

    companion object {
        const val TAG = "SecondActivity"
    }

    @BindViewModel(SecondViewModel::class)
    lateinit var secondVm: SecondViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Scimitar.bind(this)

        Log.d(TAG,"Vm injected: $vm")
    }

}