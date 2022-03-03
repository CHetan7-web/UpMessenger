package com.example.upmessenger.Utils;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;

import kotlin.math.UMathKt;

public class BlurBuilder {

    private static final float BITMAP_SCALE = 0.3f;
    private static final float BLUR_RADIUS = 7.5f;

    public static Bitmap blur(Context context, Bitmap image, float bmScale) {
        return makeBlur(context,image,bmScale,BLUR_RADIUS);
    }

    public static Bitmap blur (Context context, Bitmap image){
        return makeBlur(context,image,BITMAP_SCALE,BLUR_RADIUS);
    }

    private static Bitmap makeBlur(Context context, Bitmap image,float bmScale, float radius){

        int width = Math.round(image.getWidth() * bmScale);
        int height = Math.round(image.getHeight() * radius);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return  outputBitmap;
    }

}
