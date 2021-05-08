package com.wacinfo.visitorcloud.ui.printslip

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wacinfo.visitorcloud.R

class PrintSlipFragment : Fragment() {

    companion object {
        fun newInstance() = PrintSlipFragment()
    }

    private lateinit var viewModel: PrintSlipViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.print_slip_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PrintSlipViewModel::class.java)
        // TODO: Use the ViewModel
    }

}