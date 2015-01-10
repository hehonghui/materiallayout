/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 bboyfeiyu@gmail.com ( mr.simple )
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.simple;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.simple.materiallayout.R;

/**
 * MaterialLayout是模拟Android 5.0中View被点击的波纹效果的布局，与其他的模拟Material
 * Desigin效果的View不同，所有在MaterialLayout布局下的子视图被点击时都会产生波纹效果,而不是某个特定的View才会有这样的效果.
 * 
 * @author mrsimple
 */
public class MaterialLayout extends RelativeLayout {

    private static final int DEFAULT_RADIUS = 10;
    private static final int DEFAULT_FRAME_RATE = 10;
    private static final int DEFAULT_DURATION = 200;
    private static final int DEFAULT_ALPHA = 255;
    private static final float DEFAULT_SCALE = 0.8f;
    private static final int DEFAULT_ALPHA_STEP = 5;

    /**
     * 动画帧率
     */
    private int mFrameRate = DEFAULT_FRAME_RATE;
    /**
     * 渐变动画持续时间
     */
    private int mDuration = DEFAULT_DURATION;
    /**
     * 
     */
    private Paint mPaint = new Paint();
    /**
     * 被点击的视图的中心点
     */
    private Point mCenterPoint = null;
    /**
     * 视图的Rect
     */
    private RectF mTargetRectf;
    /**
     * 起始的圆形背景半径
     */
    private int mRadius = DEFAULT_RADIUS;
    /**
     * 最大的半径
     */
    private int mMaxRadius = DEFAULT_RADIUS;

    /**
     * 渐变的背景色
     */
    private int mCirclelColor = Color.LTGRAY;
    /**
     * 每次重绘时半径的增幅
     */
    private int mRadiusStep = 1;
    /**
     * 保存用户设置的alpha值
     */
    private int mBackupAlpha;

    /**
     * 圆形半径针对于被点击视图的缩放比例,默认为0.8
     */
    private float mCircleScale = DEFAULT_SCALE;
    /**
     * 颜色的alpha值, (0, 255)
     */
    private int mColorAlpha = DEFAULT_ALPHA;
    /**
     * 每次动画Alpha的渐变递减值
     */
    private int mAlphaStep = DEFAULT_ALPHA_STEP;

    /**
     * @param context
     */
    public MaterialLayout(Context context) {
        this(context, null);
    }

    public MaterialLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MaterialLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        if (attrs != null) {
            initTypedArray(context, attrs);
        }

        initPaint();

        this.setWillNotDraw(false);
        this.setDrawingCacheEnabled(true);
    }

    private void initTypedArray(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.MaterialLayout);
        mCirclelColor = typedArray.getColor(R.styleable.MaterialLayout_color, Color.LTGRAY);
        mDuration = typedArray.getInteger(R.styleable.MaterialLayout_duration,
                DEFAULT_DURATION);
        mFrameRate = typedArray
                .getInteger(R.styleable.MaterialLayout_framerate, DEFAULT_FRAME_RATE);
        mColorAlpha = typedArray.getInteger(R.styleable.MaterialLayout_alpha, DEFAULT_ALPHA);
        mCircleScale = typedArray.getFloat(R.styleable.MaterialLayout_scale, DEFAULT_SCALE);

        typedArray.recycle();

    }

    private void initPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mCirclelColor);
        mPaint.setAlpha(mColorAlpha);

        // 备份alpha属性用于动画完成时重置
        mBackupAlpha = mColorAlpha;
    }

    /**
     * 点击的某个坐标点是否在View的内部
     * 
     * @param touchView
     * @param x 被点击的x坐标
     * @param y 被点击的y坐标
     * @return 如果点击的坐标在该view内则返回true,否则返回false
     */
    private boolean isInFrame(View touchView, float x, float y) {
        int left = touchView.getLeft();
        int top = touchView.getTop();
        int right = touchView.getRight();
        int bottom = touchView.getBottom();
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    /**
     * 获取View的中心点左边
     * 
     * @param view 目标View
     * @return 中心点
     */
    private Point getViewCenterPoint(View view) {
        int left = view.getLeft();
        int top = view.getTop();
        int right = view.getRight();
        int bottom = view.getBottom();

        mTargetRectf = new RectF(left, top, right, bottom);
        mCenterPoint = new Point((right + left) / 2, (top + bottom) / 2);
        return mCenterPoint;
    }

    private View findTargetView(ViewGroup viewGroup, float x, float y) {
        int childCount = viewGroup.getChildCount();
        // 迭代查找被点击的目标视图
        for (int i = 0; i < childCount; i++) {
            View childView = viewGroup.getChildAt(i);
            // 如果是ViewGroup,那么深入查找其子视图
            if (childView instanceof ViewGroup) {
                findTargetView(viewGroup, x, y);
            } else if (isInFrame(childView, x, y)) { // 否则判断该点是否在该View的frame内
                return childView;
            }
        }

        return null;
    }

    private boolean isAnimEnd() {
        return mRadius >= mMaxRadius;
    }

    private void calculateMaxRadius(View view) {
        // 取视图的最长边
        int maxLength = Math.max(view.getWidth(), view.getHeight());
        // 计算Ripple圆形的半径
        mMaxRadius = (int) ((maxLength / 2) * mCircleScale);

        int redrawCount = mDuration / mFrameRate;
        // 计算每次动画半径的增值
        mRadiusStep = (mMaxRadius - DEFAULT_RADIUS) / redrawCount;
        // 计算每次alpha递减的值
        mAlphaStep = (mColorAlpha - 100) / redrawCount;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Log.d(VIEW_LOG_TAG, "touch : " + this);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View touchView = findTargetView(this, event.getX(), event.getY());
            if (touchView != null) {
                mCenterPoint = getViewCenterPoint(touchView);
                // 计算相关数据
                calculateMaxRadius(touchView);
                // 重绘视图
                invalidate();
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // 绘制Circle
        drawRippleIfNecessary(canvas);
    }

    private void drawRippleIfNecessary(Canvas canvas) {
        if (isFoundTouchedSubView()) {
            // 计算新的半径和alpha值
            mRadius += mRadiusStep;
            mColorAlpha -= mAlphaStep;

            // 裁剪一块区域,这块区域就是被点击的View的区域.通过clipRect来获取这块区域,使得绘制操作只能在这个区域范围内的进行,
            // 即使绘制的内容大于这块区域,那么大于这块区域的绘制内容将不可见. 这样保证了背景层只能绘制在被点击的视图的区域
            canvas.clipRect(mTargetRectf);
            mPaint.setAlpha(mColorAlpha);
            // 绘制背景圆形,也就是
            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mRadius, mPaint);
        }

        if (isAnimEnd()) {
            reset();
        } else {
            invalidateDelayed();
        }
    }

    /**
     * 发送重绘消息
     */
    private void invalidateDelayed() {
        this.postDelayed(new Runnable() {

            @Override
            public void run() {
                invalidate();
            }
        }, mFrameRate);
    }

    /**
     * 判断是否找到被点击的子视图
     * 
     * @return
     */
    private boolean isFoundTouchedSubView() {
        return mCenterPoint != null && mTargetRectf != null;
    }

    private void reset() {
        mCenterPoint = null;
        mTargetRectf = null;
        mRadius = DEFAULT_RADIUS;
        mColorAlpha = mBackupAlpha;
        invalidate();
    }

}
