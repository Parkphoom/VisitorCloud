package com.wacinfo.visitorcloud.utils

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.RemoteException
import android.util.Log
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2
import com.sunmi.pay.hardware.aidlv2.tax.TaxOptV2
import com.sunmi.peripheral.printer.*
import com.wacinfo.visitorcloud.MainActivity
import sunmi.paylib.SunmiPayKernel
import java.util.*
import kotlin.concurrent.schedule

class PrintUtils(private var activity: Activity) : Application()  {

    private var mSMPayKernel: SunmiPayKernel? = null

    var TAG = "PrintUtilsLOGS"

    override fun onCreate() {
        super.onCreate()
        // initialization code here
    }


    companion object {
        var mBasicOptV2 // 获取基础操作模块
                : BasicOptV2? = null
        var mReadCardOptV2 // 获取读卡模块
                : ReadCardOptV2? = null
        var mPinPadOptV2 // 获取PinPad操作模块
                : PinPadOptV2? = null
        var mSecurityOptV2 // 获取安全操作模块
                : SecurityOptV2? = null
        var mEMVOptV2 // 获取EMV操作模块
                : EMVOptV2? = null
        var mTaxOptV2 // 获取税控操作模块
                : TaxOptV2? = null
        var sunmiPrinterService: SunmiPrinterService? = null

        var isDisConnectService = true
    }

    fun connectPayService() {
        try {
            mSMPayKernel = SunmiPayKernel.getInstance()
            mSMPayKernel?.initPaySDK(activity, mConnectCallback)
        } catch (e: Exception) {
            Log.d("SystemUtil", e.toString())
        }
    }

     fun bindPrintService() {
        try {
            InnerPrinterManager.getInstance()
                .bindService(activity, object : InnerPrinterCallback() {
                    override fun onConnected(service: SunmiPrinterService) {
                        sunmiPrinterService = service
                    }

                    override fun onDisconnected() {
                        sunmiPrinterService = null
                    }
                })
        } catch (e: InnerPrinterException) {
            e.printStackTrace()
        }
    }
    private var issucces: Boolean = true
    val innerResultCallbcak: InnerResultCallbcak = object : InnerResultCallbcak() {
        override fun onRunResult(isSuccess: Boolean) {
            Log.e("lxy", "isSuccess:$isSuccess")
            if (issucces) {
                try {
//                    sunmiPrinterService.printTextWithFont(
//                            printContent + "\n", "", 30, innerResultCallbcak);
                    sunmiPrinterService!!.lineWrap(3, this)
                    issucces = false
                    Log.d(TAG, "Finished")
                    Timer("Finished", false).schedule(2000) {
                        finishTask()
                    }
                } catch (e: RemoteException) {

                    e.printStackTrace()
                    Log.d(TAG, e.toString())
                }
            }
        }

        override fun onReturnString(result: String) {
            Log.e(TAG, "result:$result")
        }

        override fun onRaiseException(code: Int, msg: String) {
            Log.e(TAG, "code:$code,msg:$msg")
        }

        override fun onPrintResult(code: Int, msg: String) {
            Log.e(TAG, "code:$code,msg:$msg")
        }
    }

     val mConnectCallback: SunmiPayKernel.ConnectCallback = object :
        SunmiPayKernel.ConnectCallback {
        override fun onConnectPaySDK() {
            Log.e(TAG, "onConnectPaySDK")
            try {
                mEMVOptV2 = mSMPayKernel!!.mEMVOptV2
                mBasicOptV2 = mSMPayKernel!!.mBasicOptV2
                mPinPadOptV2 = mSMPayKernel!!.mPinPadOptV2
                mReadCardOptV2 = mSMPayKernel!!.mReadCardOptV2
                mSecurityOptV2 = mSMPayKernel!!.mSecurityOptV2
                mTaxOptV2 = mSMPayKernel!!.mTaxOptV2
                isDisConnectService = false
                Log.d("SystemUtil", "connect success")
            } catch (e: Exception) {
                Log.d("SystemUtil", "connect fail")
                e.printStackTrace()
            }
        }

        override fun onDisconnectPaySDK() {
            Log.e(TAG, "onDisconnectPaySDK")
            Log.d("SystemUtil", "connect faillll")
            isDisConnectService = true
            //            showToast(R.string.connect_fail);
        }
    }



    private fun finishTask() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}