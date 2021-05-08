package com.wacinfo.visitorcloud.utils

import android.app.Application
import android.graphics.Bitmap

class AppSettings : Application() {
    companion object {
        //Mode
        var Active_Mode: String = ""

        var REFRESH_TOKEN: String = ""
        var ACCESS_TOKEN: String = ""
        var USER_ID: String = ""
        var UID: String = ""
        var USER_NAME: String = ""
        var USER_RULE: String = ""
        var USER_LOGO: Bitmap? = null

        var visitorType = emptyList<String>()
        var licensePlate = emptyList<String>()
        var contactTopic = emptyList<String>()
        var whitelist = emptyList<RetrofitData.Settings.WhiteList>()
        var visitFrom = emptyList<String>()
        var visitPerson = emptyList<String>()
        var vehicleType = emptyList<String>()
        var department = emptyList<String>()
        var visitPlace = emptyList<String>()
        var blacklist = emptyList<RetrofitData.Settings.BlackList>()

        //Cost
        var PaymentStatus_YES: String = "Y"
        var PaymentStatus_NO: String = "N"
        var PaymentStatus_EDIT: String = "C"

        //Appointment
        var Appointment_Status: String = "นัด"
        var Appointment_Status_MEETED: String = "มา"

    }

    fun resetSettings() {
        Active_Mode = ""
        REFRESH_TOKEN = ""
        ACCESS_TOKEN = ""
        USER_ID = ""
        UID = ""
        USER_NAME = ""
        USER_RULE = ""
        USER_LOGO = null

        visitorType = emptyList<String>()
        licensePlate = emptyList<String>()
        contactTopic = emptyList<String>()
        whitelist = emptyList<RetrofitData.Settings.WhiteList>()
        visitFrom = emptyList<String>()
        visitPerson = emptyList<String>()
        vehicleType = emptyList<String>()
        department = emptyList<String>()
        visitPlace = emptyList<String>()
        blacklist = emptyList<RetrofitData.Settings.BlackList>()
    }

    override fun onCreate() {
        super.onCreate()
        // initialization code here

    }
}