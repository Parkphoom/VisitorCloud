package com.wacinfo.visitorcloud.utils

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


class RetrofitData {


    class Activate {
        @SerializedName("status")
        var status: String? = null

        @SerializedName("message")
        var message: MessageData? = null

        class MessageData {
            @SerializedName("activeStatus")
            var activeStatus: String? = null

            @SerializedName("activeMode")
            var activeMode: String? = null
        }


        @SerializedName("userId")
        var userId: String? = null

        @SerializedName("deviceId")
        var deviceId: String? = null


    }

    class Login {
        @SerializedName("status")
        var status: String? = null

        @SerializedName("message")
        var message: ResponesMessage? = null

        class PostBody {
            @SerializedName("username")
            var username: String? = null

            @SerializedName("password")
            var password: String? = null
        }

        class ResponesMessage {
            @SerializedName("access_token")
            var access_token: String? = null

            @SerializedName("token_type")
            var token_type: String? = null

            @SerializedName("expires_in")
            var expires_in: String? = null

            @SerializedName("refresh_token")
            var refresh_token: String? = null

            @SerializedName("scope")
            var scope: String? = null
        }
    }

    class Logout{
        class Post{

            @SerializedName("refresh_token")
            var refresh_token: String? = null

        }

        class Respones{
            @SerializedName("status")
            var status: String? = null

            @SerializedName("message")
            var message: String? = null
        }

    }

    class Settings {
        @SerializedName("status")
        var status: String? = null

        @SerializedName("message")
        var message: Message? = null

        class Message {
            @SerializedName("visitorType")
            var visitorType: List<String>? = null

            @SerializedName("licensePlate")
            var licensePlate: List<String>? = null

            @SerializedName("contactTopic")
            var contactTopic: List<String>? = null

            @SerializedName("whitelist")
            var whitelist: List<WhiteList>? = null

            @SerializedName("visitFrom")
            var visitFrom: List<String>? = null

            @SerializedName("visitPerson")
            var visitPerson: List<String>? = null

            @SerializedName("vehicleType")
            var vehicleType: List<String>? = null

            @SerializedName("department")
            var department: List<String>? = null

            @SerializedName("visitPlace")
            var visitPlace: List<String>? = null

            @SerializedName("blacklist")
            var blacklist: List<BlackList>? = null
        }

        class WhiteList {
            @SerializedName("whitelistId")
            var whitelistId: String? = null

            @SerializedName("name")
            var name: String? = null

            @SerializedName("citizenId")
            var citizenId: String? = null

            @SerializedName("timeStart")
            var timeStart: String? = null

            @SerializedName("timeStop")
            var timeStop: String? = null
        }

        class BlackList {
            @SerializedName("blacklistId")
            var blacklistId: String? = null

            @SerializedName("name")
            var name: String? = null

            @SerializedName("citizenId")
            var citizenId: String? = null

            @SerializedName("timexxx")
            var timexxx: List<Time>? = null

            class Time {
                @SerializedName("blacklistTimeId")
                var blacklistTimeId: String? = null

                @SerializedName("timeStart")
                var timeStart: String? = null

                @SerializedName("timeStop")
                var timeStop: String? = null
            }
        }
    }

    class Property {
        @SerializedName("status")
        var status: String? = null

        @SerializedName("message")
        var message: String? = null

        class VisitorType {
            @SerializedName("userId")
            var userId: String? = null

            @SerializedName("visitorType")
            var visitorType: String? = null
        }

        class VisitPlace {
            @SerializedName("userId")
            var userId: String? = null

            @SerializedName("visitPlace")
            var visitPlace: String? = null
        }

        class VehicleType {
            @SerializedName("userId")
            var userId: String? = null

            @SerializedName("vehicleType")
            var vehicleType: String? = null
        }

        class LicensePlate {
            @SerializedName("userId")
            var userId: String? = null

            @SerializedName("licensePlate")
            var licensePlate: String? = null
        }


        class Edit {
            @SerializedName("userId")
            var userId: String? = null

            @SerializedName("find")
            var find: String? = null

            @SerializedName("editTo")
            var editTo: String? = null
        }

    }

    class Logo{
        @SerializedName("status")
        var status: String? = null

        @SerializedName("message")
        var message: Message? = null

        class Message{
            @SerializedName("data")
            var data: List<Data>? = null

            class Data{
                @SerializedName("logoId")
                var logoId: String? = null

                @SerializedName("name")
                var name: String? = null

                @SerializedName("imageLogo")
                var imageLogo: List<String>? = null

                @SerializedName("description")
                var description: String? = null

            }
        }
    }

    class Checknum {
        @SerializedName("status")
        var status: String? = null

        @SerializedName("message")
        var message: List<Detail>? = null

        class Detail() : Parcelable {

            @SerializedName("visitorId")
            var visitorId: String? = null

            @SerializedName("image1")
            var image1: List<String>? = null

            @SerializedName("image2")
            var image2: List<String>? = null

            @SerializedName("image3")
            var image3: List<String>? = null

            @SerializedName("image4")
            var image4: List<String>? = null

            @SerializedName("visitorNumber")
            var visitorNumber: String? = null

            @SerializedName("citizenId")
            var citizenId: String? = null

            @SerializedName("name")
            var name: String? = null

            @SerializedName("visitorType")
            var visitorType: String? = null

            @SerializedName("recordTimeIn")
            var recordTimeIn: String? = null

            @SerializedName("recordTimeOut")
            var recordTimeOut: String? = null

            @SerializedName("licensePlate")
            var licensePlate: String? = null

            @SerializedName("vehicleType")
            var vehicleType: String? = null

            @SerializedName("recordStatus")
            var recordStatus: String? = null

            @SerializedName("terminalIn")
            var terminalIn: String? = null

            @SerializedName("terminalOut")
            var terminalOut: String? = null

            @SerializedName("follower")
            var follower: String? = null

            @SerializedName("visitorFrom")
            var visitorFrom: String? = null

            @SerializedName("department")
            var department: String? = null

            @SerializedName("visitPerson")
            var visitPerson: String? = null

            @SerializedName("contactTopic")
            var contactTopic: String? = null

            @SerializedName("contactPlace")
            var contactPlace: String? = null

            @SerializedName("etc")
            var etc: String? = null

            @SerializedName("timestamp")
            var timestamp: String? = null

            constructor(parcel: Parcel) : this() {
                visitorId = parcel.readString()
                image1 = parcel.createStringArrayList()
                image2 = parcel.createStringArrayList()
                image3 = parcel.createStringArrayList()
                image4 = parcel.createStringArrayList()
                visitorNumber = parcel.readString()
                citizenId = parcel.readString()
                name = parcel.readString()
                visitorType = parcel.readString()
                recordTimeIn = parcel.readString()
                recordTimeOut = parcel.readString()
                licensePlate = parcel.readString()
                vehicleType = parcel.readString()
                recordStatus = parcel.readString()
                terminalIn = parcel.readString()
                terminalOut = parcel.readString()
                follower = parcel.readString()
                visitorFrom = parcel.readString()
                department = parcel.readString()
                visitPerson = parcel.readString()
                contactTopic = parcel.readString()
                contactPlace = parcel.readString()
                etc = parcel.readString()
                timestamp = parcel.readString()
            }

            override fun describeContents(): Int {
                TODO("Not yet implemented")
            }

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(visitorId)
                parcel.writeStringList(image1)
                parcel.writeStringList(image2)
                parcel.writeStringList(image3)
                parcel.writeStringList(image4)
                parcel.writeString(visitorNumber)
                parcel.writeString(citizenId)
                parcel.writeString(name)
                parcel.writeString(visitorType)
                parcel.writeString(recordTimeIn)
                parcel.writeString(recordTimeOut)
                parcel.writeString(licensePlate)
                parcel.writeString(vehicleType)
                parcel.writeString(recordStatus)
                parcel.writeString(terminalIn)
                parcel.writeString(terminalOut)
                parcel.writeString(follower)
                parcel.writeString(visitorFrom)
                parcel.writeString(department)
                parcel.writeString(visitPerson)
                parcel.writeString(contactTopic)
                parcel.writeString(contactPlace)
                parcel.writeString(etc)
                parcel.writeString(timestamp)
            }

            companion object CREATOR : Parcelable.Creator<Detail> {
                override fun createFromParcel(parcel: Parcel): Detail {
                    return Detail(parcel)
                }

                override fun newArray(size: Int): Array<Detail?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    class BookingVisitorID {
        @SerializedName("status")
        var status: String? = null

        @SerializedName("message")
        var message: Message? = null

        class Message {
            @SerializedName("visitorNumber")
            var visitorNumber: String? = null
        }

        class Body {
            @SerializedName("userId")
            var userId: String? = null
        }

    }

    class VisitorDetail {
        @SerializedName("status")
        var status: String? = null

        @SerializedName("message")
        var message: Detail? = null

        @SerializedName("userId")
        var userId: String? = null

        @SerializedName("image1")
        var image1: List<String>? = null

        @SerializedName("image2")
        var image2: List<String>? = null

        @SerializedName("image3")
        var image3: List<String>? = null

        @SerializedName("image4")
        var image4: List<String>? = null

        @SerializedName("visitorId")
        var visitorId: String? = null

        @SerializedName("visitorNumber")
        var visitorNumber: String? = null

        @SerializedName("citizenId")
        var citizenId: String? = null

        @SerializedName("name")
        var name: String? = null

        @SerializedName("visitorType")
        var visitorType: String? = null

        @SerializedName("recordTimeIn")
        var recordTimeIn: String? = null

        @SerializedName("recordTimeOut")
        var recordTimeOut: String? = null

        @SerializedName("licensePlate")
        var licensePlate: String? = null

        @SerializedName("vehicleType")
        var vehicleType: String? = null

        @SerializedName("recordStatus")
        var recordStatus: String? = null

        @SerializedName("terminalIn")
        var terminalIn: String? = null

        @SerializedName("terminalOut")
        var terminalOut: String? = null

        @SerializedName("follower")
        var follower: String? = null

        @SerializedName("visitorFrom")
        var visitorFrom: String? = null

        @SerializedName("department")
        var department: String? = null

        @SerializedName("visitPerson")
        var visitPerson: String? = null

        @SerializedName("contactTopic")
        var contactTopic: String? = null

        @SerializedName("contactPlace")
        var contactPlace: String? = null

        @SerializedName("etc")
        var etc: String? = null

        @SerializedName("timestamp")
        var timestamp: String? = null

        @SerializedName("cost")
        var cost: Cost? = null

        class Cost {
            @SerializedName("costType")
            var costType: String? = null

            @SerializedName("totalMinute")
            var totalMinute: String? = null

            @SerializedName("extraCost")
            var extraCost: String? = null

            @SerializedName("totalExpenses")
            var totalExpenses: String? = null

            @SerializedName("paymentStatus")
            var paymentStatus: String? = null

            @SerializedName("timestamp")
            var timestamp: String? = null

        }

        class Detail {
            @SerializedName("status")
            var status: String? = null

            @SerializedName("total")
            var total: String? = null

            @SerializedName("result")
            var result: List<VisitorDetail>? = null

        }
    }

    class Payment {
        class GetCost {
            @SerializedName("status")
            var status: String? = null

            @SerializedName("message")
            var message: Message? = null

            class Message {
                @SerializedName("total")
                var total: String? = null

                @SerializedName("result")
                var result: List<Result>? = null

                class Result {
                    @SerializedName("costId")
                    var costId: String? = null

                    @SerializedName("visitorType")
                    var visitorType: String? = null

                    @SerializedName("costType")
                    var costType: String? = null

                    @SerializedName("costTime")
                    var costTime: List<Int>? = null

                    @SerializedName("costRate")
                    var costRate: List<Int>? = null

                    @SerializedName("fine")
                    var fine: Int? = null

                    @SerializedName("checkoutStatus")
                    var checkoutStatus: String? = null
                }
            }

        }

        class PostCost {

            @SerializedName("userId")
            var userId: String? = null

            @SerializedName("visitorId")
            var visitorId: String? = null

            @SerializedName("costType")
            var costType: String? = null

            @SerializedName("totalMinute")
            var totalMinute: String? = null

            @SerializedName("extraCost")
            var extraCost: String? = null

            @SerializedName("totalExpenses")
            var totalExpenses: String? = null

            @SerializedName("paymentStatus")
            var paymentStatus: String? = null

            class Respones {
                @SerializedName("status")
                var status: String? = null

                @SerializedName("message")
                var message: String? = null
            }


        }

    }

    class Appointment {
        class Get {
            @SerializedName("status")
            var status: String? = null

            @SerializedName("message")
            var message: Message? = null

            class Message {
                @SerializedName("total")
                var total: String? = null
                @SerializedName("result")
                var result: ArrayList<Result>? = null

                class Result{
                    @SerializedName("meetingId")
                    var meetingId: String? = null

                    @SerializedName("uId")
                    var uId: String? = null

                    @SerializedName("name")
                    var name: String? = null

                    @SerializedName("meetUpLocal")
                    var meetUpLocal: String? = null

                    @SerializedName("daysToCome")
                    var daysToCome: String? = null

                    @SerializedName("licensePlate")
                    var licensePlate: String? = null

                    @SerializedName("status")
                    var status: String? = null

                    @SerializedName("registerDate")
                    var registerDate: String? = null
                }
            }
        }

        class Put{

            @SerializedName("userId")
            var userId: String? = null

            @SerializedName("meetingId")
            var meetingId : String? = null

            @SerializedName("name")
            var name: String? = null

            @SerializedName("meetUpLocal")
            var meetUpLocal: String? = null

            @SerializedName("daysToCome")
            var daysToCome: String? = null

            @SerializedName("licensePlate")
            var licensePlate: String? = null

            @SerializedName("status")
            var status: String? = null

            class Message {
                @SerializedName("status")
                var status: String? = null

                @SerializedName("message")
                var message: Message? = null

                class Message {
                    @SerializedName("operate")
                    var operate: String? = null

                    @SerializedName("data")
                    var data: String? = null
                }
            }

        }

    }

    class Whitelist {
        class Get {
            @SerializedName("status")
            var status: String? = null

            @SerializedName("message")
            var message: Whitelist.Get.Message? = null

            class Message {
                @SerializedName("whitelist")
                var whitelist: List<Whitelist.Get.Message.list>? = null

                class list {
                    @SerializedName("whitelistId")
                    var whitelistId: String? = null

                    @SerializedName("name")
                    var name: String? = null

                    @SerializedName("citizenId")
                    var citizenId: String? = null

                    @SerializedName("time")
                    var time: List<Time>? = null

                    class Time {
                        @SerializedName("whitelistTimeId")
                        var whitelistTimeId: String? = null

                        @SerializedName("timeStart")
                        var timeStart: String? = null

                        @SerializedName("timeStop")
                        var timeStop: String? = null

                    }
                }
            }

        }

        class Post{
            @SerializedName("userId")
            var userId: String? = null

            @SerializedName("name")
            var name: String? = null

            @SerializedName("citizenId")
            var citizenId: String? = null

            @SerializedName("timeStart")
            var timeStart: String? = null

            @SerializedName("timeStop")
            var timeStop: String? = null

            class Response{
                @SerializedName("status")
                var status: String? = null

                @SerializedName("message")
                var message: String? = null
            }
        }

        class Delete{
            @SerializedName("userId")
            var userId: String? = null

            @SerializedName("whitelistId")
            var whitelistId: String? = null

            @SerializedName("whitelistTimeId")
            var whitelistTimeId: String? = null

            class Response{
                @SerializedName("status")
                var status: String? = null

                @SerializedName("message")
                var message: String? = null
            }
        }
    }
    class Blacklist {
        class Get {
            @SerializedName("status")
            var status: String? = null

            @SerializedName("message")
            var message: Blacklist.Get.Message? = null

            class Message {
                @SerializedName("blacklist")
                var blacklist: List<Blacklist.Get.Message.list>? = null

                class list {
                    @SerializedName("blacklistId")
                    var blacklistId: String? = null

                    @SerializedName("name")
                    var name: String? = null

                    @SerializedName("citizenId")
                    var citizenId: String? = null

                    @SerializedName("time")
                    var time: List<Time>? = null

                    class Time {
                        @SerializedName("blacklistTimeId")
                        var blacklistTimeId: String? = null

                        @SerializedName("timeStart")
                        var timeStart: String? = null

                        @SerializedName("timeStop")
                        var timeStop: String? = null

                    }
                }
            }

        }

        class Post{
            @SerializedName("userId")
            var userId: String? = null

            @SerializedName("name")
            var name: String? = null

            @SerializedName("citizenId")
            var citizenId: String? = null

            @SerializedName("timeStart")
            var timeStart: String? = null

            @SerializedName("timeStop")
            var timeStop: String? = null

            class Response{
                @SerializedName("status")
                var status: String? = null

                @SerializedName("message")
                var message: String? = null
            }
        }

        class Delete{
            @SerializedName("userId")
            var userId: String? = null

            @SerializedName("blacklistId")
            var blacklistId: String? = null

            @SerializedName("blacklistTimeId")
            var blacklistTimeId: String? = null

            class Response{
                @SerializedName("status")
                var status: String? = null

                @SerializedName("message")
                var message: String? = null
            }
        }
    }

    class EStamp {
         class GetRespones {
            @SerializedName("status")
            var status: String? = null

            @SerializedName("message")
            var message: Message? = null

             class Message {
                @SerializedName("total")
                var total: String? = null

                @SerializedName("result")
                var result: ArrayList<Result>? = null

                 class Result {
                    @SerializedName("estampId")
                    var estampId: String? = null

                    @SerializedName("uId")
                    var uId: String? = null

                    @SerializedName("useForm")
                    var useForm: String? = null

                    @SerializedName("houseNumber")
                    var houseNumber: String? = null

                    @SerializedName("visitorNumber")
                    var visitorNumber: String? = null

                    @SerializedName("estampStatus")
                    var estampStatus: String? = null

                    @SerializedName("timeEstamp")
                    var timeEstamp: String? = null

                    fun getuId(): String? {
                        return uId
                    }

                    fun setuId(uId: String?) {
                        this.uId = uId
                    }
                }
            }
        }

        inner class Respones {
            @SerializedName("status")
            var status: String? = null

            @SerializedName("message")
            var message: String? = null
        }

         class Body(
            @field:SerializedName("userId") var userId: String,
            @field:SerializedName(
                "uId"
            ) var uId: String,
            @field:SerializedName("useForm") var useForm: String,
            @field:SerializedName(
                "houseNumber"
            ) var houseNumber: String,
            @field:SerializedName("visitorNumber") var visitorNumber: String,
            @field:SerializedName(
                "estampStatus"
            ) var estampStatus: String
        ) {

            fun getuId(): String {
                return uId
            }

            fun setuId(uId: String) {
                this.uId = uId
            }

        }
    }
}