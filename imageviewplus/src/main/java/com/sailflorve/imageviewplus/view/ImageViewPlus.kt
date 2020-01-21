package com.sailflorve.imageviewplus.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.sailflorve.imageviewplus.R
import com.sailflorve.imageviewplus.util.ViewUtil.checkPointInView
import com.sailflorve.imageviewplus.util.ViewUtil.drawLine
import com.sailflorve.imageviewplus.util.ViewUtil.getBitmapFromUri
import com.sailflorve.imageviewplus.util.ViewUtil.getBmpFromDrawable
import com.sailflorve.imageviewplus.util.ViewUtil.getDistance
import com.sailflorve.imageviewplus.util.ViewUtil.getPointInBitmap
import com.sailflorve.imageviewplus.util.ViewUtil.getRatioOfImageViewAndBitmap
import com.sailflorve.imageviewplus.util.ViewUtil.getScaledBitmap
import com.sailflorve.imageviewplus.util.ViewUtil.keepViewIn
import com.sailflorve.imageviewplus.util.ViewUtil.setPivot
import java.util.*

class ImageViewPlus : RelativeLayout {

    companion object {
        private const val TAG = "CustomImageView"
        private const val SCALE_RATIO = 400f //计算缩放比例的参数
        private const val SCALE_MIN = 0.5f //最小允许缩放
        private var mScaleMax = 4f //最大允许缩放
    }

    /* 移动和缩放 */
    private lateinit var mLastMovePoint: PointF //第一根手指上次移动的点，按下和移动时更新
    private var mPointerLastDownPoint: PointF? = null //第二根手指上次落下的点，按下时更新
    private var mPointerDownDistance = 0f
    private var isSliding = false
    private var isScaling = false
    private var mLastScale = 1f
    private var isScalable = true
    private var isDraggable = true

    /* 绘制线条和气泡 */
    private lateinit var mPaint: Paint
    private lateinit var mLines: MutableList<MutableList<PointF>>
    private var isDrawMode = false // 是否处于绘图模式
    private var isDrawing = false // 手指是否正在绘图
    private var isBubbleMode = false // 是否处于添加气泡模式
    private var mBubbleImageView: ImageView? = null
    private var mBubbleBitmap: Bitmap? = null
    private var mLineColor = Color.RED
    private var mLineWidth = 10f
    private var mLastClickTime: Long = 0
    private var mClickTimer: CountDownTimer? = null

    /* 图片Bitmap */
    private lateinit var mImageView: ImageView
    private var imgBitmap: Bitmap? = null

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.image_view_plus, this)
        init()
    }

    constructor(context: Context, set: AttributeSet) : super(context, set) {
        LayoutInflater.from(context).inflate(R.layout.image_view_plus, this)
        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        Log.d(TAG, "init: ")
        setWillNotDraw(false)
        mImageView = findViewById(R.id.image_view)
        mPaint = Paint()
        mLines = ArrayList()
        setBubbleBitmap(getBmpFromDrawable(context, R.drawable.ic_wow))
    }

    override fun onDraw(canvas: Canvas) {
        Log.d(TAG, "onDraw: ")
        super.onDraw(canvas)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        drawLine(canvas, mLines, mLineWidth, mLineColor)
    }

    override fun performClick(): Boolean {
        if (mClickTimer == null) {
            mClickTimer = object : CountDownTimer(200, 200) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    super@ImageViewPlus.performClick()
                }
            }
        }

        val time = System.currentTimeMillis()

        mLastClickTime = when {
            time - mLastClickTime < 200
                    && checkPointInView(mImageView, mLastMovePoint.x, mLastMovePoint.y) -> {

                mClickTimer!!.cancel()
                when {
                    mImageView.scaleX == 1f -> {
                        setPivot(mImageView, mLastMovePoint, mLastMovePoint)
                        mImageView.animate().scaleX(3.0f).scaleY(3.0f)
                    }
                    mImageView.scaleX > 1 -> {
                        setImgLocOriginal()
                    }
                }
                time
            }
            else -> {
                mClickTimer!!.start()
                time
            }
        }

        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isSliding = false
                mLastMovePoint = PointF(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                //处理绘图
                if (isDrawMode) {
                    addPointToLineList(event.x, event.y)
                    invalidate()
                    return true
                } else if (isBubbleMode) {
                    if (checkPointInView(mBubbleImageView!!, event.x, event.y)) {
                        slide(mBubbleImageView, event.x, event.y)
                        keepViewIn(mImageView, mBubbleImageView!!)
                    }
                }
                if (isScalable) { //处理双指缩放
                    if (event.pointerCount == 2) {
                        isScaling = true
                        calcScale(getDistance(
                                PointF(event.getX(1), event.getY(1)),
                                PointF(event.getX(0), event.getY(0))
                        ))
                        mLastMovePoint[event.getX(0)] = event.getY(0)
                        mPointerLastDownPoint!![event.getX(1)] = event.getY(1)
                        return true
                    }
                }
                //处理单指滑动
                if (isDraggable) {
                    if (event.pointerCount == 1) {
                        if (!isScaling) {
                            slide(mImageView, event.x, event.y)
                        }
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mLastScale = mImageView.scaleX
                mPointerLastDownPoint = PointF(
                        event.getX(1), event.getY(1))
                mPointerDownDistance = getDistance(
                        mLastMovePoint, mPointerLastDownPoint!!)
                //图片没有缩放时才设置Pivot
                if (mImageView.scaleX == 1f) {
                    setPivot(mImageView, mLastMovePoint, mPointerLastDownPoint!!)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
            }
            MotionEvent.ACTION_UP -> {
                if (!isSliding && !isScaling && !isDrawing) {
                    performClick()
                }
                clearPointerState()
                repairImageLocation()
            }
        }
        return true
    }

    /**
     * 设置图片的Bitmap
     *
     * @see ImageViewPlus.setImage
     * @see ImageViewPlus.setImage
     */
    fun setImage(imgBitmap: Bitmap?) {
        post {
            setImgLocOriginal()
            val newBm = getFitImageViewBitmap(imgBitmap)
            this.imgBitmap = newBm
            mImageView.setImageBitmap(newBm)
            checkBubbleNotCrossImage()
        }
    }

    fun setImage(@DrawableRes imageDrawable: Int) {
        val bmp = getBmpFromDrawable(context, imageDrawable)
        setImage(bmp)
    }

    fun setImage(uri: Uri?) {
        val bmp = getBitmapFromUri(context, uri)
        setImage(bmp)
    }

    fun setLineWidth(width: Float) {
        mLineWidth = width
    }

    fun setLineColor(@ColorInt color: Int) {
        mLineColor = color
    }

    /**
     * @param draw 为true时可以在图片上任意画图，为false时完成画图。
     */
    fun setDrawMode(draw: Boolean) {
        isDrawMode = when {
            draw -> {
                mLines.clear()
                setImgLocOriginal()
                true
            }
            else -> {
                refreshBitmapWithLine(mLines)
                false
            }
        }
    }

    /**
     * @param bubblingMode 为true时会显示一个可拖动的气泡，为false时气泡固定。
     */
    fun setBubbleMode(bubblingMode: Boolean) {
        isBubbleMode = if (!bubblingMode) {
            if (mBubbleImageView != null) {
                refreshBitmapWithBubble(mBubbleImageView!!.x, mBubbleImageView!!.y)
                removeView(mBubbleImageView)
            }
            false
        } else {
            setImgLocOriginal()
            showBubbleImgView()
            true
        }
    }

    /**
     * 清除绘制状态和添加气泡状态
     */
    fun clearMode() {
        setDrawMode(false)
        setBubbleMode(false)
    }

    /**
     * 在图片上画一条线<br></br>
     * 坐标的范围参考 [.getImageViewWidth]和[.getImageViewHeight]
     *
     * @param p1 起始坐标，相对于ImageView
     * @param p2 终点坐标，相对于ImageView
     * @see ImageViewPlus.setLineColor
     * @see ImageViewPlus.setLineWidth
     */
    fun addLine(p1: PointF, p2: PointF) {
        val newP1 = PointF(p1.x + mImageView.left, p1.y + mImageView.top)
        val newP2 = PointF(p2.x + mImageView.left, p2.y + mImageView.top)
        val lines: MutableList<MutableList<PointF>> = ArrayList()
        lines.add(ArrayList())
        lines[0].add(newP1)
        lines[0].add(newP2)
        refreshBitmapWithLine(lines)
    }

    /**
     * 把气泡添加到图片上，使用[.setBubbleBitmap]设置气泡<br></br>
     * 坐标的范围参考 [.getImageViewWidth]和[.getImageViewHeight]
     *
     * @param x 起始坐标，相对于ImageView
     * @param y 终点坐标，相对于ImageView
     */
    fun addBubble(x: Float, y: Float) {
        var newX = x
        var newY = y
        newX += mImageView.left
        newY += mImageView.top
        refreshBitmapWithBubble(newX, newY)
    }

    fun setBubbleBitmap(bubbleBitmap: Bitmap?) {
        mBubbleBitmap = bubbleBitmap
        post { checkBubbleNotCrossImage() }
    }

    /**
     * 清除所有手指状态，绘图，移动，缩放等
     */
    private fun clearPointerState() {
        isDrawing = false
        isSliding = false
        isScaling = false
    }

    /**
     * 根据当前坐标计算手指移动距离并滑动
     *
     * @param v     需要滑动的View
     * @param currX 当前坐标x
     * @param currY 当前坐标y
     */
    private fun slide(v: View?, currX: Float, currY: Float) {
        val dx = currX - mLastMovePoint.x
        val dy = currY - mLastMovePoint.y

        if (getDistance(mLastMovePoint, PointF(currX, currY)) > 10 || isSliding) {
            isSliding = true
            v!!.x = v.x + dx
            v.y = v.y + dy
            mLastMovePoint[currX] = currY
        }
    }

    private fun addPointToLineList(x: Float, y: Float) {
        if (checkPointInView(mImageView, x, y)) {
            if (isDrawing) {
                val pList = mLines[mLines.size - 1]
                pList.add(PointF(x, y))
            } else {
                val pList: MutableList<PointF> = ArrayList()
                pList.add(PointF(x, y))
                mLines.add(pList)
                isDrawing = true
            }
        }
    }

    /**
     * 计算缩放的比例并缩放
     *
     * @param currentDistance 当前手指的距离
     */
    private fun calcScale(currentDistance: Float) {
        val rate = (currentDistance - mPointerDownDistance) / SCALE_RATIO
        val scale = mLastScale + rate
        val scaleRatio = scale.coerceAtLeast(SCALE_MIN)
        scale(scaleRatio)
    }

    private fun scale(ratio: Float) {
        mImageView.scaleX = ratio
        mImageView.scaleY = ratio
        if (ratio > mScaleMax) {
            mImageView.animate().scaleX(mScaleMax).scaleY(mScaleMax)
        }
    }

    /**
     * 使图片回到原位置
     */
    private fun repairImageLocation() { //缩放<=1时：图片位置和缩放回到初始位置
        if (mImageView.scaleX <= 1) {
            setImgLocOriginal()
        }
        //缩放>1时：图片边界回到原位置。
        if (mImageView.scaleX > 1) {
            backImgToBorder()
        }
    }

    private fun setImgLocOriginal() {
        val animatorSet = AnimatorSet()
        animatorSet.setTarget(mImageView)
        val animatorList: MutableList<Animator> = ArrayList()
        animatorList.add(ObjectAnimator.ofFloat(mImageView, "translationX", 0f))
        animatorList.add(ObjectAnimator.ofFloat(mImageView, "translationY", 0f))
        //        animatorList.add(ObjectAnimator.ofFloat(mImageView, "x", mOriginLoc.x));
//        animatorList.add(ObjectAnimator.ofFloat(mImageView, "y", mOriginLoc.y));
        animatorList.add(ObjectAnimator.ofFloat(mImageView, "scaleX", 1f))
        animatorList.add(ObjectAnimator.ofFloat(mImageView, "scaleY", 1f))
        animatorSet.setDuration(225).playTogether(animatorList)
        animatorSet.start()
    }

    /**
     * 检查图片若超出边界，则返回边界
     */
    private fun backImgToBorder() {
        //计算需要回弹的超出的边界大小
        val canOverRight = ((mImageView.width
                - mImageView.pivotX)
                * (mImageView.scaleX - 1))
        val canOverLeft = mImageView.pivotX * (mImageView.scaleX - 1)
        if (mImageView.translationX > canOverLeft
                || mImageView.translationX < -canOverRight) {
            mImageView.animate().translationX(
                    if (mImageView.translationX > 0) canOverLeft else -canOverRight)
        }
        val canOverTop = mImageView.pivotY * (mImageView.scaleY - 1)
        val canOverBottom = ((mImageView.height
                * mImageView.scaleY
                * ((mImageView.height - mImageView.pivotY) / mImageView.height))
                - (mImageView.height / 2f - mImageView.pivotY)
                - mImageView.height / 2f)
        if (mImageView.translationY > canOverTop
                || mImageView.translationY < -canOverBottom) {
            mImageView.animate().translationY(
                    if (mImageView.translationY > 0) canOverTop else -canOverBottom)
        }
    }

    /**
     * 把画的线绘制到Bitmap上，并给ImageView设置新的Bitmap
     *
     * @param linesToViewGroup 点集，相对于父布局
     */
    private fun refreshBitmapWithLine(linesToViewGroup: List<MutableList<PointF>>?) {
        if (linesToViewGroup == null || linesToViewGroup.isEmpty()) {
            return
        }
        val lineToBitmap = linePointToBitmapPoint(linesToViewGroup)
        drawLineOnBitmap(lineToBitmap)
        invalidate()
    }

    /**
     * 将bubble根据bitmap比例缩放，计算其坐标，添加到bitmap。
     *
     * @param x 相对于父布局坐标
     * @param y 相对于父布局坐标
     */
    private fun refreshBitmapWithBubble(x: Float, y: Float) { //父布局坐标
        if (imgBitmap == null || mBubbleBitmap == null) {
            return
        }
        val bubbleScaled = scaledBubbleBitmap
        Log.d(TAG, "refreshBitmapWithBubble: BubbleScaled" + bubbleScaled.width + bubbleScaled.height)
        val scaledBubblePoint = getPointInBitmap(mImageView, imgBitmap!!, x, y)
        drawBitmapOnBitmap(bubbleScaled, scaledBubblePoint.x, scaledBubblePoint.y)
        invalidate()
    }

    /**
     * 在图片对应的Bitmap上绘制线
     *
     * @param lines 点集，相对于Bitmap
     */
    private fun drawLineOnBitmap(lines: List<MutableList<PointF>>) {
        val newBmp = imgBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(newBmp)
        val ratio = getRatioOfImageViewAndBitmap(mImageView, imgBitmap!!)
        drawLine(canvas, lines, mLineWidth * ratio, mLineColor)
        setImage(newBmp)
    }

    /**
     * 在图片对应的bitmap上绘制图片
     *
     * @param bitmap 需要绘制的bitmap
     * @param x      坐标，相对于bitmap
     * @param y      坐标，相对于bitmap
     */
    private fun drawBitmapOnBitmap(bitmap: Bitmap, x: Float, y: Float) {
        val newBmp = imgBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(newBmp)
        canvas.drawBitmap(bitmap, x, y, mPaint)
        setImage(newBmp)
    }

    /**
     * @param linesToViewGroup 点集（相对于父布局）
     * @return 点集(相对于Bitmap)
     */
    private fun linePointToBitmapPoint(linesToViewGroup: List<MutableList<PointF>>): List<MutableList<PointF>> {
        val linesToBitmap: List<MutableList<PointF>> = ArrayList(linesToViewGroup)
        mLines.clear()
        for (line in linesToBitmap) {
            for (pointF in line) { //缩放绘图点的坐标至适合Bitmap
                val newPoint = getPointInBitmap(mImageView, imgBitmap!!, pointF)
                pointF.x = newPoint.x
                pointF.y = newPoint.y
            }
        }
        return linesToBitmap
    }

    private fun checkBubbleNotCrossImage() {
        if (mImageView.width == 0 || mImageView.height == 0) {
            return
        }
        if (mBubbleBitmap!!.width > mImageView.width
                || mBubbleBitmap!!.height > mImageView.height) {
            mBubbleBitmap = getScaledBitmap(
                    mBubbleBitmap!!,
                    (mImageView.width * 0.8).toInt(),
                    (mImageView.height * 0.8).toInt())
        }
    }

    //缩放Bubble至适合bitmap
    private val scaledBubbleBitmap: Bitmap
        get() { //缩放Bubble至适合bitmap
            val ratio = getRatioOfImageViewAndBitmap(mImageView, imgBitmap!!)
            return getScaledBitmap(mBubbleBitmap!!, ratio)
        }

    private fun showBubbleImgView() {
        checkBubbleNotCrossImage()
        mBubbleImageView = ImageView(context)
        mBubbleImageView!!.setImageBitmap(mBubbleBitmap)
        addView(mBubbleImageView,
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        //将气泡添加到原图的中间
        mBubbleImageView!!.x = mImageView.right - mImageView.width / 2f - mBubbleBitmap!!.width / 2f
        mBubbleImageView!!.y = mImageView.bottom - mImageView.height / 2f - mBubbleBitmap!!.height / 2f
    }

    /**
     * 对于图片本身的width小于ImageView的width且width>height，<br></br>
     * 设置ImageView宽度match_parent使其顶格显示；height同样处理；<br></br>
     * 已经超过imageView宽高的图片，维持原状
     */
    private fun getFitImageViewBitmap(imgBitmap: Bitmap?): Bitmap? {
        val w = if (imgBitmap!!.width >= imgBitmap.height
                && imgBitmap.width < width
                || imgBitmap.height >= imgBitmap.width
                && imgBitmap.height < height) {
            ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            ViewGroup.LayoutParams.WRAP_CONTENT
        }
        val layoutParams = LayoutParams(
                w, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(CENTER_IN_PARENT)
        mImageView.layoutParams = layoutParams
        Log.d(TAG, "图片 Bitmap：" + imgBitmap.width + " " + imgBitmap.height)
        return imgBitmap
    }

    fun getDrawMode(): Boolean {
        return isDrawMode
    }

    fun getBubbleMode(): Boolean {
        return isBubbleMode
    }

    val imageViewWidth: Float
        get() = mImageView.width.toFloat()

    val imageViewHeight: Float
        get() = mImageView.height.toFloat()

    fun setScaleMax(max: Float) {
        mScaleMax = max
    }

}