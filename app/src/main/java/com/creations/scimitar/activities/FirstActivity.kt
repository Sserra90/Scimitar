package com.creations.scimitar.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.creations.scimitar.vm.MyViewModel
import com.creations.scimitar.R
import com.creations.scimitar.vm.ScimitarViewModelFactory
import com.creations.scimitar_annotations.BindViewModel
import com.creations.scimitar_annotations.ViewModelFactory
import com.creations.scimitar_runtime.Scimitar

open class FirstActivity : AppCompatActivity() {

    companion object {
        const val TAG = "FirstActivity"
    }

    @BindViewModel
    lateinit var vm: MyViewModel

    @ViewModelFactory(useAsDefault = false)
    lateinit var firstFactory: ScimitarViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Scimitar.bind(this)

        Log.d(TAG, "Vm injected: $vm")
    }

}