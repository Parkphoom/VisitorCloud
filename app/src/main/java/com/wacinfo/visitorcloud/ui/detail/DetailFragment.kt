package com.wacinfo.visitorcloud.ui.detail

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.*
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.bean.ApduRecvV2
import com.sunmi.pay.hardware.aidlv2.bean.ApduSendV2
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import com.wacinfo.visitorcloud.Adapter.AbstractFragment
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.DetailFragmentBinding
import com.wacinfo.visitorcloud.model.SharedViewModel
import com.wacinfo.visitorcloud.ui.dialog.SlipDialog
import com.wacinfo.visitorcloud.utils.*
import com.wacinfo.visitorcloud.utils.AppSettings.Companion.Appointment_Status
import com.wacinfo.visitorcloud.utils.Convert.hexStringToByteArray
import com.wacinfo.visitorcloud.utils.PrintUtils.Companion.isDisConnectService
import com.wacinfo.visitorcloud.utils.PrintUtils.Companion.mBasicOptV2
import com.wacinfo.visitorcloud.utils.PrintUtils.Companion.mEMVOptV2
import com.wacinfo.visitorcloud.utils.PrintUtils.Companion.mPinPadOptV2
import com.wacinfo.visitorcloud.utils.PrintUtils.Companion.mReadCardOptV2
import com.wacinfo.visitorcloud.utils.PrintUtils.Companion.mSecurityOptV2
import com.wacinfo.visitorcloud.utils.PrintUtils.Companion.mTaxOptV2
import io.reactivex.Observable
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
import sunmi.paylib.SunmiPayKernel
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class DetailFragment : Fragment(), View.OnClickListener {

    companion object {
        fun newInstance() = DetailFragment()

        private const val TAG = "DetailLog"
        private const val Image_Card_Code = 10011
        private const val Image_Cam_Code = 10022
        private const val Image_Cam_Code_2 = 20022
        private const val Image_Cam_Code_3 = 30022

        val REQUEST_WRITE_STORAGE_REQUEST_CODE = 101
        val REQUEST_WRITE_STORAGE_REQUEST_CODE_PERSON = 102
        val REQUEST_WRITE_STORAGE_REQUEST_READCARD = 103

        var CommandAPDU = arrayOf(
            "00A40400",
            "80B00004",
            "80B00011",
            "80B00075",
            "80B000D9",
            "80B000E1",
            "80B000F6",
            "80B00167",
            "80B0016F",
            "80B01579"
        )

        var LcAPDU = arrayOf(
            "08",
            "02",
            "02",
            "02",
            "02",
            "02",
            "02",
            "02",
            "02",
            "02"
        )
        var DataInAPDU = arrayOf(
            "A000000054480001",
            "000D",
            "0064",
            "0064",
            "0008",
            "0001",
            "0064",
            "0008",
            "0008",
            "0064"
        )
        var LeAPDU = arrayOf(
            "00",
            "0D",
            "64",
            "64",
            "08",
            "01",
            "64",
            "08",
            "08",
            "64"
        )

        var codephoto: Array<String>
            get() = arrayOf(
                "80B0017B",
                "80B0027A",
                "80B00379",
                "80B00478",
                "80B00577",
                "80B00676",
                "80B00775",
                "80B00874",
                "80B00973",
                "80B00A72",
                "80B00B71",
                "80B00C70",
                "80B00D6F",
                "80B00E6E",
                "80B00F6D",
                "80B0106C",
                "80B0116B",
                "80B0126A",
                "80B01369",
                "80B01468",
                "80B01567"
            )
            set(value) = TODO()
    }

    var photoURI: Uri? = null
    var CardURI: Uri? = null
    var CamURI: Uri? = null
    var Cam2URI: Uri? = null
    var Cam3URI: Uri? = null
    private lateinit var viewModel: DetailViewModel
    private lateinit var binding: DetailFragmentBinding
    private lateinit var sharedViewModel: SharedViewModel

    //    var rescan: TextView? = null
    private var isReadyToRead: Boolean = true
    private var isSkiptelno: Boolean = false
    private var dialog: Dialog? = null
    private var CARD_ABSENT_dialog: AlertDialog? = null
    private var mSMPayKernel: SunmiPayKernel? = null
    private var cardType = 0
    private var mStrMessage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = activity?.run {
            ViewModelProviders.of(this)[SharedViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        requestVisitorId()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.detail_fragment, container, false)
        binding = DetailFragmentBinding.bind(root)


        initView()
        Dexter.withContext(requireContext())
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {


                }
            }).check()

        val toolbar: Toolbar = activity?.findViewById<View>(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.info)
        toolbar.setNavigationIcon(R.drawable.ic_back_white)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        connectPayService()
        checkCard()

        viewModel = ViewModelProvider(this).get(DetailViewModel::class.java)
        viewModel.mContext = requireActivity()
        viewModel.Card_uri.observe(viewLifecycleOwner, {
            binding.imgCard.setImageURI(it)
        })
        viewModel.Car_uri.observe(viewLifecycleOwner, {
            binding.imgEmpty.visibility = View.GONE
            binding.imgCar.setImageURI(it)
        })
        viewModel.Car_uri_2.observe(viewLifecycleOwner, {
            binding.img2Empty.visibility = View.GONE
            binding.img2Car.setImageURI(it)
        })
        viewModel.Car_uri_3.observe(viewLifecycleOwner, {
            binding.img3Empty.visibility = View.GONE
            binding.img3Car.setImageURI(it)
        })

        viewModel.visitorId.observe(viewLifecycleOwner, {
            binding.vidEdt.setText(it)
        })

        return root
    }

    fun connectPayService() {
        Log.d("SystemUtil", AidlConstants.SysParam.DEVICE_MODEL)
        try {
            mSMPayKernel = SunmiPayKernel.getInstance()
            mSMPayKernel!!.initPaySDK(requireContext(), mConnectCallback)
        } catch (e: Exception) {
        }
    }


    private val mConnectCallback: SunmiPayKernel.ConnectCallback = object :
        SunmiPayKernel.ConnectCallback {
        override fun onConnectPaySDK() {
            Log.e(TAG, "onConnectPaySDK")
            try {
                mEMVOptV2 = mSMPayKernel!!.mEMVOptV2
                mBasicOptV2 = mSMPayKernel!!.mBasicOptV2
                mPinPadOptV2 = mSMPayKernel!!.mPinPadOptV2
                mReadCardOptV2 = mSMPayKernel!!.mReadCardOptV2
                mSecurityOptV2 = mSMPayKernel!!.mSecurityOptV2
                mTaxOptV2 = mSMPayKernel!!.mTaxOptV2
                isDisConnectService = false
            } catch (e: Exception) {
                Log.d("SystemUtil", "connect fail")
                e.printStackTrace()
            }
        }

        override fun onDisconnectPaySDK() {
            Log.e(TAG, "onDisconnectPaySDK")
            Log.d("SystemUtil", "connect faillll")
            isDisConnectService = true
            //            showToast(R.string.connect_fail);
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Image_Card_Code) {
            if (resultCode == Activity.RESULT_OK) {
                if (photoURI != null) {
                    viewModel.setCardUri(photoURI!!)
                    CardURI = photoURI
//                    PublicFunction().message(requireActivity(), getString(R.string.success_en))
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
        if (requestCode == Image_Cam_Code) {
            if (resultCode == Activity.RESULT_OK) {
                if (photoURI != null) {
                    viewModel.setCarUri(photoURI!!)
                    CamURI = photoURI
//                    PublicFunction().message(requireActivity(), getString(R.string.success_en))
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
        if (requestCode == Image_Cam_Code_2) {
            if (resultCode == Activity.RESULT_OK) {
                if (photoURI != null) {
                    viewModel.setCarUri2(photoURI!!)
                    Cam2URI = photoURI
                }

            }
        }
        if (requestCode == Image_Cam_Code_3) {
            if (resultCode == Activity.RESULT_OK) {
                if (photoURI != null) {
                    viewModel.setCarUri3(photoURI!!)
                    Cam3URI = photoURI
                }

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: ${item.itemId}")
        when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        binding.btnTakePicture.setOnClickListener(this)
        binding.btnTakePicture2.setOnClickListener(this)
        binding.btnTakePicture3.setOnClickListener(this)
        binding.imgCam.setOnClickListener(this)
        binding.imgCard.setOnClickListener(this)
        binding.typelistBtn.setOnClickListener(this)
        binding.placelistBtn.setOnClickListener(this)
        binding.vehiclelistBtn.setOnClickListener(this)
        binding.nextBtn.setOnClickListener(this)
        binding.licenseplatelistBtn.setOnClickListener(this)
        binding.followerlistBtn.setOnClickListener(this)
        binding.departmentlistBtn.setOnClickListener(this)
        binding.contactTopiclistBtn.setOnClickListener(this)
        binding.tvRequestVid.setOnClickListener(this)

        binding.typeEdt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.d(TAG, "afterTextChanged: $s")
                if (!s.isNullOrEmpty()) {
                    binding.typeEdt.error = null
                } else {
                    binding.typeEdt.error = getString(R.string.necessary)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d(TAG, "beforeTextChanged: $s $start $count $after")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged:$s $start $before $count")
            }
        })
        binding.vidEdt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.d(TAG, "afterTextChanged: $s")
                if (!s.isNullOrEmpty()) {
                    binding.vidEdtLayout.isErrorEnabled = false
                    getCheckNum(binding.vidEdt.text.toString())!!.subscribe(object :
                        Observer<RetrofitData.Checknum> {
                        override fun onComplete() {
                        }

                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(t: RetrofitData.Checknum) {
                            if (t.message!!.isEmpty()) {
                                binding.vidEdtLayout.isErrorEnabled = false
                            } else {
                                binding.vidEdtLayout.isErrorEnabled = true
                                binding.vidEdtLayout.error = "หมายเลข Visitor ถูกใช้แล้ว"
                            }

                        }

                        override fun onError(e: Throwable) {
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
                                requireActivity(),
                                strError
                            )
                        }
                    })
                }
//                else {
//                    binding.typeEdt.error = getString(R.string.necessary)
//                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d(TAG, "beforeTextChanged: $s $start $count $after")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged:$s $start $before $count")
            }
        })

    }

    override fun onClick(v: View?) {
        if (v == binding.btnTakePicture || v == binding.imgCam) {
            openCamera("Img_Cam", Image_Cam_Code)
        }
        if (v == binding.btnTakePicture2 || v == binding.img2Cam) {
            openCamera("Img_Cam2", Image_Cam_Code_2)
        }
        if (v == binding.btnTakePicture3 || v == binding.img3Cam) {
            openCamera("Img_Cam3", Image_Cam_Code_3)
        }
        if (v == binding.imgCard) {
            openCamera("Img_Card", Image_Card_Code)
        }
        if (v == binding.tvRequestVid) {
            binding.vidEdtLayout.isErrorEnabled = false
            requestVisitorId()
        }
        if (v == binding.typelistBtn) {
            if (AppSettings.visitorType.isNullOrEmpty()) {
                PublicFunction().message(requireActivity(), "ไม่มีข้อมูล")
            } else {
                viewModel.createSelectWheel(
                    requireActivity(),
                    AppSettings.visitorType,
                    binding.typeEdt
                )
            }
        }
        if (v == binding.placelistBtn) {
            if (AppSettings.visitPlace.isNullOrEmpty()) {
                PublicFunction().message(requireActivity(), "ไม่มีข้อมูล")
            } else {
                viewModel.createSelectWheel(
                    requireActivity(),
                    AppSettings.visitPlace,
                    binding.placeEdt
                )
            }

        }
        if (v == binding.vehiclelistBtn) {
            if (AppSettings.vehicleType.isNullOrEmpty()) {
                PublicFunction().message(requireActivity(), "ไม่มีข้อมูล")
            } else {
                viewModel.createSelectWheel(
                    requireActivity(),
                    AppSettings.vehicleType,
                    binding.vehicleEdt
                )
            }

        }
        if (v == binding.licenseplatelistBtn) {
            if (AppSettings.licensePlate.isNullOrEmpty()) {
                PublicFunction().message(requireActivity(), "ไม่มีข้อมูล")
            } else {
                viewModel.createSelectWheel(
                    requireActivity(),
                    AppSettings.licensePlate,
                    binding.licenseplateEdt
                )
            }

        }
        if (v == binding.followerlistBtn) {
            viewModel.createNumberWheel(
                requireActivity(),
                binding.followerEdt
            )
        }
        if (v == binding.contactTopiclistBtn) {
            if (AppSettings.contactTopic.isNullOrEmpty()) {
                PublicFunction().message(requireActivity(), "ไม่มีข้อมูล")
            } else {
                viewModel.createSelectWheel(
                    requireActivity(),
                    AppSettings.contactTopic,
                    binding.contactTopicEdt
                )
            }
        }
        if (v == binding.departmentlistBtn) {
            if (AppSettings.department.isNullOrEmpty()) {
                PublicFunction().message(requireActivity(), "ไม่มีข้อมูล")
            } else {
                viewModel.createSelectWheel(
                    requireActivity(),
                    AppSettings.department,
                    binding.departmentEdt
                )
            }
        }
        if (v == binding.nextBtn) {

            if (binding.vidEdt.text.isNullOrEmpty()) {
                binding.vidEdtLayout.isErrorEnabled = true
                binding.vidEdtLayout.error = getString(R.string.necessary)
            } else if (binding.typeEdt.text.isNullOrEmpty()) {
                binding.typeEdt.error = getString(R.string.necessary)
            } else if (!binding.vidEdtLayout.isErrorEnabled && !binding.typeEdt.text.isNullOrEmpty()) {
                val dialog = PublicFunction().retrofitDialog(requireContext())
                if (!dialog!!.isShowing) {
                    requireActivity().runOnUiThread {
                        dialog.show()
                    }
                }

                getCheckNum(binding.vidEdt.text.toString())!!.subscribe(object :
                    Observer<RetrofitData.Checknum> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(t: RetrofitData.Checknum) {
                        if (t.message!!.isEmpty()) {
                            binding.vidEdtLayout.isErrorEnabled = false
                            loadBlacklist(dialog)
                        } else {
                            dialog.cancel()
                            binding.vidEdtLayout.isErrorEnabled = true
                            binding.vidEdtLayout.error = "หมายเลข Visitor ถูกใช้แล้ว"
                        }

                    }

                    override fun onError(e: Throwable) {
                        dialog.cancel()
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
                            requireActivity(),
                            strError
                        )
                    }
                })

//                findNavController().navigate(R.id.action_detail_to_appointment)
            }

        }
    }

    private fun createSlip(): SlipDialog {

        val bottomSheetDialog =
            SlipDialog.newInstance().apply {
                visitorType = binding.typeEdt.text.toString()
                visitorNumber = binding.vidEdt.text.toString()
                name = binding.nameEdt.text.toString()
                cid = binding.cidEdt.text.toString()
                place = binding.placeEdt.text.toString()
                vehicle = binding.vehicleEdt.text.toString()
                licenseplate = binding.licenseplateEdt.text.toString()
                follower = binding.followerEdt.text.toString()
                department = binding.departmentEdt.text.toString()
                contactTopic = binding.contactTopicEdt.text.toString()
                etc = binding.etcEdt.text.toString()
                cardURI = CardURI
                camURI = CamURI
                cam2URI = Cam2URI
                cam3URI = Cam3URI
                onCloseListener = {
                    requireActivity().onBackPressed()
                }
            }
//        bottomSheetDialog.show(
//            requireActivity().supportFragmentManager,
//            "Bottom Sheet Dialog Fragment"
//        )
        sharedViewModel.setSlipDialog(bottomSheetDialog)

        return bottomSheetDialog
    }

    private fun openCamera(imgName: String, requestCode: Int) {
        try {
            photoURI = PublicFunction().getPhotoFileUri(requireActivity(), imgName)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                photoURI
            )
            requireActivity().startActivityFromFragment(
                this,
                cameraIntent,
                requestCode
            )

            Log.d(TAG, photoURI!!.path.toString())
        } catch (e: SecurityException) {
            Log.d(TAG, e.message.toString())
            PublicFunction().message(
                requireActivity(),
                "Camera and Storage permission are needed"
            )
        }
    }


    private fun checkCard() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_WRITE_STORAGE_REQUEST_READCARD
                )
            }
        } else {
            try {
                val allType =
                    (AidlConstants.CardType.NFC.value or AidlConstants.CardType.IC.value
                            or AidlConstants.CardType.MAGNETIC.value)
                Log.d(
                    TAG,
                    allType.toString()
                )
                mReadCardOptV2!!.checkCard(allType, mReadCardCallback, 5000)
                //           mReadCardOptV2.time
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(
                    TAG,
                    e.toString()
                )
            }
        }
    }

    private val mReadCardCallback: CheckCardCallbackV2 = object : CheckCardCallbackV2.Stub() {
        override fun findMagCard(bundle: Bundle) {
            Log.e(
                TAG,
                "findMagCard,bundle:$bundle"
            )
            cardType = AidlConstants.CardType.MAGNETIC.value
            requireActivity().runOnUiThread(Runnable {
                binding.nameEdt!!.setText("")
                binding.cidEdt!!.setText("")
                val track1 = bundle.getString("TRACK1")
                val track2 = bundle.getString("TRACK2")
                val track3 = bundle.getString("TRACK3")
                val isEmpty =
                    TextUtils.isEmpty(track1) && TextUtils.isEmpty(track2)
                if (isEmpty) {
                    failed()
                } else {
                    if (track1 != null && track2 != null) {
                        success(track1, track2)
                    }
                }
                handleResult(false)
            })
        }

        @Throws(RemoteException::class)
        override fun findICCard(atr: String) {
            if (isReadyToRead) {
                isReadyToRead = false
                Log.e(
                    TAG,
                    "findICCard, atr:$atr"
                )
                cardType = AidlConstants.CardType.IC.getValue()
                Log.e(
                    TAG,
                    "cardtype :$cardType"
                )
                if (mReadCardOptV2!!.getCardExistStatus(cardType) === AidlConstants.CardExistStatus.CARD_ABSENT) {
                    Log.e(
                        TAG,
                        "cardExit :" + "CARD_ABSENT"
                    )
                } else if (mReadCardOptV2!!.getCardExistStatus(cardType) === AidlConstants.CardExistStatus.CARD_PRESENT) {
                    Log.e(
                        TAG,
                        "cardExit :" + "CARD_PRESENT"
                    )
                }
                requireActivity().runOnUiThread {
                    ICReadTask()
                        .execute()
                }
            } else {
                handleResult(false)
            }
        }

        @Throws(RemoteException::class)
        override fun findRFCard(uuid: String) {
            Log.e(
                TAG,
                "findRFCard, uuid:$uuid"
            )
            cardType = AidlConstants.CardType.NFC.value
//            message("uuid:$uuid")
            handleResult(false)
        }

        @Throws(RemoteException::class)
        override fun onError(code: Int, msg: String) {
            Log.e(
                TAG,
                "check card error,code:" + code + "message:" + msg
            )
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class ICReadTask : AsyncTask<Void?, Int?, Void?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            binding.nameEdt.setText("")
            binding.cidEdt.setText("")
            dialog = Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar)
            val window: Window = dialog!!.getWindow()!!
            if (window != null) {
                window.setGravity(Gravity.CENTER)
                window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                dialog!!.setContentView(R.layout.dialog_loading)
                dialog!!.setCancelable(false)
                dialog!!.show()
            }
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            sendApduByApduData()
//            if (getSwitchSetting(getString(R.string.status_read_photo))) {
            sendApduByApduIMG()
//            } else {
            handleResult(true)
//            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            dialog!!.cancel()
            CARD_ABSENT_dialog = createBuilder().create()
            CARD_ABSENT_dialog!!.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun sendApduByApduData() {
        val send = ApduSendV2()
        for (i in CommandAPDU.indices) {
            send.command = hexStringToByteArray(CommandAPDU[i])
            send.lc = LcAPDU[i].toShort(16)
            send.dataIn = hexStringToByteArray(DataInAPDU[i])
            send.le = LeAPDU[i].toShort(16)
            try {
                val recv = ApduRecvV2()
                var code = 0
                try {
                    code = mReadCardOptV2!!.apduCommand(cardType, send, recv)
                    if (code < 0) {
                        Log.e(
                            TAG,
                            "apduCommand failed,code:\$code"
                        )
                    } else {
                        Log.e(
                            TAG,
                            "apduCommand success,recv:\$recv $i"
                        )
                        //                        showApduRecv(recv.outlen.toInt(), recv.outData, recv.swa, recv.swb)
                        var recStr = ""
                        try {
                            recStr = String(recv.outData, charset("TIS620"))
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }
                        recStr = recStr.replace(" ".toRegex(), "")
                        recStr = recStr.replace("\n".toRegex(), "")
                        recStr = recStr.replace("\u0090".toRegex(), "")
                        recStr = recStr.replace("##".toRegex(), " ")
                        recStr = recStr.replace("#".toRegex(), " ")
                        recStr = recStr.replace("\\s".toRegex(), " ")
                        recStr = recStr.replace("\\0", "").replace("\u0000", "")
                        val finalRecStr = recStr
                        requireActivity().runOnUiThread {
                            when (i) {
                                1 -> {
                                    run {
//                                        val sensorstr =
//                                            "X XXXX XXXXX " + finalRecStr.substring(finalRecStr.length - 3)
//                                        binding.cidEdt.setFilters(
//                                            arrayOf<InputFilter>(
//                                                LengthFilter(
//                                                    17
//                                                )
//                                            )
//                                        )
                                        binding.cidEdt.setText(finalRecStr)
//                                        binding.cidEdt.setFilters(
//                                            arrayOf<InputFilter>(
//                                                LengthFilter(
//                                                    13
//                                                )
//                                            )
//                                        )

                                    }
                                }
                                2 -> {
                                    run {
                                        val namesplit = finalRecStr.split(" ")
                                        binding.nameEdt.setText(
                                            "${namesplit[0]}${namesplit[1]} ${
                                                namesplit.get(
                                                    2
                                                )
                                            }"
                                        )
                                        Log.d("readcard2", finalRecStr)
                                    }

                                }

                                else -> {
                                }
                            }
                        }
                    }
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
            }
        }
    }

    private var strpicture = ""


    private fun sendApduByApduIMG() {
        val send = ApduSendV2()
        val recv = ApduRecvV2()
        send.command = hexStringToByteArray(CommandAPDU[0])
        send.lc = LcAPDU[0].toShort(16)
        send.dataIn = hexStringToByteArray(DataInAPDU[0])
        send.le = LeAPDU[0].toShort(16)
        try {
            mReadCardOptV2!!.apduCommand(cardType, send, recv)
            var i = 0
            while (i < codephoto.size) {
                send.command = hexStringToByteArray(codephoto[i])
                send.lc = "02".toShort(16)
                send.dataIn = hexStringToByteArray("00FF")
                send.le = "FF".toShort(16)
                try {
                    val code: Int = mReadCardOptV2!!.apduCommand(cardType, send, recv)

                    if (code < 0) {
                        Log.d(
                            TAG,
                            "apduCommand failed,code:\$code"
                        )
                    } else {
                        Log.d(
                            TAG,
                            "apduCommand success,recv:\$recv \$i"
                        )
                        //                    showApduRecv(recv.outlen.toInt(), recv.outData, recv.swa, recv.swb)
                        Log.d("codeimg", Convert.bytesToHex(recv.outData))
                        strpicture += Convert.bytesToHex(recv.outData)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d(
                        TAG,
                        e.toString()
                    )
                }
                i++
            }
            requireActivity().runOnUiThread {
                Log.d("imgggg", strpicture)
                val byteRawHex: ByteArray = Convert.hexStringToByteArray(strpicture)
                var imgBase64String: String? = ""
                imgBase64String =
                    Base64.encodeToString(byteRawHex, Base64.NO_WRAP)
                Log.d("imgggg", imgBase64String)
                var bitmapCard = BitmapFactory.decodeByteArray(byteRawHex, 0, byteRawHex.size)
                binding.imgCard.setImageBitmap(bitmapCard)
                val bytes = ByteArrayOutputStream()
                bitmapCard.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                val path: String =
                    Images.Media.insertImage(
                        requireActivity().getContentResolver(),
                        bitmapCard,
                        "Title",
                        null
                    )
                CardURI = Uri.parse(path)

                handleResult(true)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private var mTotalTime = 0
    private var mSuccessTime = 0
    private var mFailTime = 0

    private fun failed() {
        mTotalTime += 1
        mFailTime += 1
    }

    private fun success(
        track1: String,
        track2: String
    ) {
        try {
            mTotalTime += 1
            mSuccessTime += 1
            var a = arrayOfNulls<String>(2)
            var temp = " $track1"
            temp = temp.replace("^", "@")
            temp = temp.replace("$", "@")
            a = temp.split("@".toRegex()).toTypedArray()
            binding.nameEdt!!.setText(a[3].toString() + " " + a[2] + " " + a[1])
            temp = " " + track2.substring(6, 19)
            binding.cidEdt!!.setText(temp.trim())
            Log.d("MagneticRead", temp)
        } catch (e: java.lang.Exception) {
            binding.nameEdt!!.setText("")
            binding.cidEdt!!.setText("")
            Log.d("MagneticRead", e.toString())
        }
    }

    private val mHandler = Handler()

    private fun handleResult(nfcfinish: Boolean) {
        if (requireActivity().isFinishing) {
            return
        }
        mHandler.post {
            if (cardType == AidlConstants.CardType.MAGNETIC.value) {
                isReadyToRead = true
                checkCard()
            } else if (cardType == AidlConstants.CardType.NFC.value) {
                isReadyToRead = true
                checkCard()
            } else if (cardType == AidlConstants.CardType.IC.value) {
                try {
                    if (nfcfinish && mReadCardOptV2!!.getCardExistStatus(cardType) === AidlConstants.CardExistStatus.CARD_ABSENT) {
                        isReadyToRead = true
                        checkCard()
                    } else if (nfcfinish &&
                        mReadCardOptV2!!.getCardExistStatus(cardType) === AidlConstants.CardExistStatus.CARD_PRESENT
                    ) {
                        isReadyToRead = false
                        handleResult(false)
                    }
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
                // 继续检卡
                if (!requireActivity().isFinishing) {
                    try {
                        if (mReadCardOptV2!!.getCardExistStatus(cardType) === AidlConstants.CardExistStatus.CARD_ABSENT) {
                            Log.e(
                                TAG,
                                "cardExit :" + "CARD_ABSENT"
                            )
                            CARD_ABSENT_dialog?.cancel()
                            isReadyToRead = true
                            checkCard()
                        } else if (mReadCardOptV2!!.getCardExistStatus(cardType) === AidlConstants.CardExistStatus.CARD_PRESENT) {
                            Log.e(
                                TAG,
                                "cardExit :" + "CARD_PRESENT"
                            )
                            isReadyToRead = false
                            handleResult(false)
                        }
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun createBuilder(): AlertDialog.Builder {
        val builder = AlertDialog.Builder(requireContext())

        // Set the alert dialog title
        builder.setTitle("แจ้งเตือน!!!")

        // Display a message on alert dialog
        builder.setMessage("ดึงบัตรประชาชนออก")

        // Display a negative button on alert dialog
        builder.setNegativeButton("ปิด") { dialog, which ->
//            Toast.makeText(applicationContext, "You are not agree.", Toast.LENGTH_SHORT).show()
        }

        return builder
    }

    private fun cancelCheckCard() {
        try {
            mReadCardOptV2!!.cardOff(AidlConstants.CardType.NFC.value)
            mReadCardOptV2!!.cancelCheckCard()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun loadBlacklist(dialog: SweetAlertDialog) {
        // Simulating asynchronous call
        val url: String = getString(R.string.URL) + getString(R.string.PORT)
        val apigetblacklist = getString(R.string.API_Blacklist)
        val userID = AppSettings.USER_ID

        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url + apigetblacklist)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        val observable: Observable<RetrofitData.Blacklist.Get> = retrofit.create(API::class.java)
            .getBlackList(
                userID
            )
        observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Blacklist.Get> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("SimpleDateFormat")
                override fun onNext(t: RetrofitData.Blacklist.Get) {
                    Log.d(TAG, "onNext: ${t.message?.blacklist?.size}")
                    var isBlacklist = false
                    if (t.message?.blacklist?.isNotEmpty()!!) {
                        for (i in 0 until t.message?.blacklist?.size!!) {
                            if (binding.cidEdt.text?.toString() == t.message?.blacklist!![i].citizenId) {
//                                binding.nameEdt.text?.toString() == t.message?.blacklist!![i].name ||
                                for (j in t.message?.blacklist!![i].time?.indices!!) {
                                    val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
                                    val start: Date = sdf.parse(
                                        PublicFunction().convertToNewFormat(
                                            t.message?.blacklist!![i].time?.get(j)?.timeStart
                                        )
                                    )
                                    val stop: Date = sdf.parse(
                                        PublicFunction().convertToNewFormat(
                                            t.message?.blacklist!![i].time?.get(j)?.timeStop
                                        )
                                    )
                                    val now: Date = sdf.parse(PublicFunction().getDatetimenow())
                                    val isAvailable = start.time < now.time && stop.time > now.time
                                    if (isAvailable) {
                                        val alertDialog = SweetAlertDialog(
                                            requireContext(),
                                            SweetAlertDialog.ERROR_TYPE
                                        )
                                        alertDialog.setTitleText("ไม่อนุญาตผ่าน")
                                        alertDialog.setContentText("สถานะ : Blacklist")
                                        alertDialog.setConfirmText(getString(R.string.accept_th))
                                        alertDialog.setOnShowListener(DialogInterface.OnShowListener {
                                            val alertDialog = it as SweetAlertDialog
                                            val text =
                                                alertDialog.findViewById<View>(R.id.title_text) as TextView
                                            text.textAlignment = View.TEXT_ALIGNMENT_CENTER
                                            text.isSingleLine = false
                                        })
                                        alertDialog.show()
                                        isBlacklist = true
                                    }

                                }
                            }

                        }
                        if (!isBlacklist) {
                            checkAppointment(dialog)

                            createSlip()
                        } else {
                            dialog.cancel()
                        }

                    } else {
                        checkAppointment(dialog)
//                        findNavController().navigate(R.id.action_detail_to_appointment)
                        createSlip()
                    }
                }

                override fun onError(e: Throwable) {
                    dialog.cancel()
                    var strError = ""
                    Log.d(AbstractFragment.TAG, e.toString())
                    strError = e.toString()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(AbstractFragment.TAG, jObjError.getString("message"))
                            strError = jObjError.getString("message")

                            if (strError == "Unauthorized") {
                                PublicFunction().tokenError(requireActivity())
                            }
                        } catch (e: Exception) {
                            e.message?.let {
                                Log.d(AbstractFragment.TAG, it)
                                strError = it
                            }

                        }
                    }
                    Log.d(TAG, "loadBlacklist: $strError")

                }
            })

    }

    private fun checkAppointment(dialog: SweetAlertDialog) {
        val url: String = getString(R.string.URL) + getString(R.string.PORT)
        val apigetmeeting = getString(R.string.API_Appointment)
        val userID = AppSettings.USER_ID
        val uid = AppSettings.UID

        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url + apigetmeeting)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        val observable: Observable<RetrofitData.Appointment.Get> = retrofit.create(API::class.java)
            .getAppointment(
                userID,
                uid
            )
        observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Appointment.Get> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Appointment.Get) {

                    dialog.cancel()

                    val AppointmentList = ArrayList<RetrofitData.Appointment.Get.Message.Result>()
                    for (i in 0 until t.message?.result?.size!!) {
                        var isNamematch = false
                        var isPlacematch = false
                        var isLicenseplatematch = false

                        if (t.message?.result!![i].status == Appointment_Status) {
                            if (binding.nameEdt.text?.toString()!!.isNotEmpty()) {
                                isNamematch = filter(
                                    t.message?.result!![i].name!!,
                                    binding.nameEdt.text?.toString()!!
                                )
                            }
                            if (binding.placeEdt.text?.toString()!!.isNotEmpty()) {
                                isPlacematch = filter(
                                    t.message?.result!![i].meetUpLocal!!,
                                    binding.placeEdt.text?.toString()!!
                                )
                            }
                            if (binding.licenseplateEdt.text?.toString()!!.isNotEmpty()) {
                                isLicenseplatematch = filter(
                                    t.message?.result!![i].licensePlate!!,
                                    binding.licenseplateEdt.text?.toString()!!
                                )
                            }
                            if (isNamematch || isPlacematch || isLicenseplatematch) {
                                AppointmentList.add(t.message?.result!![i])
                            }
                        }

                    }

                    if (AppointmentList.isNotEmpty()) {
                        sharedViewModel.setAppointmentList(AppointmentList)
                        findNavController().navigate(R.id.action_detail_to_appointment)
                    } else {
                        createSlip().show(
                            requireActivity().supportFragmentManager,
                            "Bottom Sheet Dialog Fragment"
                        )
                    }
                }

                override fun onError(e: Throwable) {
                    dialog.cancel()
                    var strError = ""
                    Log.d(AbstractFragment.TAG, e.toString())
                    strError = e.toString()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(AbstractFragment.TAG, jObjError.getString("message"))
                            strError = jObjError.getString("message")

                            if (strError == "Unauthorized") {
                                PublicFunction().tokenError(requireActivity())
                            }
                        } catch (e: Exception) {
                            e.message?.let {
                                Log.d(AbstractFragment.TAG, it)
                                strError = it
                            }

                        }
                    }
                    Log.d(TAG, "checkAppointment: $strError")

                }
            })

    }

    private fun getCheckNum(vid: String): Observable<RetrofitData.Checknum>? {
        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_CheckNum)
        val userID = AppSettings.USER_ID
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url + apiname + userID + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        return retrofit.create(API::class.java)
            .getChecknum(vid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }

    fun filter(mainstr: String, constraint: String): Boolean {
        mainstr.replace("นางสาว", "").replace("นาง", "")
            .replace("นาย", "").replace("เด็กหญิง", "")
            .replace("เด็กชาย", "")
        if (mainstr.toLowerCase(Locale.ROOT).contains(constraint.toLowerCase(Locale.ROOT), true)) {
            return true
        }
        return false
    }

    fun requestVisitorId() {
        Observable
            .just(ConnectManager().token(requireActivity(), AppSettings.REFRESH_TOKEN))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                getVid()
            }
            .subscribe()
    }

    private fun getVid() {

        if (requireContext() != null) {
            val url: String =
                requireContext()!!.getString(R.string.URL) + requireContext()!!.getString(R.string.PORT)
            val apiBooking = requireContext()!!.getString(R.string.API_BookingVID)

            val body = RetrofitData.BookingVisitorID.Body()
            body.userId = AppSettings.USER_ID
            val client: OkHttpClient =
                OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

            retrofit.create(API::class.java)
                .postBooking(body, apiBooking)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<RetrofitData.BookingVisitorID> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(t: RetrofitData.BookingVisitorID) {
                        viewModel.setVisitorId(t.message?.visitorNumber.toString())
                    }

                    override fun onError(e: Throwable) {
                        var strError = ""
                        Log.d(TAG, e.toString())
                        strError = e.toString()
                        if (e is HttpException) {
                            try {
                                val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                                Log.d(TAG, jObjError.getString("message"))
                                strError = jObjError.getString("message")

                                if (strError == "Invalid input") {
                                    strError =
                                        requireContext()!!.getString(R.string.error_emptytext)
                                }
                                if (strError == "Unauthorized") {
                                    PublicFunction().tokenError(requireActivity())
                                }
                            } catch (e: Exception) {
                                e.message?.let {
                                    Log.d(TAG, it)
                                    strError = it
                                }

                            }
                        }

                        PublicFunction().message(
                            requireActivity(),
                            strError
                        )
                    }
                })
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        cancelCheckCard()

        if (mSMPayKernel != null) {
            mSMPayKernel!!.destroyPaySDK()
        }
    }
}