package com.wacinfo.visitorcloud.Adapter

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.wacinfo.visitorcloud.Adapter.LogItem.MyViewHolder
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.ui.dialog.AboutDialog
import com.wacinfo.visitorcloud.utils.PublicFunction
import com.wacinfo.visitorcloud.utils.RetrofitData
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFilterable
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.utils.FlexibleUtils
import eu.davidea.viewholders.FlexibleViewHolder
import java.util.*

class LogItem /*(1)*/(
    private val mContext: FragmentManager,
    private val id: String,
    private val result: RetrofitData.VisitorDetail
) : AbstractFlexibleItem<MyViewHolder>(), IFilterable<String> {
    /**
     * When an item is equals to another?
     * Write your own concept of equals, mandatory to implement or use
     * default java implementation (return this == o;) if you don't have unique IDs!
     * This will be explained in the "Item interfaces" Wiki page.
     */
    override fun equals(inObject: Any?): Boolean {
        if (inObject is LogItem) {
            return id == inObject.id
        }
        return false
    }

    /**
     * You should implement also this method if equals() is implemented.
     * This method, if implemented, has several implications that Adapter handles better:
     * - The Hash, increases performance in big list during Update & Filter operations.
     * - You might want to activate stable ids via Constructor for RV, if your id
     * is unique (read more in the wiki page: "Setting Up Advanced") you will benefit
     * of the animations also if notifyDataSetChanged() is invoked.
     */
    override fun hashCode(): Int {
        return id.hashCode()
    }

    /**
     * For the item type we need an int value: the layoutResID is sufficient.
     */
    override fun getLayoutRes(): Int {
        return R.layout.log_item
    }

    /**
     * Delegates the creation of the ViewHolder to the user (AutoMap).
     * The inflated view is already provided as well as the Adapter.
     */
    override fun createViewHolder(
        view: View,
        adapter: FlexibleAdapter<IFlexible<*>?>?
    ): MyViewHolder {
        return MyViewHolder(view, adapter)
    }

    /**
     * The Adapter and the Payload are provided to perform and get more specific
     * information.
     */
    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<*>?>?, holder: MyViewHolder,
        position: Int,
        payloads: List<Any>
    ) {


        var timein = ""
        result.recordTimeIn?.let {
            timein = if (it == "") {
                "-"
            } else {
                PublicFunction().convertToNewFormat(it)
                    .toString()
            }
        }
        var timeout = ""
        result.recordTimeOut?.let {
            timeout = if (it == "") {
                "-"
            } else {
                PublicFunction().convertToNewFormat(it)
                    .toString()
            }
        }

        holder.mTitle.text = result.visitorNumber
        holder.mTitle.isEnabled = isEnabled
        holder.mSubtitle.text = result.visitorType
        holder.mIntime.text = "เข้า : $timein"
        holder.mOuttime.text = "ออก : $timeout"
        holder.container.setOnClickListener {
            AboutDialog.newInstance()
                .apply {
                    visitorNumber = result.visitorNumber.toString()
                    visitorType = result.visitorType.toString()
                    visitorName = result.name.toString()
                    visitorCid = result.citizenId.toString()
                    vehicleType =
                        result.vehicleType.toString()
                    licensePlate =
                        result.licensePlate.toString()
                    terminalin =
                        result.terminalIn.toString()
                    terminal_out =
                        result.terminalOut.toString()
                    visitorTimeIn =
                        timein
                    visitorTimeOut =
                        timeout
                    if(result.image1?.isNotEmpty()!!){
                        imgCardUrl = result.image1?.get(0).toString()
                    }
                    if(result.image2?.isNotEmpty()!!){
                        imgCamUrl = result.image2?.get(0).toString()
                    }

                    onCloseClickListener = {
                    }

                }.run {
                    show(mContext, "")
                }
        }
    }

    /**
     * The ViewHolder used by this item.
     * Extending from FlexibleViewHolder is recommended especially when you will use
     * more advanced features.
     */
    class MyViewHolder(view: View, adapter: FlexibleAdapter<*>?) :
        FlexibleViewHolder(view, adapter) {
        var mTitle: TextView = view.findViewById(R.id.title)
        var mSubtitle: TextView = view.findViewById(R.id.subtitle)
        var container: FrameLayout = view.findViewById(R.id.container)
        var mIntime: TextView = view.findViewById(R.id.in_tv)
        var mOuttime: TextView = view.findViewById(R.id.out_tv)

    }

    private fun getTitle(): String {
        return result.visitorNumber.toString()
    }
    private fun getSubTitle(): String {
        return result.visitorType.toString()
    }

    override fun filter(constraint: String?): Boolean {
        for (word in constraint!!.split(FlexibleUtils.SPLIT_EXPRESSION).toTypedArray()) {
            if (getTitle().toLowerCase(Locale.ROOT).contains(word)) {
                return true
            }
            if (getSubTitle().toLowerCase(Locale.ROOT).contains(word)) {
                return true
            }
        }
        return false
    }
}