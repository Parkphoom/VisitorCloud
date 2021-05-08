package com.wacinfo.visitorcloud.Adapter

import android.view.View
import android.widget.TextView
import androidx.fragment.app.findFragment
import androidx.navigation.fragment.findNavController
import com.wacinfo.visitorcloud.Adapter.TitleItem.TitleViewHolder
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.ui.homescreen.Title
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder

class TitleItem /*(1)*/(
    private val id: String, private val type: String,
    private val countin: String, private val countout: String, private val countremain: String
) :
    AbstractFlexibleItem<TitleViewHolder>() {
    /**
     * When an item is equals to another?
     * Write your own concept of equals, mandatory to implement or use
     * default java implementation (return this == o;) if you don't have unique IDs!
     * This will be explained in the "Item interfaces" Wiki page.
     */
    override fun equals(inObject: Any?): Boolean {
        if (inObject is TitleItem) {
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
        return R.layout.home_title_item
    }

    /**
     * Delegates the creation of the ViewHolder to the user (AutoMap).
     * The inflated view is already provided as well as the Adapter.
     */
    override fun createViewHolder(
        view: View,
        adapter: FlexibleAdapter<IFlexible<*>?>?
    ): TitleViewHolder {
        return TitleViewHolder(view, adapter)
    }

    /**
     * The Adapter and the Payload are provided to perform and get more specific
     * information.
     */
    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<*>?>?, holder: TitleViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        holder.mType.text = type
        holder.mType.isEnabled = isEnabled
        holder.mIn.text = countin
        holder.mOut.text = countout
        holder.mRemain.text = countremain

        holder.mIn.setOnClickListener {
            if (countin.toInt() > 0) {
                it.findFragment<Title>().aboutViewModel.setTypename(type)
                it.findFragment<Title>().aboutViewModel.setGroupname("เข้า")
                it.findFragment<Title>().findNavController().navigate(R.id.action_title_to_about)
            }

        }
        holder.mOut.setOnClickListener {
            if (countout.toInt() > 0) {
                it.findFragment<Title>().aboutViewModel.setTypename(type)
                it.findFragment<Title>().aboutViewModel.setGroupname("ออก")
                it.findFragment<Title>().findNavController().navigate(R.id.action_title_to_about)
            }

        }
        holder.mRemain.setOnClickListener {
            if (countremain.toInt() > 0) {
                it.findFragment<Title>().aboutViewModel.setTypename(type)
                it.findFragment<Title>().aboutViewModel.setGroupname("ตกค้าง")
                it.findFragment<Title>().findNavController().navigate(R.id.action_title_to_about)
            }

        }


    }

    /**
     * The ViewHolder used by this item.
     * Extending from FlexibleViewHolder is recommended especially when you will use
     * more advanced features.
     */
    inner class TitleViewHolder(view: View, adapter: FlexibleAdapter<*>?) :
        FlexibleViewHolder(view, adapter) {
        var mType: TextView
        var mIn: TextView
        var mOut: TextView
        var mRemain: TextView

        init {
            mType = view.findViewById<View>(R.id.typeName_tv) as TextView
            mIn = view.findViewById<View>(R.id.txtIn_tv) as TextView
            mOut = view.findViewById<View>(R.id.txtOut_tv) as TextView
            mRemain = view.findViewById<View>(R.id.txtRemain_tv) as TextView
        }
    }
}