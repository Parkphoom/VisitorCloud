package com.wacinfo.visitorcloud.ui.checkout

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.ActivityFullScannerBinding
import com.wacinfo.visitorcloud.utils.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.dm7.barcodescanner.zbar.BarcodeFormat
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class CheckoutScannerActivity : AppCompatActivity(), ZBarScannerView.ResultHandler,
    View.OnClickListener {

    private val binding: ActivityFullScannerBinding by lazy {
        ActivityFullScannerBinding.inflate(
            layoutInflater
        )
    }
    private var mScannerView: ZBarScannerView? = null
    private val TAG = "ScannerLOG"
    private var mFlash = false
    private var mAutoFocus = false
    private var mSelectedIndices: ArrayList<Int>? = null
    private var mCameraId = -1
    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (state != null) {
                        mFlash = state.getBoolean(FLASH_STATE, false)
                        mAutoFocus = state.getBoolean(AUTO_FOCUS_STATE, true)
                        mSelectedIndices = state.getIntegerArrayList(SELECTED_FORMATS)
                        mCameraId = state.getInt(CAMERA_ID, -1)
                    } else {
                        mFlash = false
                        mAutoFocus = true
                        mSelectedIndices = null
                        mCameraId = -1
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {


                }
            }).check()



        initView()

    }

    private fun initView() {
        setContentView(binding.root)
        setupToolbar()
        val contentFrame = findViewById<View>(R.id.content_frame) as ViewGroup
        mScannerView = ZBarScannerView(this)
        setupFormats()
        contentFrame.addView(mScannerView)

        binding.flashBtn.setOnClickListener(this)
        binding.nextBtn.setOnClickListener(this)
        binding.vidEdt.setOnEditorActionListener { textView, actionId, keyEvent ->
            val result = actionId and EditorInfo.IME_MASK_ACTION
            when (result) {
                EditorInfo.IME_ACTION_DONE -> {
                    if (binding.vidEdt.text.isNullOrEmpty()) {
                        binding.vidEdtLayout.isErrorEnabled = true
                        binding.vidEdtLayout.error = getString(R.string.necessary)
                    } else {
                        Observable
                            .just(ConnectManager().token(this,AppSettings.REFRESH_TOKEN))
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete {
                                getCheckNum(binding.vidEdt.text.toString())
                            }
                            .subscribe()

                    }
                }
            }
            false
        }
    }

    override fun onClick(v: View?) {
        if (v == binding.flashBtn) {
            mFlash = !mFlash
            if (mFlash) {
                binding.flashBtn.setBackgroundResource(R.drawable.icons8_flash_on_80px)
                Log.d(TAG, "flash: on")
            } else {
                binding.flashBtn.setBackgroundResource(R.drawable.icons8_flash_off_80px)
                Log.d(TAG, "flash: off")
            }
            mScannerView!!.flash = mFlash
        }
        if (v == binding.nextBtn) {

            if (binding.vidEdt.text.isNullOrEmpty()) {
                binding.vidEdtLayout.isErrorEnabled = true
                binding.vidEdtLayout.error = getString(R.string.necessary)
            } else {
                Observable
                    .just(ConnectManager().token(this,AppSettings.REFRESH_TOKEN))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete {
                        getCheckNum(binding.vidEdt.text.toString())
                    }
                    .subscribe()
            }

        }
    }

    public override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView!!.startCamera() // Start camera on resume
        mScannerView!!.flash = mFlash
        mScannerView!!.setAutoFocus(mAutoFocus)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FLASH_STATE, mFlash)
        outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus)
        outState.putIntegerArrayList(SELECTED_FORMATS, mSelectedIndices)
        outState.putInt(CAMERA_ID, mCameraId)
    }

    public override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
        mScannerView!!.stopCamera() // Stop camera on pause
    }

    override fun handleResult(rawResult: Result) {
        // Do something with the result here
        Log.v(TAG, rawResult.contents) // Prints scan results
        Log.v(TAG, rawResult.barcodeFormat.name) // Prints the scan format (qrcode, pdf417 etc.)
        binding.vidEdt.setText(rawResult.contents)
        Observable
            .just(ConnectManager().token(this,AppSettings.REFRESH_TOKEN))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                getCheckNum(binding.vidEdt.text.toString())
            }
            .subscribe()


        // If you would like to resume scanning, call this method below:
        mScannerView!!.resumeCameraPreview(this)
    }

    private fun setupFormats() {
        val formats: MutableList<BarcodeFormat> = ArrayList()
        if (mSelectedIndices == null || mSelectedIndices!!.isEmpty()) {
            mSelectedIndices = ArrayList()
            for (i in BarcodeFormat.ALL_FORMATS.indices) {
                mSelectedIndices!!.add(i)
            }
        }
        for (index in mSelectedIndices!!) {
            formats.add(BarcodeFormat.ALL_FORMATS[index])
        }
        if (mScannerView != null) {
            mScannerView!!.setFormats(formats)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val FLASH_STATE = "FLASH_STATE"
        private const val AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE"
        private const val SELECTED_FORMATS = "SELECTED_FORMATS"
        private const val CAMERA_ID = "CAMERA_ID"
    }

    private fun getCheckNum(vid: String) {
        val dialog = PublicFunction().retrofitDialog(this)
        dialog?.show()

        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_CheckNum)
        val userID = AppSettings.USER_ID
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(this)).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url + apiname + userID + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .getChecknum(vid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Checknum> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Checknum) {
                    dialog?.cancel()
                    if (t.message!!.isEmpty()) {
                        binding.vidEdtLayout.isErrorEnabled = true
                        binding.vidEdtLayout.error = "ไม่พบหมายเลข Visitor"

                    } else {
                        binding.vidEdtLayout.isErrorEnabled = false

                        val bundle = Bundle()
                        bundle.putParcelable("Detail", t.message!![0])
                        startActivity(
                            Intent(
                                this@CheckoutScannerActivity, CheckoutDetailActivity::class.java
                            ).putExtras(bundle)
                        )
                    }

                }

                override fun onError(e: Throwable) {
                    dialog?.cancel()

                    var strError = ""
                    Log.d(TAG, e.toString())
                    strError = e.toString()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(TAG, jObjError.getString("message"))
                            strError = jObjError.getString("message")

                            if (strError == "Invalid input") {
                                strError = getString(R.string.error_emptytext)
                            }
                        } catch (e: Exception) {
                            e.message?.let {
                                Log.d(TAG, it)
                                strError = it
                            }

                        }
                    }

                    PublicFunction().message(
                        this@CheckoutScannerActivity,
                        strError
                    )
                }
            })
    }
}