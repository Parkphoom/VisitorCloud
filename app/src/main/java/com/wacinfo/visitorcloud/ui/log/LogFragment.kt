package com.wacinfo.visitorcloud.ui.log

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.wacinfo.visitorcloud.Adapter.AbstractFragment
import com.wacinfo.visitorcloud.Adapter.LogItem
import com.wacinfo.visitorcloud.Adapter.ProgressItem
import com.wacinfo.visitorcloud.MainActivity
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.LogFragmentBinding
import com.wacinfo.visitorcloud.utils.*
import eu.davidea.fastscroller.FastScroller
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.SelectableAdapter
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
import java.text.SimpleDateFormat
import java.util.*


class LogFragment : AbstractFragment(), FlexibleAdapter.EndlessScrollListener,
    SearchView.OnQueryTextListener {

    val TAG: String = LogFragment::class.java.simpleName
    private val sdf = SimpleDateFormat("yyyyMMdd")
    private val currentDateandTime: String = sdf.format(Date())
    lateinit var binding: LogFragmentBinding
    private var mAdapter: FlexibleAdapter<IFlexible<*>>? = null
    private val mProgressItem: ProgressItem = ProgressItem()
    private var mSearchView: SearchView? = null
    private lateinit var viewModel: LogViewModel
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    private val PAGE_LIMIT = 10

    fun newInstance(columnCount: Int): LogFragment {
        val fragment: LogFragment = LogFragment()
        val args = Bundle()
        args.putInt(ARG_COLUMN_COUNT, columnCount)
        fragment.arguments = args
        return fragment
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initializeRecyclerView(savedInstanceState)
        initializeSwipeToRefresh()

    }

    override fun onResume() {
        super.onResume()
        viewModel.setPagging(1)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.log_fragment, container, false)
        binding = LogFragmentBinding.bind(root)
        viewModel = ViewModelProvider(requireActivity()).get(LogViewModel::class.java)
        viewModel.mContext = requireActivity()
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_endless, menu)
        initSearchView(menu)
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

    private fun initializeRecyclerView(savedInstanceState: Bundle?) {
        // Initialize Adapter and RecyclerView
        // ExampleAdapter makes use of stableIds, I strongly suggest to implement 'item.hashCode()'
        FlexibleAdapter.useTag("EndlessScrollingAdapter")

        mAdapter = FlexibleAdapter(null, activity)

        mAdapter!!.setAutoScrollOnExpand(true) //.setAnimateToLimit(Integer.MAX_VALUE) //Use the default value
            .setNotifyMoveOfFilteredItems(true) //When true, filtering on big list is very slow, not in this case!
            .setNotifyChangeOfUnfilteredItems(true) //true by default
            .setAnimationOnForwardScrolling(true)
            .setAnimationOnReverseScrolling(true)
        mRecyclerView = binding.recyclerView
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
//        mAdapter!!.setLongPressDragEnabled(true) //Enable long press to drag items
//            .setHandleDragEnabled(true).isSwipeEnabled = true //Enable swipe items

        val swipeRefreshLayout: SwipeRefreshLayout =
            requireView().findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.isEnabled = true
        mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, SelectableAdapter.Mode.IDLE)

        // EndlessScrollListener - OnLoadMore (v5.0.0)
        mAdapter!!.setLoadingMoreAtStartUp(true) //To call only if the list is empty
            .setEndlessScrollListener(this, mProgressItem)
            .setEndlessScrollThreshold(20) //Default=1
            .setEndlessPageSize(PAGE_LIMIT)        // if newItems < 7
            .isTopEndless = false

        viewModel.data_Total.observe(viewLifecycleOwner, {
            Log.d("REPORT", "data_Total: ${it}")
            mAdapter!!.endlessTargetCount = it.toInt()
        })
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

        mSwipeRefreshLayout!!.setOnRefreshListener(OnRefreshListener { // Passing true as parameter we always animate the changes between the old and the new data set
            viewModel.setPagging(1)
            mRefreshHandler.sendEmptyMessage(REFRESH_START)
//            mRefreshHandler.sendEmptyMessageDelayed(
//                REFRESH_STOP_WITH_UPDATE,
//                1500L
//            ) //Simulate network time
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

                true
            }
            REFRESH_STOP -> {
                Log.d("REFRESH", "REFRESH_STOP")
                mSwipeRefreshLayout!!.isRefreshing = false
                true
            }
            REFRESH_START -> {
                Log.d("REFRESH", "REFRESH_START")
                Log.d("ConnectManager", "loadLog: $currentDateandTime")
                val url: String =
                    getString(R.string.URL) + getString(R.string.PORT)
                val apigetlog = getString(R.string.API_Getlog)
                val apigetinfo = getString(R.string.API_Getinfo)
                val userID = AppSettings.USER_ID


                val client: OkHttpClient =
                    OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
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
                            true,
                            currentDateandTime,
                            1,
                            10,
                            -1,
                            AppSettings.ACCESS_TOKEN
                        )

                val observable2: Observable<RetrofitData.VisitorDetail> =
                    retrofit.create(API::class.java)
                        .getLog(
                            apigetlog,
                            userID,
                            true,
                            currentDateandTime,
                            1,
                            10,
                            -1,
                            AppSettings.ACCESS_TOKEN
                        )

                var enterList: MutableList<RetrofitData.VisitorDetail> = ArrayList()
                var total: Int = 0

                Observable
                    .merge(observable1, observable2)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<RetrofitData.VisitorDetail> {
                        override fun onComplete() {

                            if (enterList.isNotEmpty()) {
                                viewModel.setTotal(total.toString())
                                Log.d("result  REFRESH", "total $total")
                                Log.d("result  REFRESH", enterList.size.toString())
                                val result = enterList
                                val newItems: MutableList<AbstractFlexibleItem<*>?> = ArrayList()

                                mAdapter!!.setEndlessProgressItem(mProgressItem)

                                val totalItemsOfType =
                                    mAdapter!!.getItemCountOfTypes(R.layout.log_item)
                                if (viewModel.data_Total.value!!.toInt() > 0) {
                                    for (i in result.indices) {
                                        newItems.add(
                                            LogItem(
                                                childFragmentManager,
                                                (totalItemsOfType + i).toString(),
                                                result[i]
                                            )
                                        )
                                    }

                                    mAdapter!!.updateDataSet(
                                        newItems as List<IFlexible<*>>?,
                                        true
                                    )

                                    for (item in newItems) {
                                        // Option A. (Best use case) Initialization is performed:
                                        // - Expanded status is ignored. WARNING: possible subItems duplication!
                                        // - Automatic scroll is skipped
                                        mAdapter!!.expand(item, true)

                                        // Option B. Simple expansion is performed:
                                        // - WARNING: Automatic scroll is performed!
                                        //mAdapter.expand(item);
                                    }
                                    viewModel.setPagging(2)
                                    mSwipeRefreshLayout!!.isRefreshing = false
                                }
                            }
                        }

                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(t: RetrofitData.VisitorDetail) {
                            Log.d(TAG, "onNext: ${t.message?.total}")
                            if (t.message?.result?.isNotEmpty() == true) {
                                enterList.addAll(t.message?.result!!)
                                total += t.message?.total.toString().toInt()
                            }

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

//                retrofit.create<API>(API::class.java)
//                    .getReport(
//                        apigetinfo,
//                        userID,
//                        currentDateandTime,
//                        true,
//                        1,
//                        10,
//                        -1,
//                        "",
//                        AppSettings.ACCESS_TOKEN
//                    )
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    ?.subscribe(object : io.reactivex.Observer<RetrofitData.VisitorDetail> {
//                        override fun onComplete() {
//
//                        }
//
//                        override fun onSubscribe(d: Disposable) {
//
//                        }
//
//                        override fun onNext(t: RetrofitData.VisitorDetail) {
//                            if (t.message?.result?.isNotEmpty() == true) {
//                                viewModel.setTotal(t.message?.total)
//                                Log.d("result  REFRESH", "total ${t.message?.total}")
//                                Log.d("result  REFRESH", t.message?.result?.size.toString())
//                                val result = t.message?.result
//                                val newItems: MutableList<AbstractFlexibleItem<*>?> = ArrayList()
//
//                                mAdapter!!.setEndlessProgressItem(mProgressItem)
//
//                                val totalItemsOfType =
//                                    mAdapter!!.getItemCountOfTypes(R.layout.recycler_item)
//                                if (viewModel.data_Total.value!!.toInt() > 0) {
//                                    for (i in result!!.indices) {
//                                        newItems.add(
//                                            MyItem(
//                                                childFragmentManager,
//                                                (totalItemsOfType + i).toString(),
//                                                result[i]
//                                            )
//                                        )
//                                    }
//
//                                    mAdapter!!.updateDataSet(
//                                        newItems as List<IFlexible<*>>?,
//                                        true
//                                    )
//
//                                    for (item in newItems) {
//                                        // Option A. (Best use case) Initialization is performed:
//                                        // - Expanded status is ignored. WARNING: possible subItems duplication!
//                                        // - Automatic scroll is skipped
//                                        mAdapter!!.expand(item, true)
//
//                                        // Option B. Simple expansion is performed:
//                                        // - WARNING: Automatic scroll is performed!
//                                        //mAdapter.expand(item);
//                                    }
//                                    viewModel.setPagging(2)
//                                    mSwipeRefreshLayout!!.isRefreshing = false
//                                }
//                            }
//
//                        }
//
//                        override fun onError(e: Throwable) {
//                            var strError = ""
//                            strError = e.toString()
//                            if (e is HttpException) {
//                                try {
//                                    val jObjError = JSONObject(e.response()!!.errorBody()?.string())
//                                    strError = jObjError.getString("message")
//
//                                    if (strError == "Invalid input") {
//                                        strError = getString(R.string.error_emptytext)
//                                    }
//                                    if (strError == "Unauthorized") {
//                                        PublicFunction().tokenError(activity!!)
//                                    }
//                                } catch (e: Exception) {
//                                    e.message?.let {
//                                        strError = it
//                                    }
//
//                                }
//                            }
//
//
//                        }
//                    })


                mSwipeRefreshLayout!!.isRefreshing = true
                true
            }
            else -> false
        }
    }

    override fun noMoreLoad(newItemsSize: Int) {
        Log.d(
            TAG,
            "newItemsSize=$newItemsSize"
        )
        Log.d(
            TAG,
            "Total pages loaded=" + mAdapter!!.endlessCurrentPage
        )
        Log.d(
            TAG,
            "Total items loaded=" + mAdapter!!.mainItemCount
        )
    }

    override fun onLoadMore(lastPosition: Int, currentPage: Int) {
        // We don't want load more items when searching into the current Collection!
        // Alternatively, for a special filter, if we want load more items when filter is active, the
        // new items that arrive from remote, should be already filtered, before adding them to the Adapter!
        if (mAdapter!!.hasFilter()) {
            mAdapter!!.onLoadMoreComplete(null)
            return
        }
        val newItems: MutableList<AbstractFlexibleItem<*>?> = ArrayList()
        // Simulating asynchronous call
        val url: String = getString(R.string.URL) + getString(R.string.PORT)
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

        val observable1: Observable<RetrofitData.VisitorDetail> = retrofit.create(API::class.java)
            .getLog(
                apigetinfo,
                userID,
                true,
                currentDateandTime,
                viewModel.Pagging.value!!,
                10,
                -1,
                AppSettings.ACCESS_TOKEN
            )

        val observable2: Observable<RetrofitData.VisitorDetail> = retrofit.create(API::class.java)
            .getLog(
                apigetlog,
                userID,
                true,
                currentDateandTime,
                viewModel.Pagging.value!!,
                10,
                -1,
                AppSettings.ACCESS_TOKEN
            )


        var enterList: MutableList<RetrofitData.VisitorDetail> = ArrayList()
        var total = 0

        Observable
            .merge(observable1, observable2)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.VisitorDetail> {
                override fun onComplete() {
                    if (enterList.isNotEmpty()) {
                        viewModel.setTotal(total.toString())
                        Log.d(TAG, "total $total")
                        Log.d(TAG, enterList.size.toString())

                        val totalItemsOfType =
                            mAdapter!!.getItemCountOfTypes(R.layout.log_item)
                        if (viewModel.data_Total.value!!.toInt() > 0) {
                            for (i in 0 until enterList.size) {
                                newItems.add(
                                    LogItem(
                                        childFragmentManager,
                                        (totalItemsOfType + i).toString(),
                                        enterList[i]
                                    )
                                )
                            }

                            mAdapter!!.onLoadMoreComplete(
                                newItems as List<IFlexible<*>>?,
                                if (newItems.isEmpty()) -1 else 3000L
                            )
                            viewModel.setPagging((viewModel.Pagging.value!!) + 1)

                            for (item in newItems) {
                                // Option A. (Best use case) Initialization is performed:
                                // - Expanded status is ignored. WARNING: possible subItems duplication!
                                // - Automatic scroll is skipped
                                mAdapter!!.expand(item, true)

                                // Option B. Simple expansion is performed:
                                // - WARNING: Automatic scroll is performed!
                                //mAdapter.expand(item);
                            }
                        }

                    }
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.VisitorDetail) {
                    Log.d(TAG, "onNext: ${t.message?.total}")
                    if (t.message?.result?.isNotEmpty() == true) {
                        enterList.addAll(t.message?.result!!)
                        total += t.message?.total.toString().toInt()
                    }
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
//
//        Observable
//            .just(ConnectManager().token(requireActivity(), AppSettings.REFRESH_TOKEN))
//            .delay(300, TimeUnit.MILLISECONDS)
//            .subscribeOn(Schedulers.newThread())
//            .observeOn(AndroidSchedulers.mainThread())
//            .doOnComplete {
//                viewModel.loadLog(viewModel.Pagging.value!!)
//                    ?.subscribe(object : io.reactivex.Observer<RetrofitData.VisitorDetail> {
//                        override fun onComplete() {
//
//                        }
//
//                        override fun onSubscribe(d: Disposable) {
//
//                        }
//
//                        override fun onNext(t: RetrofitData.VisitorDetail) {
//                            if (t.message?.result?.isNotEmpty() == true) {
//                                viewModel.setTotal(t.message?.total)
//                                Log.d("result", "total ${t.message?.total}")
//                                Log.d("result", t.message?.result?.size.toString())
//                                val result = t.message?.result
//
//                                val totalItemsOfType =
//                                    mAdapter!!.getItemCountOfTypes(R.layout.recycler_item)
//                                if (viewModel.data_Total.value!!.toInt() > 0) {
//                                    for (i in 0 until result!!.size) {
//                                        newItems.add(
//                                            MyItem(
//                                                childFragmentManager,
//                                                (totalItemsOfType + i).toString(),
//                                                result[i]
//                                            )
//                                        )
//                                    }
//
//                                    mAdapter!!.onLoadMoreComplete(
//                                        newItems as List<IFlexible<*>>?,
//                                        if (newItems.isEmpty()) -1 else 3000L
//                                    )
//                                    viewModel.setPagging((viewModel.Pagging.value!!) + 1)
//
//                                    for (item in newItems) {
//                                        // Option A. (Best use case) Initialization is performed:
//                                        // - Expanded status is ignored. WARNING: possible subItems duplication!
//                                        // - Automatic scroll is skipped
//                                        mAdapter!!.expand(item, true)
//
//                                        // Option B. Simple expansion is performed:
//                                        // - WARNING: Automatic scroll is performed!
//                                        //mAdapter.expand(item);
//                                    }
//                                }
//
//                            }
//
//                        }
//
//                        override fun onError(e: Throwable) {
//                            var strError = ""
//                            strError = e.toString()
//                            if (e is HttpException) {
//                                try {
//                                    val jObjError = JSONObject(e.response()!!.errorBody()?.string())
//                                    strError = jObjError.getString("message")
//
//                                    if (strError == "Invalid input") {
//                                        strError = getString(R.string.error_emptytext)
//                                    }
//                                    if (strError == "Unauthorized") {
//                                        PublicFunction().tokenError(activity!!)
//                                    }
//                                } catch (e: Exception) {
//                                    e.message?.let {
//                                        strError = it
//                                    }
//
//                                }
//                            }
//
//
//                        }
//                    })
//            }
//            .subscribe()


    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return onQueryTextChange(query)
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (mAdapter!!.hasNewFilter(newText) == true) {
            Log.d(TAG, "onQueryTextChange newText: $newText")
            mAdapter!!.setFilter(newText)

            // Fill and Filter mItems with your custom list and automatically animate the changes
            // - Option A: Use the internal list as original list
            mAdapter!!.filterItems(300)

            // - Option B: Provide any new list to filter
//            mAdapter.filterItems(DatabaseService.getInstance().getDatabaseList(), DatabaseConfiguration.delay);
        }
        // Disable SwipeRefresh if search is active!!
        mSwipeRefreshLayout!!.isEnabled = !mAdapter!!.hasFilter()
        return true
    }

    /* ===========
     * SEARCH VIEW
     * =========== */
    fun initSearchView(menu: Menu) {
        // Associate searchable configuration with the SearchView
        Log.d(TAG, "onCreateOptionsMenu setup SearchView!")
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.action_search)
        if (searchItem != null) {

            mSearchView = MenuItemCompat.getActionView(searchItem) as SearchView
            mSearchView!!.inputType = InputType.TYPE_TEXT_VARIATION_FILTER
            mSearchView!!.imeOptions =
                EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_FULLSCREEN
            mSearchView!!.queryHint = getString(R.string.search)
            mSearchView!!.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            mSearchView!!.setOnQueryTextListener(this)
        }
    }

}