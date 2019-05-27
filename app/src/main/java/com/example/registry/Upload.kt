package com.example.registry

import android.net.Uri
import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.apache.commons.io.FileUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Upload {

    fun uploadFile(fileUri: Uri) {
        // create upload service client
        val service = ServiceGenerator.createService(FileUploadService::class.java)

        // use the FileUtils to get the actual file by uri
        val  file = FileUtils.getFile(fileUri.path)

        // create RequestBody instance from file
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)

        // MultipartBody.Part is used to send also the actual file name
        val body = MultipartBody.Part.createFormData("picture", file.name, requestFile)

        // add another part within the multipart request
        val descriptionString = "123"
        val description = RequestBody.create(
            MultipartBody.FORM, descriptionString
        )

        // finally, execute the request
        val call = service.upload(description, body)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                Log.v("Upload", "success")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Upload error:", t.message)
            }
        })
        return
    }
}
