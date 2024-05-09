package com.example.finalprojectaps

import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object NetworkUtils {

    fun getToken(clientId: String, clientSecret: String, callback: (String?) -> Unit) {
        val url = "https://accounts.spotify.com/api/token"
        val credentials = "$clientId:$clientSecret"
        val base64Credentials = android.util.Base64.encodeToString(credentials.toByteArray(), android.util.Base64.NO_WRAP)

        val client = OkHttpClient()
        val body = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .build()
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Basic $base64Credentials")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val accessToken = JSONObject(responseBody).optString("access_token")
                    callback(accessToken)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }
        })
    }

    private fun getEncodedCredentials(clientId: String, clientSecret: String): String {
        val credentials = "$clientId:$clientSecret"
        return android.util.Base64.encodeToString(credentials.toByteArray(), android.util.Base64.DEFAULT).trim()
    }
}
