package com.simple.filmfactory.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.simple.filmfactory.R;

/**
 * 展示圆形图片控件
 * 可以自由设置四角的圆角半径
 * 可以设置圆框宽度和颜色
 *
 * @author wanxuedong
 */
public class RotatingCircleView extends AppCompatImageView {

    private static final int COLORDRAWABLE_DIMENSION = 1;
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int ALL = 0x1111;
    private static final int TOP_LEFT = 0x0001;
    private static final int TOP_RIGHT = 0x0010;
    private static final int BOTTOM_LEFT = 0x0100;
    private static final int BOTTOM_RIGHT = 0x1000;

    private Paint imgPaint;
    private RectF imgRect;
    /**
     * 获取到的图片
     **/
    private Bitmap bitmap;
    /**
     * 获取到图形宽高
     **/
    private int bitMapWidth, bitMapHeight;
    /**
     * 最终图形展示半径
     **/
    private float imgRadius;


    /**
     * 外围线条的部分属性
     **/
    private Paint borderPaint;
    private RectF borderRect;
    private float borderRadius;
    private int borderColor;
    private int borderWidth;

    /**
     * 设置图片的缩放与平移参数
     **/
    private BitmapShader bitmapShader;
    private Matrix matrix;
    /**
     * 是否通过初始化设置图片
     **/
    private boolean hasInit;
    /**
     * 是否通过后期进行设置图片
     */
    private boolean hasSetImg;
    /**
     * 当绘制的不是一个正圆时，菱角的半径
     **/
    private int chestnutRadius = 0;
    /**
     * 当前是全圆还是周边圆，0x1111为全圆,暂且只支持俩个并列属性
     **/
    private int circleKind = ALL;


    public RotatingCircleView(Context context) {
        super(context);
        init();
    }

    public RotatingCircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public RotatingCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //从attrs.xml中获取自定义的属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RotatingCircleView, defStyleAttr, 0);

        borderWidth = a.getDimensionPixelSize(R.styleable.RotatingCircleView_circle_width, 0);
        borderColor = a.getColor(R.styleable.RotatingCircleView_circle_color, 0);
        circleKind = a.getInt(R.styleable.RotatingCircleView_circle_kind, 0x1111);
        chestnutRadius = a.getDimensionPixelSize(R.styleable.RotatingCircleView_circle_radius, 0);
        //回收属性，避免对下次的使用造成影响
        a.recycle();
        init();
    }

    private void init() {
        setScaleType(ScaleType.CENTER_CROP);
        imgPaint = new Paint();
        imgRect = new RectF();
        borderRect = new RectF();
        borderPaint = new Paint();
        hasInit = true;
        matrix = new Matrix();
        //下面调用了initBitMap为了保证到图片一开始就被设置好了也能够成圆形展示
        if (hasSetImg) {
            initBitMap();
            hasSetImg = false;
        }
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
    }

    @Override
    public ScaleType getScaleType() {
        return ScaleType.CENTER_CROP;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }

        //绘制全圆
        if (circleKind == ALL) {
            //画圆（圆形头像部分）
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, imgRadius, imgPaint);
            //若设置了圆的边框宽度，将边框画出来
            if (borderWidth != 0) {
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, borderRadius, borderPaint);
            }
        } else {     //绘制部分圆
            Path path = new Path();
            float[] radi = {chestnutRadius, chestnutRadius, chestnutRadius, chestnutRadius, chestnutRadius, chestnutRadius, chestnutRadius, chestnutRadius};
            switch (circleKind) {
                case TOP_LEFT:
                    radi = new float[]{chestnutRadius, chestnutRadius, 0, 0, 0, 0, 0, 0};
                    break;
                case TOP_RIGHT:
                    radi = new float[]{0, 0, chestnutRadius, chestnutRadius, 0, 0, 0, 0};
                    break;
                case BOTTOM_RIGHT:
                    radi = new float[]{0, 0, 0, 0, chestnutRadius, chestnutRadius, 0, 0};
                    break;
                case BOTTOM_LEFT:
                    radi = new float[]{0, 0, 0, 0, 0, 0, chestnutRadius, chestnutRadius};
                    break;
                case TOP_LEFT | TOP_RIGHT:
                    radi = new float[]{chestnutRadius, chestnutRadius, chestnutRadius, chestnutRadius, 0, 0, 0, 0};
                    break;
                case TOP_LEFT | BOTTOM_LEFT:
                    radi = new float[]{chestnutRadius, chestnutRadius, 0, 0, 0, 0, chestnutRadius, chestnutRadius};
                    break;
                case TOP_LEFT | BOTTOM_RIGHT:
                    radi = new float[]{chestnutRadius, chestnutRadius, 0, 0, chestnutRadius, chestnutRadius, 0, 0};
                    break;
                case TOP_RIGHT | BOTTOM_RIGHT:
                    radi = new float[]{0, 0, chestnutRadius, chestnutRadius, chestnutRadius, chestnutRadius, 0, 0};
                    break;
                case TOP_RIGHT | BOTTOM_LEFT:
                    radi = new float[]{0, 0, chestnutRadius, chestnutRadius, 0, 0, chestnutRadius, chestnutRadius};
                    break;
                case BOTTOM_LEFT | BOTTOM_RIGHT:
                    radi = new float[]{0, 0, 0, 0, chestnutRadius, chestnutRadius, chestnutRadius, chestnutRadius};
                    break;
                default:
            }
            path.addRoundRect(imgRect, radi, Path.Direction.CCW);
            canvas.drawPath(path, imgPaint);
            canvas.drawPath(path, borderPaint);
        }
    }

    /**
     * 重写以下四个设置图片的方法是为了,保证当图片不是一开始就设置好也能成圆形展示
     **/
    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        bitmap = bm;
        initBitMap();
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        bitmap = getBitmapFromDrawable(getDrawable());
        initBitMap();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        bitmap = getBitmapFromDrawable(getDrawable());
        initBitMap();
    }

    @Override
    public void setImageURI(@Nullable Uri uri) {
        super.setImageURI(uri);
        bitmap = getBitmapFromDrawable(getDrawable());
        initBitMap();
    }

    /**
     * 重写保证每次重新设置图片可以成功展示
     **/
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initBitMap();
    }

    /**
     * 这里的获取测量模式获取会不准确，详情请见：https://blog.csdn.net/baidu_34928905/article/details/79017089
     **/
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                break;
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.UNSPECIFIED:
                widthSize = Math.min(bitMapHeight, bitMapWidth);
            default:
        }
        switch (heightMode) {
            case MeasureSpec.AT_MOST:
                break;
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.UNSPECIFIED:
                heightSize = Math.min(bitMapHeight, bitMapWidth);
            default:
        }
        setMeasuredDimension(widthSize, heightSize);
    }


    private void initBitMap() {

        if (!hasInit) {
            hasSetImg = true;
            return;
        }

        if (bitmap == null) {
            return;
        }

        bitMapWidth = bitmap.getWidth();
        bitMapHeight = bitmap.getHeight();

        //设置好图片展示属性
        bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        imgPaint.setAntiAlias(true);
        imgPaint.setShader(bitmapShader);

        //设置好外圈线条参数
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(borderWidth);

        borderRect.set(0, 0, getWidth(), getHeight());
        borderRadius = Math.min((borderRect.height() - borderWidth) / 2, (borderRect.width() - borderWidth) / 2);

        //根据控件宽高确定最大圆形展示半径
        imgRect.set(borderWidth, borderWidth, borderRect.width() - borderWidth, borderRect.height() - borderWidth);
        imgRadius = Math.min(imgRect.height() / 2, imgRect.width() / 2);

        updateShaderMatrix();

    }

    /**
     * 判断好最终的缩放级别和平移参数，就设置到bitmapShader上
     **/
    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;
        matrix.set(null);
        if (bitMapWidth * imgRect.height() > imgRect.width() * bitMapHeight) {
            scale = imgRect.height() / (float) bitMapHeight;
            dx = (imgRect.width() - bitMapWidth * scale) * 0.5f;
        } else {
            scale = imgRect.width() / (float) bitMapWidth;
            dy = (imgRect.height() - bitMapHeight * scale) * 0.5f;
        }
        matrix.setScale(scale, scale);
        matrix.postTranslate((int) (dx + 0.5f) + borderWidth, (int) (dy + 0.5f) + borderWidth);
        bitmapShader.setLocalMatrix(matrix);
        invalidate();
    }

    /**
     * 将图片Drawable转成bitmap
     **/
    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        try {
            Bitmap bitmap;
            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                //获取图片固有的宽高
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

}
