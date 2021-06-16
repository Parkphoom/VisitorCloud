package com.wacinfo.visitorcloud.ui.log

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.utils.API
import com.wacinfo.visitorcloud.utils.AppSettings
import com.wacinfo.visitorcloud.utils.RetrofitData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class LogViewModel : ViewModel() {

    var mContext: Activity? = null
    private val sdf = SimpleDateFormat("yyyyMMdd")
    private val currentDateandTime: String = sdf.format(Date())

    private val total = MutableLiveData<String>().apply {
        value = "0"
    }
    val data_Total: LiveData<String> = total
    fun setTotal(dataTotal: String?) {
        total.value = dataTotal!!
    }

    private val pagging = MutableLiveData<Int>().apply {
        value = 1
    }
    val Pagging: LiveData<Int> = pagging
    fun setPagging(page: Int) {
        pagging.value = page
    }


    var logList: MutableLiveData<List<RetrofitData.VisitorDetail>?>? = null

    //we will call this method to get the data
    fun getLogs(pageNum: Int) {
        //if the list is null
//        if (logList == null) {
//            logList = MutableLiveData<List<RetrofitData.VisitorDetail>?>()
//            //we will load it asynchronously from server in this method
//
//            Observable
//                .just(ConnectManager().token(mContext!!, AppSettings.REFRESH_TOKEN))
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnComplete {
//                    loadLog(pageNum)
//                }
//                .subscribe()
//
//
//        } else {
//            Observable
//                .just(ConnectManager().token(mContext!!, AppSettings.REFRESH_TOKEN))
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnComplete {
//                    loadLog(pageNum)
//                }
//                .subscribe()
//        }


    }



}