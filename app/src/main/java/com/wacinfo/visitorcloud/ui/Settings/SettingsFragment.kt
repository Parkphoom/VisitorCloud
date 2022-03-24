package com.wacinfo.visitorcloud.ui.Settings

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.SettingsFragmentBinding


class SettingsFragment : Fragment(), View.OnClickListener {
    private lateinit var dashboardBinding: SettingsFragmentBinding

    private lateinit var viewModel: SettingsViewModel
    private lateinit var TypelistViewModel: TypelistViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.settings_fragment, container, false)
        dashboardBinding = SettingsFragmentBinding.bind(root)
        dashboardBinding.visitorTypeLayout.setOnClickListener(this)
        dashboardBinding.visitorPlaceLayout.setOnClickListener(this)
        dashboardBinding.vehicleTypeLayout.setOnClickListener(this)
        dashboardBinding.licenseplateLayout.setOnClickListener(this)
        dashboardBinding.departmentLayout.setOnClickListener(this)
        dashboardBinding.contactTopicLayout.setOnClickListener(this)
        dashboardBinding.terminalInLayout.setOnClickListener(this)
        dashboardBinding.companyLayout.setOnClickListener(this)
        dashboardBinding.watermarkLayout.setOnClickListener(this)
        dashboardBinding.printDetailLayout.setOnClickListener(this)
        dashboardBinding.endpageLayout.setOnClickListener(this)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        viewModel.text.observe(viewLifecycleOwner, Observer {
//            dashboardBinding.text.text = it
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TypelistViewModel = ViewModelProviders.of(requireActivity())
            .get(com.wacinfo.visitorcloud.ui.Settings.TypelistViewModel::class.java)

    }

    override fun onClick(v: View?) {
        if (v == dashboardBinding.visitorTypeLayout) {
            TypelistViewModel.setPageType(TypelistViewModel.VISITOR_TYPE)
            findNavController().navigate(R.id.action_setting_to_list)
        }
        if (v == dashboardBinding.visitorPlaceLayout) {
            TypelistViewModel.setPageType(TypelistViewModel.VISITOR_PLACE)
            findNavController().navigate(R.id.action_setting_to_list)
        }
        if (v == dashboardBinding.vehicleTypeLayout) {
            TypelistViewModel.setPageType(TypelistViewModel.VEHICLE_TYPE)
            findNavController().navigate(R.id.action_setting_to_list)
        }
        if (v == dashboardBinding.licenseplateLayout) {
            TypelistViewModel.setPageType(TypelistViewModel.VEHICLE_LICENSE)
            findNavController().navigate(R.id.action_setting_to_list)
        }
        if (v == dashboardBinding.departmentLayout) {
            TypelistViewModel.setPageType(TypelistViewModel.VISITOR_DEPARTMENT)
            findNavController().navigate(R.id.action_setting_to_list)
        }
        if (v == dashboardBinding.contactTopicLayout) {
            TypelistViewModel.setPageType(TypelistViewModel.VISITOR_CONTACTTOPIC)
            findNavController().navigate(R.id.action_setting_to_list)
        }
        if (v == dashboardBinding.terminalInLayout) {

            MaterialDialog(requireContext()).show {
                title(R.string.txt_ternimal)
                input(
                    prefill = requireContext()
                        .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                        .getString(getString(R.string.Pref_Terminal), ""),
                    hint = getString(R.string.error_emptytext),
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                ) { _, text ->
                    requireContext()
                        .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                        .edit()
                        .putString(getString(R.string.Pref_Terminal), text.toString())
                        .apply()
                }
                positiveButton(R.string.save)
                negativeButton(R.string.cancel_th)
            }
        }
        if (v == dashboardBinding.companyLayout) {

            MaterialDialog(requireContext()).show {
                title(R.string.company)
                input(
                    prefill = requireContext()
                        .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                        .getString(getString(R.string.Pref_CompanyName), ""),
                    hint = getString(R.string.error_emptytext),
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                ) { _, text ->
                    requireContext()
                        .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                        .edit()
                        .putString(getString(R.string.Pref_CompanyName), text.toString())
                        .apply()
                }
                positiveButton(R.string.save)
                negativeButton(R.string.cancel_th)
            }
        }
        if (v == dashboardBinding.watermarkLayout) {

            MaterialDialog(requireContext()).show {
                title(R.string.txt_watermetk)
                input(
                    prefill = requireContext()
                        .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                        .getString(
                            getString(R.string.Pref_Watermark),
                            getString(R.string.app_name)
                        ),
                    hint = getString(R.string.error_emptytext),
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                ) { _, text ->
                    requireContext()
                        .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                        .edit()
                        .putString(getString(R.string.Pref_Watermark), text.toString())
                        .apply()
                }
                positiveButton(R.string.save)
                negativeButton(R.string.cancel_th)
            }

        }
        if (v == dashboardBinding.printDetailLayout) {
            val printSetting = arrayListOf(
                getString(R.string.qrCode),
                getString(R.string.txt_signature),
                getString(R.string.txt_endpage)
            )
            val printSettingList = ArrayList<Int>()
            val qrStatus = requireContext()
                .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                .getBoolean(
                    getString(R.string.Pref_Setting_QR), true
                )
            if (qrStatus) {
                printSettingList.add(0)
            }
            val signatureStatus = requireContext()
                .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                .getBoolean(
                    getString(R.string.Pref_Setting_SG), true
                )
            if (signatureStatus) {
                printSettingList.add(1)
            }
            val enapageStatus = requireContext()
                .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                .getBoolean(
                    getString(R.string.Pref_Setting_EP), true
                )
            if (enapageStatus) {
                printSettingList.add(2)
            }
            MaterialDialog(requireContext()).show {
                title(R.string.print_detail)
                listItemsMultiChoice(
                    items = printSetting,
                    initialSelection = printSettingList.toIntArray(), allowEmptySelection = true
                ) { _, index, text ->
                    if (index.indexOf(0) > -1) {

                        requireContext()
                            .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                            .edit()
                            .putBoolean(
                                getString(R.string.Pref_Setting_QR), true
                            ).apply()
                    } else if (index.indexOf(0) == -1) {

                        requireContext()
                            .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                            .edit()
                            .putBoolean(
                                getString(R.string.Pref_Setting_QR), false
                            ).apply()
                    }

                    if (index.indexOf(1) > -1) {

                        requireContext()
                            .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                            .edit()
                            .putBoolean(
                                getString(R.string.Pref_Setting_SG), true
                            ).apply()
                    } else if (index.indexOf(1) == -1) {

                        requireContext()
                            .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                            .edit()
                            .putBoolean(
                                getString(R.string.Pref_Setting_SG), false
                            ).apply()
                    }
                    if (index.indexOf(2) > -1) {

                        requireContext()
                            .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                            .edit()
                            .putBoolean(
                                getString(R.string.Pref_Setting_EP), true
                            ).apply()
                    } else if (index.indexOf(2) == -1) {

                        requireContext()
                            .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                            .edit()
                            .putBoolean(
                                getString(R.string.Pref_Setting_EP), false
                            ).apply()
                    }

                }
                positiveButton(R.string.save)
                negativeButton(R.string.cancel_th)
            }
        }
        if (v == dashboardBinding.endpageLayout) {

            MaterialDialog(requireContext()).show {
                title(R.string.txt_endpage)
                input(
                    prefill = requireContext()
                        .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                        .getString(
                            getString(R.string.Pref_Setting_EP_Text),
                            ""
                        ),
                    hint = getString(R.string.txt_endpage),
                    inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE),
                    waitForPositiveButton = false, allowEmpty = true
                ) { _, text ->
                    requireContext()
                        .getSharedPreferences(getString(R.string.SharePreferencesSetting), 0)
                        .edit()
                        .putString(getString(R.string.Pref_Setting_EP_Text), text.toString())
                        .apply()
                }
                positiveButton(R.string.save)
                negativeButton(R.string.cancel_th)
            }
        }

    }

}