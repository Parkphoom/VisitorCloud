package com.wacinfo.visitorcloud.ui.homescreen

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.model.Cell
import com.wacinfo.visitorcloud.model.ColumnHeader
import com.wacinfo.visitorcloud.model.RowHeader
import com.wacinfo.visitorcloud.utils.*
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AboutViewModel(application: Application) : AndroidViewModel(application) {


    private val typename = MutableLiveData<String>()
    val Type: LiveData<String> = typename

    fun setTypename(str: String) {
        typename.value = str
    }
    private val typenameList = MutableLiveData<List<String>>()
    val TypeList: LiveData<List<String>> = typenameList

    fun setTypenameList(str: List<String>) {
        typenameList.value = str
    }

    private val groupname = MutableLiveData<String>()
    val Group: LiveData<String> = groupname
    fun setGroupname(str: String) {
        groupname.value = str
    }

    private val pagging = MutableLiveData<Int>().apply {
        value = 1
    }
    val Pagging: LiveData<Int> = pagging
    fun setPagging(page: Int) {
        pagging.value = page
    }


    private val simpleRowHeaderList: List<RowHeader>
        get() {
            val list: MutableList<RowHeader> = ArrayList()
            for (i in 0 until ROW_SIZE) {
                val header = RowHeader(i.toString(), TypeList.value?.get(i).toString())
                list.add(header)
            }

            return list
        }

    private val randomColumnHeaderList: List<ColumnHeader>
        get() {
            val list: MutableList<ColumnHeader> = ArrayList()
            for (i in COLUMN_TYPE.indices) {
                val header = ColumnHeader(i.toString(), COLUMN_TYPE[i])
                list.add(header)
            }
            return list
        }

    private val cellListForSortingTest: List<List<Cell>>
        private get() {
            val list: MutableList<List<Cell>> = ArrayList()
            for (i in 0 until ROW_SIZE) {
                val cellList: MutableList<Cell> = ArrayList()
                for (j in COLUMN_TYPE.indices) {
                    var text: Any? = "cell $j $i"
                    val random = Random().nextInt()
                    if (j == 0) {
                        text = i
                    } else if (j == 1) {
                        text = random
                    } else if (j == IMG_CARD_COLUMN_INDEX) {
//                        text = if (random % 2 == 0) HAPPY else SAD
                        text = "https://i.imgur.com/tGbaZCY.jpg"
                    } else if (j == IMG_CAM_COLUMN_INDEX) {
                        text =
                            "http://10.0.0.205:4004/images/17122020.8063e68d-4b22-4c1b-957e-9c7235cb8b17?width=400&height=400&watermark=true&watermarkText=TEST"
//                        text = if (random % 2 == 0) BOY else GIRL
                    }

                    // Create dummy id.
                    val id = "$j-$i"
                    var cell: Cell
                    cell = if (j == ROW_SIZE) {
                        Cell(id, text)
                    } else if (j == 4) {
                        // NOTE female and male keywords for filter will have conflict since "f emale"
                        // contains "male"
                        Cell(id, text)
                    } else {
                        Cell(id, text)
                    }
                    cellList.add(cell)
                }
                list.add(cellList)
            }

            return list
        }

    var logList: MutableLiveData<MutableList<List<Cell>>>? = null

    //we will call this method to get the data
    fun getLogs(): LiveData<MutableList<List<Cell>>>? {
        //if the list is null
        if (logList == null) {
            logList = MutableLiveData<MutableList<List<Cell>>>()
            //we will load it asynchronously from server in this method
            Observable
                .just(ConnectManager().token(context!!, AppSettings.REFRESH_TOKEN))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    loadLog()
                }
                .subscribe()
        } else {
            Observable
                .just(ConnectManager().token(context!!, AppSettings.REFRESH_TOKEN))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    loadLog()
                }
                .subscribe()
        }
        //finally we will return the list
        return logList
    }

    fun resetLog() {
        logList?.value?.clear()
    }

    @SuppressLint("SimpleDateFormat", "CheckResult")
    fun loadLog() {

        val url: String = context!!.getString(R.string.URL) + context!!.getString(R.string.PORT)
        val apigetlog = context!!.getString(R.string.API_Getlog)
        val apigetinfo = context!!.getString(R.string.API_Getinfo)
        val userID = AppSettings.USER_ID
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(context!!)).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        var observable: Observable<RetrofitData.VisitorDetail>? = null
        if (groupname.value.toString() == "เข้า") {
            val observable1 = retrofit.create(API::class.java)
                .getLog(
                    apigetlog,
                    userID,
                    false,
                    currentDateandTime,
                    1,
                    100,
                    -1
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
            val observable2 = retrofit.create(API::class.java)
                .getLog(
                    apigetinfo,
                    userID,
                    false,
                    currentDateandTime,
                    1,
                    100,
                    -1
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
            observable = Observable.merge(observable1, observable2)
        }
        if (groupname.value.toString() == "ออก") {
            observable = retrofit.create(API::class.java)
                .getLog(
                    apigetlog,
                    userID,
                    false,
                    currentDateandTime,
                    1,
                    100,
                    -1
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
        if (groupname.value.toString() == "ตกค้าง") {
            observable = retrofit.create(API::class.java)
                .getLogWithBefore(
                    apigetinfo,
                    userID,
                    true,
                    currentDateandTime,
                    1,
                    100,
                    -1
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        val typeList: MutableList<String> = ArrayList()
        val vidList: MutableList<String> = ArrayList()
        val timeinList: MutableList<String> = ArrayList()
        val timeoutList: MutableList<String> = ArrayList()
        val imgCardList: MutableList<String> = ArrayList()
        val imgCamList: MutableList<String> = ArrayList()


        ROW_SIZE = 0
        result = ArrayList()
        observable?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : Observer<RetrofitData.VisitorDetail> {
                override fun onComplete() {
                    setTypenameList(typeList)
                    logList?.value = getLogList(vidList, timeinList, timeoutList, imgCardList, imgCamList)
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.VisitorDetail) {
                    val count = t.message?.result?.toMutableList()
                        ?.size
//                        ?.count { it.visitorType == typename.value.toString() }!!
                    if (count != 0) {
                        ROW_SIZE += t.message?.result?.toMutableList()
                            ?.count { it.visitorType == typename.value.toString() }!!
                        if (typename.value.toString() == "รวม") {
                            ROW_SIZE += t.message?.result?.toMutableList()
                                ?.count { it.visitorType!!.isNotEmpty() }!!
                        }
                        Log.d(TAG, "onNext: $ROW_SIZE")

                        for (i in 0 until count!!) {
                            if (t.message?.result!![i].visitorType == typename.value.toString()) {
                                result as ArrayList<RetrofitData.VisitorDetail> += t.message?.result!![i]
                                t.message?.result!![i].visitorType?.let {
                                    typeList.add(it)
                                }
                                t.message?.result!![i].visitorNumber?.let {
                                    vidList.add(it)
                                }
                                t.message?.result!![i].recordTimeIn?.let {
                                    if (it == "") {
                                        timeinList.add(
                                            "-"
                                        )
                                    } else {
                                        timeinList.add(
                                            PublicFunction().convertToNewFormat(it)
                                                .toString()
                                        )
                                    }

                                }
                                t.message?.result!![i].recordTimeOut?.let {
                                    if (it == "") {
                                        timeoutList.add(
                                            "-"
                                        )
                                    } else {
                                        timeoutList.add(
                                            PublicFunction().convertToNewFormat(it)
                                                .toString()
                                        )
                                    }
                                }

                                if (t.message?.result!![i].image1?.isNotEmpty() == true) {
                                    t.message?.result!![i].image1?.get(0)?.let {
                                        imgCardList.add(
                                            it
                                        )
                                    }
                                } else {
                                    imgCardList.add(
                                        ""
                                    )
                                }
                                if (t.message?.result!![i].image2?.isNotEmpty() == true) {
                                    t.message?.result!![i].image2?.get(0)?.let {
                                        imgCamList.add(
                                            it
                                        )
                                    }
                                } else {
                                    imgCamList.add(
                                        ""
                                    )
                                }


                            } else if (typename.value.toString() == "รวม") {
                                result as ArrayList<RetrofitData.VisitorDetail> += t.message?.result!![i]

                                t.message?.result!![i].visitorType?.let {
                                    typeList.add(it)
                                }
                                t.message?.result!![i].visitorNumber?.let {
                                    vidList.add(it)
                                }
                                t.message?.result!![i].recordTimeIn?.let {
                                    if (it == "") {
                                        timeinList.add(
                                            "-"
                                        )
                                    } else {
                                        timeinList.add(
                                            PublicFunction().convertToNewFormat(it)
                                                .toString()
                                        )
                                    }

                                }
                                t.message?.result!![i].recordTimeOut?.let {
                                    if (it == "") {
                                        timeoutList.add(
                                            "-"
                                        )
                                    } else {
                                        timeoutList.add(
                                            PublicFunction().convertToNewFormat(it)
                                                .toString()
                                        )
                                    }
                                }

                                if (t.message?.result!![i].image1?.isNotEmpty() == true) {
                                    t.message?.result!![i].image1?.get(0)?.let {
                                        imgCardList.add(
                                            it
                                        )
                                    }
                                } else {
                                    imgCardList.add(
                                        ""
                                    )
                                }
                                if (t.message?.result!![i].image2?.isNotEmpty() == true) {
                                    t.message?.result!![i].image2?.get(0)?.let {
                                        imgCamList.add(
                                            it
                                        )
                                    }
                                } else {
                                    imgCamList.add(
                                        ""
                                    )
                                }


                            }

                        }
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
                                strError = context!!.getString(R.string.error_emptytext)
                            }
                            if (strError == "Unauthorized") {
                                PublicFunction().tokenError(context!!)
                            }


                        } catch (e: Exception) {
                            e.message?.let {
                                Log.d(TAG, it)
                                strError = it
                            }

                        }
                    }

                    PublicFunction().message(
                        context as Activity,
                        strError
                    )
                }
            })
    }

    private fun getLogList(
        vidList: MutableList<String>,
        timeinlist: MutableList<String>,
        timeoutlist: MutableList<String>,
        imgCardList: MutableList<String>,
        imgCamList: MutableList<String>
    ): MutableList<List<Cell>> {

        val list: MutableList<List<Cell>> = ArrayList()
        for (i in 0 until ROW_SIZE) {
            val cellList: MutableList<Cell> = ArrayList()
            for (j in COLUMN_TYPE.indices) {
                var text: Any? = "cell $j $i"
                if (j == 0) {
                    text = vidList[i]
                } else if (j == 1) {
                    text = timeinlist[i]
                } else if (j == 2) {
                    text = timeoutlist[i]
                } else if (j == IMG_CARD_COLUMN_INDEX) {
                    text = imgCardList[i]
                } else if (j == IMG_CAM_COLUMN_INDEX) {
                    text = imgCamList[i]
                }

                // Create dummy id.

                var cell: Cell
                cell = when (j) {
                    ROW_SIZE -> {
                        val id = "$j-$i"
                        Cell(id, text)
                    }
                    4 -> {
                        // NOTE female and male keywords for filter will have conflict since "female"
                        // contains "male"
                        val id = "$j-$i"
                        Cell(id, text)
                    }
                    else -> {
                        val id = "$j-$i"
                        Cell(id, text)
                    }
                }
                cellList.add(cell)
            }
            list.add(cellList)
        }
        return list
    }

    val cellList: List<List<Cell>>
        get() = cellListForSortingTest
    val rowHeaderList: List<RowHeader>
        get() = simpleRowHeaderList
    val columnHeaderList: List<ColumnHeader>
        get() = randomColumnHeaderList
    var result: MutableList<RetrofitData.VisitorDetail>? = null

    private val COLUMN_TYPE = arrayOf(
        "เลข Visitor",
        "เข้า",
        "ออก",
        "ภาพบัตร",
        "ภาพถ่าย",
    )
    var context: Activity? = null
    private val TAG = "AboutViewModelLog"
    val sdf = SimpleDateFormat("yyyyMMdd")
    val currentDateandTime: String = sdf.format(Date())

    companion object {

        // Columns indexes
        const val IMG_CARD_COLUMN_INDEX = 3
        const val IMG_CAM_COLUMN_INDEX = 4

        // Constant values for icons
        const val SAD = 1
        const val HAPPY = 2
        const val BOY = 1
        const val GIRL = 2

        // Constant size for dummy data sets

        private var ROW_SIZE = 0

    }
}