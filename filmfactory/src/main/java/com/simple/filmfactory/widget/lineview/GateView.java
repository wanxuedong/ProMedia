package com.simple.filmfactory.widget.lineview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.simple.filmfactory.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 导航栏
 *
 * @author simpo
 */
public class GateView extends LinearLayout {

    private Context context;

    //文字属性
    //选中时文字的颜色和大小
    private int choseColor = Color.GRAY;
    private int unchoseColor = Color.GRAY;
    //未选中时文字的颜色和大小
    private int choseSize = 0;
    private int unchoseSize = 0;
    //默认选中第1个
    private int defaultChose = 1;

    //导航栏线条属性
    private MoveLine moveLine;
    //线条的颜色，高度，宽度，与文字之间的间隔
    private int lineColor = Color.GRAY;
    private int lineHeight = 0;
    private int lineWidth = 0;
    private int line_padding = 0;

    private OnNavigateLisenter onNavigateLisenter;

    private List<GateHolder> views = new ArrayList<>();
    private TypedArray ta;

    public GateView(Context context) {
        super(context);
        init(context);
    }

    public GateView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ta = context.obtainStyledAttributes(attrs, R.styleable.gateView);
        choseColor = ta.getInteger(R.styleable.gateView_chose_color, Color.GRAY);
        unchoseColor = ta.getInteger(R.styleable.gateView_unchose_color, Color.GRAY);
        lineColor = ta.getInteger(R.styleable.gateView_line_color, Color.GRAY);
        defaultChose = ta.getInteger(R.styleable.gateView_default_chose, 1);
        choseSize = (int) ta.getDimension(R.styleable.gateView_chose_size, ScreenUtil.dip2px(context, 8));
        unchoseSize = (int) ta.getDimension(R.styleable.gateView_unchose_size, ScreenUtil.dip2px(context, 8));
        lineHeight = (int) ta.getDimension(R.styleable.gateView_line_height, ScreenUtil.dip2px(context, 2));
        lineWidth = (int) ta.getDimension(R.styleable.gateView_line_width, -1);
        line_padding = (int) ta.getDimension(R.styleable.gateView_line_padding, ScreenUtil.dip2px(context, 2));
        init(context);
    }

    public GateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setOrientation(VERTICAL);
    }

    public void setOnNavigateLisenter(OnNavigateLisenter onNavigateLisenter) {
        this.onNavigateLisenter = onNavigateLisenter;
    }

    /**
     * 设置导航栏展示数据
     */
    public void setNavigation(List<String> list) {
        removeAllViews();
        views.clear();
        if (list == null || list.size() == 0) {
            return;
        }
        moveLine = new MoveLine(context, list.size(), lineColor, lineHeight, lineWidth, defaultChose);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setWeightSum(list.size());
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(HORIZONTAL);
        for (int i = 0; i < list.size(); i++) {
            TextView gateView = new TextView(context);
            gateView.setText(list.get(i));
            gateView.setGravity(Gravity.CENTER);
            GateHolder gateHolder = new GateHolder();
            gateHolder.setView(gateView);
            //默认选中第一个
            if (i == defaultChose - 1) {
                gateHolder.setChose(true);
                gateView.setTextSize(choseSize);
                gateView.setTextColor(choseColor);
            } else {
                gateHolder.setChose(false);
                gateView.setTextSize(unchoseSize);
                gateView.setTextColor(unchoseColor);
            }
            final int finalI = i;
            gateView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean chose = views.get(finalI).isChose();
                    moveLine.moveTo(finalI + 1);
                    if (chose) {
                        return;
                    }
                    if (onNavigateLisenter != null) {
                        onNavigateLisenter.click(finalI + 1);
                    }
                    for (int j = 0; j < views.size(); j++) {
                        if (j == finalI) {
                            views.get(j).setChose(true);
                            views.get(j).getView().setTextSize(choseSize);
                            views.get(j).getView().setTextColor(choseColor);
                        } else {
                            views.get(j).setChose(false);
                            views.get(j).getView().setTextSize(unchoseSize);
                            views.get(j).getView().setTextColor(unchoseColor);
                        }
                    }
                    invalidate();
                }
            });
            views.add(gateHolder);
            linearLayout.addView(gateView);
            LayoutParams layoutParams = (LayoutParams) gateView.getLayoutParams();
            layoutParams.weight = 1.0f;
            gateView.setLayoutParams(layoutParams);
        }
        addView(linearLayout);
        LayoutParams linear = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(linear);
        addView(moveLine);
        LayoutParams layoutParams = (LayoutParams) moveLine.getLayoutParams();
        layoutParams.setMargins(0, line_padding, 0, 0);
        moveLine.setLayoutParams(layoutParams);
    }

    /**
     * 绑定导航栏视图和是否选中关系
     **/
    public class GateHolder {
        private TextView view;
        private boolean chose;

        public TextView getView() {
            return view;
        }

        public void setView(TextView view) {
            this.view = view;
        }

        public boolean isChose() {
            return chose;
        }

        public void setChose(boolean chose) {
            this.chose = chose;
        }
    }

    public interface OnNavigateLisenter {
        void click(int position);
    }

}
