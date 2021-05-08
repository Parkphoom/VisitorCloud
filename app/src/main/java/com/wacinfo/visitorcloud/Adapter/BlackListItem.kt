package com.wacinfo.visitorcloud.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
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

class BlackListItem /*(1)*/(
    private val mContext: Context,
    private val id: String,
    private val blacklistId: String,
    private val blacklistName: String,
    private val result: RetrofitData.Blacklist.Get.Message.list.Time
) : AbstractFlexibleItem<BlackListItem.MyViewHolder>(), IFilterable<String> {
    private val TAG: String = "SecurityTAG"

    /**
     * When an item is equals to another?
     * Write your own concept of equals, mandatory to implement or use
     * default java implementation (return this == o;) if you don't have unique IDs!
     * This will be explained in the "Item interfaces" Wiki page.
     */
    override fun equals(inObject: Any?): Boolean {
        if (inObject is BlackListItem) {
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
        return R.layout.security_item
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
    @SuppressLint("SetTextI18n")
    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<*>?>?, holder: MyViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        Log.d("bindViewHolder", position.toString())
        holder.mName.text = blacklistName
        holder.mStart.text =
            PublicFunction().convertToNewFormat(result.timeStart) + " - " + PublicFunction().convertToNewFormat(
                result.timeStop

            )
        holder.container.setOnClickListener {

        }

    }

    /**
     * The ViewHolder used by this item.
     * Extending from FlexibleViewHolder is recommended especially when you will use
     * more advanced features.
     */
    class MyViewHolder(view: View, adapter: FlexibleAdapter<*>?) :
        FlexibleViewHolder(view, adapter) {
        var mName: TextView = view.findViewById(R.id.name_tv)
        var mStart: TextView = view.findViewById(R.id.time_tv)
        var container: RelativeLayout = view.findViewById(R.id.container)
    }

    private fun getName(): String {
        return blacklistName
    }

    private fun getTime(): String {
        return "${result.timeStart.toString()}${result.timeStop.toString()}"
    }

    override fun filter(constraint: String?): Boolean {
        for (word in constraint!!.split(FlexibleUtils.SPLIT_EXPRESSION).toTypedArray()) {
            if (getName().toLowerCase(Locale.ROOT).contains(word)) {
                return true
            }
            if (getTime().toLowerCase(Locale.ROOT).contains(word)) {
                return true
            }
        }
        return false
    }



}