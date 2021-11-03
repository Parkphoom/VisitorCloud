package com.wacinfo.visitorcloud.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.RemoteException
import android.provider.OpenableColumns
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import cn.pedant.SweetAlert.SweetAlertDialog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.gson.Gson
import com.sunmi.peripheral.printer.InnerResultCallbcak
import com.wacinfo.visitorcloud.MainActivity
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.FragmentBottomsheetBinding
import com.wacinfo.visitorcloud.model.SharedViewModel
import com.wacinfo.visitorcloud.utils.*
import com.wacinfo.visitorcloud.utils.PrintUtils.Companion.sunmiPrinterService
import id.zelory.compressor.Compressor
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.concurrent.schedule


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SlipDialog : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentBottomsheetBinding
    var onCloseListener: (() -> Unit)? = null

    private val TAG = "BottomSheetLOG"
    private val NORMAL_FRAGMENT_TAG = "Bottom Sheet Dialog Fragment"
    private val ITEM_FRAGMENT_TAG = "ITEM Dialog Fragment"

    var visitorType = ""
    var visitorNumber = ""
    var name = ""
    var cid = ""
    var timeIn = ""
    var place = ""
    var vehicle = ""
    var licenseplate = ""
    var cardURI: Uri? = null
    var camURI: Uri? = null
    private var printUtils: PrintUtils? = null
    private var prefs: SharedPreferences? = null
    private lateinit var sharedViewModel: SharedViewModel

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = View.inflate(context, R.layout.fragment_bottomsheet, container)
        binding = FragmentBottomsheetBinding.bind(root)
        sharedViewModel = activity?.run {
            ViewModelProviders.of(this)[SharedViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        printUtils = PrintUtils(requireActivity())
        printUtils!!.bindPrintService()

        prefs = context?.getSharedPreferences(
            getString(R.string.SharePreferencesSetting),
            Context.MODE_PRIVATE
        )

        binding.terminalTv.text =
            context?.getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                ?.getString(getString(R.string.Pref_Terminal), "")
        binding.vidTv.text = visitorNumber
        binding.visitorTypeTv.text = visitorType
        binding.visitorNameTv.text = name
        binding.visitorCidTv.text = cid
        binding.timeinTv.text = PublicFunction().getDatetimenow()
        binding.placeTv.text = place
        binding.vehicleTv.text = vehicle
        binding.licenseplateTv.text = licenseplate

        binding.nextBtn.setOnClickListener(this)
        binding.printBtn.setOnClickListener(this)


        if (AppSettings.USER_LOGO != null) {
            binding.logoImg.setImageBitmap(AppSettings.USER_LOGO)
        } else {
            binding.logoImg.setImageDrawable(resources.getDrawable(R.drawable.waclogo))
        }

        binding.printSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.printSwitch.text = getString(R.string.print_slip)
            } else {
                binding.printSwitch.text = getString(R.string.do_not_print_slip)
            }

        }


        return root
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView = View.inflate(context, R.layout.fragment_bottomsheet, null)

//        binding = FragmentBottomsheetBinding.bind(contentView)
//
//        dialog.setContentView(contentView)
//        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
//        dialog.window?.setLayout(
//            WindowManager.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.MATCH_PARENT
//        )
//        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//        binding.vidTv.text = visitorNumber
//        binding.visitorTypeTv.text = visitorType
//        binding.visitorNameTv.text = name
//        binding.visitorCidTv.text = cid
//        binding.timeinTv.text = timeIn
//        binding.placeTv.text = place
//        binding.vehicleTv.text = vehicle
//        binding.licenseplateTv.text = licenseplate
//
//        binding.nextBtn.setOnClickListener(this)
    }

    companion object {
        fun newInstance(): SlipDialog {

            return SlipDialog()
        }
    }

    private fun insertLOG(
        logData: RetrofitData.VisitorDetail,
        image1: MultipartBody.Part?,
        image2: MultipartBody.Part?,
        image3: MultipartBody.Part?,
        image4: MultipartBody.Part?,
        pDialog: SweetAlertDialog
    ) {
        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_VisitorUp)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .insertVisitorLOG(
                apiname,
                createBody(logData.userId.toString()),
                image1,
                image2,
                image3,
                image4,
                createBody(logData.visitorNumber.toString()),
                createBody(logData.citizenId.toString()),
                createBody(logData.name.toString()),
                createBody(logData.visitorType.toString()),
                createBody(logData.recordTimeIn.toString()),
                createBody(logData.licensePlate.toString()),
                createBody(logData.vehicleType.toString()),
                createBody(logData.recordStatus.toString()),
                createBody(logData.terminalIn.toString()),
                createBody(logData.contactPlace.toString())
            )
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : Observer<RetrofitData.VisitorDetail?> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.VisitorDetail) {
                    val result = t.message?.status
                    if (result == getString(R.string.Complete)) {
                        PublicFunction().message(
                            requireActivity(),
                            getString(R.string.save_success)
                        )

                        if (this@SlipDialog.tag != NORMAL_FRAGMENT_TAG) {
                            var tag = this@SlipDialog.tag.toString()
                            tag = tag.replace("$ITEM_FRAGMENT_TAG ", "")

                            val data = sharedViewModel.appointmentList.value?.get(tag.toInt())

                            val value = RetrofitData.Appointment.Put()
                            value.userId = AppSettings.USER_ID
                            value.meetingId = data!!.meetingId
                            value.name = data.name
                            value.meetUpLocal = data.meetUpLocal
                            value.daysToCome = data.daysToCome
                            value.licensePlate = data.licensePlate
                            value.status = AppSettings.Appointment_Status_MEETED
                            passMeeting(value)
                        }

                        if (binding.printSwitch.isChecked) {
                            sunmiPrintSlip(pDialog)
                        } else {
                            pDialog.cancel()
//                            dialog?.cancel()
                            finishTask()
                        }

                        binding.nextBtn.isEnabled = true
//                        dialog?.cancel()
//                        onCloseListener?.invoke()
                    }
                    Log.d(TAG, "onNext: ${t.message?.status}")
                }

                override fun onError(e: Throwable) {
                    pDialog.cancel()
                    binding.nextBtn.isEnabled = true
                    var strError = ""
                    Log.d(TAG, "onError: $e")
                    Log.d(TAG, "onError: ${Gson().toJson(e)}")
                    strError = e.toString()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(TAG, jObjError.getString("message"))
                            strError = jObjError.getString("message")
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

    private fun passMeeting(
        value: RetrofitData.Appointment.Put
    ) {
        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_Appointment)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .updateMeeting(
                apiname,
                value
            )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : Observer<RetrofitData.Appointment.Put.Message> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Appointment.Put.Message) {
                    Log.d(TAG, "onNext: ${t.message!!.operate}")
                    Log.d(TAG, "onNext: ${t.message!!.data}")

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


    // add another part within the multipart request
    fun createBody(value: String): RequestBody {
        return RequestBody.create("multipart/form-data".toMediaTypeOrNull(), value)
    }


    override fun onClick(v: View?) {
        if (v == binding.nextBtn) {
            binding.nextBtn.isEnabled = false

            val pDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.titleText = "Loading"
            pDialog.setCancelable(false)
            pDialog.show()

            val postdata = RetrofitData.VisitorDetail()
            postdata.userId = AppSettings.USER_ID
            postdata.visitorNumber = visitorNumber
            postdata.citizenId = cid
            postdata.name = name
            postdata.visitorType = visitorType
            postdata.recordTimeIn = ""
            postdata.licensePlate = licenseplate
            postdata.vehicleType = vehicle
            postdata.recordStatus = "in"
            postdata.terminalIn = requireActivity().getSharedPreferences(
                getString(R.string.SharePreferencesSetting),
                0
            ).getString(getString(R.string.Pref_Terminal), "ไม่ระบุ")
            postdata.contactPlace = place

            var card: MultipartBody.Part? = null
            var cam: MultipartBody.Part? = null
            if (cardURI != null) {
                val cardfile = File(getFilePathForN(cardURI, requireContext()))
                val requestcardFile: RequestBody =
                    cardfile.asRequestBody("image/png".toMediaTypeOrNull())
                card = MultipartBody.Part.createFormData("image1", cardfile.name, requestcardFile)
            }
            if (camURI != null) {
                val camfile = File(getFilePathForN(camURI, requireContext()))
                val requestcamFile: RequestBody =
                    camfile.asRequestBody("image/png".toMediaTypeOrNull())
                cam = MultipartBody.Part.createFormData("image2", camfile.name, requestcamFile)
            }

            Observable
                .just(ConnectManager().token(requireActivity(), AppSettings.REFRESH_TOKEN))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    insertLOG(postdata, card, cam, null, null, pDialog)
                }
                .subscribe()

        }
        if (v == binding.printBtn) {
//            sunmiPrintSlip()
        }
    }


    private fun getFilePathForN(uri: Uri?, context: Context): String? {
        val returnCursor = context.contentResolver.query(uri!!, null, null, null, null)
        /*
     * Get the column indexes of the data in the Cursor,
     *     * move to the first row in the Cursor, get the data,
     *     * and display it.
     * */
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = java.lang.Long.toString(returnCursor.getLong(sizeIndex))
        val file = File(context.filesDir, name)
        try {
            val inputStream = context.contentResolver.openInputStream(uri!!)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable = inputStream!!.available()

            //int bufferSize = 1024;
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e("File Size", "Size " + file.length())
            inputStream.close()
            outputStream.close()
            Log.e("File Path", "Path " + file.path)
            Log.e("File Size", "Size " + file.length())
        } catch (e: java.lang.Exception) {
            Log.e("Exception", e.message!!)
        }

        val compressedFileName = "wac_" + file.name

        val compressedImage: File = Compressor(requireContext())
            .setMaxWidth(500)
            .setMaxHeight(850)
            .setQuality(92)
            .setCompressFormat(Bitmap.CompressFormat.WEBP)
            .setDestinationDirectoryPath(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ).absolutePath
            )
            .compressToFile(file, compressedFileName)
        Log.d("picturee_compres", compressedImage.absolutePath)
        Log.d("picturee_compres", compressedImage.length().toString())
        return compressedImage.absolutePath
    }


    private fun sunmiPrintSlip(pDialog: SweetAlertDialog) {
        Log.d(TAG, "sunmiPrintSlip: ")
        try {

            sunmiPrinterService!!.enterPrinterBuffer(true)
//            val logopath: String = prefs?.getString(getString(R.string.settinglogopath), "").toString()
            binding.logoImg.invalidate()
            val bitmap = binding.logoImg.drawable.toBitmap()
            if (bitmap != null) {
//                val logo = BitmapFactory.decodeFile(logopath)
                sunmiPrinterService!!.setAlignment(1, null)
                sunmiPrinterService!!.printBitmapCustom(
                    PublicFunction().getResizedBitmap(bitmap, 192, 144),
                    1,
                    innerResultCallbcak
                )
                sunmiPrinterService!!.lineWrap(1, innerResultCallbcak)
            }

            sunmiPrinterService!!.setAlignment(1, null)
            sunmiPrinterService!!.printTextWithFont(
                setPrintHeader() + "\n",
                "",
                25f,
                innerResultCallbcak
            )
            sunmiPrinterService!!.setAlignment(0, null)
            sunmiPrinterService!!.printTextWithFont(
                setPrintContent() + "\n",
                "",
                25f,
                innerResultCallbcak
            )
            sunmiPrinterService!!.lineWrap(1, innerResultCallbcak)
            sunmiPrinterService!!.setAlignment(1, null)

            //print QR
            if (getSwitchSetting(getString(R.string.Pref_Setting_QR))) {
                sunmiPrinterService!!.printQRCode(
                    binding.vidTv.text.toString(),
                    10,
                    3,
                    innerResultCallbcak
                )
            }

            //print signature
            if (getSwitchSetting(getString(R.string.Pref_Setting_SG))) {
                sunmiPrinterService!!.printTextWithFont(
                    setPrintBottom(),
                    "",
                    25f,
                    innerResultCallbcak
                )
                sunmiPrinterService!!.lineWrap(2, innerResultCallbcak)
            }

            if (getSwitchSetting(getString(R.string.Pref_Setting_EP))) {
                val bottom: String =
                    requireContext().getSharedPreferences(
                        getString(R.string.SharePreferencesSetting),
                        0
                    )
                        .getString(getString(R.string.Pref_Setting_EP_Text), "")!!
                if (bottom != "") {
                    sunmiPrinterService!!.printTextWithFont(
                        bottom + "\n",
                        "bold",
                        28f,
                        innerResultCallbcak
                    )
                }
            }
            sunmiPrinterService!!.lineWrap(1, innerResultCallbcak)

            sunmiPrinterService!!.exitPrinterBufferWithCallback(true, innerResultCallbcak)
            pDialog.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, e.toString())
            if (e is NullPointerException) {
                pDialog.cancel()
                finishTask()
            }

        }
    }

    private fun setPrintHeader(): String {
        val header1 = prefs?.getString(getString(R.string.Pref_CompanyName), "").toString()
        var printheader = """
            
            $header1
            *************************
            """.trimIndent()
        printheader += """
            
            Visitor
            *************************
            """.trimIndent()

        return printheader
    }

    private fun setPrintContent(): String {
        var printContent = ""
//        if (getSwitchSetting(getString(R.string.status_print_terminal))) {
        printContent += "\nประตู: ${binding.terminalTv.text}"
//        }
//        if (getSwitchSetting(getString(R.string.status_print_terminalid))) {
//            printContent += "\nหมายเลขประตู : $stationID"
//        }
//        if (getSwitchSetting(getString(R.string.status_print_cardnumber))) {
        printContent += "\nหมายเลข VISITOR: ${binding.vidTv.text}"
//        }
//        if (getSwitchSetting(getString(R.string.status_print_visitortype))) {
        printContent += "\nประเภท VISITOR: ${binding.visitorTypeTv.text}"
//        }
//        if (getSwitchSetting(getString(R.string.status_print_personalid))) {
//            if (getSwitchSetting(getString(R.string.status_sensor_idcard))) {
        try {
            val sensorstr =
                "X XXXX XXXXX " + binding.visitorCidTv.text.substring(binding.visitorCidTv.text.length - 3)
            printContent += "\nเลขประจำตัว: $sensorstr"
            Log.d("readcard1", sensorstr)
        } catch (e: StringIndexOutOfBoundsException) {
            printContent += "\nเลขประจำตัว: ${binding.visitorCidTv.text}"
        }
//            } else {
//                printContent += "\nเลขประจำตัว : $visitorid"
//            }
//        }
//        if (getSwitchSetting(getString(R.string.status_print_nameth))) {
        printContent += "\nชื่อ-สกุล: ${binding.visitorNameTv.text}"
//        }
//        if (getSwitchSetting(getString(R.string.status_print_nameen))) {
//            printContent += "\nName : $visitornameEN"
//        }
//        if (getSwitchSetting(getString(R.string.status_print_time))) {
        printContent += "\nเวลาเข้า: ${binding.timeinTv.text}"
//        }
//        if (getSwitchSetting(getString(R.string.status_print_destination))) {
        printContent += "\nผู้รับการติดต่อ: ${binding.placeTv.text}"
//        }
//        if (getSwitchSetting(getString(R.string.status_print_carid))) {
        printContent += "\nเลขทะเบียนรถ: ${binding.licenseplateTv.text}"
//        }
//        if (getSwitchSetting(getString(R.string.status_print_cartype))) {
        printContent += "\nชนิดรถ: ${binding.vehicleTv.text}"
//        }
        return printContent
    }

    private fun setPrintBottom(): String {
        val printborder = """
            ___________________________
            
            
            กรุณา ประทับตรา
            
            ___________________________
            """.trimIndent()


        return printborder
    }

    private fun getSwitchSetting(pref: String): Boolean {
        return requireContext().getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
            .getBoolean(pref, true)
    }

    private var issucces: Boolean = true
    private val innerResultCallbcak: InnerResultCallbcak = object : InnerResultCallbcak() {
        override fun onRunResult(isSuccess: Boolean) {
            Log.e("lxy", "isSuccess:$isSuccess")
            if (issucces) {
                try {
                    sunmiPrinterService!!.lineWrap(3, this)
                    issucces = false
                    Log.d(TAG, "Finished")
                    Timer("Finished", false).schedule(2000) {
                        finishTask()
                    }
                } catch (e: RemoteException) {

                    e.printStackTrace()
                    Log.d(TAG, e.toString())
                }
            }
        }

        override fun onReturnString(result: String) {
            Log.e(TAG, "result:$result")
        }

        override fun onRaiseException(code: Int, msg: String) {
            Log.e(TAG, "code:$code,msg:$msg")
        }

        override fun onPrintResult(code: Int, msg: String) {
            Log.e(TAG, "code:$code,msg:$msg")
        }
    }

    private fun finishTask() {

        requireActivity().runOnUiThread {
            val body = RetrofitData.EStamp.Body( AppSettings.USER_ID, AppSettings.UID,
                "offline",place, visitorNumber, "มา")

            postEStamp(body)
//            MaterialDialog(requireContext()).show {
//                title(R.string.e_stamp)
//                input(
//                    hintRes = R.string.home_id_hint,
//                    waitForPositiveButton = false,
//                    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_CLASS_PHONE
//                )
//                { dialog, text ->
//
//
//                }
//                positiveButton(R.string.save) {
//                    val homeId = it.getInputField().text
//                    if (homeId.isNotEmpty()) {
//                        val body = RetrofitData.EStamp.Body( AppSettings.USER_ID, AppSettings.UID,
//                            "offline", homeId.toString(), visitorNumber, "มา")
//
//                        postEStamp(body)
//                    }
//
//
//
//
//                }
//                negativeButton(R.string.Skip) {
//                    dialog?.cancel()
//                    val intent = Intent(requireContext(), MainActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    startActivity(intent)
//                }
//            }
        }


    }

    private fun postEStamp(estampbody: RetrofitData.EStamp.Body) {
        val rdialog= PublicFunction().retrofitDialog(requireContext())
        if (!rdialog!!.isShowing) {
            requireActivity().runOnUiThread {
                rdialog.show()
            }
        }
        val url = getString(R.string.URL) + getString(R.string.PORT)
        val api = getString(R.string.API_E_Stamp)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        retrofit.create(API::class.java)
            .postEStamp(estampbody, api)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.EStamp.Respones> {
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(respones: RetrofitData.EStamp.Respones) {
                    PublicFunction().message(
                        requireActivity(),
                        getString(R.string.e_stamp) + " สำเร็จ"
                    )
                    rdialog.cancel()

                    dialog?.cancel()

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)

                    Log.d(TAG, Gson().toJson(respones.message))
                }

                override fun onError(e: Throwable) {
//                    PublicFunction().errorDialog(rdialog).show()
                    rdialog.cancel()
                    e.printStackTrace()
                    Log.d(TAG, e.toString())

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }

                override fun onComplete() {}
            })
    }
}