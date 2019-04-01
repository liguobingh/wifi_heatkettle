package com.yunmi.heatkettle.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.yunmi.heatkettle.utils.UMUtils;

/**
 * 水温圆环进度 View
 * Created by William on 2017/5/23.
 */

public class  UMWaterTempView extends View {
    private Context context;
    private int defaultSize;// 默认宽高值
    private final static int defaultPadding = 20;// 默认 Padding 值
    private final static float mStartAngle = 135f;// 圆环起始角度
    private final static float mSweepAngle = 270f;// 圆环扫过角度
    private Paint mArcPaint;// 圆环画笔
    private Paint mArcProgressPaint;// 进度圆环画笔
    private RectF mMiddleRect;// 外层矩形
    private RectF mMiddleProgressRect;// 进度矩形
    private float mCurrentAngle = 0f;// 当前进度
    private float mTotalAngle = 270f;// 总进度
    //    private int arcDistance;// 距离圆环的值
    //    private int circleDistance;// 距离外圆的值
    //    private Paint mCirclePaint;// 内层圆环画笔
    //    private Paint mTextPaint;// 文本画笔
    //    private Paint mBitmapPaint;// Bitmap 画笔
    //    private int radius;// 半径
    //    private int mMinTemp = 0;// 最小温度
    //    private int mMaxTemp = 950;// 最大温度
    //    private Bitmap mMinBitmap, mMaxBitmap;
    //    private Matrix matrix;
    //    private int mode = 3;   // 工作模式: 0.常温；1.温水；2.鲜开水

    public UMWaterTempView(Context context) {
        this(context, null);
        this.context = context;
    }

    public UMWaterTempView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
    }

    public UMWaterTempView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        defaultSize = UMUtils.dp2px(230, context);
//        arcDistance = UMUtils.dp2px(12, context);
//        circleDistance = UMUtils.dp2px(12, context);

        // 圆环画笔
        mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcPaint.setStrokeWidth(10);
        mArcPaint.setColor(Color.WHITE);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setAlpha(80);

        // 圆环进度画笔
        mArcProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcProgressPaint.setStrokeWidth(10);
        mArcProgressPaint.setColor(Color.WHITE);
        mArcProgressPaint.setStyle(Paint.Style.STROKE);
        mArcProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        // 圆形画笔
//        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mCirclePaint.setColor(Color.WHITE);
//        mCirclePaint.setAlpha(40);

        // 正中间字体画笔
//        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mTextPaint.setColor(Color.WHITE);
//        mTextPaint.setStyle(Paint.Style.FILL);
//        mTextPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/FZLTXHK.ttf"));
//        mTextPaint.setTextAlign(Paint.Align.CENTER);

        // Bitmap 画笔
//        mBitmapPaint = new Paint();
//        mBitmapPaint.setStyle(Paint.Style.FILL);
//        mBitmapPaint.setAntiAlias(true);

//        matrix = new Matrix();
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inScaled = false;
//        if (UMUtils.getDensity(context) <= 2.0f)
//            options.inSampleSize = 2;
//        else options.inSampleSize = 1;

//        BufferedInputStream bisMin = new BufferedInputStream(getResources().openRawResource(+R.drawable.icon_water_temp_min, new TypedValue()));
//        BufferedInputStream bisMax = new BufferedInputStream(getResources().openRawResource(+R.drawable.icon_water_temp_max, new TypedValue()));

//        mMinBitmap = BitmapFactory.decodeResourceStream(getResources(), new TypedValue(), bisMin, null, options);
//        mMaxBitmap = BitmapFactory.decodeResourceStream(getResources(), new TypedValue(), bisMax, null, options);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(resolveMeasure(widthMeasureSpec, defaultSize),
                resolveMeasure(heightMeasureSpec, defaultSize));
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        // radius = this.width / 2;
        mMiddleRect = new RectF(
                defaultPadding, defaultPadding,
                width - defaultPadding, height - defaultPadding);

        mMiddleProgressRect = new RectF(
                defaultPadding, defaultPadding,
                width - defaultPadding, height - defaultPadding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawMiddleArc(canvas);
        drawRingProgress(canvas);
//        drawCircle(canvas);
//        drawText(canvas);
//        drawBitmap(canvas);
    }

//    /**
//     * 绘制文本
//     */
//    private void drawText(Canvas canvas) {
////        mTextPaint.setTextSize(height * 32 / 100);
////        Paint.FontMetrics a = mTextPaint.getFontMetrics();
////        float descent = a.descent;
////        float tWidth = mTextPaint.measureText("00", 0, 2);
////        float tHeight = (getHeight() - mTextPaint.getTextSize()) / 2 + descent;
////        float y = (getHeight() + mTextPaint.getTextSize()) / 2 - descent + defaultPadding;
////        String str = mMinTemp == 0 ? "--" : String.valueOf(mMinTemp);
//        // 绘制温度
////        canvas.drawText(str, width / (float) 2, y, mTextPaint);
//
////        mTextPaint.setTextSize(height * 8 / 100);
//        // 绘制单位
////        canvas.drawText("℃", width / (float) 2 + tWidth / (float) 2 + 12.0f, tHeight, mTextPaint);
//
//        mTextPaint.setTextSize(height * 7 / 100);
//        // 出水温度
//        String str = context.getResources().getString(R.string.um_water_temp);
//        switch (mode) {
//            case 0:
//                str = context.getResources().getString(R.string.um_water_temp_normal);
//                break;
//            case 1:
//                str = context.getResources().getString(R.string.um_water_temp_warm);
//                break;
//            case 2:
//                str = context.getResources().getString(R.string.um_water_temp_hot);
//                break;
//        }
//        canvas.drawText(str, width / (float) 2, height - defaultPadding, mTextPaint);
//    }

//    /**
//     * 绘制 Bitmap
//     */
//    private void drawBitmap(Canvas canvas) {
//        matrix.reset();
//        matrix.postTranslate(0, height / 2 - mMinBitmap.getHeight());
//        canvas.drawBitmap(mMinBitmap, matrix, mBitmapPaint);
//
//        matrix.reset();
//        matrix.postTranslate(width - mMaxBitmap.getWidth(), height / 2 - mMaxBitmap.getHeight());
//        canvas.drawBitmap(mMaxBitmap, matrix, mBitmapPaint);
//    }

//    /**
//     * 绘制圆形
//     */
//    private void drawCircle(Canvas canvas) {
//        canvas.drawCircle(width / 2, height / 2, radius - defaultPadding - 2 * arcDistance, mCirclePaint);  // 外圆
//        canvas.drawCircle(width / 2, height / 2, radius - defaultPadding - 2 * arcDistance - circleDistance, mCirclePaint);   // 内圆
//    }

    /**
     * 绘制圆环
     */
    private void drawMiddleArc(Canvas canvas) {
        canvas.drawArc(mMiddleRect, mStartAngle, mSweepAngle, false, mArcPaint);
    }

    /**
     * 绘制进度圆环
     */
    private void drawRingProgress(Canvas canvas) {
        Path path = new Path();
        path.addArc(mMiddleProgressRect, mStartAngle, mCurrentAngle);
        canvas.drawPath(path, mArcProgressPaint);
    }

    /**
     * 根据传入的值进行测量
     */
    public int resolveMeasure(int measureSpec, int defaultSize) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = defaultSize;
                break;
            case MeasureSpec.AT_MOST:
                // 设置 warp_content 时设置默认值
                result = Math.min(specSize, defaultSize);
                break;
            case MeasureSpec.EXACTLY:
                // 设置 math_parent 和设置了固定宽高值
                break;
            default:
                result = defaultSize;
        }
        return result;
    }

    public void setValues(int values) {
        if (values <= 0) {
//            mMaxTemp = values;
            mTotalAngle = 0f;
        } else if (values > 0 && values < 90) {
//            mMaxTemp = values;
            mTotalAngle = values * (270f / 90);
        } else {
//            mMaxTemp = values;
            mTotalAngle = 270f;
        }
        startAnim();
    }

    public void startAnim() {
        if (this.getAnimation() != null)
            this.clearAnimation();
        // 画弧动画
        ValueAnimator mAngleAnim = ValueAnimator.ofFloat(mCurrentAngle, mTotalAngle);
        mAngleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        mAngleAnim.setDuration(2000);
        mAngleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCurrentAngle = (float) valueAnimator.getAnimatedValue();
                postInvalidate();
            }
        });
        mAngleAnim.start();
        // 数字动画
//        ValueAnimator mNumAnim = ValueAnimator.ofInt(mMinTemp, mMaxTemp);
//        mNumAnim.setDuration(2000);
//        mNumAnim.setInterpolator(new LinearInterpolator());
//        mNumAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                mMinTemp = (int) valueAnimator.getAnimatedValue();
//                postInvalidate();
//            }
//        });
//        mNumAnim.start();
    }
//    public void setMode(int mode) {
//        this.mode = mode;
//    }
}
