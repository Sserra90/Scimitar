package com.creations.scimitar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.creations.scimitar_annotations.BindViewModel

class MainActivity : AppCompatActivity() {

    @BindViewModel(MyViewModel::class)
    lateinit var vm: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}
