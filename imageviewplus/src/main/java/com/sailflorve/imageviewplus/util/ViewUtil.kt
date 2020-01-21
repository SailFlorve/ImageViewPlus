package com.sailflorve.imageviewplus.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import java.io.FileNotFoundException
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object ViewUtil {

    private const val TAG = "ViewUtil"

    @JvmStatic
    fun getDistance(p1: PointF, p2: PointF): Float {
        return sqrt((p2.x - p1.x.toDouble()).pow(2.0) + (p2.y - p1.y.toDouble()).pow(2.0)).toFloat()
    }

    @JvmStatic
    fun getBmpFromDrawable(context: Context, @DrawableRes resId: Int): Bitmap {
        return BitmapFactory.decodeResource(context.resources, resId)
    }

    /**
     * 获得正好适合于给定宽高的Bitmap
     *
     * @param src 原Bitmap
     * @return 缩放后的Bitmap
     */
    @JvmStatic
    fun getScaledBitmap(src: Bitmap, srcW: Int, srcH: Int): Bitmap {
        Log.d(TAG, "getScaledBitmap: $srcW$srcH")
        if (src.width == srcW && src.height < srcH
                || src.height == srcH && src.width < srcW) {
            return src
        }
        val ratio = when {
            src.width >= src.height -> {
                srcW / src.width.toFloat()
            }
            src.height >= src.width -> {
                srcH / src.height.toFloat()
            }
            else -> {
                return getScaledBitmap(src, 1f)
            }
        }
        Log.d(TAG, "getScaledBitmap: $ratio")
        return getScaledBitmap(src, ratio)
    }

    @JvmStatic
    fun getScaledBitmap(src: Bitmap, ratio: Float): Bitmap {
        val matrix = Matrix()
        matrix.postScale(ratio, ratio)
        return Bitmap.createBitmap(src, 0, 0,
                src.width, src.height, matrix, true)
    }

    /**
     * 判断x,y是否在View上。
     */
    @JvmStatic
    fun checkPointInView(v: View, x: Float, y: Float): Boolean {
        return (x > v.x && x < v.x + v.width
                && y > v.y && y < v.y + v.height)
    }

    /**
     * 使某View始终处于另一个View之内。
     *
     * @param out    外面的View
     * @param inside 在里面的View
     */
    @JvmStatic
    fun keepViewIn(out: View, inside: View) { //Right
        if (inside.x + inside.width > out.right) {
            inside.x = out.right - inside.width.toFloat()
        }
        if (inside.y + inside.height > out.bottom) {
            inside.y = out.bottom - inside.height.toFloat()
        }
        if (inside.x < out.x) {
            inside.x = out.x
        }
        if (inside.y < out.y) {
            inside.y = out.y
        }
    }

    /**
     * Bitmap的像素数与ImageView所占像素不一致时，计算两者之间的缩放。
     */
    @JvmStatic
    fun getRatioOfImageViewAndBitmap(iv: ImageView, bm: Bitmap): Float {
        return if (iv.width >= iv.height) {
            bm.width / iv.width.toFloat()
        } else {
            bm.height / iv.height.toFloat()
        }
    }

    /**
     * 计算ViewGroup中的点相对于Bitmap的坐标
     *
     * @param imageView 包含Bitmap的ImageView
     * @param bitmap    ImageView中的Bitmap
     * @param point     点在父布局的坐标
     * @return 点在bitmap中的坐标
     */
    @JvmStatic
    fun getPointInBitmap(imageView: ImageView, bitmap: Bitmap, point: PointF): PointF {
        return getPointInBitmap(imageView, bitmap, point.x, point.y)
    }

    @JvmStatic
    fun getPointInBitmap(imageView: ImageView, bitmap: Bitmap, x: Float, y: Float): PointF {
        val xToIv = x - imageView.left
        val yToIv = y - imageView.top
        val ratio = getRatioOfImageViewAndBitmap(imageView, bitmap)
        return PointF(xToIv * ratio, yToIv * ratio)
    }

    /**
     * 根据两根手指的坐标，设置View的Pivot。
     */
    @JvmStatic
    fun setPivot(view: View, pointer1: PointF, pointer2: PointF) {
        var newX = (abs(pointer1.x + pointer2.x) / 2 - view.left)
        newX = if (newX > 0) newX else 0f
        var newY = (abs(pointer1.y + pointer2.y) / 2 - view.top)
        newY = if (newY > 0) newY else 0f
        view.pivotX = newX
        view.pivotY = newY
    }

    @JvmStatic
    fun getBitmapFromUri(context: Context, uri: Uri?): Bitmap? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri!!)
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    @JvmStatic
    fun drawLine(canvas: Canvas, lines: List<List<PointF>>?,
                 width: Float, @ColorInt color: Int) {
        val paint = Paint()
        if (lines == null || lines.isEmpty()) {
            return
        }
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = width
        paint.color = color
        for (pointList in lines) {
            for (i in 0 until pointList.size - 1) {
                canvas.drawLine(
                        pointList[i].x, pointList[i].y,
                        pointList[i + 1].x, pointList[i + 1].y,
                        paint
                )
            }
        }
    }
}