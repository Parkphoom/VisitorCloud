package com.wacinfo.visitorcloud.Adapter

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.utils.PublicFunction
import com.wacinfo.visitorcloud.utils.RetrofitData
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFilterable
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.utils.FlexibleUtils
import eu.davidea.viewholders.FlexibleViewHolder
import java.util.*

class AppointmentItem /*(1)*/(
    private val mContext: FragmentManager,
    private val id: String,
    private val result: RetrofitData.Appointment.Get.Message.Result
) : AbstractFlexibleItem<AppointmentItem.MyViewHolder>(), IFilterable<String> {
    /**
     * When an item is equals to another?
     * Write your own concept of equals, mandatory to implement or use
     * default java implementation (return this == o;) if you don't have unique IDs!
     * This will be explained in the "Item interfaces" Wiki page.
     */
    override fun equals(inObject: Any?): Boolean {
        if (inObject is AppointmentItem) {
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
        return R.layout.appointment_item
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
        Log.d("bindViewHolder", position.toString())

//        var timein = ""
//        result.?.let {
//            timein = if (it == "") {
//                "-"
//            } else {
//                PublicFunction().convertToNewFormat(it)
//                    .toString()
//            }
//        }
//        var timeout = ""
//        result.recordTimeOut?.let {
//            timeout = if (it == "") {
//                "-"
//            } else {
//                PublicFunction().convertToNewFormat(it)
//                    .toString()
//            }
//        }
//        holder.itemView.setActivated(isSelected(position));

        holder.mName.text = result.name
        holder.mLicensePlate.text = result.licensePlate
        holder.mPlace.text = result.meetUpLocal
        holder.mTime.text = PublicFunction().convertToNewFormat(result.daysToCome)

//        holder.container.setOnClickListener {
//
//        }
    }

    /**
     * The ViewHolder used by this item.
     * Extending from FlexibleViewHolder is recommended especially when you will use
     * more advanced features.
     */
    class MyViewHolder(view: View, adapter: FlexibleAdapter<*>?) :
        FlexibleViewHolder(view, adapter) {
        var mName: TextView = view.findViewById(R.id.name_tv)
        var mLicensePlate: TextView = view.findViewById(R.id.licenseplate_tv)
        var mPlace: TextView = view.findViewById(R.id.place_tv)
        var mTime: TextView = view.findViewById(R.id.time_appointment_tv)
//        var container: FrameLayout = view.findViewById(R.id.container)
    }

    private fun getName(): String {
        return result.name.toString()
    }
    private fun getLicensePlate(): String {
        return result.licensePlate.toString()
    }


    override fun filter(constraint: String?): Boolean {
        for (word in constraint!!.split(FlexibleUtils.SPLIT_EXPRESSION).toTypedArray()) {
            if (getName().toLowerCase(Locale.ROOT).contains(word)) {
                return true
            }
            if (getLicensePlate().toLowerCase(Locale.ROOT).contains(word)) {
                return true
            }
        }
        return false
    }

}