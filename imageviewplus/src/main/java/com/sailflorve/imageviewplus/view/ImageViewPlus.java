package com.sailflorve.imageviewplus.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

import com.sailflorve.imageviewplus.R;
import com.sailflorve.imageviewplus.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;


public class ImageViewPlus extends RelativeLayout {
    private static final String TAG = "CustomImageView";

    /* 移动和缩放 */
    private PointF mLastMovePoint; //第一根手指上次移动的点，按下和移动时更新
    private PointF mPointerLastDownPoint; //第二根手指上次落下的点，按下时更新
    private float mPointerDownDistance;
    private boolean isMoving = false;
    private boolean isScaling = false;

    private static final float SCALE_RATIO = 400; //计算缩放比例的参数
    private static final float SCALE_MIN = 0.5f; //最大允许缩放
    private static float mScaleMax = 4f; //最大允许缩放

    private float mLastScale = 1;

    private boolean mScalable = true;
    private boolean mDraggable = true;

    /* 绘制线条和气泡 */
    private Paint mPaint;
    private List<List<PointF>> mLines;

    private boolean isDrawMode = false; // 是否处于绘图模式
    private boolean isDrawing = false; // 手指是否正在绘图
    private boolean isBubbleMode = false; // 是否处于添加气泡模式

    private ImageView mBubbleImageView;
    private Bitmap mBubbleBitmap;

    private int mLineColor = Color.RED;
    private float mLineWidth = 10;

    /* 图片Bitmap */

    private ImageView mImageView;
    private Bitmap mImgBitmap;


    public ImageViewPlus(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.image_view_plus, this);
        init();
    }

    public ImageViewPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.image_view_plus, this);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        Log.d(TAG, "init: ");
        setWillNotDraw(false);

        mImageView = findViewById(R.id.image_view);

        mPaint = new Paint();
        mLines = new ArrayList<>();

        setBubbleBitmap(ViewUtil.getBmpFromDrawable(getContext(), R.drawable.ic_wow));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: ");
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        ViewUtil.drawLine(canvas, mLines, mLineWidth, mLineColor);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                isMoving = false;
                mLastMovePoint = new PointF(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_MOVE:
                //处理绘图
                if (isDrawMode) {
                    addPointToLineList(event.getX(), event.getY());
                    invalidate();
                    break;
                } else if (isBubbleMode) {
                    if (ViewUtil.checkPointInView(mBubbleImageView, event.getX(), event.getY())) {
                        slide(mBubbleImageView, event.getX(), event.getY());
                        ViewUtil.keepViewIn(mImageView, mBubbleImageView);
                    }
                    break;
                }

                if (mScalable) {
                    //处理双指缩放
                    if (event.getPointerCount() == 2) {
                        isScaling = true;
                        calcScale(ViewUtil.getDistance(
                                new PointF(event.getX(1), event.getY(1)),
                                new PointF(event.getX(0), event.getY(0))
                        ));
                        mLastMovePoint.set(event.getX(0), event.getY(0));
                        mPointerLastDownPoint.set(event.getX(1), event.getY(1));
                        break;
                    }
                }

                //处理单指滑动
                if (mDraggable) {
                    if (event.getPointerCount() == 1) {
                        if (!isScaling) {
                            slide(mImageView, event.getX(), event.getY());
                        }
                    }
                }

                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mLastScale = mImageView.getScaleX();

                mPointerLastDownPoint = new PointF(
                        event.getX(1), event.getY(1));

                mPointerDownDistance = ViewUtil.getDistance(
                        mLastMovePoint, mPointerLastDownPoint);

                //图片没有缩放时才设置Pivot
                if (mImageView.getScaleX() == 1) {
                    ViewUtil.setPivot(mImageView, mLastMovePoint, mPointerLastDownPoint);
                }

                break;

            case MotionEvent.ACTION_POINTER_UP:
                break;

            case MotionEvent.ACTION_UP:
                clearPointerState();
                repairImageLocation();
                break;
        }
        return true;
    }

    /**
     * 设置图片的Bitmap
     *
     * @see ImageViewPlus#setImage(Uri)
     * @see ImageViewPlus#setImage(int)
     */
    public void setImage(Bitmap imgBitmap) {
        post(() -> {
            setImgLocOriginal();
            Bitmap newBm = getFitImageViewBitmap(imgBitmap);

            this.mImgBitmap = newBm;
            mImageView.setImageBitmap(newBm);
            invalidate();

            checkBubbleNotCrossImage();
        });
    }

    public void setImage(@DrawableRes int imageDrawable) {
        Bitmap bmp = ViewUtil.getBmpFromDrawable(getContext(), imageDrawable);
        setImage(bmp);
    }

    public void setImage(Uri uri) {
        Bitmap bmp = ViewUtil.getBitmapFromUri(getContext(), uri);
        setImage(bmp);
    }

    public void setLineWidth(float width) {
        mLineWidth = width;
    }

    public void setLineColor(@ColorInt int color) {
        mLineColor = color;
    }

    /**
     * @param draw 为true时可以在图片上任意画图，为false时完成画图。
     */
    public void setDrawMode(boolean draw) {
        if (draw) {
            mLines.clear();
            setImgLocOriginal();
            isDrawMode = true;
        } else {
            refreshBitmapWithLine(mLines);
            isDrawMode = false;
        }
    }

    /**
     * @param bubblingMode 为true时会显示一个可拖动的气泡，为false时气泡固定。
     */
    public void setBubbleMode(boolean bubblingMode) {
        if (bubblingMode) {
            setImgLocOriginal();
            showBubbleImgView();
            isBubbleMode = true;
        } else {
            if (mBubbleImageView != null) {
                refreshBitmapWithBubble(mBubbleImageView.getX(), mBubbleImageView.getY());
                removeView(mBubbleImageView);
            }
            isBubbleMode = false;
        }
    }

    public Bitmap getImgBitmap() {
        return mImgBitmap;
    }

    /**
     * 清除绘制状态和添加气泡状态
     */
    public void clearMode() {
        setDrawMode(false);
        setBubbleMode(false);
    }


    /**
     * 在图片上画一条线<br>
     * 坐标的范围参考 {@link #getImageViewWidth()}和{@link #getImageViewHeight()}
     *
     * @param p1 起始坐标，相对于ImageView
     * @param p2 终点坐标，相对于ImageView
     * @see ImageViewPlus#setLineColor(int)
     * @see ImageViewPlus#setLineWidth(float)
     */
    public void addLine(PointF p1, PointF p2) {
        p1 = new PointF(p1.x + mImageView.getLeft(), p1.y + mImageView.getTop());
        p2 = new PointF(p2.x + mImageView.getLeft(), p2.y + mImageView.getTop());

        List<List<PointF>> lines = new ArrayList<>();
        lines.add(new ArrayList<>());
        lines.get(0).add(p1);
        lines.get(0).add(p2);

        refreshBitmapWithLine(lines);
//
//        drawOnBitmap(lines);
    }

    /**
     * 把气泡添加到图片上，使用{@link #setBubbleBitmap(Bitmap)}设置气泡<br>
     * 坐标的范围参考 {@link #getImageViewWidth()}和{@link #getImageViewHeight()}
     *
     * @param x 起始坐标，相对于ImageView
     * @param y 终点坐标，相对于ImageView
     */
    public void addBubble(float x, float y) {
        x = x + mImageView.getLeft();
        y = y + mImageView.getTop();
        refreshBitmapWithBubble(x, y);
    }


    public void setBubbleBitmap(Bitmap bubbleBitmap) {
        mBubbleBitmap = bubbleBitmap;
        post(this::checkBubbleNotCrossImage);
    }


    /**
     * 清除所有手指状态，绘图，移动，缩放等
     */
    private void clearPointerState() {
        isDrawing = false;
        isMoving = false;
        isScaling = false;
    }

    /**
     * 根据当前坐标计算手指移动距离并滑动
     *
     * @param v     需要滑动的View
     * @param currX 当前坐标x
     * @param currY 当前坐标y
     */
    private void slide(View v, float currX, float currY) {
        float dx = currX - mLastMovePoint.x;
        float dy = currY - mLastMovePoint.y;

        if (ViewUtil.getDistance(
                mLastMovePoint, new PointF(currX, currY)) > 10
                || isMoving) {
            isMoving = true;
            v.setX(v.getX() + dx);
            v.setY(v.getY() + dy);
            mLastMovePoint.set(currX, currY);
        }
    }


    private void addPointToLineList(float x, float y) {
        if (ViewUtil.checkPointInView(mImageView, x, y)) {
            if (isDrawing) {
                List<PointF> pList = mLines.get(mLines.size() - 1);
                if (pList == null) {
                    pList = new ArrayList<>();
                }
                pList.add(new PointF(x, y));
            } else {
                List<PointF> pList = new ArrayList<>();
                pList.add(new PointF(x, y));
                mLines.add(pList);
                isDrawing = true;
            }
        }
    }

    /**
     * 计算缩放的比例并缩放
     *
     * @param currentDistance 当前手指的距离
     */
    private void calcScale(float currentDistance) {

        float rate = (currentDistance - mPointerDownDistance) / SCALE_RATIO;

        float scale = mLastScale + rate;
        float scaleRatio = Math.max(scale, SCALE_MIN);

        scale(scaleRatio);
    }

    private void scale(float ratio) {
        mImageView.setScaleX(ratio);
        mImageView.setScaleY(ratio);
        if (ratio > mScaleMax) {
            mImageView.animate().scaleX(mScaleMax).scaleY(mScaleMax);
        }
    }

    /**
     * 使图片回到原位置
     */
    private void repairImageLocation() {
        //缩放<=1时：图片位置和缩放回到初始位置
        if (mImageView.getScaleX() <= 1) {
            setImgLocOriginal();
        }

        //缩放>1时：图片边界回到原位置。
        if (mImageView.getScaleX() > 1) {
            backImgToBorder();
        }
    }

    private void setImgLocOriginal() {
        if (mImageView == null) {
            return;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setTarget(mImageView);

        List<Animator> animatorList = new ArrayList<>();
        animatorList.add(ObjectAnimator.ofFloat(mImageView, "translationX", 0));
        animatorList.add(ObjectAnimator.ofFloat(mImageView, "translationY", 0));
//        animatorList.add(ObjectAnimator.ofFloat(mImageView, "x", mOriginLoc.x));
//        animatorList.add(ObjectAnimator.ofFloat(mImageView, "y", mOriginLoc.y));
        animatorList.add(ObjectAnimator.ofFloat(mImageView, "scaleX", 1));
        animatorList.add(ObjectAnimator.ofFloat(mImageView, "scaleY", 1));

        animatorSet.setDuration(225).playTogether(animatorList);
        animatorSet.start();
    }

    /**
     * 检查图片若超出边界，则返回边界
     */
    private void backImgToBorder() {
        if (mImageView == null) {
            return;
        }
        //计算需要回弹的超出的边界大小
        float canOverRight = (mImageView.getWidth()
                - mImageView.getPivotX())
                * (mImageView.getScaleX() - 1);

        float canOverLeft = mImageView.getPivotX() * (mImageView.getScaleX() - 1);

        if (mImageView.getTranslationX() > canOverLeft
                || mImageView.getTranslationX() < -canOverRight) {
            mImageView.animate().translationX(
                    mImageView.getTranslationX() > 0 ? canOverLeft : -canOverRight);
        }

        float canOverTop = mImageView.getPivotY() * (mImageView.getScaleY() - 1);
        float canOverBottom = mImageView.getHeight()
                * mImageView.getScaleY()
                * ((mImageView.getHeight() - mImageView.getPivotY()) / mImageView.getHeight())
                - (mImageView.getHeight() / 2f - mImageView.getPivotY())
                - mImageView.getHeight() / 2f;

        if (mImageView.getTranslationY() > canOverTop
                || mImageView.getTranslationY() < -canOverBottom) {
            mImageView.animate().translationY(
                    mImageView.getTranslationY() > 0 ? canOverTop : -canOverBottom);
        }
    }


    /**
     * 把画的线绘制到Bitmap上，并给ImageView设置新的Bitmap
     *
     * @param linesToViewGroup 点集，相对于父布局
     */
    private void refreshBitmapWithLine(List<List<PointF>> linesToViewGroup) {
        if (linesToViewGroup == null || linesToViewGroup.isEmpty()) {
            return;
        }

        List<List<PointF>> lineToBitmap = linePointToBitmapPoint(linesToViewGroup);
        drawLineOnBitmap(lineToBitmap);

        invalidate();
    }

    /**
     * 将bubble根据bitmap比例缩放，计算其坐标，添加到bitmap。
     *
     * @param x 相对于父布局坐标
     * @param y 相对于父布局坐标
     */
    private void refreshBitmapWithBubble(float x, float y) {
        //父布局坐标
        if (mImgBitmap == null || mBubbleBitmap == null) {
            return;
        }
        Bitmap bubbleScaled = getScaledBubbleBitmap();
        Log.d(TAG, "refreshBitmapWithBubble: BubbleScaled" + bubbleScaled.getWidth() + bubbleScaled.getHeight());

        PointF scaledBubblePoint = ViewUtil.getPointInBitmap(mImageView, mImgBitmap, x, y);

        drawBitmapOnBitmap(bubbleScaled, scaledBubblePoint.x, scaledBubblePoint.y);

        invalidate();
    }

    /**
     * 在图片对应的Bitmap上绘制线
     *
     * @param lines 点集，相对于Bitmap
     */
    private void drawLineOnBitmap(List<List<PointF>> lines) {
        Bitmap newBmp = mImgBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(newBmp);

        float ratio = ViewUtil.getRatioOfImageViewAndBitmap(mImageView, mImgBitmap);

        ViewUtil.drawLine(canvas, lines, mLineWidth * ratio, mLineColor);
        setImage(newBmp);
    }

    /**
     * 在图片对应的bitmap上绘制图片
     *
     * @param bitmap 需要绘制的bitmap
     * @param x      坐标，相对于bitmap
     * @param y      坐标，相对于bitmap
     */
    private void drawBitmapOnBitmap(Bitmap bitmap, float x, float y) {
        Bitmap newBmp = mImgBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(newBmp);

        canvas.drawBitmap(bitmap, x, y, mPaint);

        setImage(newBmp);
    }

    /**
     * @param linesToViewGroup 点集（相对于父布局）
     * @return 点集(相对于Bitmap)
     */
    private List<List<PointF>> linePointToBitmapPoint(List<List<PointF>> linesToViewGroup) {
        List<List<PointF>> linesToBitmap = new ArrayList<>(linesToViewGroup);
        mLines.clear();
        for (List<PointF> line : linesToBitmap) {
            for (PointF pointF : line) {
                //缩放绘图点的坐标至适合Bitmap
                PointF newPoint = ViewUtil.getPointInBitmap(mImageView, mImgBitmap, pointF);
                pointF.x = newPoint.x;
                pointF.y = newPoint.y;
            }
        }
        return linesToBitmap;
    }

    private void checkBubbleNotCrossImage() {
        if (mImageView.getWidth() == 0 || mImageView.getHeight() == 0) {
            return;
        }
        if (mBubbleBitmap.getWidth() > mImageView.getWidth()
                || mBubbleBitmap.getHeight() > mImageView.getHeight()) {
            mBubbleBitmap = ViewUtil.getScaledBitmap(
                    mBubbleBitmap,
                    (int) (mImageView.getWidth() * 0.8),
                    (int) (mImageView.getHeight() * 0.8));
        }
    }


    private Bitmap getScaledBubbleBitmap() {
        //缩放Bubble至适合bitmap
        float ratio = ViewUtil.getRatioOfImageViewAndBitmap(mImageView, mImgBitmap);
        return ViewUtil.getScaledBitmap(mBubbleBitmap, ratio);
    }

    private void showBubbleImgView() {
        checkBubbleNotCrossImage();
        mBubbleImageView = new ImageView(getContext());
        mBubbleImageView.setImageBitmap(mBubbleBitmap);
        addView(mBubbleImageView,
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        //将气泡添加到原图的中间
        mBubbleImageView.setX(
                mImageView.getRight() - mImageView.getWidth() / 2f - mBubbleBitmap.getWidth() / 2f);
        mBubbleImageView.setY(
                mImageView.getBottom() - mImageView.getHeight() / 2f - mBubbleBitmap.getHeight() / 2f);
    }


    /**
     * 对于图片本身的width小于ImageView的width且width>height，<br>
     * 设置ImageView宽度match_parent使其顶格显示；height同样处理；<br>
     * 已经超过imageView宽高的图片，维持原状
     */
    private Bitmap getFitImageViewBitmap(Bitmap imgBitmap) {
        int w;
        if (imgBitmap.getWidth() >= imgBitmap.getHeight()
                && imgBitmap.getWidth() < getWidth()
                || imgBitmap.getHeight() >= imgBitmap.getWidth()
                && imgBitmap.getHeight() < getHeight()) {
            w = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            w = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        LayoutParams layoutParams = new LayoutParams(
                w, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mImageView.setLayoutParams(layoutParams);
        Log.d(TAG, "图片 Bitmap：" + imgBitmap.getWidth() + " " + imgBitmap.getHeight());
        return imgBitmap;
    }

    public boolean getDrawMode() {
        return isDrawMode;
    }

    public boolean getBubbleMode() {
        return isBubbleMode;
    }

    public float getImageViewWidth() {
        return mImageView.getWidth();
    }

    public float getImageViewHeight() {
        return mImageView.getHeight();
    }

    public boolean isScalable() {
        return mScalable;
    }

    public void setScalable(boolean scalable) {
        this.mScalable = scalable;
    }

    public boolean isDraggable() {
        return mDraggable;
    }

    public void setDraggable(boolean draggable) {
        this.mDraggable = draggable;
    }

    public void setScaleMax(float max) {
        mScaleMax = max;
    }
}
