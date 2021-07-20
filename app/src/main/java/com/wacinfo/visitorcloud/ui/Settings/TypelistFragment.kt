package com.wacinfo.visitorcloud.ui.Settings

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.wacinfo.visitorcloud.Adapter.TypeListAdapter
import com.wacinfo.visitorcloud.MainActivity
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.FragmentTypelistBinding
import com.wacinfo.visitorcloud.model.TypeItem
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


class TypelistFragment : Fragment() {

    private lateinit var typelistViewModel: TypelistViewModel
    private val TAG: String = "TypelistLog"
    private var typeItem: List<TypeItem>? = null
    private lateinit var binding: FragmentTypelistBinding
    var viewAdapter: TypeListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val toolbar: Toolbar = activity?.findViewById<View>(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back_white)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        Observable
            .just(ConnectManager().token(requireActivity(), AppSettings.REFRESH_TOKEN))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                syncSettings()
            }
            .subscribe()


        typelistViewModel = ViewModelProvider(requireActivity()).get(TypelistViewModel::class.java)

        selectList()

    }

    private fun selectList() {
        var listData: List<String>
        var listObserv = MutableLiveData<List<String>>()
        if (typelistViewModel.PageType.value == typelistViewModel.VISITOR_TYPE) {
            listObserv = typelistViewModel.visitorList as MutableLiveData<List<String>>
        } else if (typelistViewModel.PageType.value == typelistViewModel.VISITOR_PLACE) {
            listObserv = typelistViewModel.visitplace as MutableLiveData<List<String>>
        } else if (typelistViewModel.PageType.value == typelistViewModel.VEHICLE_TYPE) {
            listObserv = typelistViewModel.vehicletype as MutableLiveData<List<String>>
        } else if (typelistViewModel.PageType.value == typelistViewModel.VEHICLE_LICENSE) {
            listObserv = typelistViewModel.licenseplate as MutableLiveData<List<String>>
        }
        listObserv.observe(viewLifecycleOwner, {
            listData = it
            typeItem = ArrayList()
            for (element in listData) {
                (typeItem as ArrayList<TypeItem>).add(TypeItem(element))
            }
            binding.typeList.run {
                val LM = LinearLayoutManager(requireContext())
                LM.orientation = LinearLayoutManager.VERTICAL
                layoutManager = LM
                setHasFixedSize(true)
                viewAdapter = TypeListAdapter(
                    requireContext(),
                    typelistViewModel,
                    typeItem as ArrayList<TypeItem>
                )
                adapter = viewAdapter
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_typelist, container, false)
        binding = FragmentTypelistBinding.bind(view)


        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.type_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_add -> {
                MaterialDialog(requireContext()).show {
                    title(R.string.add)
                    input(
                        hint = getString(R.string.error_emptytext),
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                    ) { _, text ->
                        addNewType(text.toString())
                    }
                    positiveButton(R.string.save)
                    negativeButton(R.string.cancel_en)
                    lifecycleOwner(this@TypelistFragment)
                }

                true
            }


            else ->
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                super.onOptionsItemSelected(item)
        }
    }

    private fun addNewType(newType: String) {
        val apiname: String
        when (typelistViewModel.PageType.value) {
            typelistViewModel.VISITOR_TYPE -> {
                apiname = resources.getString(R.string.API_VisitorType)
                val Data = RetrofitData.Property.VisitorType()
                Data.userId = AppSettings.USER_ID
                Data.visitorType = newType

                Observable
                    .just(ConnectManager().token(requireActivity(), AppSettings.REFRESH_TOKEN))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete {
                        addToVisitorType(Data, apiname)
                    }
                    .subscribe()

            }
            typelistViewModel.VISITOR_PLACE -> {
                apiname = resources.getString(R.string.API_VisitPlace)
                val Data = RetrofitData.Property.VisitPlace()
                Data.userId = AppSettings.USER_ID
                Data.visitPlace = newType
                Observable
                    .just(ConnectManager().token(requireActivity(), AppSettings.REFRESH_TOKEN))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete {
                        addToVisitPlace(Data, apiname)
                    }
                    .subscribe()

            }
            typelistViewModel.VEHICLE_TYPE -> {
                apiname = resources.getString(R.string.API_VehicleType)
                val Data = RetrofitData.Property.VehicleType()
                Data.userId = AppSettings.USER_ID
                Data.vehicleType = newType
                Observable
                    .just(ConnectManager().token(requireActivity(), AppSettings.REFRESH_TOKEN))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete {
                        addToVehicleType(Data, apiname)
                    }
                    .subscribe()


            }
            typelistViewModel.VEHICLE_LICENSE -> {
                apiname = resources.getString(R.string.API_LicensePlate)
                val Data = RetrofitData.Property.LicensePlate()
                Data.userId = AppSettings.USER_ID
                Data.licensePlate = newType
                Observable
                    .just(ConnectManager().token(requireActivity(), AppSettings.REFRESH_TOKEN))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete {
                        addToLicensePlate(Data, apiname)
                    }
                    .subscribe()


            }
        }

    }

    private fun addToVisitorType(typeData: RetrofitData.Property.VisitorType, apiname: String) {
        val dialog= PublicFunction().retrofitDialog(requireContext())
        dialog!!.show()

        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        retrofit.create(API::class.java)
            .postVisitorType(typeData, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Property> {
                override fun onComplete() {
                    syncSettings()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Property) {
                    dialog.cancel()
                    PublicFunction().message(requireActivity(), t.message.toString())
                }

                override fun onError(e: Throwable) {
                    PublicFunction().errorDialog(dialog).show()
                    e.printStackTrace()
                    Log.d(TAG, e.toString())
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(TAG, jObjError.getString("message"))
                            PublicFunction().message(
                                requireActivity(),
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            e.message?.let {
                                Log.d(TAG, it)
                                PublicFunction().message(
                                    requireActivity(),
                                    it
                                )
                            }
                        }
                    }

                }
            })
    }

    private fun addToVisitPlace(typeData: RetrofitData.Property.VisitPlace, apiname: String) {
        val dialog= PublicFunction().retrofitDialog(requireContext())
        dialog!!.show()

        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        retrofit.create(API::class.java)
            .postVisitPlace(typeData, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Property> {
                override fun onComplete() {
                    syncSettings()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Property) {
                    dialog.cancel()
                    PublicFunction().message(requireActivity(), t.message.toString())
                }

                override fun onError(e: Throwable) {
                    PublicFunction().errorDialog(dialog).show()
                    e.printStackTrace()
                    Log.d(TAG, e.toString())
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(TAG, jObjError.getString("message"))
                            PublicFunction().message(
                                requireActivity(),
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            e.message?.let {
                                Log.d(TAG, it)
                                PublicFunction().message(
                                    requireActivity(),
                                    it
                                )
                            }
                        }
                    }

                }
            })
    }

    private fun addToVehicleType(typeData: RetrofitData.Property.VehicleType, apiname: String) {
        val dialog= PublicFunction().retrofitDialog(requireContext())
        dialog!!.show()
        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        retrofit.create(API::class.java)
            .postVehicleType(typeData, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Property> {
                override fun onComplete() {
                    syncSettings()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Property) {
                    dialog.cancel()
                    PublicFunction().message(requireActivity(), t.message.toString())
                }

                override fun onError(e: Throwable) {
                    PublicFunction().errorDialog(dialog).show()
                    e.printStackTrace()
                    Log.d(TAG, e.toString())
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(TAG, jObjError.getString("message"))
                            PublicFunction().message(
                                requireActivity(),
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            e.message?.let {
                                Log.d(TAG, it)
                                PublicFunction().message(
                                    requireActivity(),
                                    it
                                )
                            }
                        }
                    }

                }
            })
    }

    private fun addToLicensePlate(typeData: RetrofitData.Property.LicensePlate, apiname: String) {
        val dialog= PublicFunction().retrofitDialog(requireContext())
        dialog!!.show()

        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        retrofit.create(API::class.java)
            .postLicensePlate(typeData, apiname)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Property> {
                override fun onComplete() {
                    syncSettings()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Property) {
                    dialog.cancel()
                    PublicFunction().message(requireActivity(), t.message.toString())
                }

                override fun onError(e: Throwable) {
                    PublicFunction().errorDialog(dialog).show()
                    e.printStackTrace()
                    Log.d(TAG, e.toString())
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(TAG, jObjError.getString("message"))
                            PublicFunction().message(
                                requireActivity(),
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            e.message?.let {
                                Log.d(TAG, it)
                                PublicFunction().message(
                                    requireActivity(),
                                    it
                                )
                            }
                        }
                    }

                }
            })
    }


    private fun syncSettings() {
        Log.d(TAG, "//////////syncSettings")
        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_Property)
        val userID = AppSettings.USER_ID
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("$url$apiname")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .getProperty(userID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Settings> {
                override fun onComplete() {
                    when (typelistViewModel.PageType.value) {
                        typelistViewModel.VISITOR_TYPE -> {
                            typelistViewModel.setVisitorList(AppSettings.visitorType)
                        }
                        typelistViewModel.VISITOR_PLACE -> {
                            typelistViewModel.setVisitPlace(AppSettings.visitPlace)
                        }
                        typelistViewModel.VEHICLE_TYPE -> {
                            typelistViewModel.setVehicleType(AppSettings.vehicleType)
                        }
                        typelistViewModel.VEHICLE_LICENSE -> {
                            typelistViewModel.setLicensePlate(AppSettings.licensePlate)
                        }
                    }

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Settings) {
                    try {
                        if (t.message?.visitPlace?.isNotEmpty() == true) {
                            AppSettings.visitPlace = t.message?.visitPlace!!
                        } else {
                            AppSettings.visitPlace = emptyList<String>()
                        }
                        if (t.message?.vehicleType?.isNotEmpty() == true) {
                            AppSettings.vehicleType = t.message?.vehicleType!!
                        } else {
                            AppSettings.vehicleType = emptyList<String>()

                        }
                        if (t.message?.licensePlate?.isNotEmpty() == true) {
                            AppSettings.licensePlate = t.message?.licensePlate!!
                        } else {
                            AppSettings.licensePlate = emptyList<String>()
                        }
                        if (t.message?.visitorType?.isNotEmpty() == true) {
                            AppSettings.visitorType = t.message?.visitorType!!
                        } else {
                            AppSettings.visitorType = emptyList<String>()
                        }
                        if (t.message?.contactTopic?.isNotEmpty() == true) {
                            AppSettings.contactTopic = t.message?.contactTopic!!
                        } else {
                            AppSettings.contactTopic = emptyList<String>()
                        }
                        if (t.message?.whitelist?.isNotEmpty() == true) {
                            AppSettings.whitelist = t.message?.whitelist!!
                        } else {
                            AppSettings.whitelist = emptyList<RetrofitData.Settings.WhiteList>()
                        }
                        if (t.message?.visitFrom?.isNotEmpty() == true) {
                            AppSettings.visitFrom = t.message?.visitFrom!!
                        } else {
                            AppSettings.visitFrom = emptyList<String>()
                        }
                        if (t.message?.visitPerson?.isNotEmpty() == true) {
                            AppSettings.visitPerson = t.message?.visitPerson!!
                        } else {
                            AppSettings.visitPerson = emptyList<String>()
                        }
                        if (t.message?.department?.isNotEmpty() == true) {
                            AppSettings.department = t.message?.department!!
                        } else {
                            AppSettings.department = emptyList<String>()
                        }
                        if (t.message?.blacklist?.isNotEmpty() == true) {
                            AppSettings.blacklist = t.message?.blacklist!!
                        } else {
                            AppSettings.blacklist = emptyList<RetrofitData.Settings.BlackList>()
                        }


                    } catch (e: NullPointerException) {
                        Log.d(TAG, "onNext: $e")
                    }


                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    Log.d(TAG, e.toString())
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            Log.d(TAG, jObjError.getString("message"))
                        } catch (e: Exception) {
                            e.message?.let { Log.d(TAG, it) }
                        }
                    }

                    PublicFunction().message(requireActivity(), "Sync data fail")
                }
            })
    }

}