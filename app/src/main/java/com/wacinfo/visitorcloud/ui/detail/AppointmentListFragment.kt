package com.wacinfo.visitorcloud.ui.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.wacinfo.visitorcloud.Adapter.AbstractFragment
import com.wacinfo.visitorcloud.Adapter.AppointmentItem
import com.wacinfo.visitorcloud.Adapter.ProgressItem
import com.wacinfo.visitorcloud.MainActivity
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.AppointmentListFragmentBinding
import com.wacinfo.visitorcloud.model.SharedViewModel
import com.wacinfo.visitorcloud.ui.dialog.SlipDialog
import com.wacinfo.visitorcloud.utils.FadeInDownItemAnimator
import eu.davidea.fastscroller.FastScroller
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.SelectableAdapter
import eu.davidea.flexibleadapter.helpers.EmptyViewHelper
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import java.util.*


class AppointmentListFragment : AbstractFragment(), FlexibleAdapter.EndlessScrollListener,
    FlexibleAdapter.OnItemClickListener {

    companion object {
        fun newInstance() = AppointmentListFragment()
    }

    private lateinit var viewModel: AppointmentListViewModel
    private lateinit var sharedViewModel: SharedViewModel
    lateinit var binding: AppointmentListFragmentBinding
    private var mAdapter: FlexibleAdapter<IFlexible<*>>? = null
    private val mProgressItem: ProgressItem = ProgressItem()
    private var mActivatedPosition = 0
    private val PAGE_LIMIT = 10
    private var dialog: SlipDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.appointment_list_fragment, container, false)
        binding = AppointmentListFragmentBinding.bind(root)

        binding.nextBtn.setOnClickListener {
            Log.d(TAG, "onActivityCreated: $dialog")
            dialog?.show(
                requireActivity().supportFragmentManager,
                "Bottom Sheet Dialog Fragment"
            )
        }
        val toolbar: Toolbar = activity?.findViewById<View>(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.appointment)
        toolbar.setNavigationIcon(R.drawable.ic_back_white)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AppointmentListViewModel::class.java)
        sharedViewModel = activity?.run {
            ViewModelProviders.of(this)[SharedViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        sharedViewModel.slipDialog.observe(viewLifecycleOwner, {
            dialog = it
        })
        initializeRecyclerView(savedInstanceState)
//        initializeSwipeToRefresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mAdapter!!.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            // Selection
            mAdapter!!.onRestoreInstanceState(savedInstanceState)
            if (mAdapter!!.selectedItemCount > 0) {
//                mActionMode = startSupportActionMode(this)
                setContextTitle(mAdapter!!.selectedItemCount)
            }
        }
    }


    override fun onItemClick(view: View?, position: Int): Boolean {
        Log.d(TAG, "onItemClick: $position")
        dialog?.show(
            requireActivity().supportFragmentManager,
            "ITEM Dialog Fragment $position"
        )
//        if (position != mActivatedPosition) setActivatedPosition(position);

        return true; //Important!
    }

    private fun setActivatedPosition(position: Int) {
        Log.d(TAG, "setActivatedPosition: $position")
        mActivatedPosition = position
        mAdapter!!.toggleSelection(position) //Important!
        Log.d(TAG, "setActivatedPosition: count ${mAdapter?.getItem(position)}")

//        MaterialDialog(requireContext()).show {
//            title(R.string.appointment)
//            message(
//                text = "${getString(R.string.name_th)} : ${binding.nameEdt.text}\n" +
//                        "${getString(R.string.txt_license_plate)} : ${binding.licenseplateEdt.text}\n" +
//                        "${getString(R.string.txt_visitor_place)} : ${binding.placeEdt.text}\n" +
//                        "${getString(R.string.txt_appointment_time)} : 01/03/2021 10:30"
//            )
//            positiveButton(R.string.next) {
//                val bottomSheetDialog = SlipDialog.newInstance().apply {
//                    visitorType = binding.typeEdt.text.toString()
//                    visitorNumber = binding.vidEdt.text.toString()
//                    name = binding.nameEdt.text.toString()
//                    cid = binding.cidEdt.text.toString()
//                    place = binding.placeEdt.text.toString()
//                    vehicle = binding.vehicleEdt.text.toString()
//                    licenseplate = binding.licenseplateEdt.text.toString()
//                    cardURI = CardURI
//                    camURI = CamURI
//                    onCloseListener = {
//                        requireActivity().onBackPressed()
//                    }
//                }
//                bottomSheetDialog.show(
//                    requireActivity().supportFragmentManager,
//                    "Bottom Sheet Dialog Fragment"
//                )
//            }
//            lifecycleOwner(requireActivity())
//        }
    }


    private fun setContextTitle(count: Int) {
        Log.d(TAG, "setContextTitle: $count")
    }

    private fun initializeRecyclerView(savedInstanceState: Bundle?) {
        // Initialize Adapter and RecyclerView
        // ExampleAdapter makes use of stableIds, I strongly suggest to implement 'item.hashCode()'
        FlexibleAdapter.useTag("EndlessScrollingAdapter")

        mAdapter = FlexibleAdapter(null, activity)
        mAdapter!!.setNotifyChangeOfUnfilteredItems(true).mode = SelectableAdapter.Mode.SINGLE
        mAdapter!!.mode = SelectableAdapter.Mode.SINGLE
        mAdapter!!.setAutoScrollOnExpand(true) //.setAnimateToLimit(Integer.MAX_VALUE) //Use the default value
            .setNotifyMoveOfFilteredItems(true) //When true, filtering on big list is very slow, not in this case!
            .setNotifyChangeOfUnfilteredItems(true) //true by default
            .setAnimationOnForwardScrolling(true)
            .setAnimationOnReverseScrolling(true)
        mRecyclerView = binding.recyclerAppoingment
        mRecyclerView.layoutManager = createNewLinearLayoutManager()
        mRecyclerView.adapter = mAdapter
        mRecyclerView.setHasFixedSize(true) //Size of RV will not change
        mAdapter!!.addListener(this)
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

        // EndlessScrollListener - OnLoadMore (v5.0.0)
        mAdapter!!.setLoadingMoreAtStartUp(true) //To call only if the list is empty
            .setEndlessScrollListener(this, mProgressItem)
            .setEndlessScrollThreshold(20) //Default=1
            .setEndlessPageSize(PAGE_LIMIT)        // if newItems < 7
            .isTopEndless = false

//        viewModel.data_Total.observe(viewLifecycleOwner, {
//            Log.d("REPORT", "data_Total: ${it}")
//            mAdapter!!.endlessTargetCount = it.toInt()
//        })
    }

    override fun noMoreLoad(newItemsSize: Int) {
    }

    override fun onLoadMore(lastPosition: Int, currentPage: Int) {
        if (mAdapter!!.hasFilter()) {
            mAdapter!!.onLoadMoreComplete(null)
            return
        }
        val newItems: MutableList<AbstractFlexibleItem<*>?> = ArrayList()

        val totalItemsOfType = mAdapter!!.getItemCountOfTypes(R.layout.appointment_item)

        sharedViewModel.appointmentList.observe(viewLifecycleOwner, {
            Log.d(TAG, "onLoadMore: ${it.size}")
            for (i in 0 until it.size) {
                newItems.add(
                    AppointmentItem(
                        childFragmentManager,
                        (totalItemsOfType + i).toString(),
                        it[i]
                    )
                )
            }
        })

        mAdapter!!.onLoadMoreComplete(
            newItems as List<IFlexible<*>>?,
            if (newItems.isEmpty()) -1 else 3000L
        )
//                            viewModel.setPagging((viewModel.Pagging.value!!) + 1)

        for (item in newItems) {
            mAdapter!!.expand(item, true)
        }

    }


}