package com.wacinfo.visitorcloud.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.auth0.android.jwt.JWT
import com.wacinfo.visitorcloud.MainActivity
import com.wacinfo.visitorcloud.R
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.connection.ConnectInterceptor.intercept
import org.json.JSONObject
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class ConnectManager {


    fun authorizationHeader(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", AppSettings.ACCESS_TOKEN)
                    .build()
                chain.proceed(newRequest)
            })
            .build()
    }
    fun getToken(activity: Activity,token: String): Observable<RetrofitData.Login>? {
        val url: String = activity.getString(R.string.URL) + activity.getString(R.string.PORT)
        val apiname = activity.getString(R.string.API_Token)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

       return retrofit.create(API::class.java)
            .postToken(token, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())


    }

    fun token(activity: Activity,token: String) {

        val url: String = activity.getString(R.string.URL) + activity.getString(R.string.PORT)
        val apiname = activity.getString(R.string.API_Token)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .postToken(token, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Login> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Login) {
                    val parsedJWT = JWT(t.message?.refresh_token.toString())
                    val subscriptionMetaData = parsedJWT.getClaim("userId")
                    val userID = subscriptionMetaData.asString()
                    AppSettings.ACCESS_TOKEN = t.message?.access_token.toString()
                    Log.d("ConnectManager", "gettoken: ${AppSettings.ACCESS_TOKEN}")
                    syncSettings(userID.toString(), t.message?.access_token.toString(), activity)
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())

                        } catch (e: Exception) {
                        }
                    } else {
                        PublicFunction().message(activity, "Sync data fail")
                    }


                }
            })
    }
    fun syncSettings(userID: String, access_token: String, activity: Activity) {
        val url: String = activity.getString(R.string.URL) + activity.getString(R.string.PORT)
        val apiname = activity.getString(R.string.API_Property)

        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(activity)).build()

        val retrofit: Retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl("$url$apiname")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

            .build()

        retrofit.create(API::class.java)
            .getProperty(userID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Settings> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Settings) {
                    try {
                        if (t.message?.visitPlace?.isNotEmpty() == true) {
                            AppSettings.visitPlace = t.message?.visitPlace!!
                        } else {
                            AppSettings.visitPlace = emptyList<String>()
                        }
                        if (t.message?.vehicleType?.isNotEmpty() == true) {
                            AppSettings.vehicleType = t.message?.vehicleType!!
                        } else {
                            AppSettings.vehicleType = emptyList<String>()

                        }
                        if (t.message?.licensePlate?.isNotEmpty() == true) {
                            AppSettings.licensePlate = t.message?.licensePlate!!
                        } else {
                            AppSettings.licensePlate = emptyList<String>()
                        }
                        if (t.message?.visitorType?.isNotEmpty() == true) {
                            AppSettings.visitorType = t.message?.visitorType!!
                        } else {
                            AppSettings.visitorType = emptyList<String>()
                        }
                        if (t.message?.contactTopic?.isNotEmpty() == true) {
                            AppSettings.contactTopic = t.message?.contactTopic!!
                        } else {
                            AppSettings.contactTopic = emptyList<String>()
                        }
                        if (t.message?.whitelist?.isNotEmpty() == true) {
                            AppSettings.whitelist = t.message?.whitelist!!
                        } else {
                            AppSettings.whitelist = emptyList<RetrofitData.Settings.WhiteList>()
                        }
                        if (t.message?.visitFrom?.isNotEmpty() == true) {
                            AppSettings.visitFrom = t.message?.visitFrom!!
                        } else {
                            AppSettings.visitFrom = emptyList<String>()
                        }
                        if (t.message?.visitPerson?.isNotEmpty() == true) {
                            AppSettings.visitPerson = t.message?.visitPerson!!
                        } else {
                            AppSettings.visitPerson = emptyList<String>()
                        }
                        if (t.message?.department?.isNotEmpty() == true) {
                            AppSettings.department = t.message?.department!!
                        } else {
                            AppSettings.department = emptyList<String>()
                        }
                        if (t.message?.blacklist?.isNotEmpty() == true) {
                            AppSettings.blacklist = t.message?.blacklist!!
                        } else {
                            AppSettings.blacklist = emptyList<RetrofitData.Settings.BlackList>()
                        }


                    } catch (e: NullPointerException) {
                    }

                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                        } catch (e: Exception) {
                            e.message?.let { }
                        }
                    }

                    PublicFunction().message(activity, "Sync data fail")
                }
            })
    }
    fun refreshToken(context: Activity): Call<RetrofitData.Login> {
        val url: String = context.getString(R.string.URL) + context.getString(R.string.PORT)
        val apiToken = context.getString(R.string.API_Token)
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        return retrofit.create(API::class.java).refreshToken(AppSettings.REFRESH_TOKEN, apiToken)
    }
}