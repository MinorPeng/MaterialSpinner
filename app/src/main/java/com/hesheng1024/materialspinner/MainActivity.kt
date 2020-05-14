package com.hesheng1024.materialspinner

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hesheng1024.spinner.MaterialSpinner

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val spinner = findViewById<MaterialSpinner>(R.id.spinner)
        spinner.setOnItemSelectedListener(object : MaterialSpinner.OnItemSelectedListener {
            override fun onItemSelected(
                    view: MaterialSpinner?,
                    position: Int,
                    id: Long,
                    item: Any
            ) {
                Log.d("Main", "$item")
            }

        })
        
    }
}
