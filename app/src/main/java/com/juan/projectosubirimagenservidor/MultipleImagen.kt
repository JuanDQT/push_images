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
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_multiple_imagen.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.lang.Exception

class MultipleImagen : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private val PERMISSION_READ_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 200
    private var uri: Uri? = null
    private val TAGGER = "TAGGER"
    private var imagePaths: ArrayList<String> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_imagen)

        btnAll.setOnClickListener {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                abrirGaleria()
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.read_permission), PERMISSION_READ_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        btnDelete.setOnClickListener {
            imagePaths = arrayListOf()
            tvTotal.text = "Total images: ${imagePaths.count()}"
            ivTemp.setImageBitmap(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {

                if (data?.clipData != null) {
                    data?.let { d ->

                        if (d.clipData.itemCount > 1) {
                            for (x in 0 until d.clipData.itemCount) {
                                val item = d.clipData.getItemAt(x)
//                                val item = d.clipData.getItemAt(x)
                                imagePaths.add(getRealPathFromURIPath(item.uri))
                            }
                            tvTotal.text = "Total images: ${imagePaths.count()}"
                            Picasso.get().load(File(imagePaths[0])).into(ivTemp)

                            return
                        }
                    }

                } else {
                    imagePaths.add(getRealPathFromURIPath(data!!.data))
                    tvTotal.text = "Total images: ${imagePaths.count()}"
                    // Solo un item
                }

                Picasso.get().load(File(imagePaths[0])).into(ivTemp)
            } else {
                imagePaths = arrayListOf()
                tvTotal.text = "Total images: ${imagePaths.count()}"
            }



        }
    }

    fun abrirGaleria() {
        val openGalleryIntent = Intent(Intent.EXTRA_ALLOW_MULTIPLE)
        openGalleryIntent.type = "image/*"
        openGalleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        openGalleryIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(openGalleryIntent, getString(R.string.select_photos)), GALLERY_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        if (requestCode == PERMISSION_READ_REQUEST_CODE) {
            abrirGaleria()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?) {
        // Do nothing
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.itmSubir -> {
                Toast.makeText(this, "Subimos", Toast.LENGTH_SHORT).show()
                val parts: ArrayList<MultipartBody.Part> = arrayListOf()
                if (imagePaths.count() > 0) {

                    for (path in imagePaths) {
                        val file = File(path)
                        val fileToUpload = MultipartBody.Part.createFormData("files[]", "MMM" + file.name, RequestBody.create(MediaType.parse("image/*"), file))
                        parts.add(fileToUpload)
                    }
                    val idIncidencia = RequestBody.create(MediaType.parse("text/plain"), "999")
                    MyService().fullUpdload(parts, idIncidencia)

                }
                // TODO: RECOGER TODAS LAS FOTOS Y SUBIRLAS.
                // TODO: ACABADO ESTO, VALIDAR CON LA VERSION < N DE ANDROID


//                val filePath = getRealPathFromURIPath(uri!!)
//                val file = File(filePath)
//                val file2 = File(filePath)
//                Log.d(TAGGER, "Filename " + file.getName())
////                val mFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
//                val mFile = RequestBody.create(MediaType.parse("image/*"), file)
//
//                val fileToUpload1 = MultipartBody.Part.createFormData("files[]", "ABC" + file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
//                val fileToUpload12 = MultipartBody.Part.createFormData("files[]", "CDE" + file.getName(), RequestBody.create(MediaType.parse("image/*"), file2))
//
////                part = MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));


//                Service().updateImage(fileToUpload, filename)
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