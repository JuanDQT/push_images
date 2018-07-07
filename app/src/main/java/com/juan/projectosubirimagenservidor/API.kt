package com.juan.projectosubirimagenservidor

import retrofit2.Call
import okhttp3.RequestBody
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*


/**
 * Created by juan on 4/11/17.
 */

interface API {

    @FormUrlEncoded
    @POST("doLogin.php")
    fun doLogin(@FieldMap parans: HashMap<String, String>): Call<String>

    @Multipart
    @POST("SubirImagen.php")
    fun uploadImage(@Part file: MultipartBody.Part, @Part("name") name: RequestBody): Call<ResponseBody>

    @Multipart
    @POST("SubirMultipleImagenes.php")
    fun fullUpload(@Part files: List<MultipartBody.Part>): Call<ResponseBody>
//    fun fullUpload(@Part file: List<MultipartBody.Part>, @Part("idIncidencia") idIncidencia: RequestBody): Call<ResponseBody>
//    fun fullUpload(@Part file: List<MultipartBody.Part>, @Part("idIncidencia") idIncidencia: RequestBody): Call<ResponseBody>
//    fun fullUpload(@Part file: Array<MultipartBody.Part>, @Part("idIncidencia") idIncidencia: RequestBody): Call<ResponseBody>
}