package com.juan.projectosubirimagenservidor

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
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
import android.os.Build
import android.support.v4.content.FileProvider
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.UploadProgressListener
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class UnaImagen : AppCompatActivity(), EasyPermissions.PermissionCallbacks, UploadProgressListener, JSONObjectRequestListener {

    private val PERMISSION_WRITE_REQUEST_CODE = 1
    private val PERMISSION_CAMERA_REQUEST_CODE = 2
    private val PERMISSION_ALL = 3
    private val GALLERY_REQUEST_CODE = 300
    private val CAMERA_REQUEST_CODE = 400
    private val TAGGER = "TAGGER"
    private var filePath: String? = null
    private val FOLDER_NAME = ".juan"
    private var uriFromCamera: Uri? = null
    private var dialogPercent: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_una_imagen)

        btnGaleria.setOnClickListener {

            if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                abrirGaleria()
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.read_permission), PERMISSION_WRITE_REQUEST_CODE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        btnCamera.setOnClickListener {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)) {
                abrirCamara()
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.all_permision), PERMISSION_ALL, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            }
        }

        btnDelete.setOnClickListener {
            filePath = null
            ivPicture.setImageBitmap(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            filePath = getRealPathFromURIPath(uri!!)

            val myBitmap = BitmapFactory.decodeFile(filePath)
            ivPicture.setImageBitmap(myBitmap)

            btnDelete.visibility = View.VISIBLE
        }

        if (requestCode == CAMERA_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this@UnaImagen, "hasMemorySD: " + Common.hasMemorySD(), Toast.LENGTH_SHORT).show()

                uriFromCamera?.let {
                    val file = File(it.path)
                    filePath = file.path

                    Picasso.get().load(it).into(ivPicture)
                    btnDelete.visibility = View.VISIBLE
                }
            } else {
                uriFromCamera = null
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

    // REFERENCE: https://inthecheesefactory.com/blog/how-to-share-access-to-file-with-fileprovider-on-android-nougat/en
    fun abrirCamara() {

        getUriToSave()?.let {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                // Solo lo sobreescribimos para darle un valor temporal
                uriFromCamera = FileProvider.getUriForFile(this@UnaImagen, packageName + ".provider", it)
            } else {
                uriFromCamera = Uri.fromFile(it)
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFromCamera)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)

            // Devolvemos el valor al uri de path file.
            // TODO: cargarlo con URI de geabsolute.. de mutlieimagen?
            uriFromCamera = Uri.fromFile(it)
        }
    }

    fun abrirGaleria() {
        val openGalleryIntent = Intent(Intent.ACTION_PICK)
        openGalleryIntent.type = "image/*"
        startActivityForResult(openGalleryIntent, GALLERY_REQUEST_CODE)
    }

    fun getUriToSave(): File? {
        var file: File? = null
        if (Common.hasMemorySD()) {

        } else {
            val folder = File(Environment.getExternalStorageDirectory(), FOLDER_NAME)

            if (!folder.exists())
                folder.mkdirs()

            if (folder.exists()) {

                if (!File(folder, ".nomedia").exists())
                    File(folder, ".nomedia").createNewFile()
                folder.mkdirs()

                val df = SimpleDateFormat("dd_mm_yyyy_HH_MM_ss")
                val formattedDate = df.format(Calendar.getInstance().time)

                file = File(folder, formattedDate + ".jpg")

            }
        }

        file?.let {
            if (it.exists()) {
                it.delete()
            }
            return it
        }
        return null

    }

    // MARK: PERMISOS

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        if (requestCode == PERMISSION_WRITE_REQUEST_CODE) {
            abrirGaleria()
        }
        if (requestCode == PERMISSION_ALL && list.count() > 1) {
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

                    AndroidNetworking.upload("http://192.168.1.116/ApiPruebas/SubirImagen.php")
                            .addMultipartFile("file", file)
                            .setTag("uploadTest")
                            .setPriority(Priority.HIGH)
                            .build()
                            .setUploadProgressListener(this).getAsJSONObject(this)

                }

            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mi_menu, menu)
        return true
    }

    fun createOrUpdateDialog(percent: Int = 0) {

        dialogPercent?.let {
            val pb = it.findViewById<ProgressBar>(R.id.pbLoading) as ProgressBar
            val desc = it.findViewById<TextView>(R.id.tvDescription) as TextView

            pb.setProgress(percent)
            desc.text = "$percent %"

            return
        }

        val builder = AlertDialog.Builder(this@UnaImagen)
        val view = layoutInflater.inflate(R.layout.ad_progress, null)
        builder.setView(view)
        dialogPercent = builder.create()
        dialogPercent?.show()
    }


    // MARK: Progress uploader methods

    override fun onProgress(bytesUploaded: Long, totalBytes: Long) {
        val percent = 100 * bytesUploaded / totalBytes
        createOrUpdateDialog(percent.toInt())
    }


    override fun onResponse(response: JSONObject?) {
        dialogPercent?.dismiss()
        dialogPercent = null
    }

    override fun onError(anError: ANError?) {
        Log.e(TAGGER, "[ERROR]")
    }

}
