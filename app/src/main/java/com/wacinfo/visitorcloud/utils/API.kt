package com.wacinfo.visitorcloud.utils

import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface API {

    @POST("{api}")
    fun postActivate(
        @Body data: RetrofitData.Activate, @Path(value = "api", encoded = true) api: String
    ): Observable<RetrofitData.Activate>

    @POST("{api}")
    fun postLogin(
        @Body data: RetrofitData.Login.PostBody, @Path(value = "api", encoded = true) api: String
    ): Observable<RetrofitData.Login>

    @POST("{api}")
    fun postLogout(
        @Body data: RetrofitData.Logout.Post,
        @Path(value = "api", encoded = true) api: String
    ): Observable<RetrofitData.Logout.Respones>

    @POST("{api}")
    fun postBooking(
        @Body body: RetrofitData.BookingVisitorID.Body,
        @Path(value = "api", encoded = true) api: String
    ): Observable<RetrofitData.BookingVisitorID>

    @POST("{api}")
    fun postToken(
        @Header("Authorization") authHeader: String,
        @Path(value = "api", encoded = true) api: String
    ): Observable<RetrofitData.Login>


    @GET("{id}")
    fun getProperty(
        @Path(value = "id", encoded = true) id: String
    )
            : Observable<RetrofitData.Settings>

    @PUT("{api}")
    fun editProperty(
        @Body data: RetrofitData.Property.Edit, @Path(value = "api", encoded = true) api: String
    )
            : Observable<RetrofitData.Property>

    //Logo
    @GET("{id}")
    fun getLogo(
        @Path(value = "id", encoded = true) id: String
    )
            : Observable<RetrofitData.Logo>

    //Visitor Type
    @POST("{api}")
    fun postVisitorType(
        @Body data: RetrofitData.Property.VisitorType,
        @Path(value = "api", encoded = true) api: String
    )
            : Observable<RetrofitData.Property>

    @HTTP(method = "DELETE", path = "{api}", hasBody = true)
    fun deleteVisitorType(
        @Body data: RetrofitData.Property.VisitorType,
        @Path(value = "api", encoded = true) api: String
    )
            : Observable<RetrofitData.Property>

    //Visit Place
    @POST("{api}")
    fun postVisitPlace(
        @Body data: RetrofitData.Property.VisitPlace,
        @Path(value = "api", encoded = true) api: String
    )
            : Observable<RetrofitData.Property>

    @HTTP(method = "DELETE", path = "{api}", hasBody = true)
    fun deleteVisitPlace(
        @Body data: RetrofitData.Property.VisitPlace,
        @Path(value = "api", encoded = true) api: String
    )
            : Observable<RetrofitData.Property>

    //Vehicle Type
    @POST("{api}")
    fun postVehicleType(
        @Body data: RetrofitData.Property.VehicleType,
        @Path(value = "api", encoded = true) api: String
    )
            : Observable<RetrofitData.Property>

    @HTTP(method = "DELETE", path = "{api}", hasBody = true)
    fun deleteVehicleType(
        @Body data: RetrofitData.Property.VehicleType,
        @Path(value = "api", encoded = true) api: String
    )
            : Observable<RetrofitData.Property>

    //LicensePlate
    @POST("{api}")
    fun postLicensePlate(
        @Body data: RetrofitData.Property.LicensePlate,
        @Path(value = "api", encoded = true) api: String
    ): Observable<RetrofitData.Property>

    @HTTP(method = "DELETE", path = "{api}", hasBody = true)
    fun deleteLicensePlate(
        @Body data: RetrofitData.Property.LicensePlate,
        @Path(value = "api", encoded = true) api: String
    )
            : Observable<RetrofitData.Property>

    @GET("{vid}")
    fun getChecknum(
        @Path(value = "vid", encoded = true) id: String
    )
            : Observable<RetrofitData.Checknum>

    @Multipart
    @POST("{api}")
    fun insertVisitorLOG(
        @Path(value = "api", encoded = true) id: String,
        @Part("userId") userId: RequestBody,
        @Part image1: MultipartBody.Part?,
        @Part image2: MultipartBody.Part?,
        @Part image3: MultipartBody.Part?,
        @Part image4: MultipartBody.Part?,
        @Part("visitorNumber") visitorNumber: RequestBody,
        @Part("citizenId") citizenId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("visitorType") visitorType: RequestBody,
        @Part("recordTimeIn") recordTimeIn: RequestBody,
        @Part("licensePlate") licensePlate: RequestBody,
        @Part("vehicleType") vehicleType: RequestBody,
        @Part("recordStatus") recordStatus: RequestBody,
        @Part("terminalIn") terminalIn: RequestBody,
        @Part("contactPlace") contactPlace: RequestBody

    ): Observable<RetrofitData.VisitorDetail?>?


    //Checkout
    @POST("{api}")
    fun postCheckOut(
        @Body data: RetrofitData.VisitorDetail,
        @Path(value = "api", encoded = true) api: String

    ): Observable<RetrofitData.VisitorDetail>

    @GET("{api}?")
    fun getLog(
        @Path(value = "api", encoded = true) api: String,
        @Query(value = "_id", encoded = true) _id: String,
        @Query(value = "_before", encoded = true) _before: Boolean,
        @Query(value = "_time", encoded = true) _time: String,
        @Query(value = "_page", encoded = true) _page: Int,
        @Query(value = "_limit", encoded = true) _limit: Int,
        @Query(value = "_sort", encoded = true) _sort: Int
    )
            : Observable<RetrofitData.VisitorDetail>

    @GET("{api}?")
    fun getLogWithBefore(
        @Path(value = "api", encoded = true) api: String,
        @Query(value = "_id", encoded = true) _id: String,
        @Query(value = "_before", encoded = true) _before: Boolean,
        @Query(value = "_time", encoded = true) _time: String,
        @Query(value = "_page", encoded = true) _page: Int,
        @Query(value = "_limit", encoded = true) _limit: Int,
        @Query(value = "_sort", encoded = true) _sort: Int
    )
            : Observable<RetrofitData.VisitorDetail>

    @GET("{api}?")
    fun getReport(
        @Path(value = "api", encoded = true) api: String,
        @Query(value = "_id", encoded = true) _id: String,
        @Query(value = "_time", encoded = true) _time: String,
        @Query(value = "_before", encoded = true) _before: Boolean,
        @Query(value = "_page", encoded = true) _page: Int,
        @Query(value = "_limit", encoded = true) _limit: Int,
        @Query(value = "_sort", encoded = true) _sort: Int,
        @Query(value = "_type", encoded = true) _type: String
    )
            : Observable<RetrofitData.VisitorDetail>


    //Cost
    @GET("{api}?")
    fun getCost(
        @Path(value = "api", encoded = true) api: String,
        @Query(value = "_id", encoded = true) _id: String,
        @Query(value = "_page", encoded = true) _page: Int,
        @Query(value = "_limit", encoded = true) _limit: Int
    )
            : Observable<RetrofitData.Payment.GetCost>


    @POST("{api}")
    fun postCost(
        @Body data: RetrofitData.Payment.PostCost,
        @Path(value = "api", encoded = true) api: String

    ): Observable<RetrofitData.Payment.PostCost.Respones>

    //Blacklist
    @GET("{userId}")
    fun getBlackList(
        @Path(value = "userId", encoded = true) userId: String
    ): Observable<RetrofitData.Blacklist.Get>

    @POST("{api}")
    fun postBlackList(
        @Path(value = "api", encoded = true) api: String,
        @Body data: RetrofitData.Blacklist.Post
    ): Observable<RetrofitData.Blacklist.Post.Response>


    @HTTP(method = "DELETE", path = "{api}", hasBody = true)
    fun deleteBlackList(
        @Path(value = "api", encoded = true) api: String,
        @Body data: RetrofitData.Blacklist.Delete
    )
            : Observable<RetrofitData.Blacklist.Delete.Response>


    //Blacklist
    @GET("{userId}")
    fun getWhiteList(
        @Path(value = "userId", encoded = true) userId: String
    ): Observable<RetrofitData.Whitelist.Get>


    //Appointment
    @GET("{userId}")
    fun getAppointment(
        @Path(value = "userId", encoded = true) userId: String,
        @Query(value = "uId", encoded = true) uId: String
//        @Query(value = "_page", encoded = true) _page: String,
//        @Query(value = "_limit", encoded = true) _limit: String,
//        @Query(value = "_sort", encoded = true) _sort: String,

    ): Observable<RetrofitData.Appointment.Get>

    @PUT("{api}")
    fun updateMeeting(
        @Path(value = "api", encoded = true) api: String,
        @Body data: RetrofitData.Appointment.Put
    )
            : Observable<RetrofitData.Appointment.Put.Message>

    @POST("{api}")
    fun refreshToken(
        @Header("Authorization") authHeader: String,
        @Path(value = "api", encoded = true) api: String
    ): Call<RetrofitData.Login>
}