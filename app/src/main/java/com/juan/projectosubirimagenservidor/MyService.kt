package com.juan.projectosubirimagenservidor

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.juan.projectosubirimagenservidor.BuildConfig
import com.juan.projectosubirimagenservidor.Common
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by juan on 4/11/17.
 */

class MyService {

    private val service: API
    private val DEBUG: Boolean = false
    private val TAGGER = "TAGGER"

    init {

        val okHttpClient = OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(25, TimeUnit.SECONDS)
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.1.116/ApiPruebas/")
//                .baseUrl("http://localhost/ApiPruebas/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

        service = retrofit.create(API::class.java)
    }

    fun uploadImage(partFile: MultipartBody.Part, otroParametro: RequestBody) {

        service.uploadImage(partFile, otroParametro).enqueue(object : Callback<ResponseBody> {

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.d(TAGGER, "Error " + t?.message)
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                Log.d(TAGGER, "Response " + response?.raw()?.message())
            }
        } )

    }

    fun fullUpdload(partFiles: List<MultipartBody.Part>, idIncidencia: RequestBody) {

        service.fullUpload(partFiles).enqueue(object : Callback<ResponseBody> {

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.d(TAGGER, "Error " + t?.message)
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                Log.d(TAGGER, "Response " + response?.raw()?.message())
            }

        } )

    }

    fun getCommonParameters(params: HashMap<String, String>): HashMap<String, String> {

        params.put("version", BuildConfig.VERSION_NAME)
        if (DEBUG) {
            params.put("debug", "")
        }
        return params
    }
}

