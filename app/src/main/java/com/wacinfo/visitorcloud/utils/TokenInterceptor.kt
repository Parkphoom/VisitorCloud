package com.wacinfo.visitorcloud.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.lang.reflect.Proxy

class TokenInterceptor(val context: Activity) : Interceptor {
    @SuppressLint("LogNotTimber")
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest: Request = chain.request().newBuilder()
            .header("Authorization", AppSettings.ACCESS_TOKEN)
//            .header("Content-Type", "application/json")
//            .header("Accept", "application/json")
            .build()
        val response = chain.proceed(newRequest)
        if (response.code == 400) {
            if (response.message == "Unauthorized" || response.message == "Bad Request" || response.message == "HTTP 400 Bad Request") {
                //Call refresh token service
                val refreshTokenResponse = ConnectManager().refreshToken(context).execute()
                Log.d("TokenInterceptor", "${response.code} : ${response.message}")
                if (refreshTokenResponse.isSuccessful) {
                    //Rewrite request of the service and call it gain
                    val newRequest = chain.request().newBuilder()
                        .addHeader(
                            "Authorization",
                            "${refreshTokenResponse.body()?.message?.access_token}"
                        )
//                        .addHeader("Content-Type", "application/json")
//                        .addHeader("Accept", "application/json")
                        .build()
                    AppSettings.ACCESS_TOKEN =
                        refreshTokenResponse.body()?.message?.access_token.toString()
                    response.close()
                    return chain.proceed(newRequest) //return response with new token
                }
            }
        }
        return response //return response with new token
    }
}