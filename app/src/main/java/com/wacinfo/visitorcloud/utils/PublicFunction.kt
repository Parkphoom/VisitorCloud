package com.wacinfo.visitorcloud.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.afollestad.materialdialogs.MaterialDialog
import com.github.dhaval2404.imagepicker.ImagePicker
import com.wacinfo.visitorcloud.Login.LoginActivity
import com.wacinfo.visitorcloud.R
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import android.telephony.TelephonyManager




class PublicFunction {
    fun message(activity: Activity, msg: String?) {
        try {
            activity.runOnUiThread(Runnable {
                Toast.makeText(
                    activity.application,
                    msg,
                    Toast.LENGTH_SHORT
                ).show()
            })
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            Toast.makeText(
                activity.application,
                throwable.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun getDatetimenow(): String {
        val currentDate =
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        return currentDate
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    fun convertToNewFormat(dateStr: String?): String? {
        val utc: TimeZone = TimeZone.getTimeZone("UTC")
        val sourceFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val destFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        sourceFormat.timeZone = utc
        val convertedDate: Date = sourceFormat.parse(dateStr)
        return destFormat.format(convertedDate)
    }

    @SuppressLint("HardwareIds")
    fun getIMEINumber(context: Context): String? {
        var IMEINumber = ""
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            IMEINumber = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        }
        return IMEINumber
    }


    fun getPhotoFileUri(context: Context, name: String): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = name + "_${timeStamp}.png"

        var uri: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "DCIM/${context.getString(R.string.app_name)}/"
                )
            }

            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        }

        return uri ?: getUriForPreQ(fileName, context)
    }

    private fun getUriForPreQ(fileName: String, context: Context): Uri {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val photoFile = File(dir, "/${context.getString(R.string.app_name)}/$fileName")
        if (photoFile.parentFile?.exists() == false) photoFile.parentFile?.mkdir()
        return FileProvider.getUriForFile(
            context,
            "com.wacinfo.visitorcloud.provider",
            photoFile
        )
    }

    fun tokenError(mContext: Activity) {
        MaterialDialog(mContext).show {
            title(R.string.some_error)
            message(text = "กรุณาเข้าสู่ระบบอีกครั้ง")
            cancelable(false)
            positiveButton(R.string.accept_th) {
                val i = Intent(mContext, LoginActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                mContext.finish()
                mContext.startActivity(i)

            }
        }
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        var bm = bm
        var width = bm.width
        var height = bm.height
        Log.v("Pictures", "Width and height are $width--$height")
        if (width > height) {
            // landscape
            val ratio = width.toFloat() / newWidth
            width = newWidth
            height = (height / ratio).toInt()
        } else if (height > width) {
            // portrait
            val ratio = height.toFloat() / newHeight
            height = newHeight
            width = (width / ratio).toInt()
        } else {
            // square
            height = newHeight
            width = newWidth
        }
        bm = Bitmap.createScaledBitmap(bm, width, height, true)
        //        Matrix m = new Matrix();
//        m.setRectToRect(new RectF(0, 0, bm.getWidth(), bm.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        Log.v("Pictures", "after scaling Width and height are $width--$height")
        return bm
    }

    fun pickGalleryImage(activity: Activity) {
        val GALLERY_IMAGE_REQ_CODE = 102
        ImagePicker.Companion.with(activity) // Crop Image(User can choose Aspect Ratio)
            .crop() // User can only select image from Gallery
            .galleryOnly() // Image resolution will be less than 1080 x 1920
            .maxResultSize(1080, 1920)
            .start(GALLERY_IMAGE_REQ_CODE)
    }

    fun retrofitDialog(context: Context): SweetAlertDialog? {
        return SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
            .setConfirmButtonBackgroundColor(ContextCompat.getColor(context, R.color.green_inactive))
            .setTitleText("Please wait...")
    }

    fun errorDialog(dialog: SweetAlertDialog): SweetAlertDialog {
        dialog.setTitleText("Fail!")
            .setConfirmText("Close")
            .setConfirmButtonBackgroundColor(ContextCompat.getColor(dialog.context, R.color.red_active))
            .setConfirmClickListener(null)
            .changeAlertType(SweetAlertDialog.ERROR_TYPE)
        return dialog
    }

    fun successDialog(dialog: SweetAlertDialog, message: String): SweetAlertDialog {
        dialog.setTitleText("Success!")
            .setContentText(message)
            .setConfirmText("OK")
            .setConfirmClickListener(null)
            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
        return dialog
    }
    @SuppressLint("HardwareIds")
    fun getDeviceIMEI(context: Context): String? {
        var deviceUniqueIdentifier: String? = null
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        if (null != tm) {
            deviceUniqueIdentifier = tm.deviceId
        }
        if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length) {
            deviceUniqueIdentifier =
                Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
        }
        return deviceUniqueIdentifier
    }
}