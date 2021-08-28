package com.wacinfo.visitorcloud.ui.checkout

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcelable
import android.os.RemoteException
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import belka.us.androidtoggleswitch.widgets.ToggleSwitch
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer
import com.sunmi.peripheral.printer.InnerResultCallbcak
import com.wacinfo.visitorcloud.MainActivity
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.ActivityCheckoutDetailBinding
import com.wacinfo.visitorcloud.utils.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class CheckoutDetailActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG: String = "CheckoutDetailLog"
    private var prefs: SharedPreferences? = null
    private var objDetail: RetrofitData.Checknum.Detail? = null
    private val binding: ActivityCheckoutDetailBinding by lazy {
        ActivityCheckoutDetailBinding.inflate(
            layoutInflater
        )
    }

    var img1_url = ""
    var img2_url = ""
    var paid_type = "A"
    private var Normalprice = 0
    private var Totalprice = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val bundle = intent.extras
        objDetail = bundle!!.getParcelable<Parcelable>("Detail") as RetrofitData.Checknum.Detail?

        prefs = getSharedPreferences(
            getString(R.string.SharePreferencesSetting),
            Context.MODE_PRIVATE
        )

        initView()
        setDetail()
    }


    override fun onClick(v: View?) {
        if (v == binding.acceptBtn) {
            val checkoutData = RetrofitData.VisitorDetail()
            checkoutData.userId = AppSettings.USER_ID
            checkoutData.visitorId = objDetail?.visitorId
            checkoutData.recordStatus = "out"
            checkoutData.terminalOut = getSharedPreferences(
                getString(R.string.SharePreferencesSetting),
                0
            ).getString(getString(R.string.Pref_Terminal), "ไม่ระบุ")

            Observable
                .just(ConnectManager().token(this, AppSettings.REFRESH_TOKEN))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    checkOut(checkoutData)
                }
                .subscribe()

        }
        if (v == binding.rejectBtn) {
            startActivity(
                Intent(
                    this@CheckoutDetailActivity, MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()
        }
        if (v == binding.imgCard) {
            binding.imgCard.let {
                StfalconImageViewer.Builder(
                    this,
                    ArrayList(listOf(img1_url))
                ) { imageView, image ->
                    val picasso = Picasso.Builder(applicationContext)
                        .downloader(OkHttp3Downloader(ConnectManager().authorizationHeader()))
                        .build()
                    picasso
                        .load(image)
                        .transform(WatermarkTransformation(this, getString(R.string.app_name)))
                        .into(imageView)
                }
                    .show()
            }
        }
        if (v == binding.imgCam) {
            binding.imgCam.let {
                StfalconImageViewer.Builder(
                    this,
                    ArrayList(listOf(img2_url))
                ) { imageView, image ->
                    val picasso = Picasso.Builder(applicationContext)
                        .downloader(OkHttp3Downloader(ConnectManager().authorizationHeader()))
                        .build()
                    picasso
                        .load(image)
                        .transform(WatermarkTransformation(this, getString(R.string.app_name)))
                        .into(imageView)
                }
                    .show()
            }
        }
    }

    private fun initView() {

        binding.acceptBtn.setOnClickListener(this)
        binding.rejectBtn.setOnClickListener(this)

        if (!objDetail?.image1.isNullOrEmpty()) {
            val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
            val apiname = resources.getString(R.string.API_GetImages)
            img1_url = "${url}$apiname${objDetail?.image1?.get(0)}"

            val picasso = Picasso.Builder(applicationContext)
                .downloader(OkHttp3Downloader(ConnectManager().authorizationHeader()))
                .build()
            picasso
                .load(img1_url)
                .transform(WatermarkTransformation(this, getString(R.string.app_name)))
                .into(binding.imgCard)
            binding.imgCard.setOnClickListener(this)
        }
        if (!objDetail?.image2.isNullOrEmpty()) {
            val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
            val apiname = resources.getString(R.string.API_GetImages)
            img2_url = "${url}$apiname${objDetail?.image2?.get(0)}"

            val picasso = Picasso.Builder(applicationContext)
                .downloader(OkHttp3Downloader(ConnectManager().authorizationHeader()))
                .build()
            picasso
                .load(img2_url)
                .transform(WatermarkTransformation(this, getString(R.string.app_name)))
                .into(binding.imgCam)
            binding.imgCam.setOnClickListener(this)
        }

        binding.printSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.printSwitch.text = getString(R.string.print_slip)
            } else {
                binding.printSwitch.text = getString(R.string.do_not_print_slip)
            }

        }
    }

    private fun setDetail() {
        binding.typeEdt.setText(objDetail?.visitorType)
        binding.nameEdt.setText(objDetail?.name)
        binding.cidEdt.setText(objDetail?.citizenId)
        binding.vidEdt.setText(objDetail?.visitorNumber)
        binding.placeEdt.setText(objDetail?.contactPlace)
        binding.vehicleEdt.setText(objDetail?.vehicleType)
        binding.licenseplateEdt.setText(objDetail?.licensePlate)
        binding.terminalEdt.setText(objDetail?.terminalIn)
        binding.timeinEdt.setText(PublicFunction().convertToNewFormat(objDetail?.recordTimeIn.toString()))

        getEStamp()
    }

    private fun checkOut(checkoutData: RetrofitData.VisitorDetail) {
        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_VisitorUp)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(this)).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .postCheckOut(checkoutData, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.VisitorDetail> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.VisitorDetail) {
                    val result = t.message?.status
                    val dialog = PublicFunction().retrofitDialog(this@CheckoutDetailActivity)
                    dialog?.show()

                    if (result == getString(R.string.Complete)) {
                        PublicFunction().message(
                            this@CheckoutDetailActivity,
                            getString(R.string.save_success)
                        )
                        if (AppSettings.Active_Mode == getString(R.string.Active_Mode_Normal)) {
                            if (binding.printSwitch.isChecked) {
                                sunmiPrintSlip(dialog!!)
                            } else {
                                dialog?.cancel()
                            }
                            startActivity(
                                Intent(
                                    this@CheckoutDetailActivity, MainActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            finish()
                        } else if (AppSettings.Active_Mode == getString(R.string.Active_Mode_Parking)) {
                            dialog?.cancel()
                            checkPayment(objDetail)
                        }


                    } else {
                        PublicFunction().message(
                            this@CheckoutDetailActivity,
                            result
                        )
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
                        this@CheckoutDetailActivity,
                        strError
                    )
                }
            })
    }

    private fun checkPayment(mainObj: RetrofitData.Checknum.Detail?) {
        val dialog = PublicFunction().retrofitDialog(this@CheckoutDetailActivity)
        dialog!!.show()
        val url: String =
            getString(R.string.URL) + getString(R.string.PORT)
        val apigetinfo = getString(R.string.API_GetCost)
        val userID = AppSettings.USER_ID

        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(this)).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        val observable: Observable<RetrofitData.Payment.GetCost> =
            retrofit.create(API::class.java)
                .getCost(apigetinfo, userID, 1, 2000)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())

        val listPayment: MutableList<RetrofitData.Payment.GetCost.Message.Result> = ArrayList()

        observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Payment.GetCost> {
                @SuppressLint("SimpleDateFormat")
                override fun onComplete() {

                    if (listPayment.isNotEmpty()) {
                        dialog.cancel()

                        val dateformat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                        Log.d(TAG, "onNext: paychecked")
                        val timeout: String = PublicFunction().getDatetimenow()

                        val date1: Date = dateformat.parse(
                            PublicFunction().convertToNewFormat(
                                objDetail?.recordTimeIn.toString()
                            )
                        )
                        val date2: Date = dateformat.parse(timeout)

                        createDialogPaid(
                            objDetail,
                            timeout,
                            difMinutes(
                                date1,
                                date2
                            ),
                            listPayment
                        )
                    } else {
                        if (binding.printSwitch.isChecked) {
                            sunmiPrintSlip(dialog!!)
                        } else {
                            dialog?.cancel()
                        }
                        startActivity(
                            Intent(
                                this@CheckoutDetailActivity, MainActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                        finish()
                    }

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Payment.GetCost) {
                    t.message?.result?.map {
                        if (it.visitorType == mainObj?.visitorType && it.checkoutStatus == "true") {
                            listPayment.add(it)
                        }

                    }

                }

                override fun onError(e: Throwable) {
                    var strError = ""
                    strError = e.toString()
                    dialog.cancel()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            strError = jObjError.getString("message")

                            if (strError == "Invalid input") {
                                strError = getString(R.string.error_emptytext)
                            }
                            if (strError == "Unauthorized") {
                                PublicFunction().tokenError(this@CheckoutDetailActivity)
                            }
                        } catch (e: Exception) {
                            e.message?.let {
                                strError = it
                            }

                        }
                    }


                }
            })


    }

    private var labels: ArrayList<String>? = null

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun createDialogPaid(
        Obj: RetrofitData.Checknum.Detail?,
        time_out: String,
        parkingtime: Long,
        listPayment: MutableList<RetrofitData.Payment.GetCost.Message.Result>
    ) {
        labels = java.util.ArrayList<String>()
        labels!!.add("A")
        labels!!.add("B")
        labels!!.add("C")
        labels!!.add("D")
        labels!!.add("E")
        val dialog = Dialog(this, R.style.UpDownDialogAnimation)
        // Setting dialogview
        val window = dialog.window
        window!!.setGravity(Gravity.CENTER)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setTitle(null)
        dialog.setContentView(R.layout.dialog_paid)
        dialog.setCancelable(false)
        val toggleSwitchClass: ToggleSwitch = dialog.findViewById(R.id.multiple_toggle_switch)
        toggleSwitchClass.setLabels(labels)
        val vid_tv = dialog.findViewById<TextView>(R.id.vid_tv)
        val carId_tv = dialog.findViewById<TextView>(R.id.carId_tv)
        val statusin_tv = dialog.findViewById<TextView>(R.id.statusin_tv)
        val statusout_tv = dialog.findViewById<TextView>(R.id.statusout_tv)
        val visitortype_tv = dialog.findViewById<TextView>(R.id.visitortype_tv)
        val parkingtime_tv = dialog.findViewById<TextView>(R.id.parkingtime_tv)
        val parkingfree_tv = dialog.findViewById<TextView>(R.id.parkingfree_tv)
        val Unpaid_btn = dialog.findViewById<Button>(R.id.Unpaid_btn)
        val Paid_btn = dialog.findViewById<Button>(R.id.Paid_btn)
        val editprice_text_input: TextInputEditText = dialog.findViewById(R.id.editprice_text_input)
        val print_Switch: SwitchMaterial = dialog.findViewById(R.id.printSwitch)
        print_Switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                print_Switch.text = getString(R.string.print_slip)
            } else {
                print_Switch.text = getString(R.string.do_not_print_slip)
            }

        }

        try {
            setUpToggle(
                toggleSwitchClass,
                Obj?.visitorType.toString(),
                editprice_text_input,
                parkingtime,
                parkingfree_tv,
                Unpaid_btn,
                Paid_btn,
                listPayment
            )
            vid_tv.text = Obj?.visitorNumber
            carId_tv.text = Obj?.licensePlate
            statusin_tv.text = PublicFunction().convertToNewFormat(Obj?.recordTimeIn.toString())
            statusout_tv.text = time_out
            visitortype_tv.text = Obj?.visitorType
            Log.d(
                TAG, "printDifference: " + parkingtime
            )
            val hours = (parkingtime / 60).toInt() //since both are ints, you get an int
            val minutes = (parkingtime % 60).toInt()
            val strparkingtime = String.format("%d:%02d", hours, minutes) + " ชั่วโมง"
            parkingtime_tv.text = strparkingtime

            Log.d(
                TAG, "printDifference: " + strparkingtime
            )

            Unpaid_btn.setOnClickListener { view: View? ->
                var inputcost = 0
                if (editprice_text_input.text!!.isNotEmpty()) {
                    inputcost = editprice_text_input.text.toString().toInt()
                }

                val costData = RetrofitData.Payment.PostCost()
                costData.userId = AppSettings.USER_ID
                costData.visitorId = objDetail?.visitorId
                costData.costType = paid_type
                costData.totalMinute = parkingtime.toString()
                costData.extraCost =
                    ((inputcost - Normalprice).toString())
                costData.totalExpenses = Normalprice.toString()
                costData.paymentStatus = AppSettings.PaymentStatus_NO

                print_Switch.isChecked = false
                postCost(costData, print_Switch)
            }
            Paid_btn.setOnClickListener { view: View? ->

                var inputcost = 0
                if (editprice_text_input.text!!.isNotEmpty()) {
                    inputcost = editprice_text_input.text.toString().toInt()
                }

                val costData = RetrofitData.Payment.PostCost()
                costData.userId = AppSettings.USER_ID
                costData.visitorId = objDetail?.visitorId
                costData.costType = paid_type
                costData.totalMinute = parkingtime.toString()
                costData.extraCost =
                    ((inputcost - Normalprice).toString())
                costData.totalExpenses = Normalprice.toString()
                if (costData.extraCost!!.toInt() == 0) {
                    costData.paymentStatus = AppSettings.PaymentStatus_YES
                } else {
                    costData.paymentStatus = AppSettings.PaymentStatus_EDIT
                }
                Totalprice = inputcost
                postCost(costData, print_Switch)
            }

        } catch (e: JSONException) {
            Log.d(TAG, e.toString())
            e.printStackTrace()
        }
        dialog.show()
    }


    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun setUpToggle(
        toggleSwitch: ToggleSwitch,
        typename: String,
        textInputEditText: TextInputEditText,
        parkingtime: Long,
        parkingfree_tv: TextView,
        unpaid_btn: Button,
        paid_btn: Button,
        listPayment: MutableList<RetrofitData.Payment.GetCost.Message.Result>
    ) {
        toggleSwitch.setOnToggleSwitchChangeListener { position: Int, isChecked: Boolean ->
            paid_type = labels?.get(position).toString()

            for (x in 0 until listPayment.size) {
                if (listPayment[x].visitorType == typename) {

                    if (listPayment[x].checkoutStatus == "true" && listPayment[x].costType == paid_type) {
                        Log.d(TAG, "setUpToggle: ${listPayment[x].costRate}")

                        unpaid_btn.isEnabled = true
                        paid_btn.isEnabled = true
                        for (i in listPayment[x].costRate?.indices!!) {
                            try {
                                val itemshour = listPayment[x].costTime
                                val itemsrate = listPayment[x].costRate
                                //                        strFree = String.format("%d:%02d", hours, minutes) + " ชั่วโมง";
                                if (itemsrate != null) {
                                    for (j in itemsrate.indices) {
                                        if (itemsrate[j] != 0) {
                                            val pos = j - 1
                                            if (pos <= 0) {
                                                if (itemsrate[0] != 0) {
                                                    parkingfree_tv.text = 0.toString() + " ชั่วโมง"
                                                } else {
                                                    val firsthour = itemshour?.get(0)
                                                    val hours =
                                                        firsthour!!.toInt() / 60 //since both are ints, you get an int
                                                    val minutes = firsthour.toInt() % 60
                                                    parkingfree_tv.text = String.format(
                                                        "%d:%02d",
                                                        hours,
                                                        minutes
                                                    ) + " ชั่วโมง"
                                                }
                                            } else {
                                                val hours =
                                                    itemshour!![pos] / 60 //since both are ints, you get an int
                                                val minutes = itemshour[pos] % 60
                                                parkingfree_tv.text = String.format(
                                                    "%d:%02d",
                                                    hours,
                                                    minutes
                                                ) + " ชั่วโมง"
                                            }
                                            break
                                        }
                                    }

                                }
                                if (itemshour != null) {
                                    var isfine = true
                                    for (j in itemshour.indices) {
                                        if (j == 0) {
                                            val firsthour = itemshour[j]

                                            if (parkingtime <= firsthour) {
                                                if (itemsrate != null) {
                                                    Normalprice = itemsrate[j]
                                                    textInputEditText.setText(Normalprice.toString())
                                                    if (textInputEditText.text.toString() == "0") {
                                                        textInputEditText.setText("")
                                                    }
                                                    isfine = false
                                                    break
                                                }
                                            }
                                        } else
                                            if (j <= 72) {
                                                if (parkingtime < itemshour[j]) {

                                                    if (itemsrate != null) {
                                                        Normalprice = itemsrate.get(j).toInt()
                                                        textInputEditText.setText(Normalprice.toString())
                                                        if (textInputEditText.text.toString() == "0") {
                                                            textInputEditText.setText("")
                                                        }
                                                        isfine = false
                                                        break
                                                    }
                                                }
                                            }
                                    }
                                    if (isfine) {
                                        Normalprice = listPayment[x].fine!!
                                        textInputEditText.setText(Normalprice.toString())
                                        if (textInputEditText.text.toString() == "0") {
                                            textInputEditText.setText("")
                                        }
                                        break
                                    }
                                }
                            } catch (e: JSONException) {
                                Log.d(TAG, "toggleSwitch: $e")
                                e.printStackTrace()
                                break
                            }

                        }
                        break
                    } else {
                        unpaid_btn.isEnabled = false
                        paid_btn.isEnabled = false
                        parkingfree_tv.text = "0 ชั่วโมง"
                        textInputEditText.setText("")
                    }
                }
            }


        }
        toggleSwitch.checkedTogglePosition = 0
    }

    fun difMinutes(startDate: Date, endDate: Date): Long {
        //milliseconds
        val different = endDate.time - startDate.time
        println("startDate : $startDate")
        println("endDate : $endDate")
        println("different : $different")
        val seconds = different / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        println("minutes : $minutes")

        return minutes.absoluteValue
    }


    private fun postCost(costData: RetrofitData.Payment.PostCost, print_Switch: SwitchMaterial) {
        val dialog = PublicFunction().retrofitDialog(this@CheckoutDetailActivity)
        dialog?.show()

        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_PostCost)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(this)).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .postCost(costData, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Payment.PostCost.Respones> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Payment.PostCost.Respones) {
                    val result = t.message
                    Log.d(TAG, "postCost: $result")
                    if (result == "update complete") {
                        if (print_Switch.isChecked) {
                            sunmiPrintSlip(dialog!!)
                        } else {
                            dialog?.cancel()
                        }

                        startActivity(
                            Intent(
                                this@CheckoutDetailActivity, MainActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                        finish()
                    } else {
                        dialog!!.cancel()
                    }

                }

                override fun onError(e: Throwable) {
                    var strError = ""
                    Log.d(TAG, e.toString())
                    strError = e.toString()
                    dialog!!.cancel()
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
                        this@CheckoutDetailActivity,
                        strError
                    )
                }
            })
    }

    private fun sunmiPrintSlip(pDialog: SweetAlertDialog) {
        Log.d(TAG, "sunmiPrintSlip: ")
        try {

            PrintUtils.sunmiPrinterService!!.enterPrinterBuffer(true)
            val logo: Bitmap? = if (AppSettings.USER_LOGO != null) {
                AppSettings.USER_LOGO
            } else {
                BitmapFactory.decodeResource(resources, R.drawable.waclogo)
            }
            PrintUtils.sunmiPrinterService!!.setAlignment(1, null)
            PrintUtils.sunmiPrinterService!!.printBitmapCustom(
                PublicFunction().getResizedBitmap(logo!!, 192, 144),
                1,
                innerResultCallbcak
            )
            PrintUtils.sunmiPrinterService!!.lineWrap(1, innerResultCallbcak)

            PrintUtils.sunmiPrinterService!!.setAlignment(1, null)
            PrintUtils.sunmiPrinterService!!.printTextWithFont(
                setPrintHeader(),
                "",
                25f,
                innerResultCallbcak
            )
            PrintUtils.sunmiPrinterService!!.setAlignment(0, null)
            PrintUtils.sunmiPrinterService!!.printTextWithFont(
                setPrintContent() + "\n",
                "",
                25f,
                innerResultCallbcak
            )

            PrintUtils.sunmiPrinterService!!.lineWrap(1, innerResultCallbcak)
            PrintUtils.sunmiPrinterService!!.setAlignment(1, null)
            val border = "(เศษของ 1 ชม.คิดเป็น 1 ชม.)\n" +
                    "*** ขอบคุณที่ใช้บริการ ***\n" +
                    "วันที่พิมพ์ ${PublicFunction().getDatetimenow()}"

            PrintUtils.sunmiPrinterService!!.printTextWithFont(
                border + "\n", "", 25f, innerResultCallbcak
            )

            PrintUtils.sunmiPrinterService!!.exitPrinterBufferWithCallback(
                true,
                innerResultCallbcak
            )
            pDialog.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, e.toString())
            if (e is NullPointerException) {
                pDialog.cancel()
            }
//            Timer("Finished", false).schedule(2000) {
//                startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
//                finish()
//            }
        }
    }

    private fun setPrintHeader(): String {
//        val header1 = prefs?.getString(getString(R.string.settinhprintheadertext), "").toString()
        val header1 = ""
        var printheader = """
            
            $header1
            *************************
            """.trimIndent()
        printheader += if (Totalprice == -1) {
            """
                
                Visitor
                *************************
                """.trimIndent()
        } else {
            """
                
                ใบเสร็จรับเงิน
                *************************
                """.trimIndent()
        }


        return printheader
    }

    private fun setPrintContent(): String {
        var printContent = "\nหมายเลข VISITOR:${objDetail?.visitorNumber}"
        printContent += "\nทะเบียนรถ:${objDetail?.licensePlate}"
        printContent += "\nเวลาเข้า:${PublicFunction().convertToNewFormat(objDetail!!.recordTimeIn)}"
        printContent += "\nเวลาออก:${PublicFunction().getDatetimenow()}"
        if (Totalprice > -1) {
            printContent += "\nค่าบริการจอดรถ:$Totalprice บาท"
        }


        return printContent
    }


    private var issucces: Boolean = true
    private val innerResultCallbcak: InnerResultCallbcak = object : InnerResultCallbcak() {
        override fun onRunResult(isSuccess: Boolean) {
            Log.e("lxy", "isSuccess:$isSuccess")
            if (issucces) {
                try {
                    PrintUtils.sunmiPrinterService!!.lineWrap(3, this)
                    issucces = false
                    Log.d(TAG, "Finished")
//                    Timer("Finished", false).schedule(2000) {
//                        finishTask()
//                    }
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

    private fun getEStamp() {
        val rdialog= PublicFunction().retrofitDialog(this)
        if (!rdialog!!.isShowing) {
            runOnUiThread {
                rdialog.show()
            }
        }
        val url = getString(R.string.URL) + getString(R.string.PORT)
        val api = getString(R.string.API_E_Stamp)
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(TokenInterceptor(this)).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("$url$api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        retrofit.create(API::class.java)
            .getEStamp(AppSettings.USER_ID, objDetail?.contactPlace.toString(), objDetail?.visitorNumber.toString(), "offline", "1", "-1")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.EStamp.GetRespones> {
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(respones: RetrofitData.EStamp.GetRespones) {
                    Log.d(TAG, Gson().toJson(respones))
//                    var resp: String? = respones.message?.result?.get(0)?.estampStatus
                    var resp: String? = respones.message?.result?.get(0)?.estampStatus
                    if (resp == "มา") {
                        resp = "ยังไม่ได้แสตมป์"
                    } else if (resp == "พบ") {
                        resp = "แสตมป์แล้ว"
                    }
                    binding.estampEdt.setText(resp)
                    rdialog.cancel()

                }

                override fun onError(e: Throwable) {
                    binding.estampEdt.setText(getString(R.string.No_Data))
                    rdialog.cancel()
                    e.printStackTrace()
                    Log.d(TAG, e.toString())
                }

                override fun onComplete() {}
            })
    }
}