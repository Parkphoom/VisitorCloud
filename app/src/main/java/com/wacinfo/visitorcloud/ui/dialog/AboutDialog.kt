package com.wacinfo.visitorcloud.ui.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer
import com.stfalcon.imageviewer.loader.ImageLoader
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.DialogAboutBinding
import com.wacinfo.visitorcloud.utils.ConnectManager
import com.wacinfo.visitorcloud.utils.WatermarkTransformation


class AboutDialog : DialogFragment() {
    private val TAG = "AboutDialog"

    var onCloseClickListener: (() -> Unit)? = null
    var onOkClickListener: (() -> Unit)? = null
    private lateinit var binding: DialogAboutBinding

    var visitorNumber = ""
    var visitorType = ""
    var visitorName = ""
    var visitorCid = ""
    var visitorTimeIn = ""
    var visitorTimeOut = ""
    var vehicleType = ""
    var licensePlate = ""
    var terminalin = ""
    var terminal_out = ""
    var imgCardUrl = ""
    var imgCamUrl = ""

    companion object {
        fun newInstance(): AboutDialog = AboutDialog().apply {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.dialog_about, container, false)

        binding = DialogAboutBinding.bind(root)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initial()

        val picasso = Picasso.Builder(requireContext())
            .downloader(OkHttp3Downloader(ConnectManager().authorizationHeader()))
            .build()
        val url: String =
            getString(R.string.URL) + getString(R.string.PORT)
        val apiname = getString(R.string.API_GetImages)


        if(imgCardUrl.isNotEmpty()){
            picasso
                .load(
                    "${url}$apiname${imgCardUrl}?width=144&height=144"
                )
                .transform(WatermarkTransformation(requireContext(),getString(R.string.app_name)))
                .into(binding.imgTitle)

            binding.imgTitle.setOnClickListener {
                dialog?.context?.let { it1 ->
                    StfalconImageViewer.Builder(context,
                        ArrayList(
                            listOf(
                                "${url}$apiname${imgCardUrl}?width=400&height=400"
                            )
                        ),
                        ImageLoader { imageView, image ->
                            val picasso = Picasso.Builder(requireContext())
                                .downloader(OkHttp3Downloader(ConnectManager().authorizationHeader()))
                                .build()
                            picasso
                                .load(image)
                                .transform(WatermarkTransformation(requireContext(),getString(R.string.app_name)))
                                .into(imageView)
                        })
                        .show()
                }
            }
        }

        if(imgCamUrl.isNotEmpty()){
            picasso
                .load(
                    "${url}$apiname${imgCamUrl}?width=144&height=144"
                )
                .transform(WatermarkTransformation(requireContext(),getString(R.string.app_name)))
                .into(binding.imgCam)
            binding.imgCam.setOnClickListener {
                dialog?.context?.let { it1 ->
                    StfalconImageViewer.Builder(context,
                        ArrayList(
                            listOf(
                                "${url}$apiname${imgCamUrl}?width=400&height=400"
                            )
                        ),
                        ImageLoader { imageView, image ->
                            val picasso = Picasso.Builder(requireContext())
                                .downloader(OkHttp3Downloader(ConnectManager().authorizationHeader()))
                                .build()
                            picasso
                                .load(image)
                                .transform(WatermarkTransformation(requireContext(),getString(R.string.app_name)))
                                .into(imageView)
                        })
                        .show()
                }
            }
        }



    }


    @SuppressLint("SetTextI18n")
    private fun initial() {
//        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.tvTitle.text = getString(R.string.txt_vid) + " : " + visitorNumber

        binding.btnCancel.setOnClickListener {
            onCloseClickListener?.invoke()
            dialog?.dismiss()
        }

        binding.tvType.text = visitorType
        binding.tvTimeIn.text = visitorTimeIn
        binding.tvTimeOut.text = visitorTimeOut
        binding.tvName.text = visitorName
        binding.tvCitizenID.text = visitorCid
        binding.tvVehicleType.text = vehicleType
        binding.tvLicensePlate.text = licensePlate
        binding.tvTerminalIn.text = terminalin
        binding.tvTerminalOut.text = terminal_out

    }

}