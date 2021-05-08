package com.wacinfo.visitorcloud.ui.homescreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.wacinfo.visitorcloud.Adapter.AbstractFragment
import com.wacinfo.visitorcloud.Adapter.TitleItem
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.FragmentTitleBinding
import com.wacinfo.visitorcloud.ui.checkout.CheckoutScannerActivity
import com.wacinfo.visitorcloud.utils.*
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@Suppress("UNCHECKED_CAST")
class Title : AbstractFragment(), View.OnClickListener {
    lateinit var viewModel: TitleViewModel
    lateinit var titlebinding: FragmentTitleBinding
    lateinit var aboutViewModel: AboutViewModel

    private var mAdapter: FlexibleAdapter<IFlexible<*>>? = null

    var TAG = "TitleLog"

    val sdf = SimpleDateFormat("yyyyMMdd")
    val currentDateandTime: String = sdf.format(Date())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_title, container, false)
        setHasOptionsMenu(true)


        titlebinding = FragmentTitleBinding.bind(view)
        titlebinding.inBtn.setOnClickListener(this)
        titlebinding.outBtn.setOnClickListener(this)

        viewModel = ViewModelProvider(requireActivity()).get(TitleViewModel::class.java)
        viewModel.mContext = requireActivity()
        aboutViewModel = ViewModelProviders.of(requireActivity()).get(AboutViewModel::class.java)
        aboutViewModel.context = requireActivity()

        titlebinding.inBtn.setOnClickListener(this)

        val toolbar: Toolbar = activity?.findViewById<View>(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)
        toolbar.title = ""

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        Log.v(
            TAG,
            "onCreateOptionsMenu called!"
        )
    }


    override fun onResume() {
        super.onResume()
        titlebinding.titleprogress.visibility = View.VISIBLE
        val dialog = PublicFunction().retrofitDialog(requireContext())
        dialog!!.show()



        mAdapter = FlexibleAdapter(null, activity)
        mAdapter!!.setAutoScrollOnExpand(true) //.setAnimateToLimit(Integer.MAX_VALUE) //Use the default value
            .setNotifyMoveOfFilteredItems(true) //When true, filtering on big list is very slow, not in this case!
            .setNotifyChangeOfUnfilteredItems(true) //true by default
            .setAnimationOnForwardScrolling(true)
            .setAnimationOnReverseScrolling(true)
        mRecyclerView = titlebinding.recyclerView
        mRecyclerView!!.layoutManager = createNewLinearLayoutManager()
        mRecyclerView!!.adapter = mAdapter
        mRecyclerView!!.setHasFixedSize(true) //Size of RV will not change
        // NOTE: Use the custom FadeInDownAnimator for ALL notifications for ALL items,
        // but ScrollableFooterItem implements AnimatedViewHolder with a unique animation: SlideInUp!
        mRecyclerView.itemAnimator = FadeInDownItemAnimator()

        loadLog()

        Observable
            .just(ConnectManager().token(requireActivity(), AppSettings.REFRESH_TOKEN))
            .delay(250, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                syncSettings()
                dialog.cancel()
            }
            .subscribe()

    }

    override fun onPause() {
        super.onPause()
        resetLog()
    }

    override fun onClick(v: View?) {
        if (v == titlebinding.inBtn) {
//            findNavController().navigate(R.id.action_title_to_detail)
            findNavController().navigate(R.id.detailScreen)

        }
        if (v == titlebinding.outBtn) {
            activity?.let {
                val intent = Intent(it, CheckoutScannerActivity::class.java)
                it.startActivity(intent)
            }
        }
    }

    private fun syncSettings() {
        val url: String = resources.getString(R.string.URL) + resources.getString(R.string.PORT)
        val apiname = resources.getString(R.string.API_Property)
        val userID = AppSettings.USER_ID
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val newRequest: Request = chain.request().newBuilder()
                .addHeader("X-Authorization", "Bearer ${AppSettings.ACCESS_TOKEN}")
                .build()
            chain.proceed(newRequest)
        }).build()

        val retrofit: Retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl("$url$apiname")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .getProperty(userID, AppSettings.ACCESS_TOKEN)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Settings> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Settings) {
                    try {
                        if (t.message?.visitPlace?.isNotEmpty() == true) {
                            AppSettings.visitPlace = t.message?.visitPlace!!
                        } else {
                            AppSettings.visitPlace = emptyList()
                        }
                        if (t.message?.vehicleType?.isNotEmpty() == true) {
                            AppSettings.vehicleType = t.message?.vehicleType!!
                        } else {
                            AppSettings.vehicleType = emptyList()
                        }
                        if (t.message?.licensePlate?.isNotEmpty() == true) {
                            AppSettings.licensePlate = t.message?.licensePlate!!
                        } else {
                            AppSettings.licensePlate = emptyList()
                        }
                        if (t.message?.visitorType?.isNotEmpty() == true) {
                            AppSettings.visitorType = t.message?.visitorType!!
                        } else {
                            AppSettings.visitorType = emptyList()
                        }
                        if (t.message?.contactTopic?.isNotEmpty() == true) {
                            AppSettings.contactTopic = t.message?.contactTopic!!
                        } else {
                            AppSettings.contactTopic = emptyList()
                        }
                        if (t.message?.whitelist?.isNotEmpty() == true) {
                            AppSettings.whitelist = t.message?.whitelist!!
                        } else {
                            AppSettings.whitelist = emptyList()
                        }
                        if (t.message?.visitFrom?.isNotEmpty() == true) {
                            AppSettings.visitFrom = t.message?.visitFrom!!
                        } else {
                            AppSettings.visitFrom = emptyList()
                        }
                        if (t.message?.visitPerson?.isNotEmpty() == true) {
                            AppSettings.visitPerson = t.message?.visitPerson!!
                        } else {
                            AppSettings.visitPerson = emptyList()
                        }
                        if (t.message?.department?.isNotEmpty() == true) {
                            AppSettings.department = t.message?.department!!
                        } else {
                            AppSettings.department = emptyList()
                        }
                        if (t.message?.blacklist?.isNotEmpty() == true) {
                            AppSettings.blacklist = t.message?.blacklist!!
                        } else {
                            AppSettings.blacklist = emptyList()
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

    @SuppressLint("SimpleDateFormat", "CheckResult")
    fun loadLog() {

        Log.d("ConnectManager", "loadlog")
        val url: String =
            getString(R.string.URL) + getString(R.string.PORT)
        val apigetlog = getString(R.string.API_Getlog)
        val apigetinfo = getString(R.string.API_Getinfo)
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

        val observable1: Observable<RetrofitData.VisitorDetail> =
            retrofit.create(API::class.java)
                .getLog(
                    apigetinfo,
                    userID,
                    false,
                    currentDateandTime,
                    1,
                    100,
                    -1,
                    AppSettings.ACCESS_TOKEN
                )
        val observable2: Observable<RetrofitData.VisitorDetail> =
            retrofit.create(API::class.java)
                .getLog(
                    apigetlog,
                    userID,
                    false,
                    currentDateandTime,
                    1,
                    100,
                    -1,
                    AppSettings.ACCESS_TOKEN
                )

        val observable3: Observable<RetrofitData.VisitorDetail> =
            retrofit.create(API::class.java)
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

//                    logList?.value = getLogList(enterList, outList, remainList)
                    val newItems: MutableList<AbstractFlexibleItem<*>?> = ArrayList()

                    var entercount = 0
                    var outcount = 0
                    var remaincount = 0

                    for (i in AppSettings.visitorType.indices) {

                        entercount += enterList[i]
                        outcount += outList[i]
                        remaincount += remainList[i]
                        val totalItemsOfType = mAdapter!!.getItemCountOfTypes(R.layout.home_title_item)

                        newItems.add(
                            TitleItem(
                                (totalItemsOfType + i).toString(),
                                AppSettings.visitorType[i],
                                enterList[i].toString(),
                                outList[i].toString(),
                                remainList[i].toString()
                            )
                        )
                    }
                    val totalItemsOfType = mAdapter!!.getItemCountOfTypes(R.layout.home_title_item)
                    if (AppSettings.visitorType.isNotEmpty()) {
                        newItems.add(
                            TitleItem(
                                (totalItemsOfType + 1).toString(),
                                "รวม",
                                entercount.toString(),
                                outcount.toString(),
                                remaincount.toString()
                            )
                        )
                    }


                    mAdapter!!.onLoadMoreComplete(
                        newItems as List<IFlexible<*>>?,
                        if (newItems.isEmpty()) -1 else 3000L
                    )

                    titlebinding.titleprogress.visibility = View.GONE

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
                                strError = getString(R.string.error_emptytext)
                            }
                            if (strError == "Unauthorized") {
                                PublicFunction().tokenError(requireActivity())
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

    fun resetLog() {
//        logList?.value?.clear()
    }


}
