package com.wacinfo.visitorcloud.Adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.evrencoskun.tableview.sort.SortState
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.holder.ImageCellViewHolder
import com.wacinfo.visitorcloud.model.Cell
import com.wacinfo.visitorcloud.model.ColumnHeader
import com.wacinfo.visitorcloud.model.RowHeader
import com.wacinfo.visitorcloud.ui.homescreen.AboutViewModel
import com.wacinfo.visitorcloud.utils.ConnectManager


class TableAboutAdapter(private val m_jContext: Activity) :
    AbstractTableAdapter<ColumnHeader?, RowHeader?, Cell?>() {

     lateinit var  picasso: Picasso

    // Cell View Types by Column Position
    private val CARD_CELL_TYPE = 1
    private val CAM_CELL_TYPE = 2

    /**
     * This is sample CellViewHolder class
     * This viewHolder must be extended from AbstractViewHolder class instead of RecyclerView.ViewHolder.
     */
    internal inner class MyCellViewHolder(itemView: View) : AbstractViewHolder(itemView) {
        val cell_textview: TextView = itemView.findViewById<View>(R.id.cell_data) as TextView

    }


    /**
     * This is where you create your custom Cell ViewHolder. This method is called when Cell
     * RecyclerView of the TableView needs a new RecyclerView.ViewHolder of the given type to
     * represent an item.
     *
     * @param viewType : This value comes from #getCellItemViewType method to support different type
     * of viewHolder as a Cell item.
     *
     * @see .getCellItemViewType
     * @return
     */
    override fun onCreateCellViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        // Get cell xml layout

        val inflater = LayoutInflater.from(parent.context)
        val layout: View

        return when (viewType) {
            CARD_CELL_TYPE -> {
                // Get image cell layout which has ImageView on the base instead of TextView.


                layout = inflater.inflate(R.layout.table_view_image_cell_layout, parent, false)
                ImageCellViewHolder(
                    layout
                )
            }
            CAM_CELL_TYPE -> {
                // Get image cell layout which has ImageView instead of TextView.

                layout = inflater.inflate(R.layout.table_view_image_cell_layout, parent, false)
                ImageCellViewHolder(
                    layout
                )
            }
            else -> {
                // For cells that display a text
                layout = LayoutInflater.from(m_jContext).inflate(
                    R.layout.cell_layout,
                    parent, false
                )

                // Create a Cell ViewHolder
                MyCellViewHolder(layout)
            }
        }

    }

    override fun onBindCellViewHolder(
        holder: AbstractViewHolder,
        cellItemModel: Cell?,
        columnPosition: Int,
        rowPosition: Int
    ) {
        // Get the holder to update cell item text

//         picasso = Picasso.Builder(m_jContext)
//            .downloader(OkHttp3Downloader(ConnectManager().authorizationHeader()))
//            .build()

        val url: String =
            m_jContext.getString(R.string.URL) + m_jContext.getString(R.string.PORT)
        val apiname = m_jContext.getString(R.string.API_GetImages)

        when (holder.itemViewType) {
            CARD_CELL_TYPE -> {
                val cardViewHolder: ImageCellViewHolder = holder as ImageCellViewHolder

                if (cellItemModel != null) {

                    val textURL =
                        "${url}$apiname${cellItemModel.data.toString()}?width=38&height=38&watermark=true&watermarkText=${
                            m_jContext.getString(R.string.app_name)
                        }"

                    m_jContext.runOnUiThread {
                        picasso
                            .load(textURL)
                            .into(cardViewHolder.cell_image)
                    }

                }

            }
            CAM_CELL_TYPE -> {
                val cardViewHolder: ImageCellViewHolder = holder as ImageCellViewHolder
                if (cellItemModel != null) {
                    val textURL =
                        "${url}$apiname${cellItemModel.data.toString()}?width=38&height=38&watermark=true&watermarkText=${
                            m_jContext.getString(R.string.app_name)
                        }"
                    m_jContext.runOnUiThread {
                        picasso
                            .load(textURL)
                            .into(cardViewHolder.cell_image)
                    }

                }


            }
            else -> {
                // Get the holder to update cell item text
                val viewHolder = holder as MyCellViewHolder

                viewHolder.cell_textview.text = cellItemModel!!.data.toString()

                viewHolder.itemView.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                viewHolder.cell_textview.requestLayout()
            }
        }
    }

    /**
     * This is sample CellViewHolder class.
     * This viewHolder must be extended from AbstractViewHolder class instead of RecyclerView.ViewHolder.
     */
    internal inner class MyColumnHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView) {
        val cell_textview: TextView
        val cell_container: LinearLayout

        init {
            cell_textview = itemView.findViewById<View>(R.id.column_header_textView) as TextView
            cell_container = itemView.findViewById<View>(R.id.column_header_container) as LinearLayout
        }
    }

    /**
     * This is where you create your custom Column Header ViewHolder. This method is called when
     * Column Header RecyclerView of the TableView needs a new RecyclerView.ViewHolder of the given
     * type to represent an item.
     *
     * @param viewType : This value comes from "getColumnHeaderItemViewType" method to support
     * different type of viewHolder as a Column Header item.
     *
     * @see .getColumnHeaderItemViewType
     * @return
     */
    override fun onCreateColumnHeaderViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder {

        // Get Column Header xml Layout
        val layout = LayoutInflater.from(m_jContext)
            .inflate(R.layout.table_view_column_header_layout, parent, false)

        // Create a ColumnHeader ViewHolder
        return MyColumnHeaderViewHolder(layout)
    }

    override fun onBindColumnHeaderViewHolder(
        holder: AbstractViewHolder,
        columnHeaderItemModel: ColumnHeader?,
        columnPosition: Int
    ) {

        // Get the holder to update cell item text
        val columnHeaderViewHolder = holder as MyColumnHeaderViewHolder
        columnHeaderViewHolder.cell_textview.text = columnHeaderItemModel!!.data.toString()

        // If your TableView should have auto resize for cells & columns.
        // Then you should consider the below lines. Otherwise, you can ignore them.

        // It is necessary to remeasure itself.
        columnHeaderViewHolder.cell_container.layoutParams.width =
            LinearLayout.LayoutParams.WRAP_CONTENT
        columnHeaderViewHolder.cell_textview.requestLayout()
    }

    /**
     * This is sample CellViewHolder class.
     * This viewHolder must be extended from AbstractViewHolder class instead of RecyclerView.ViewHolder.
     */
    internal inner class MyRowHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView) {
        val cell_textview: TextView

        init {
            cell_textview = itemView.findViewById<View>(R.id.row_header_textview) as TextView
        }

    }

    /**
     * This is where you create your custom Row Header ViewHolder. This method is called when
     * Row Header RecyclerView of the TableView needs a new RecyclerView.ViewHolder of the given
     * type to represent an item.
     *
     * @param viewType : This value comes from "getRowHeaderItemViewType" method to support
     * different type of viewHolder as a row Header item.
     *
     * @see .getRowHeaderItemViewType
     * @return
     */
    override fun onCreateRowHeaderViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {

        // Get Row Header xml Layout
        val layout = LayoutInflater.from(m_jContext)
            .inflate(R.layout.table_view_row_header_layout, parent, false)

        // Create a Row Header ViewHolder
        return MyRowHeaderViewHolder(layout)
    }

    override fun onBindRowHeaderViewHolder(
        holder: AbstractViewHolder,
        rowHeaderItemModel: RowHeader?,
        rowPosition: Int
    ) {

        // Get the holder to update row header item text
        val rowHeaderViewHolder = holder as MyRowHeaderViewHolder
        rowHeaderViewHolder.cell_textview.text = rowHeaderItemModel!!.data.toString()

    }

    override fun onCreateCornerView(parent: ViewGroup): View {

        val corner = LayoutInflater.from(parent.context)
            .inflate(R.layout.table_view_corner_layout, parent, false)

        corner.setOnClickListener { view: View? ->
            val sortState = this.tableView
                .rowHeaderSortingStatus
            if (sortState != SortState.ASCENDING) {
                Log.d("TableViewAdapter", "Order Ascending")
                this.tableView.sortRowHeader(SortState.ASCENDING)
            } else {
                Log.d("TableViewAdapter", "Order Descending")
                this.tableView.sortRowHeader(SortState.DESCENDING)
            }
        }

        return corner
    }

    override fun getColumnHeaderItemViewType(pXPosition: Int): Int {
        // The unique ID for this type of column header item
        // If you have different items for Cell View by X (Column) position,
        // then you should fill this method to be able create different
        // type of CellViewHolder on "onCreateCellViewHolder"
        return 0
    }

    override fun getRowHeaderItemViewType(pYPosition: Int): Int {
        // The unique ID for this type of row header item
        // If you have different items for Row Header View by Y (Row) position,
        // then you should fill this method to be able create different
        // type of RowHeaderViewHolder on "onCreateRowHeaderViewHolder"
        return 0
    }

    override fun getCellItemViewType(column: Int): Int {
        // The unique ID for this type of cell item
        // If you have different items for Cell View by X (Column) position,
        // then you should fill this method to be able create different
        // type of CellViewHolder on "onCreateCellViewHolder"
        return when (column) {
            AboutViewModel.IMG_CARD_COLUMN_INDEX -> CARD_CELL_TYPE
            AboutViewModel.IMG_CAM_COLUMN_INDEX -> CAM_CELL_TYPE
            else ->                 // Default view type
                0
        }
    }


}