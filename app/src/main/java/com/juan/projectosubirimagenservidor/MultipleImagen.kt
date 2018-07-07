package com.juan.projectosubirimagenservidor

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.ANRequest
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.UploadProgressListener
import com.androidnetworking.internal.ANRequestQueue
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_multiple_imagen.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.lang.Exception

class MultipleImagen : AppCompatActivity(), EasyPermissions.PermissionCallbacks, UploadProgressListener, JSONObjectRequestListener {

    private val PERMISSION_READ_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 200
    private val TAGGER = "TAGGER"
    private var imagePaths: ArrayList<String> = arrayListOf()
    private var dialogPercent: AlertDialog? = null

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

                if (!Common().isOnline) {
                    return true
                }

                createOrUpdateDialog()

                Toast.makeText(this, "Subimos", Toast.LENGTH_SHORT).show()

                val mapParts = mutableMapOf<String, File>()

                if (imagePaths.count() > 0) {

                    for ((index, path) in imagePaths.withIndex()) {
                        val file = File(path)
                        mapParts.put("files[$index]", file)
                    }

                    AndroidNetworking.upload("http://192.168.1.116/ApiPruebas/SubirMultipleImagenes.php")
                            .addMultipartFile(mapParts.toMap())
                            .addMultipartParameter("key", "value")
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

    fun createOrUpdateDialog(percent: Int = 0) {

        dialogPercent?.let {
            val pb = it.findViewById<ProgressBar>(R.id.pbLoading) as ProgressBar
            val desc = it.findViewById<TextView>(R.id.tvDescription) as TextView

            pb.setProgress(percent)
            desc.text = "$percent %"

            return
        }

        val builder = AlertDialog.Builder(this@MultipleImagen)
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