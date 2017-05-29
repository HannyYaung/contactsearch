package com.hanny.contactsearch.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by fan on 2016/2/23.
 * 快速索引
 */
public class QuickIndexBar extends View {
    private Paint paint;
    private float mCellHeight;
    private int mWidth;

    //26英文字母
    private static final String[] LETTERS = new String[]{"A", "B", "C", "D",
            "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
            "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#", " "};

    private int mHeight;
    private float mTextHeight;
    private int currentIndex = -1;

    private OnLetterChangeListener onLetterChangeListener;

    public OnLetterChangeListener getOnLetterChangeListener() {
        return onLetterChangeListener;
    }

    public void setOnLetterChangeListener(OnLetterChangeListener onLetterChangeListener) {
        this.onLetterChangeListener = onLetterChangeListener;
    }

    //暴露接口
    public interface OnLetterChangeListener {
        void onLetterChange(String letter);

        void onReset();
    }

    public QuickIndexBar(Context context) {
        this(context, null);
    }

    public QuickIndexBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickIndexBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();

//        画笔默认是 黑色  设置为白色

        //设置字体大小
        paint.setTextSize(dip2px(context, 14));

        //抗锯齿
        paint.setAntiAlias(true);

        // 获取字体的高度
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();

        //  下边界  - 上边界

        //ceil 天花板    0.1  1
        mTextHeight = (float) Math.ceil(fontMetrics.descent - fontMetrics.ascent);
    }

    // 测量完成  改变的时候调用
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //获取测量后的宽度和高度
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        //每个字母的高度
        mCellHeight = mHeight * 1.0f / LETTERS.length;
    }

    // 怎么画
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //遍历并绘制26英文字母
        for (int i = 0; i < LETTERS.length; i++) {
            String text = LETTERS[i];

            //测量字体宽度
            float mTextWidth = paint.measureText(text);

            //获取字母的xy坐标，坐标默认为字母左下角
            float x = mWidth / 2 - mTextWidth / 2;
            float y = mCellHeight / 2 + mTextHeight / 2 + mCellHeight * i;

            //判断当前索引并绘制相应的颜色
            if (currentIndex == i) {
                //当索引为当前的字母时绘制的颜色
                paint.setColor(Color.parseColor("#ff933e"));
            } else {
                paint.setColor(Color.parseColor("#454545"));
            }
            // 字.画字()；
            canvas.drawText(text, x, y, paint);
        }
    }

    //触摸事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 计算当前点击的 字母
                float downY = event.getY();
                // 1.1  ---  1  1.4 --- 1  1.5 --- 1
                currentIndex = (int) (downY / mCellHeight);
                if (currentIndex < 0 || currentIndex > LETTERS.length - 1) {
                } else {
//                    Utils.showToast(getContext(), LETTERS[currentIndex]);
                    if (onLetterChangeListener != null) {
                        onLetterChangeListener.onLetterChange(LETTERS[currentIndex]);
                    }
                }

                //重新绘制
//                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                // 计算当前点击的 字母
                float moveY = event.getY();
                currentIndex = (int) (moveY / mCellHeight); // 1.1  ---  1  1.4 --- 1  1.5 --- 1

                if (currentIndex < 0 || currentIndex > LETTERS.length - 1) {
                } else {
                    if (onLetterChangeListener != null) {
                        onLetterChangeListener.onLetterChange(LETTERS[currentIndex]);
                    }
                }
                //重新绘制
//                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                currentIndex = -1;
                if (onLetterChangeListener != null) {
                    onLetterChangeListener.onReset();
                }

                break;
        }
        //重新绘制
        invalidate();

        //   返回true  为了收到 move  & up 事件
        return true;
    }


    /**
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}

