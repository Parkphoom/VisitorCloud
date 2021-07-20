package com.wacinfo.visitorcloud.ui.dialog

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.DialogEditBinding
import com.wacinfo.visitorcloud.ui.Settings.TypelistViewModel
import com.wacinfo.visitorcloud.utils.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class EditDialog : BottomSheetDialogFragment(), View.OnClickListener {
    var onCloseClickListener: (() -> Unit)? = null
    private lateinit var typelistViewModel: TypelistViewModel
    private lateinit var binding: DialogEditBinding
    var header = ""
    var oldvalue = ""
    var newvalue = ""

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.dialog_edit, container)
        binding = DialogEditBinding.bind(root)
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        typelistViewModel = ViewModelProvider(requireActivity()).get(TypelistViewModel::class.java)
    }

    private fun initView() {
        binding.headerTv.text = header
        binding.oldEdt.setText(oldvalue)
        binding.newEdt.setText(newvalue)
        binding.delBtn.setOnClickListener(this)
        binding.editBtn.setOnClickListener(this)
    }

    companion object {
        fun newInstance(): EditDialog = EditDialog().apply {

        }
    }

    override fun onClick(v: View?) {
        if (v == binding.delBtn) {
            MaterialDialog(requireContext()).show {
                title(R.string.delete_data_th)
                message(text = "ลบข้อมูลชื่อประเภท : $oldvalue")

                positiveButton(R.string.accept_th) {

                    when (typelistViewModel.PageType.value) {
                        typelistViewModel.VISITOR_TYPE -> {
                            val Data = RetrofitData.Property.VisitorType()
                            Data.userId = AppSettings.USER_ID
                            Data.visitorType = oldvalue
                            Observable
                                .just(ConnectManager().token(requireActivity(), AppSettings.REFRESH_TOKEN))
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnComplete {
                                    deleteVisitorType(Data)
                                }
                                .subscribe()

                        }
                        typelistViewModel.VISITOR_PLACE -> {
                            val Data = RetrofitData.Property.VisitPlace()
                            Data.userId = AppSettings.USER_ID
                            Data.visitPlace = oldvalue

                            Observable
                                .just(ConnectManager().token(requireActivity(),AppSettings.REFRESH_TOKEN))
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnComplete {
                                    deleteVisitPlace(Data)
                                }
                                .subscribe()

                        }
                        typelistViewModel.VEHICLE_TYPE -> {
                            val Data = RetrofitData.Property.VehicleType()
                            Data.userId = AppSettings.USER_ID
                            Data.vehicleType = oldvalue
                            Observable
                                .just(ConnectManager().token(requireActivity(),AppSettings.REFRESH_TOKEN))
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnComplete {
                                    deleteVehicleType(Data)
                                }
                                .subscribe()

                        }
                        typelistViewModel.VEHICLE_LICENSE -> {
                            val Data = RetrofitData.Property.LicensePlate()
                            Data.userId = AppSettings.USER_ID
                            Data.licensePlate = oldvalue
                            Observable
                                .just(ConnectManager().token(requireActivity(),AppSettings.REFRESH_TOKEN))
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnComplete {
                                    deleteLicensePlate(Data)
                                }
                                .subscribe()


                        }
                    }
                }
                negativeButton(R.string.cancel_th)
            }
        }
        if (v == binding.editBtn) {
            newvalue = binding.newEdt.text.toString()

            if (newvalue.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_emptytext),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val editData = RetrofitData.Property.Edit()
                editData.userId = AppSettings.USER_ID
                editData.find = oldvalue
                editData.editTo = newvalue

                var apiname = requireContext().resources.getString(R.string.API_VisitorType)
                when (typelistViewModel.PageType.value) {
                    typelistViewModel.VISITOR_TYPE -> {
                        apiname = resources.getString(R.string.API_VisitorType)
                    }
                    typelistViewModel.VISITOR_PLACE -> {
                        apiname = resources.getString(R.string.API_VisitPlace)
                    }
                    typelistViewModel.VEHICLE_TYPE -> {
                        apiname = resources.getString(R.string.API_VehicleType)
                    }
                    typelistViewModel.VEHICLE_LICENSE -> {
                        apiname = resources.getString(R.string.API_LicensePlate)
                    }
                }


                Observable
                    .just(ConnectManager().token(requireActivity(), AppSettings.REFRESH_TOKEN))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete {
                        editProperty(editData, apiname)
                    }
                    .subscribe()

            }


        }
    }

    private fun editProperty(data: RetrofitData.Property.Edit, apiname: String) {
        val url: String =
            requireContext().resources.getString(R.string.URL) + requireContext().resources.getString(
                R.string.PORT
            )
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .editProperty(data, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Property> {
                override fun onComplete() {
                    onCloseClickListener?.invoke()
                    dialog?.dismiss()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Property) {
                    PublicFunction().message(requireActivity() as Activity, t.message.toString())
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            PublicFunction().message(
                                requireActivity() as Activity,
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            PublicFunction().message(requireActivity() as Activity, e.toString())
                        }
                    } else {
                        PublicFunction().message(requireActivity() as Activity, e.toString())
                    }

                }
            })
    }

    private fun deleteVisitorType(data: RetrofitData.Property.VisitorType) {
        val url: String =
            requireContext().resources.getString(R.string.URL) + requireContext().resources.getString(
                R.string.PORT
            )
        val apiname = requireContext().resources.getString(R.string.API_VisitorType)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .deleteVisitorType(data, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Property> {
                override fun onComplete() {
                    onCloseClickListener?.invoke()
                    dialog?.dismiss()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Property) {
                    PublicFunction().message(requireActivity() as Activity, t.message.toString())
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            PublicFunction().message(
                                requireActivity() as Activity,
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            PublicFunction().message(requireActivity() as Activity, e.toString())
                        }
                    } else {
                        PublicFunction().message(requireActivity() as Activity, e.toString())
                    }

                }
            })
    }

    private fun deleteVisitPlace(data: RetrofitData.Property.VisitPlace) {
        val url: String =
            requireContext().resources.getString(R.string.URL) + requireContext().resources.getString(
                R.string.PORT
            )
        val apiname = requireContext().resources.getString(R.string.API_VisitPlace)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .deleteVisitPlace(data, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Property> {
                override fun onComplete() {
                    dialog?.dismiss()
                    onCloseClickListener?.invoke()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Property) {
                    PublicFunction().message(requireActivity() as Activity, t.message.toString())
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            PublicFunction().message(
                                requireActivity() as Activity,
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            PublicFunction().message(requireActivity() as Activity, e.toString())
                        }
                    } else {
                        PublicFunction().message(requireActivity() as Activity, e.toString())
                    }

                }
            })
    }

    private fun deleteVehicleType(data: RetrofitData.Property.VehicleType) {
        val url: String =
            requireContext().resources.getString(R.string.URL) + requireContext().resources.getString(
                R.string.PORT
            )
        val apiname = requireContext().resources.getString(R.string.API_VehicleType)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .deleteVehicleType(data, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Property> {
                override fun onComplete() {
                    onCloseClickListener?.invoke()
                    dialog?.dismiss()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Property) {
                    PublicFunction().message(requireActivity() as Activity, t.message.toString())
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            PublicFunction().message(
                                requireActivity() as Activity,
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            PublicFunction().message(requireActivity() as Activity, e.toString())
                        }
                    } else {
                        PublicFunction().message(requireActivity() as Activity, e.toString())
                    }

                }
            })
    }

    private fun deleteLicensePlate(data: RetrofitData.Property.LicensePlate) {
        val url: String =
            requireContext().resources.getString(R.string.URL) + requireContext().resources.getString(
                R.string.PORT
            )
        val apiname = requireContext().resources.getString(R.string.API_LicensePlate)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .deleteLicensePlate(data, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Property> {
                override fun onComplete() {
                    onCloseClickListener?.invoke()
                    dialog?.dismiss()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Property) {
                    PublicFunction().message(requireActivity() as Activity, t.message.toString())
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            PublicFunction().message(
                                requireActivity() as Activity,
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            PublicFunction().message(requireActivity() as Activity, e.toString())
                        }
                    } else {
                        PublicFunction().message(requireActivity() as Activity, e.toString())
                    }

                }
            })
    }
}