package com.wacinfo.visitorcloud.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wacinfo.visitorcloud.ui.dialog.SlipDialog
import com.wacinfo.visitorcloud.utils.RetrofitData

class SharedViewModel : ViewModel() {

    val slipDialog = MutableLiveData<SlipDialog>()
    fun setSlipDialog(dialog: SlipDialog) {
        slipDialog.value = dialog
    }
    val cid = MutableLiveData<String>()
    fun setCID(strcid: String) {
        cid.value = strcid
    }

    val appointmentList = MutableLiveData<ArrayList<RetrofitData.Appointment.Get.Message.Result>>()
    fun setAppointmentList(listItem: ArrayList<RetrofitData.Appointment.Get.Message.Result>) {
        appointmentList.value = listItem
    }
}