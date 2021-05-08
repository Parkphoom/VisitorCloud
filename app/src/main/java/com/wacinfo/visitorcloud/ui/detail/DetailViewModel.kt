package com.wacinfo.visitorcloud.ui.detail

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.qqtheme.framework.picker.OptionPicker
import com.afollestad.materialdialogs.MaterialDialog
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.ui.dialog.SlipDialog
import com.wacinfo.visitorcloud.utils.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class DetailViewModel : ViewModel() {

    var mContext: Activity? = null
    private val TAG = "DetailViewModellog"

    private val Img_Card_Uri = MutableLiveData<Uri>()
    val Card_uri: LiveData<Uri> = Img_Card_Uri
    fun setCardUri(uri: Uri) {
        Img_Card_Uri.value = uri
    }

    private val Img_Car_Uri = MutableLiveData<Uri>()
    val Car_uri: LiveData<Uri> = Img_Car_Uri
    fun setCarUri(uri: Uri) {
        Img_Car_Uri.value = uri
    }

    private val _visitorName = MutableLiveData<String>()
    val visitorName: LiveData<String> = _visitorName
    fun setVisitorName(visitorName: String) {
        _visitorName.value = visitorName
    }



    private val _visitorId = MutableLiveData<String>()
    val visitorId: LiveData<String> = _visitorId
    fun setVisitorId(visitorid: String) {
        _visitorId.value = visitorid
    }



    fun createSelectWheel(activity: Activity, list: List<String>, editText: EditText) {

        val picker = OptionPicker(
            activity,
            list
        )
        picker.setOffset(2)
        picker.selectedIndex = 0
        picker.setTextSize(14)
        picker.setOnOptionPickListener(object : OptionPicker.OnOptionPickListener() {
            override fun onOptionPicked(index: Int, item: String?) {
                editText.setText(item)
                editText.text?.length?.let { editText.setSelection(it) }
            }
        })
        picker.show()
    }



}