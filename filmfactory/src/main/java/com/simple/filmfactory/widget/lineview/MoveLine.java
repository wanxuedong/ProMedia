package com.simple.filmfactory.widget.lineview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.simple.filmfactory.R;


/**
 * 导航栏下面的可移动线条
 *
 * @author simpo*/
public class MoveLine extends LinearLayout {

    private Context context;
    private Paint paint;
    private int count = 5;      //默认标题栏总数量
    private LineView lineView;  //中间位置的导航线条
    private float screenWidth; //屏幕宽度
    private float leftMargin;
    private int lastPosition = 1;
    private int lineColor = Color.GRAY;
    private int lineHeight = 0;
    private int lineWidth = 0;
    private TypedArray ta;
    private int statu = 0;

    public MoveLine(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MoveLine(Context context, int count, int lineColor, int lineHeight, int lineWidth, int defaultChose) {
        super(context);
        this.context = context;
        this.count = count;
        this.lineColor = lineColor;
        this.lineHeight = lineHeight;
        this.lineWidth = lineWidth;
        this.lastPosition = defaultChose;
        init();
    }

    public MoveLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        ta = context.obtainStyledAttributes(attrs, R.styleable.viewMove);
        count = ta.getInteger(R.styleable.viewMove_view_count, 5);
        lineColor = ta.getInteger(R.styleable.viewMove_view_color, Color.GRAY);
        lineHeight = (int) ta.getDimension(R.styleable.viewMove_view_height, ScreenUtil.dip2px(context, 3));
        lineWidth = (int) ta.getDimension(R.styleable.viewMove_view_width, -1);
        lastPosition = ta.getInteger(R.styleable.viewMove_defaultposition, 1);
        ta.recycle();
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        paint = new Paint();
        paint.setColor(Color.RED);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (lineWidth < 0) {
            lineWidth = getMeasuredWidth() / count;
        }
        if (lineHeight < 0) {
            lineHeight = ScreenUtil.dip2px(context, 3);
        }
        screenWidth = getMeasuredWidth();
        leftMargin = (screenWidth / count - lineWidth) / 2;
        if (statu == 0) {
            statu = 1;
            drawLine();
        }
    }

    private void drawLine() {
        if (lineWidth == -1) {
            lineWidth = getMeasuredWidth() / count;
        }
        lineView = new LineView(context, lineWidth, lineHeight);
        lineView.setLineColor(lineColor);
        addView(lineView);
        LayoutParams layoutParams = (LayoutParams) lineView.getLayoutParams();
        layoutParams.setMargins((int) (leftMargin + (lastPosition - 1) * (screenWidth / count)), 0, 0, 0);
        lineView.setLayoutParams(layoutParams);
    }

    private void moveLine(final View view, float fromPosition, float toPosition) {
        if (view != null) {
            float fromMargin = (fromPosition - 1) * (screenWidth / count) + leftMargin;
            float toMargin = (toPosition - 1) * (screenWidth / count) + leftMargin;
            MoveAnimate.marginValueAnimator(view, fromMargin, toMargin, 500, false, new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
                    layoutParams.setMargins((int) Float.parseFloat(valueAnimator.getAnimatedValue() + ""), 0, 0, 0);
                    lineView.setLayoutParams(layoutParams);
                }
            });
        }
    }

    //设置需要移动到的位置
    public void moveTo(int position) {
        if (position > count) {
            this.lastPosition = count;
        } else if (position < 1) {
            this.lastPosition = 1;
        } else {
            moveLine(lineView, lastPosition, position);
            this.lastPosition = position;
        }
    }

    //设置导航条总数
    public void setMoveNumber(int count) {
        if (count > 0) {
            this.count = count;
        }
    }

    //设置默认显示的位置
    public void setDefaultPosition(int position) {
        lastPosition = position;
    }

}
