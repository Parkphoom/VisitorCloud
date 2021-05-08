package com.wacinfo.visitorcloud.ui.Settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wacinfo.visitorcloud.utils.AppSettings

class TypelistViewModel : ViewModel() {

    val VISITOR_TYPE = "visitor-type"
    val VISITOR_PLACE = "visitor-place"
    val VEHICLE_TYPE = "vehicle-type"
    val VEHICLE_LICENSE = "vehicle-license"

    //srceen
    private val pageType = MutableLiveData<String>().apply {
        value = ""
    }
    val PageType: LiveData<String> = pageType

    fun setPageType(list: String) {
        pageType.value = list
    }

    //visitorType
    private var visitorType = MutableLiveData<List<String>>().apply {
        value = AppSettings.visitorType
    }
    val visitorList: LiveData<List<String>> = visitorType
    fun setVisitorList(list: List<String>) {
        visitorType.value = list
    }

    //visitPlace
    private var visitPlace = MutableLiveData<List<String>>().apply {
        value = AppSettings.visitPlace
    }
    val visitplace: LiveData<List<String>> = visitPlace
    fun setVisitPlace(list: List<String>) {
        visitPlace.value = list
    }

    //vehicletype
    private var vehicleType = MutableLiveData<List<String>>().apply {
        value = AppSettings.vehicleType
    }
    val vehicletype: LiveData<List<String>> = vehicleType
    fun setVehicleType(list: List<String>) {
        vehicleType.value = list
    }

    //LicensePlate
    private var licensePlate = MutableLiveData<List<String>>().apply {
        value = AppSettings.licensePlate
    }
    val licenseplate: LiveData<List<String>> = licensePlate
    fun setLicensePlate(list: List<String>) {
        licensePlate.value = list
    }


}