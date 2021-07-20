package com.wacinfo.visitorcloud.Adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import com.wacinfo.visitorcloud.R
import com.wacinfo.visitorcloud.model.TypeItem
import com.wacinfo.visitorcloud.ui.dialog.EditDialog
import com.wacinfo.visitorcloud.ui.Settings.TypelistViewModel
import com.wacinfo.visitorcloud.utils.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class TypeListAdapter(
    context: Context,
    viewModel: TypelistViewModel,
    private val Dataset: List<TypeItem>
) :
    RecyclerView.Adapter<TypeListAdapter.ViewHolder>() {
    val mContext = context
    val mViewModel = viewModel
    private val TAG = "TypeListAdapterLOG"

    class ViewHolder(val item: View) : RecyclerView.ViewHolder(item)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        // create a new view
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_view_item, parent, false)


        return ViewHolder(itemView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val typename = Dataset[position].getName()

        holder.item.findViewById<TextView>(R.id.name_tv).text = typename

        holder.item.findViewById<RelativeLayout>(R.id.container).setOnClickListener {
            EditDialog.newInstance()
                .apply {
                    header = mContext.getString(R.string.edit)
                    oldvalue = typename.toString()
                    onCloseClickListener = {
                        syncSettings()
//                        Observable
//                            .just(ConnectManager().token(requireActivity(), AppSettings.REFRESH_TOKEN))
//                            .subscribeOn(Schedulers.newThread())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .doOnComplete {
//                                syncSettings()
//                            }
//                            .subscribe()

                    }

                }
                .run {
                    show(holder.item.findFragment<Fragment>().childFragmentManager, "")
                }

        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = Dataset.size


    private fun syncSettings() {
        val dialog= PublicFunction().retrofitDialog(mContext)
        dialog!!.show()

        val url: String =
            mContext.resources.getString(R.string.URL) + mContext.resources.getString(R.string.PORT)
        val apiname = mContext.resources.getString(R.string.API_Property)
        val userID = AppSettings.USER_ID
        val client: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(TokenInterceptor(mContext as Activity)).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("$url$apiname")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        retrofit.create(API::class.java)
            .getProperty(userID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RetrofitData.Settings> {
                override fun onComplete() {
                    when (mViewModel.PageType.value) {
                        mViewModel.VISITOR_TYPE -> {
                            mViewModel.setVisitorList(AppSettings.visitorType)
                        }
                        mViewModel.VISITOR_PLACE -> {
                            mViewModel.setVisitPlace(AppSettings.visitPlace)
                        }
                        mViewModel.VEHICLE_TYPE -> {
                            mViewModel.setVehicleType(AppSettings.vehicleType)
                        }
                        mViewModel.VEHICLE_LICENSE -> {
                            mViewModel.setLicensePlate(AppSettings.licensePlate)
                        }
                    }
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(t: RetrofitData.Settings) {
                    try {
                        if (t.message?.visitPlace?.isNotEmpty() == true) {
                            AppSettings.visitPlace = t.message?.visitPlace!!
                        } else {
                            AppSettings.visitPlace = emptyList<String>()
                        }
                        if (t.message?.vehicleType?.isNotEmpty() == true) {
                            AppSettings.vehicleType = t.message?.vehicleType!!
                        } else {
                            AppSettings.vehicleType = emptyList<String>()

                        }
                        if (t.message?.licensePlate?.isNotEmpty() == true) {
                            AppSettings.licensePlate = t.message?.licensePlate!!
                        } else {
                            AppSettings.licensePlate = emptyList<String>()
                        }
                        if (t.message?.visitorType?.isNotEmpty() == true) {
                            AppSettings.visitorType = t.message?.visitorType!!
                        } else {
                            AppSettings.visitorType = emptyList<String>()
                        }
                        if (t.message?.contactTopic?.isNotEmpty() == true) {
                            AppSettings.contactTopic = t.message?.contactTopic!!
                        } else {
                            AppSettings.contactTopic = emptyList<String>()
                        }
                        if (t.message?.whitelist?.isNotEmpty() == true) {
                            AppSettings.whitelist = t.message?.whitelist!!
                        } else {
                            AppSettings.whitelist = emptyList<RetrofitData.Settings.WhiteList>()
                        }
                        if (t.message?.visitFrom?.isNotEmpty() == true) {
                            AppSettings.visitFrom = t.message?.visitFrom!!
                        } else {
                            AppSettings.visitFrom = emptyList<String>()
                        }
                        if (t.message?.visitPerson?.isNotEmpty() == true) {
                            AppSettings.visitPerson = t.message?.visitPerson!!
                        } else {
                            AppSettings.visitPerson = emptyList<String>()
                        }
                        if (t.message?.department?.isNotEmpty() == true) {
                            AppSettings.department = t.message?.department!!
                        } else {
                            AppSettings.department = emptyList<String>()
                        }
                        if (t.message?.blacklist?.isNotEmpty() == true) {
                            AppSettings.blacklist = t.message?.blacklist!!
                        } else {
                            AppSettings.blacklist = emptyList<RetrofitData.Settings.BlackList>()
                        }

                        dialog.cancel()
                    } catch (e: NullPointerException) {
                        Log.d(TAG, "onNext: $e")
                    }

                }

                override fun onError(e: Throwable) {
                    PublicFunction().errorDialog(dialog).show()
                    e.printStackTrace()
                    if (e is HttpException) {
                        try {
                            val jObjError = JSONObject(e.response()!!.errorBody()?.string())
                            PublicFunction().message(
                                mContext as Activity,
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            PublicFunction().message(mContext as Activity, e.toString())
                        }
                    } else {
                        PublicFunction().message(mContext as Activity, e.toString())
                    }
                }
            })
    }
}
