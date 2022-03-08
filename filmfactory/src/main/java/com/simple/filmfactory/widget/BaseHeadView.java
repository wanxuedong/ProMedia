package com.simple.filmfactory.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.simple.filmfactory.R;
import com.simple.filmfactory.utils.SV;


/**
 * 基础头部控件
 **/

public class BaseHeadView extends RelativeLayout {

    /**
     * 设置左右俩边图片
     **/
    private int leftImg;
    private ImageView leftImageView;
    private int rightImg;
    private ImageView rightImageView;
    /**
     * 是否显示左侧图片
     **/
    private boolean showLeftImg;
    /**
     * 设置左中右标题和字体颜色
     **/
    private String leftTitle;
    private String centerTitle;
    private String rightTitle;
    private int leftTitleColor;
    private int centerTitleColor;
    private int rightTitleColor;
    /**
     * 设置整体背景颜色
     **/
    private int bgColor;

    public BaseHeadView(Context context) {
        super(context);
    }

    public BaseHeadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public BaseHeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.base_include_head, BaseHeadView.this, true);
        //获取自定义属性
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BaseHeader);
        leftImg = typedArray.getResourceId(R.styleable.BaseHeader_leftImg, -1);
        rightImg = typedArray.getResourceId(R.styleable.BaseHeader_rightImg, -1);
        showLeftImg = typedArray.getBoolean(R.styleable.BaseHeader_showLeftImg, true);
        leftTitle = typedArray.getString(R.styleable.BaseHeader_leftTitle);
        centerTitle = typedArray.getString(R.styleable.BaseHeader_centerTitle);
        rightTitle = typedArray.getString(R.styleable.BaseHeader_rightTitle);
        leftTitleColor = typedArray.getColor(R.styleable.BaseHeader_leftTitleColor, -1);
        centerTitleColor = typedArray.getColor(R.styleable.BaseHeader_centerTitleColor, -1);
        rightTitleColor = typedArray.getColor(R.styleable.BaseHeader_rightTitleColor, -1);
        bgColor = typedArray.getColor(R.styleable.BaseHeader_bgColor, -1);
        typedArray.recycle();
        refreshView();
    }

    //刷新布局
    private void refreshView() {
        leftImageView = findViewById(R.id.left_img);
        if (leftImg != -1) {
            leftImageView.setImageResource(leftImg);
        }
        rightImageView = findViewById(R.id.right_img);
        if (rightImg != -1) {
            rightImageView.setImageResource(rightImg);
        }
        if (!showLeftImg) {
            findViewById(R.id.left_img).setVisibility(GONE);
        }
        SV.set((TextView) findViewById(R.id.left_title), leftTitle);
        SV.set((TextView) findViewById(R.id.center_title), centerTitle);
        SV.set((TextView) findViewById(R.id.right_title), rightTitle);
        if (leftTitleColor != -1) {
            ((TextView) findViewById(R.id.left_title)).setTextColor(leftTitleColor);
        }
        if (centerTitleColor != -1) {
            ((TextView) findViewById(R.id.center_title)).setTextColor(centerTitleColor);
        }
        if (rightTitleColor != -1) {
            ((TextView) findViewById(R.id.right_title)).setTextColor(rightTitleColor);
        }
        if (bgColor != -1) {
            findViewById(R.id.head_bg).setBackgroundColor(bgColor);
        }
    }

    public ImageView getLeftImageView() {
        return leftImageView;
    }

    public ImageView getRightImageView() {
        return rightImageView;
    }
}
