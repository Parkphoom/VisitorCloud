package com.wacinfo.visitorcloud.ui.Security

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.wacinfo.visitorcloud.Adapter.AbstractFragment
import com.wacinfo.visitorcloud.Adapter.ProgressItem
import com.wacinfo.visitorcloud.Adapter.BlackListItem
import com.wacinfo.visitorcloud.Adapter.WhiteListItem
import com.wacinfo.visitorcloud.MainActivity
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.SecurityFragmentBinding
import com.wacinfo.visitorcloud.utils.*
import eu.davidea.fastscroller.FastScroller
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.helpers.EmptyViewHelper
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
import java.util.*

class SecurityFragment : AbstractFragment() {

    companion object {
        fun newInstance() = SecurityFragment()

        private val PAGE_LIMIT = 10
    }

    private lateinit var viewModel: SecurityViewModel
    lateinit var binding: SecurityFragmentBinding

    private var mAdapter: FlexibleAdapter<IFlexible<*>>? = null
    private val mProgressItem: ProgressItem = ProgressItem()
    private var mSearchView: SearchView? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.security_fragment, container, false)
        binding = SecurityFragmentBinding.bind(root)


        return root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SecurityViewModel::class.java)
        viewModel.mContext = requireActivity()

        binding.listModeSwitch.setOnToggleSwitchChangeListener { position, isChecked ->
            Log.d(TAG, "initView: $position")
            initializeRecyclerView()
            initializeSwipeToRefresh()

        }
        binding.listModeSwitch.checkedTogglePosition = 0

    }


    /* ====================================
  * OPTION MENU PREPARATION & MANAGEMENT
  * ==================================== */
    override fun onPrepareOptionsMenu(menu: Menu) {
        Log.v(TAG, "onPrepareOptionsMenu called!")
        if (mSearchView != null) {
            //Has searchText?
            if (!mAdapter!!.hasFilter()) {
                Log.d(TAG, "onPrepareOptionsMenu Clearing SearchView!")
                mSearchView!!.isIconified = true // This also clears the text in SearchView widget
            } else {
                //Necessary after the restoreInstanceState
                menu.findItem(R.id.action_search).expandActionView() //must be called first
                //This restores the text, must be after the expandActionView()
                mSearchView!!.setQuery(
                    mAdapter!!.getFilter(String::class.java),
                    false
                ) //submit = false!!!
                mSearchView!!.clearFocus() //Optionally the keyboard can be closed
                //mSearchView.setIconified(false);//This is not necessary
            }
        }
        // Fast Scroller
        val fastScrollerItem = menu.findItem(R.id.fast_scroller)
        if (fastScrollerItem != null) {
            fastScrollerItem.isChecked = mAdapter!!.isFastScrollerEnabled
        }

        return super.onPrepareOptionsMenu(menu)
    }

    private fun initializeRecyclerView() {
        // Initialize Adapter and RecyclerView
        // ExampleAdapter makes use of stableIds, I strongly suggest to implement 'item.hashCode()'
        FlexibleAdapter.useTag("EndlessScrollingAdapter")

        mAdapter = FlexibleAdapter(null, activity)

        mAdapter!!.setAutoScrollOnExpand(true) //.setAnimateToLimit(Integer.MAX_VALUE) //Use the default value
            .setNotifyMoveOfFilteredItems(true) //When true, filtering on big list is very slow, not in this case!
            .setNotifyChangeOfUnfilteredItems(true) //true by default
            .setAnimationOnForwardScrolling(true)
            .setAnimationOnReverseScrolling(true)
        mRecyclerView = binding.recyclerSecuritylist
        mRecyclerView.layoutManager = createNewLinearLayoutManager()
        mRecyclerView.adapter = mAdapter
        mRecyclerView.setHasFixedSize(true) //Size of RV will not change
        // NOTE: Use the custom FadeInDownAnimator for ALL notifications for ALL items,
        // but ScrollableFooterItem implements AnimatedViewHolder with a unique animation: SlideInUp!
        mRecyclerView.itemAnimator = FadeInDownItemAnimator()

        // Add FastScroll to the RecyclerView, after the Adapter has been attached the RecyclerView!!!
        val fastScroller: FastScroller? = requireView().findViewById(R.id.fast_scroller)
        fastScroller?.addOnScrollStateChangeListener(activity as MainActivity?)
        mAdapter!!.fastScroller = fastScroller

        // New empty views handling, to set after FastScroller
        EmptyViewHelper.create(
            mAdapter,
            requireView().findViewById(R.id.empty_view),
            requireView().findViewById(R.id.filter_view)
        )

        val swipeRefreshLayout: SwipeRefreshLayout =
            requireView().findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.isEnabled = true

        loadBlacklist()
    }
    private fun initializeSwipeToRefresh() {
        // Swipe down to force synchronize
        mSwipeRefreshLayout = activity?.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout!!.setDistanceToTriggerSync(390)
        //mSwipeRefreshLayout.setEnabled(true); //Controlled by fragments!
        mSwipeRefreshLayout!!.setColorSchemeResources(
            android.R.color.holo_purple, android.R.color.holo_blue_light,
            android.R.color.holo_green_light, android.R.color.holo_orange_light
        )

        mSwipeRefreshLayout!!.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener { // Passing true as parameter we always animate the changes between the old and the new data set
            mAdapter?.clear()
            mRefreshHandler.sendEmptyMessage(REFRESH_START)
        })
    }
    /*
       * Operations for Handler.
       */
    private val REFRESH_STOP = 0
    private val REFRESH_STOP_WITH_UPDATE = 1
    private val REFRESH_START = 2

    private val mRefreshHandler = Handler(
        Looper.getMainLooper()
    ) { message ->
        when (message.what) {
            REFRESH_STOP_WITH_UPDATE -> {
                Log.d("REFRESH", "REFRESH_STOP_WITH_UPDATE")
                mSwipeRefreshLayout!!.isRefreshing = false
                true
            }
            REFRESH_STOP -> {
                Log.d("REFRESH", "REFRESH_STOP")
                mSwipeRefreshLayout!!.isRefreshing = false
                true
            }
            REFRESH_START -> {
                eu.davidea.flexibleadapter.utils.Log.d("REFRESH", "REFRESH_START")
                loadBlacklist()
                mSwipeRefreshLayout!!.isRefreshing = true
                true
            }
            else -> false
        }
    }

    private fun loadBlacklist() {
        if (mAdapter!!.hasFilter()) {
            mAdapter!!.onLoadMoreComplete(null)
            return
        }
        val newItems: MutableList<AbstractFlexibleItem<*>?> = ArrayList()
        // Simulating asynchronous call
        val url: String = getString(R.string.URL) + getString(R.string.PORT)
        val apigetblacklist = getString(R.string.API_Blacklist)
        val apigetwhitelist = getString(R.string.API_Whitelist)
        var apiname =""
        if(binding.listModeSwitch.checkedTogglePosition == 0){
            apiname = apigetblacklist
        }else if(binding.listModeSwitch.checkedTogglePosition == 1){
            apiname = apigetwhitelist
        }

        val userID = AppSettings.USER_ID

        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(requireActivity())).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url + apiname)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        val blacklistobserv:
                Observable<RetrofitData.Blacklist.Get> = retrofit.create(API::class.java)
            .getBlackList(
                userID
            )
        val whitelistobserv:
                Observable<RetrofitData.Whitelist.Get> = retrofit.create(API::class.java)
            .getWhiteList(
                userID
            )

        if(binding.listModeSwitch.checkedTogglePosition == 0){
            blacklistobserv
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<RetrofitData.Blacklist.Get> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(t: RetrofitData.Blacklist.Get) {
                        Log.d(TAG, "onNext: ${t.message?.blacklist?.size}")
                        if (t.message?.blacklist?.isNotEmpty()!!) {
                            val totalItemsOfType = mAdapter!!.getItemCountOfTypes(R.layout.security_item)
                            for (i in 0 until t.message?.blacklist?.size!!) {
                                for (j in t.message?.blacklist!![i].time!!.indices) {
                                    newItems.add(
                                        BlackListItem(
                                            requireContext(),
                                            (totalItemsOfType + i).toString(),
                                            t.message?.blacklist!![i].blacklistId.toString(),
                                            t.message?.blacklist!![i].name.toString(),
                                            t.message?.blacklist!![i].time!![j]
                                        )
                                    )
                                }
                            }

                            mAdapter!!.onLoadMoreComplete(
                                newItems as List<IFlexible<*>>?,
                                if (newItems.isEmpty()) -1 else 3000L
                            )

                            for (item in newItems) {
                                mAdapter!!.expand(item, true)
                            }

                        }
                        mRefreshHandler.sendEmptyMessage(REFRESH_STOP_WITH_UPDATE)
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
        }else if(binding.listModeSwitch.checkedTogglePosition == 1){
            whitelistobserv
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<RetrofitData.Whitelist.Get> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(t: RetrofitData.Whitelist.Get) {
                        Log.d(TAG, "onNext: ${t.message?.whitelist?.size}")
                        if (t.message?.whitelist?.isNotEmpty()!!) {
                            val totalItemsOfType =
                                mAdapter!!.getItemCountOfTypes(R.layout.security_item)
                            for (i in 0 until t.message?.whitelist?.size!!) {
                                for (j in t.message?.whitelist!![i].time!!.indices) {
                                    newItems.add(
                                        WhiteListItem(
                                            requireContext(),
                                            (totalItemsOfType + i).toString(),
                                            t.message?.whitelist!![i].whitelistId.toString(),
                                            t.message?.whitelist!![i].name.toString(),
                                            t.message?.whitelist!![i].time!![j]
                                        )
                                    )
                                }
                            }

                            mAdapter!!.onLoadMoreComplete(
                                newItems as List<IFlexible<*>>?,
                                if (newItems.isEmpty()) -1 else 3000L
                            )

                            for (item in newItems) {
                                mAdapter!!.expand(item, true)
                            }

                        }
                        mRefreshHandler.sendEmptyMessage(REFRESH_STOP_WITH_UPDATE)
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



    }


}