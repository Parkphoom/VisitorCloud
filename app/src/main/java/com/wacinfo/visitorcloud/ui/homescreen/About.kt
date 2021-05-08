/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wacinfo.visitorcloud.ui.homescreen

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.evrencoskun.tableview.filter.Filter
import com.evrencoskun.tableview.pagination.Pagination
import com.evrencoskun.tableview.pagination.Pagination.OnTableViewPageTurnedListener
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.wacinfo.visitorcloud.Adapter.TableAboutAdapter
import com.wacinfo.visitorcloud.Adapter.TableViewListener
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.FragmentAboutBinding
import com.wacinfo.visitorcloud.model.Cell
import com.wacinfo.visitorcloud.utils.ConnectManager
import com.wacinfo.visitorcloud.utils.PublicFunction


/**
 * Shows "About"
 */
class About : Fragment(), View.OnClickListener {
    lateinit var viewModel: AboutViewModel
    private lateinit var binding: FragmentAboutBinding
    private var mPagination: Pagination? = null
    private var mTableFilter: Filter? = null // This is used for filtering the table.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_about, container, false)
        binding = FragmentAboutBinding.bind(root)

        viewModel = ViewModelProvider(requireActivity()).get(AboutViewModel::class.java)
        viewModel.context = requireActivity()
        initializeView()
        initializeTableView()

        val toolbar: Toolbar = activity?.findViewById<View>(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back_white)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        return root
    }

    private fun initializeView() {
        binding.nextButton.setOnClickListener(this)
        binding.previousButton.setOnClickListener(this)
        binding.pageNumberText.addTextChangedListener(onPageTextChanged)
    }


    override fun onStart() {
        super.onStart()
        viewModel.Type.observe(viewLifecycleOwner, {
            binding.aboutTv.text = it
        })
        viewModel.Group.observe(viewLifecycleOwner, {
            binding.tvGroup.text = it
        })

    }

    override fun onPause() {
        super.onPause()
        viewModel.resetLog()
    }

    override fun onClick(view: View?) {
        if (view == binding.nextButton) {
            nextTablePage()
        }
        if (view == binding.previousButton) {
            previousTablePage()
        }
    }

    private fun initializeTableView() {
        val dialog = PublicFunction().retrofitDialog(requireContext())
        dialog!!.show()
        viewModel.getLogs()!!.observe(viewLifecycleOwner, {
            dialog.cancel()
            if (it.size == 0) {
                binding.tableabout.visibility = View.GONE
            } else {
                binding.tableabout.visibility = View.VISIBLE

                val tableViewAdapter =
                    TableAboutAdapter(requireActivity())
                tableViewAdapter.picasso = Picasso.Builder(requireActivity())
                    .downloader(OkHttp3Downloader(ConnectManager().authorizationHeader()))
                    .build()

                binding.tableabout.setAdapter(tableViewAdapter)
                binding.tableabout.tableViewListener = TableViewListener(
                    requireContext(),
                    binding.tableabout,
                    true
                )
                tableViewAdapter.setAllItems(
                    viewModel.columnHeaderList,
                    viewModel.rowHeaderList,
                    it as List<MutableList<Cell?>>?
                )
                mTableFilter = Filter(binding.tableabout)
                Log.d("Pagination", it.size.toString())
                mPagination = Pagination(binding.tableabout, 10)
                mPagination!!.setOnTableViewPageTurnedListener(onTableViewPageTurnedListener)
                binding.pageNumberText.hint = "1"

                binding.queryString.addTextChangedListener(mSearchTextWatcher)
            }
        })
    }


    fun filterTableForMood(filter: String) {
        // Sets a filter to the table, this will only filter a specific column.
        // In the example data, this will filter the mood column.
        mTableFilter?.set(AboutViewModel.IMG_CARD_COLUMN_INDEX, filter)
    }

    fun filterTableForGender(filter: String) {
        // Sets a filter to the table, this will only filter a specific column.
        // In the example data, this will filter the gender column.
        mTableFilter?.set(AboutViewModel.IMG_CAM_COLUMN_INDEX, filter)
    }

    private val mItemSelectionListener: OnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            // 0. index is for empty item of spinner.
            if (position > 0) {
                val filter = Integer.toString(position)
                filterTableForMood(filter)
                filterTableForGender(filter)
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // Left empty intentionally.
        }
    }

    private fun nextTablePage() {
        mPagination!!.nextPage()
    }

    private fun previousTablePage() {
        mPagination!!.previousPage()
    }

    fun goToTablePage(page: Int) {
        mPagination!!.goToPage(page)
    }

    fun setTableItemsPerPage(itemsPerPage: Int) {
        mPagination!!.itemsPerPage = itemsPerPage
    }

    fun filterTable(filter: String) {
        // Sets a filter to the table, this will filter ALL the columns.
        mTableFilter!!.set(filter)
    }


    private val onPageTextChanged: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val page: Int
            page = if (TextUtils.isEmpty(s)) {
                1
            } else {
                s.toString().toInt()
            }
            goToTablePage(page)
        }

        override fun afterTextChanged(s: Editable) {}
    }

    private val onTableViewPageTurnedListener =
        OnTableViewPageTurnedListener { numItems, itemsStart, itemsEnd ->
            val currentPage = mPagination!!.currentPage
            val pageCount = mPagination!!.pageCount
            binding.previousButton.visibility = View.VISIBLE
            binding.nextButton.visibility = View.VISIBLE

            if (currentPage == 1 && pageCount == 1) {
                binding.previousButton.visibility = View.INVISIBLE
                binding.nextButton.visibility = View.INVISIBLE
            }

            if (currentPage == 1) {
                binding.previousButton.visibility = View.INVISIBLE
            }

            if (currentPage == pageCount) {
                binding.nextButton.visibility = View.INVISIBLE
            }
            binding.pageNumberText.hint = currentPage.toString()
            viewModel.setPagging(currentPage)
        }

    private val mSearchTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            filterTable(s.toString())
        }

        override fun afterTextChanged(s: Editable) {}
    }

}


