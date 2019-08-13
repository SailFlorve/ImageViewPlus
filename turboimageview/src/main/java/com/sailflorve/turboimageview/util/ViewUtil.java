package com.sailflorve.turboimageview.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class ViewUtil {
    private static final String TAG = "ViewUtil";

    public static float getDistance(PointF p1, PointF p2) {
        return (float) Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }

    public static Bitmap getBmpFromDrawable(Context context, @DrawableRes int resId) {
        return BitmapFactory.decodeResource(context.getResources(), resId);
    }

    /**
     * 获得正好适合于给定宽高的Bitmap
     *
     * @param src 原Bitmap
     * @return 缩放后的Bitmap
     */
    public static Bitmap getScaledBitmap(Bitmap src, int srcW, int srcH) {
        Log.d("ViewUtil", "getScaledBitmap: " + srcW + srcH);
        float ratio;

        if (src.getWidth() == srcW && src.getHeight() < srcH
                || src.getHeight() == srcH && src.getWidth() < srcW) {
            return src;
        }

        if (src.getWidth() >= src.getHeight()) {
            ratio = srcW / (float) src.getWidth();
        } else if (src.getHeight() >= src.getWidth()) {
            ratio = srcH / (float) src.getHeight();
        } else {
            return getScaledBitmap(src, 1f);
        }

        Log.d("ViewUtil", "getScaledBitmap: " + ratio);
        return getScaledBitmap(src, ratio);
    }

    public static Bitmap getScaledBitmap(Bitmap src, float ratio) {
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
        return Bitmap.createBitmap(src, 0, 0,
                src.getWidth(), src.getHeight(), matrix, true);
    }

    /**
     * 判断x,y是否在View上。
     */
    public static boolean checkPointInView(View v, float x, float y) {

        return (x > v.getX() && x < v.getX() + v.getWidth())
                && (y > v.getY() && y < v.getY() + v.getHeight());
    }

    /**
     * 使某View始终处于另一个View之内。
     *
     * @param out    外面的View
     * @param inside 在里面的View
     */
    public static void keepViewIn(View out, View inside) {
        //Right
        if (inside.getX() + inside.getWidth() > out.getRight()) {
            inside.setX(out.getRight() - inside.getWidth());
        }

        if (inside.getY() + inside.getHeight() > out.getBottom()) {
            inside.setY(out.getBottom() - inside.getHeight());
        }
        if (inside.getX() < out.getX()) {
            inside.setX(out.getX());
        }

        if (inside.getY() < out.getY()) {
            inside.setY(out.getY());
        }

    }


    /**
     * Bitmap的像素数与ImageView所占像素不一致时，计算两者之间的缩放。
     */
    public static float getRatioOfImageViewAndBitmap(ImageView iv, Bitmap bm) {
        float ratio;
        if (iv.getWidth() >= iv.getHeight()) {
            ratio = bm.getWidth() / (float) iv.getWidth();
        } else {
            ratio = bm.getHeight() / (float) iv.getHeight();
        }
        return ratio;
    }

    /**
     * 计算ViewGroup中的点相对于Bitmap的坐标
     *
     * @param imageView 包含Bitmap的ImageView
     * @param bitmap    ImageView中的Bitmap
     * @param point     点在父布局的坐标
     * @return 点在bitmap中的坐标
     */
    public static PointF getPointInBitmap(ImageView imageView, Bitmap bitmap, PointF point) {
        return getPointInBitmap(imageView, bitmap, point.x, point.y);
    }

    public static PointF getPointInBitmap(ImageView imageView, Bitmap bitmap, float x, float y) {
        float xToIv = x - imageView.getLeft();
        float yToIv = y - imageView.getTop();
        float ratio = getRatioOfImageViewAndBitmap(imageView, bitmap);
        return new PointF(xToIv * ratio, yToIv * ratio);
    }

    /**
     * 根据两根手指的坐标，设置View的Pivot。
     */
    public static void setPivot(View view, PointF pointer1, PointF pointer2) {
        float newX = Math.abs(pointer1.x + pointer2.x) / 2
                - view.getLeft();
        newX = newX > 0 ? newX : 0;
        float newY = Math.abs(pointer1.y + pointer2.y) / 2
                - view.getTop();
        newY = newY > 0 ? newY : 0;

        view.setPivotX(newX);
        view.setPivotY(newY);
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void drawLine(Canvas canvas, List<List<PointF>> lines,
                                float width, @ColorInt int color) {
        Paint paint = new Paint();

        if (lines == null || lines.isEmpty()) {
            return;
        }

        paint.setStrokeCap(Paint.Cap.ROUND);

        paint.setStrokeWidth(width);
        paint.setColor(color);

        for (List<PointF> pointList : lines) {
            for (int i = 0; i < pointList.size() - 1; i++) {
                canvas.drawLine(
                        pointList.get(i).x, pointList.get(i).y,
                        pointList.get(i + 1).x, pointList.get(i + 1).y,
                        paint
                );
            }
        }
    }
}
