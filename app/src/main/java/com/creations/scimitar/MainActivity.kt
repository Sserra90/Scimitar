package com.creations.scimitar

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.creations.scimitar_annotations.BindViewModel
import com.creations.scimitar_runtime.Scimitar

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    @BindViewModel(MyViewModel::class)
    lateinit var vm: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Scimitar.bind(this)

        Log.d(TAG,"Vm injected: $vm")
    }

}
