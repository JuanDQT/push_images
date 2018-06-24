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

class MultipleImagen : AppCompatActivity() {
    private val READ_REQUEST_CODE = 300
    private val REQUEST_GALLERY_CODE = 200
    private var uri: Uri? = null
    private val TAGGER = "TAGGER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSingle.setOnClickListener {
            val openGalleryIntent = Intent(Intent.ACTION_PICK)
            openGalleryIntent.type = "image/*"
            startActivityForResult(openGalleryIntent, REQUEST_GALLERY_CODE)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_GALLERY_CODE && resultCode == Activity.RESULT_OK) {
            uri = data.data
            if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                val filePath = getRealPathFromURIPath(uri!!)
                val file = File(filePath)
                val file2 = File(filePath)
                Log.d(TAGGER, "Filename " + file.getName())
//                val mFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                val mFile = RequestBody.create(MediaType.parse("image/*"), file)

                val fileToUpload1= MultipartBody.Part.createFormData("files[]", "ABC"+file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                val fileToUpload12= MultipartBody.Part.createFormData("files[]", "CDE"+file.getName(), RequestBody.create(MediaType.parse("image/*"), file2))
                val idIncidencia = RequestBody.create(MediaType.parse("text/plain"), "999")

//                part = MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));


                MyService().fullUpdload(listOf(fileToUpload1, fileToUpload12), idIncidencia)

//                Service().updateImage(fileToUpload, filename)
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.read_permission), READ_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.itmSubir -> {
                Toast.makeText(this, "Subimos", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mi_menu, menu)
        return true
    }

    private fun getRealPathFromURIPath(contentURI: Uri): String {
        val cursor = this.contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) {
            return contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            return cursor.getString(idx)
        }
    }

}