package com.wacinfo.visitorcloud.Adapter

import android.animation.Animator
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wacinfo.visitorcloud.Adapter.ProgressItem.ProgressViewHolder
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.databinding.LogFragmentBinding
import com.wacinfo.visitorcloud.databinding.ProgressItemBinding
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.Payload
import eu.davidea.flexibleadapter.helpers.AnimatorHelper
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder

/**
 * @author Davide Steduto
 * @since 22/04/2016
 */
class ProgressItem : AbstractFlexibleItem<ProgressViewHolder>() {
    var status = StatusEnum.MORE_TO_LOAD
    override fun equals(o: Any?): Boolean {
        return this === o //The default implementation
    }

    override fun getLayoutRes(): Int {
        return R.layout.progress_item
    }

    override fun createViewHolder(
        view: View?,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?
    ): ProgressViewHolder? {
        return ProgressViewHolder(view, adapter)
    }

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?,
        holder: ProgressViewHolder?,
        position: Int,
        payloads: MutableList<Any>?
    ) {
        val context = holder!!.itemView.context
        holder.binding.progressBar.visibility = View.GONE
        holder.binding.progressMessage.visibility = View.VISIBLE
        if (!adapter!!.isEndlessScrollEnabled) {
            status = StatusEnum.DISABLE_ENDLESS
        } else if (payloads!!.contains(Payload.NO_MORE_LOAD)) {
            status = StatusEnum.NO_MORE_LOAD
        }
        when (status) {
            StatusEnum.NO_MORE_LOAD -> {
                holder.binding.progressMessage.text =
                    context.getString(R.string.no_more_load_retry)
                // Reset to default status for next binding
                status = StatusEnum.MORE_TO_LOAD
            }
            StatusEnum.DISABLE_ENDLESS -> holder.binding.progressMessage.text =
                context.getString(R.string.endless_disabled)
            StatusEnum.ON_CANCEL -> {
                holder.binding.progressMessage.text = context.getString(R.string.endless_cancel)
                // Reset to default status for next binding
                status = StatusEnum.MORE_TO_LOAD
            }
            StatusEnum.ON_ERROR -> {
                holder.binding.progressMessage.text = context.getString(R.string.endless_error)
                // Reset to default status for next binding
                status = StatusEnum.MORE_TO_LOAD
            }
            else -> {
                holder.binding.progressBar.visibility = View.VISIBLE
                holder.binding.progressMessage.visibility = View.GONE
            }
        }
    }

    class ProgressViewHolder(view: View?, adapter: FlexibleAdapter<*>?) :
        FlexibleViewHolder(view, adapter) {
        var binding: ProgressItemBinding = ProgressItemBinding.bind(view!!)
        override fun scrollAnimators(animators: List<Animator>, position: Int, isForward: Boolean) {
            AnimatorHelper.scaleAnimator(animators, itemView, 0f)
        }

    }

    enum class StatusEnum {
        MORE_TO_LOAD,  //Default = should have an empty Payload
        DISABLE_ENDLESS,  //Endless is disabled because user has set limits
        NO_MORE_LOAD,  //Non-empty Payload = Payload.NO_MORE_LOAD
        ON_CANCEL, ON_ERROR
    }
}