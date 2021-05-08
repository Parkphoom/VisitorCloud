package com.wacinfo.visitorcloud.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;


import com.squareup.picasso.Transformation;
import com.wacinfo.visitorcloud.R;
import com.watermark.androidwm.WatermarkBuilder;
import com.watermark.androidwm.bean.WatermarkPosition;
import com.watermark.androidwm.bean.WatermarkText;

public class WatermarkTransformation implements Transformation {

    private String waterMark;
    private Context mContext;
    private static final int PADDING = 8;

    public WatermarkTransformation(Context context, String waterMark) {
        this.waterMark = waterMark;
        this.mContext = context;
    }

    @Override
    public Bitmap transform(Bitmap source) {
//        WatermarkPosition watermarkPosition = new WatermarkPosition(0.2,0.3, -30);
        WatermarkText watermarkText = new WatermarkText(waterMark)
                .setPositionX(0.1)
                .setPositionY(0.5)
//                .setPosition(watermarkPosition)
                .setTextColor(Color.GREEN)
                .setTextShadow(0.1f, 5, 5, mContext.getResources().getColor(R.color.green_active))
                .setTextAlpha(150)
//                .setRotation(-30)
                .setTextSize(30);
        Bitmap mutableBitmap = WatermarkBuilder
                .create(mContext, source)
//                .setTileMode(true)
                .loadWatermarkText(watermarkText)
                .getWatermark()
                .getOutputImage();
        source.recycle();
        return  mutableBitmap;
    }

    @Override
    public String key() {
        return "WaterMarkTransformation-" + waterMark;
    }


}