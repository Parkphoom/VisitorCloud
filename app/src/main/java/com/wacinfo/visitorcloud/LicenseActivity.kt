package com.wacinfo.visitorcloud

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.wacinfo.visitorcloud.Login.LoginActivity
import com.wacinfo.visitorcloud.databinding.ActivityLicenseBinding
import com.wacinfo.visitorcloud.utils.API
import com.wacinfo.visitorcloud.utils.AppSettings
import com.wacinfo.visitorcloud.utils.AppSettings.Companion.Active_Mode
import com.wacinfo.visitorcloud.utils.PublicFunction
import com.wacinfo.visitorcloud.utils.RetrofitData
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


class LicenseActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "LicenseActivityLOG"
    var DeviceID = ""
    var isPermiss = false
    private val binding: ActivityLicenseBinding by lazy {
        ActivityLicenseBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.activate_license)
        Dexter.withContext(this)
            .withPermission(Manifest.permission.READ_PHONE_STATE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    binding.keyEdt.setText(PublicFunction().getIMEINumber(this@LicenseActivity))
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) {
                }
            }).check()
        initView()

    }

    private fun initView() {

        binding.activateBtn.setOnClickListener(this)

        val deviceID = PublicFunction().getIMEINumber(this)
//        var sensorstr = ""
//        for (i in 0 until (deviceID?.length?.minus(6)!!)) {
//            sensorstr += "X"
//        }
//        sensorstr += deviceID.substring(deviceID.length - 6)
        binding.keyEdt.setText(deviceID)

        binding.keyEdtLayout.setEndIconOnClickListener {
            val clipboard: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Text device Id",PublicFunction().getIMEINumber(this))
            clipboard.setPrimaryClip(clip)

            PublicFunction().message(this, getString(R.string.device_id) + " Copied to Clipboard")
        }
    }

    override fun onClick(v: View?) {
        if (v == binding.activateBtn) {
            val data = RetrofitData.Activate()
            data.deviceId = PublicFunction().getIMEINumber(this)
            data.userId = AppSettings.USER_ID
            postActivate(data)

        }
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
//            .client(client)
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
//                    startActivity(Intent(this@LicenseActivity, LoginActivity::class.java))
//                    finish()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Activate) {
                    Log.d(TAG, t.message?.activeStatus.toString())
                    if (t.message?.activeStatus == getString(R.string.Activate_ready)) {
                        Active_Mode = t.message?.activeMode.toString()

                        startActivity(Intent(this@LicenseActivity, MainActivity::class.java))
                        finish()
                    }

                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    Log.d(TAG, "//////////////")
                    Log.d(TAG, e.toString())
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(TAG, "//////////////")
                            Log.d(TAG, jObjError.getString("message"))
                        } catch (e: Exception) {
                            e.message?.let { Log.d(TAG, it) }
                        }
                    }

                    PublicFunction().message(
                        this@LicenseActivity,
                        getString(R.string.activate_license_fail)
                    )
                }
            })
    }
}