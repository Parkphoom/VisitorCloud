package com.wacinfo.visitorcloud.Login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.auth0.android.jwt.JWT
import com.google.gson.JsonSyntaxException
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.wacinfo.visitorcloud.LicenseActivity
import com.wacinfo.visitorcloud.MainActivity
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.ActivityLoginBinding
import com.wacinfo.visitorcloud.ui.AdminMenuActivity
import com.wacinfo.visitorcloud.utils.*
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "onNext"
    private val LOGIN_MODE_ADMIN = "admin"
    private val LOGIN_MODE_USER = "user"
    private var LoginMODE = ""
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()

    }

    private fun initView() {
        binding.loginBtn.setOnClickListener(this)

        LoginMODE = LOGIN_MODE_ADMIN

    }

    @SuppressLint("CheckResult")
    override fun onClick(v: View?) {
        if (v == binding.loginBtn) {
            if (LoginMODE == LOGIN_MODE_ADMIN) {
                val body = RetrofitData.Login.PostBody()
                body.username = binding.usernameEdt.text.toString()
                body.password = binding.passwordEdt.text.toString()
                io.reactivex.Observable
                    .just(login(body))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete {}
                    .subscribe {}
            }

        }
    }

    private fun login(body: RetrofitData.Login.PostBody) {
       val dialog= PublicFunction().retrofitDialog(this)
        if (!dialog!!.isShowing) {
            runOnUiThread {
                dialog.show()
            }
        }

        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_Login)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .postLogin(body, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Login> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Login) {
                    AppSettings.REFRESH_TOKEN = t.message?.refresh_token.toString()
                    token(AppSettings.REFRESH_TOKEN,dialog)
                }

                override fun onError(e: Throwable) {
                    PublicFunction().errorDialog(dialog).show()

                    e.printStackTrace()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(TAG, jObjError.getString("message"))
                        } catch (e: Exception) {
                            Log.d(TAG, "onError: $e")
                        }
                    }

                }
            })
    }

    private fun token(token: String, dialog: SweetAlertDialog) {
        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_Token)

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
                    val userIDData = parsedJWT.getClaim("userId")
                    val userID = userIDData.asString()
                    val ruleData = parsedJWT.getClaim("rule")
                    val rule = ruleData.asString()?.toLowerCase(Locale.getDefault())
                    val uidData = parsedJWT.getClaim("uId")
                    val uid = uidData.asString()

                    if (rule.equals(getString(R.string.RULE_Admin))) {
                        AppSettings.ACCESS_TOKEN = t.message?.access_token.toString()
                        AppSettings.USER_ID = userID!!
                        AppSettings.UID = uid!!
                        AppSettings.USER_NAME = binding.usernameEdt.text.toString()
                        AppSettings.USER_RULE = rule.toString().toUpperCase()
//                        Log.d(TAG, "onNext: ${AppSettings.ACCESS_TOKEN}")
//                        Log.d(TAG, "onNext: ${AppSettings.USER_ID}")
                        dialog.cancel()
                        syncSettings(userID.toString(), AppSettings.ACCESS_TOKEN)

                    } else {

                        dialog.setTitleText(R.string.RULE_FAIL)
                            .setConfirmText("Close")
                            .setConfirmButtonBackgroundColor(ContextCompat.getColor(dialog.context, R.color.red_active))
                            .setConfirmClickListener(null)
                            .changeAlertType(SweetAlertDialog.ERROR_TYPE)
                        dialog.show()
                    }


                }

                override fun onError(e: Throwable) {
                    PublicFunction().errorDialog(dialog).show()
                    e.printStackTrace()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(TAG, jObjError.getString("message"))
                        } catch (e: Exception) {
                            Log.d(TAG, e.toString())
                        }
                    } else {
                        PublicFunction().message(this@LoginActivity, "Sync data fail")
                    }


                }
            })
    }

    private fun syncSettings(userID: String, access_token: String) {
        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_Property)

        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val newRequest: Request = chain.request().newBuilder()
                    .addHeader("X-Authorization", "Bearer $access_token")
                    .build()
                chain.proceed(newRequest)
            }).build()

        val retrofit: Retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl("$url$apiname")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .getProperty(userID, access_token)
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

                        getLogo()
                    } catch (e: NullPointerException) {
                        Log.d(TAG, e.toString())
                    }

                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    if (e is HttpException) {
                        try {
                            Log.d(TAG, e.response().toString())
                        } catch (e: Exception) {
                            e.message?.let { Log.d(TAG, e.toString()) }
                        }
                    } else {
                        Log.d(TAG, "onError: $e")
                    }

                    PublicFunction().message(this@LoginActivity, "Sync data fail")
                }
            })
    }

    val TargetBitmap = object :Target{
        override fun onBitmapLoaded(
            bitmap: Bitmap?,
            from: Picasso.LoadedFrom?
        ) {
            Log.d(TAG, "onBitmapLoaded: ")
            if (bitmap != null) {
                AppSettings.USER_LOGO = bitmap
            }

            val data = RetrofitData.Activate()
            data.deviceId = PublicFunction().getIMEINumber(this@LoginActivity)
            data.userId = AppSettings.USER_ID

            postActivate(data)

        }

        override fun onBitmapFailed(
            e: java.lang.Exception?,
            errorDrawable: Drawable?
        ) {
            Log.d(TAG, "onBitmapFailed: $e")
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        }

    }
    private fun getLogo() {
        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_Logo)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url+apiname)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .getLogo(
                AppSettings.USER_ID,
                AppSettings.ACCESS_TOKEN
            )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : Observer<RetrofitData.Logo> {
                override fun onComplete() {


                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Logo) {
                    Log.d(TAG, "onNext: ${t.status}")
                    if(t.message!!.data!!.isNotEmpty()){
                        val picasso = Picasso.Builder(this@LoginActivity)
                            .downloader(OkHttp3Downloader(ConnectManager().authorizationHeader()))
                            .build()
                        picasso
                            .load( "${url}${getString(R.string.API_GetLogo)}${t.message!!.data!![0].imageLogo!![0]}")
                            .into(TargetBitmap)

                    }else{
                        val data = RetrofitData.Activate()
                        data.deviceId = PublicFunction().getIMEINumber(this@LoginActivity)
                        data.userId = AppSettings.USER_ID

                        postActivate(data)
                    }


                }

                override fun onError(e: Throwable) {
                    var strError = ""
                    Log.d(TAG, e.toString())
                    strError = e.toString()

                    if(e is JsonSyntaxException){
                        val data = RetrofitData.Activate()
                        data.deviceId = PublicFunction().getIMEINumber(this@LoginActivity)
                        data.userId = AppSettings.USER_ID

                        postActivate(data)
                    }else{
                        PublicFunction().message(
                            this@LoginActivity,
                            strError
                        )
                    }


                }
            })
    }

    private fun postActivate(data: RetrofitData.Activate) {
        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_Activate)

        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val newRequest: Request = chain.request().newBuilder()
                    .addHeader("X-Authorization", "Bearer ${AppSettings.ACCESS_TOKEN}")
                    .build()
                chain.proceed(newRequest)
            }).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .postActivate(data, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Activate> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Activate) {
                    if (t.message?.activeStatus == getString(R.string.Activate_ready)) {
                        AppSettings.Active_Mode = t.message?.activeMode.toString()

                        getSharedPreferences(getString(R.string.SharePreferencesSetting),0)
                            .edit()
                            .putBoolean(getString(R.string.Pref_Login_Status),true)
                            .putString(getString(R.string.Pref_Login_Username),binding.usernameEdt.text.toString())
                            .putString(getString(R.string.Pref_Login_Password),binding.passwordEdt.text.toString())
                            .apply()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                        )
                        startActivity(intent)
                        finish()
                    }

                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    Log.d(TAG, e.toString())

                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(TAG, jObjError.getString("message"))
                            if (jObjError.getString("message") == getString(R.string.Active_cannot)) {
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        LicenseActivity::class.java
                                    )
                                )
                                finish()
                            }

                        } catch (e: Exception) {
                            e.message?.let { Log.d(TAG, it) }
                        }
                    }

                    PublicFunction().message(
                        this@LoginActivity,
                        getString(R.string.activate_license_fail)
                    )
                }
            })
    }
}