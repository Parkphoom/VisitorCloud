package com.wacinfo.visitorcloud.Adapter

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.listener.ITableViewListener
import com.wacinfo.visitorcloud.R
import androidx.navigation.fragment.findNavController
import com.wacinfo.visitorcloud.ui.dialog.AboutDialog
import com.wacinfo.visitorcloud.ui.homescreen.About
import com.wacinfo.visitorcloud.ui.homescreen.Title
import com.wacinfo.visitorcloud.utils.RetrofitData

/**
 * Created by evrencoskun on 21/09/2017.
 */
class TableViewListener(
    private val mContext: Context,
    private val mTableView: TableView,
    private val imageStatus: Boolean
) :
    ITableViewListener {
    /**
     * Called when user click any cell item.
     *
     * @param cellView : Clicked Cell ViewHolder.
     * @param column   : X (Column) position of Clicked Cell item.
     * @param row      : Y (Row) position of Clicked Cell item.
     */
    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {

        // Do what you want.
        if (!imageStatus) {
            val type = mTableView.findFragment<Title>().viewModel.rowHeaderList[row].data
            val group = mTableView.findFragment<Title>().viewModel.columnGroupList[column]

            mTableView.findFragment<Title>().aboutViewModel.setTypename(type.toString())
            mTableView.findFragment<Title>().aboutViewModel.setGroupname(group)
            mTableView.findFragment<Title>().findNavController()
                .navigate(R.id.action_title_to_about)

        } else {
            AboutDialog.newInstance()
                .apply {
//                    visitorNumber = mTableView.findFragment<About>().viewModel.cellList[row][0].data.toString()
                    var page = mTableView.findFragment<About>().viewModel.Pagging.value
                    if (page != null) {
                        page = if (page > 1) {
                            row + ((page - 1) * 10)
                        } else {
                            row
                        }
                    }
                    visitorNumber =
                        mTableView.findFragment<About>().viewModel.logList?.value?.get(page!!)
                            ?.get(0)?.data.toString()
                    visitorType =
                        mTableView.findFragment<About>().viewModel.rowHeaderList[page!!].data.toString()
                    visitorName =
                        mTableView.findFragment<About>().viewModel.result?.get(page)?.name.toString()
                    visitorCid =
                        mTableView.findFragment<About>().viewModel.result?.get(page)?.citizenId.toString()
                    vehicleType =
                        mTableView.findFragment<About>().viewModel.result?.get(page)?.vehicleType.toString()
                    licensePlate =
                        mTableView.findFragment<About>().viewModel.result?.get(page)?.licensePlate.toString()
                    terminalin =
                        mTableView.findFragment<About>().viewModel.result?.get(page)?.terminalIn.toString()
                    terminal_out =
                        mTableView.findFragment<About>().viewModel.result?.get(page)?.terminalOut.toString()
                    visitorTimeIn =
                        mTableView.findFragment<About>().viewModel.logList?.value?.get(page)
                            ?.get(1)?.data.toString()
                    visitorTimeOut =
                        mTableView.findFragment<About>().viewModel.logList?.value?.get(page)
                            ?.get(2)?.data.toString()
                    imgCardUrl =
                        mTableView.findFragment<About>().viewModel.logList?.value?.get(page)
                            ?.get(3)?.data.toString()
                    imgCamUrl =
                        mTableView.findFragment<About>().viewModel.logList?.value?.get(page)
                            ?.get(4)?.data.toString()

                    onCloseClickListener = {
                    }

                }.run {
                    show(mTableView.findFragment<About>().childFragmentManager, "")
                }
        }


    }

    /**
     * Called when user double click any cell item.
     *
     * @param cellView : Clicked Cell ViewHolder.
     * @param column   : X (Column) position of Clicked Cell item.
     * @param row      : Y (Row) position of Clicked Cell item.
     */
    override fun onCellDoubleClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        // Do what you want.
//        Log.d("Cell ", "onCellDoubleClicked: ")
//        showToast("Cell $column $row has been double clicked.")
    }

    /**
     * Called when user long press any cell item.
     *
     * @param cellView : Long Pressed Cell ViewHolder.
     * @param column   : X (Column) position of Long Pressed Cell item.
     * @param row      : Y (Row) position of Long Pressed Cell item.
     */
    override fun onCellLongPressed(
        cellView: RecyclerView.ViewHolder, column: Int,
        row: Int
    ) {
        // Do What you want
//        showToast("Cell $column $row has been long pressed.")
    }

    /**
     * Called when user click any column header item.
     *
     * @param columnHeaderView : Clicked Column Header ViewHolder.
     * @param column           : X (Column) position of Clicked Column Header item.
     */
    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {
        // Do what you want.
//        showToast("Column header  $column has been clicked.")
    }

    /**
     * Called when user double click any column header item.
     *
     * @param columnHeaderView : Clicked Column Header ViewHolder.
     * @param column           : X (Column) position of Clicked Column Header item.
     */
    override fun onColumnHeaderDoubleClicked(
        columnHeaderView: RecyclerView.ViewHolder,
        column: Int
    ) {
        // Do what you want.
//        showToast("Column header  $column has been double clicked.")
    }

    /**
     * Called when user long press any column header item.
     *
     * @param columnHeaderView : Long Pressed Column Header ViewHolder.
     * @param column           : X (Column) position of Long Pressed Column Header item.
     */
    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {

//        if (columnHeaderView instanceof ColumnHeaderViewHolder) {
//            // Create Long Press Popup
//            ColumnHeaderLongPressPopup popup = new ColumnHeaderLongPressPopup(
//                    (ColumnHeaderViewHolder) columnHeaderView, mTableView);
//            // Show
//            popup.show();
//        }
    }

    /**
     * Called when user click any Row Header item.
     *
     * @param rowHeaderView : Clicked Row Header ViewHolder.
     * @param row           : Y (Row) position of Clicked Row Header item.
     */
    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {
        // Do whatever you want.
//        showToast("Row header $row has been clicked.")
    }

    /**
     * Called when user double click any Row Header item.
     *
     * @param rowHeaderView : Clicked Row Header ViewHolder.
     * @param row           : Y (Row) position of Clicked Row Header item.
     */
    override fun onRowHeaderDoubleClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {
        // Do whatever you want.
//        showToast("Row header $row has been double clicked.")
    }

    /**
     * Called when user long press any row header item.
     *
     * @param rowHeaderView : Long Pressed Row Header ViewHolder.
     * @param row           : Y (Row) position of Long Pressed Row Header item.
     */
    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {

//        // Create Long Press Popup
//        RowHeaderLongPressPopup popup = new RowHeaderLongPressPopup(rowHeaderView, mTableView);
//        // Show
//        popup.show();
    }

    private fun showToast(p_strMessage: String) {
        Toast.makeText(mContext, p_strMessage, Toast.LENGTH_SHORT).show()
    }
}