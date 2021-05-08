package com.wacinfo.visitorcloud.ui.homescreen

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class TitleViewModel : ViewModel() {

     var mContext :Activity? = null
    var TAG = "TitleLog_viewmodel"
    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    private val simpleRowHeaderList: List<RowHeader>
        get() {
            val list: MutableList<RowHeader> = ArrayList()
            for (i in AppSettings.visitorType.indices) {
                val header = RowHeader(i.toString(), AppSettings.visitorType[i])
                list.add(header)

            }
            return list
        }

    private val randomColumnHeaderList: List<ColumnHeader>
        get() {
            val list: MutableList<ColumnHeader> = ArrayList()
            for (i in columnGroupList.indices) {
                val header = ColumnHeader(i.toString(), columnGroupList[i])
                list.add(header)
            }
            return list
        }


    private val cellListForSortingTest: List<List<Cell>>
        private get() {
            val list: MutableList<List<Cell>> = ArrayList()
            for (i in AppSettings.visitorType.indices) {
                val cellList: MutableList<Cell> = ArrayList()
                for (j in 0 until COLUMN_SIZE) {
                    var text: Any? = "cell $j $i"
                    val random = Random().nextInt()
                    if (j == 0) {
                        text = i
                    } else if (j == 1) {
                        text = random
                    } else if (j == MOOD_COLUMN_INDEX) {
                        text = if (random % 2 == 0) HAPPY else SAD
                    } else if (j == GENDER_COLUMN_INDEX) {
                        text = if (random % 2 == 0) BOY else GIRL
                    }

                    // Create dummy id.
                    val id = "$j-$i"
                    var cell: Cell
                    cell = if (j == 3) {
                        Cell(id, text)
                    } else if (j == 4) {
                        // NOTE female and male keywords for filter will have conflict since "female"
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
                .just(ConnectManager().token(mContext!!, AppSettings.REFRESH_TOKEN))
                .delay(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    loadLog()
                }
                .subscribe()
        } else {

            Observable
                .just(ConnectManager().token(mContext!!, AppSettings.REFRESH_TOKEN))
                .delay(300, TimeUnit.MILLISECONDS)
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
        if(mContext != null){
            Log.d("ConnectManager", "loadlog")
            val url: String = mContext!!.getString(R.string.URL) + mContext!!.getString(R.string.PORT)
            val apigetlog = mContext!!.getString(R.string.API_Getlog)
            val apigetinfo = mContext!!.getString(R.string.API_Getinfo)
            val userID = AppSettings.USER_ID

            val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
                val newRequest: Request = chain.request().newBuilder()
                    .addHeader("X-Authorization", "Bearer ${AppSettings.ACCESS_TOKEN}")
                    .build()
                chain.proceed(newRequest)
            }).build()
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

            val observable1: Observable<RetrofitData.VisitorDetail> = retrofit.create(API::class.java)
                .getLog(apigetinfo, userID, false,currentDateandTime, 1, 100, -1,AppSettings.ACCESS_TOKEN)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())


            val observable2: Observable<RetrofitData.VisitorDetail> = retrofit.create(API::class.java)
                .getLog(apigetlog, userID,false, currentDateandTime, 1, 100,-1, AppSettings.ACCESS_TOKEN)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())


            val observable3: Observable<RetrofitData.VisitorDetail> = retrofit.create(API::class.java)
                .getLogWithBefore(
                    apigetinfo,
                    userID,
                    true,
                    currentDateandTime,
                    1,
                    100,
                    -1,
                    AppSettings.ACCESS_TOKEN
                )
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())


            val enterList: MutableList<Int> = ArrayList(AppSettings.visitorType.size)
            val outList: MutableList<Int> = ArrayList(AppSettings.visitorType.size)
            val remainList: MutableList<Int> = ArrayList(AppSettings.visitorType.size)
            for (i in AppSettings.visitorType) {
                enterList.add(0)
                outList.add(0)
                remainList.add(0)
            }
            var count = 1

            Observable
                .merge(observable1, observable2, observable3)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<RetrofitData.VisitorDetail> {
                    override fun onComplete() {

                        logList?.value = getLogList(enterList, outList, remainList)

                        for (i in AppSettings.visitorType.indices) {
                            Log.d("logList", i.toString())
                            Log.d(
                                "logList",
                                "${enterList[i]} : ${outList[i]} : ${remainList[i]} "
                            )
                        }

                        Log.d("logList", "/////////")
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(t: RetrofitData.VisitorDetail) {
                        for (i in AppSettings.visitorType.indices) {

                            if (count == 1) {
                                val countin = t.message?.result?.toMutableList()
                                    ?.count { it.visitorType == AppSettings.visitorType[i] }!!
                                enterList[i] += countin
                            } else if (count == 2) {
                                val countout = t.message?.result?.toMutableList()
                                    ?.count { it.visitorType == AppSettings.visitorType[i] && !it.recordTimeOut.isNullOrEmpty() }!!
                                enterList[i] += countout
                                outList[i] += countout
                            } else if (count == 3) {

                                val countremain = t.message?.result?.toMutableList()
                                    ?.count { it.visitorType == AppSettings.visitorType[i] }!!
                                remainList[i] += countremain
                            }
                        }
                        count++
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
                                    strError = mContext!!.getString(R.string.error_emptytext)
                                }
                                if (strError == "Unauthorized") {
                                    PublicFunction().tokenError(mContext!!)
                                }
                            } catch (e: Exception) {
                                e.message?.let {
                                    Log.d(TAG, it)
                                    strError = it
                                }

                            }
                        }


                    }
                })
        }



    }

    private fun getLogList(
        enterlist: MutableList<Int>,
        outlist: MutableList<Int>,
        remainlist: MutableList<Int>
    ): MutableList<List<Cell>> {

        val list: MutableList<List<Cell>> = ArrayList()

        for (i in AppSettings.visitorType.indices) {
            val cellList: MutableList<Cell> = ArrayList()
            for (j in 0 until COLUMN_SIZE) {
                var text: Any? = "cell $j $i"
                if (j == 0) {
                    text = enterlist[i]
                } else if (j == 1) {
                    text = outlist[i]
                } else if (j == 2) {
                    text = remainlist[i]
                }

                // Create dummy id.
                val id = "$j-$i"
                var cell: Cell
                cell = if (j == 3) {
                    Cell(id, text)
                } else if (j == 4) {
                    // NOTE female and male keywords for filter will have conflict since "female"
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

    val cellList: List<List<Cell>>
        get() = cellListForSortingTest
    val rowHeaderList: List<RowHeader>
        get() = simpleRowHeaderList
    val columnHeaderList: List<ColumnHeader>
        get() = randomColumnHeaderList
    val columnGroupList = arrayOf(
        "เข้า",
        "ออก",
        "ตกค้าง"
    )
    val sdf = SimpleDateFormat("yyyyMMdd")
    val currentDateandTime: String = sdf.format(Date())

    companion object {

        // Columns indexes
        const val MOOD_COLUMN_INDEX = 3
        const val GENDER_COLUMN_INDEX = 4

        // Constant values for icons
        const val SAD = 1
        const val HAPPY = 2
        const val BOY = 1
        const val GIRL = 2

        // Constant size for dummy data sets

        private const val COLUMN_SIZE = 3

    }


}