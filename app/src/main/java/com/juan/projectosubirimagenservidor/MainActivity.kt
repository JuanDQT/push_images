package com.juan.projectosubirimagenservidor

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val TAGGER = "TAGGER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSingle.setOnClickListener {
            startActivity(Intent(this@MainActivity, UnaImagen::class.java))
        }

        btnAll.setOnClickListener {
            startActivity(Intent(this@MainActivity, MultipleImagen::class.java))
        }
    }
}