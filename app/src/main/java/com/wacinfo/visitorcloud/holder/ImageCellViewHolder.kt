package com.wacinfo.visitorcloud.holder

import android.view.View
import android.widget.ImageView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.wacinfo.visitorcloud.R

class ImageCellViewHolder(itemView: View) : AbstractViewHolder(itemView) {
    val cell_image: ImageView

    init {
        cell_image = itemView.findViewById(R.id.cell_image)
    }
}