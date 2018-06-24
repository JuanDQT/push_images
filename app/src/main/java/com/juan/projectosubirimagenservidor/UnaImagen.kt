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
import kotlinx.android.synthetic.main.activity_una_imagen.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import android.graphics.BitmapFactory
import android.os.Environment
import android.view.View
import pub.devrel.easypermissions.AppSettingsDialog
import java.util.logging.Logger
import android.graphics.Bitmap
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class UnaImagen : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private val WRITE_REQUEST_CODE = 300
    private val CAMERA_REQUEST_CODE = 400
    private val TAGGER = "TAGGER"
    private var filePath: String? = null
    private val FOLDER_NAME = ".juan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_una_imagen)

        btnGaleria.setOnClickListener {

            if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                abrirGaleria()
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.read_permission), WRITE_REQUEST_CODE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        btnCamera.setOnClickListener {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
                abrirCamara()
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.camera_permision), CAMERA_REQUEST_CODE, Manifest.permission.CAMERA)
            }
        }

        btnDelete.setOnClickListener {
            filePath = null
            ivPicture.setImageBitmap(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data.data
            filePath = getRealPathFromURIPath(uri!!)

            val myBitmap = BitmapFactory.decodeFile(filePath)
            ivPicture.setImageBitmap(myBitmap)

            btnDelete.visibility = View.VISIBLE
        }

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this@UnaImagen, "hasMemorySD: " + Common.hasMemorySD(), Toast.LENGTH_SHORT).show()

            val photo= data.getExtras().get("data") as Bitmap
            ivPicture.setImageBitmap(photo)

            saveFile(photo)?.let {
                filePath = it
                btnDelete.visibility = View.VISIBLE
            }
        }

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

    fun abrirCamara() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    fun abrirGaleria() {
        val openGalleryIntent = Intent(Intent.ACTION_PICK)
        openGalleryIntent.type = "image/*"
        startActivityForResult(openGalleryIntent, WRITE_REQUEST_CODE)
    }

    fun saveFile(imageToSave: Bitmap): String? {
        var file: File? = null
        if (Common.hasMemorySD()) {

        } else {
            val folder = File(Environment.getExternalStorageDirectory(), FOLDER_NAME)

            if (!File(folder, ".nomedia").exists())
                File(folder,".nomedia").createNewFile()
            folder.mkdirs()
            if (folder.exists()) {

                val df = SimpleDateFormat("dd_mm_yyyy_HH_MM_ss")
                val formattedDate = df.format(Calendar.getInstance().time)

                file = File(folder,  formattedDate + ".jpg")
                Toast.makeText(this@UnaImagen, "NO EXISTE", Toast.LENGTH_SHORT).show()

            }
        }

        file?.let {
            if (it.exists()) {
                it.delete()
            }
            try {
                val out = FileOutputStream(file)
                imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()

                return file.path
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    // MARK: PERMISOS

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        if (requestCode == 300) {
            abrirGaleria()
        }
        if (requestCode == 400) {
            abrirCamara()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        //Log.e(TAGGER, "Permiso deny")
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.itmSubir -> {

                filePath?.let {
                    val file = File(it)
                    val fileToUpload = MultipartBody.Part.createFormData("file", "SINGLE_" + file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                    val otroParametro = RequestBody.create(MediaType.parse("text/plain"), "OTRO")

                    MyService().uploadImage(fileToUpload, otroParametro)
                }

            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mi_menu, menu)
        return true
    }

}
