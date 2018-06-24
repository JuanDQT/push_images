package com.juan.projectosubirimagenservidor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

class MainActivity : AppCompatActivity() {
    private val READ_REQUEST_CODE = 300
    private val REQUEST_GALLERY_CODE = 200
    private var uri: Uri? = null
    private val TAGGER = "TAGGER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSingle.setOnClickListener {
            startActivity(Intent(this@MainActivity, UnaImagen::class.java))
        }
    }
}