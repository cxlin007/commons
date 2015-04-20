package com.common.refreshview;

import android.view.View;
import android.view.ViewGroup;

/**
 *  处理布局相关操作
 *
 * Created by chenxunlin_91 on 2015/4/20.
 */
public class PullToRefreshLayoutManager {

    PullToRefreshView mPullToRefreshView;

    public PullToRefreshLayoutManager(PullToRefreshView pullToRefreshView){
        this.mPullToRefreshView = pullToRefreshView;
    }

    /**
     * 初始化布局
     */
    public void initLayout(){
        final int childCount = mPullToRefreshView.getChildCount();
        if (childCount != 2) {
            throw new IllegalStateException("pullToRefreshView only can host 2 elements");
        }

        if (mPullToRefreshView.mHeaderId != 0 && mPullToRefreshView.mHeaderView == null) {
            mPullToRefreshView.mHeaderView = mPullToRefreshView.findViewById(mPullToRefreshView.mHeaderId);
        }

        if (mPullToRefreshView.mContainterId != 0 && mPullToRefreshView.mContent == null) {
            mPullToRefreshView.mContent = mPullToRefreshView.findViewById(mPullToRefreshView.mContainterId);
        }

        if (mPullToRefreshView.mHeaderView == null || mPullToRefreshView.mContent == null) {
            mPullToRefreshView.mHeaderView = mPullToRefreshView.getChildAt(0);
            mPullToRefreshView.mContent = mPullToRefreshView.getChildAt(1);
        }

        mPullToRefreshView.mHeaderView.bringToFront();//把头部视图移到子组件列表的首位
    }

    /**
     * 计算布局
     */
    public void measureLayout(int widthMeasureSpec, int heightMeasureSpec){
         measureChild(mPullToRefreshView.mHeaderView, widthMeasureSpec, heightMeasureSpec);
         measureChild(mPullToRefreshView.mContent, widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 计算子组件的大小
     *
     * @param child
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    private void measureChild(View child, int widthMeasureSpec, int heightMeasureSpec) {
        final ViewGroup.LayoutParams lp = child.getLayoutParams();

        final int childWidthMeasureSpec = mPullToRefreshView.getChildMeasureSpec(widthMeasureSpec,
                mPullToRefreshView.getPaddingLeft() + mPullToRefreshView.getPaddingRight(), lp.width);
        final int childHeightMeasureSpec = mPullToRefreshView.getChildMeasureSpec(heightMeasureSpec,
                mPullToRefreshView.getPaddingTop() + mPullToRefreshView.getPaddingBottom(), lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    /**
     * 视图布局
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    public void layout(boolean changed, int l, int t, int r, int b){
        int height = mPullToRefreshView.getMeasuredHeight();
        int width = mPullToRefreshView.getMeasuredWidth();
        int left = mPullToRefreshView.getPaddingLeft();
        int top = mPullToRefreshView.getPaddingTop();
        int right = mPullToRefreshView.getPaddingRight();
        int bottom = mPullToRefreshView.getPaddingBottom();

        mPullToRefreshView.mContent.layout(left, top, left + width - right, top + height - bottom);
        int mRefreshViewHeight = mPullToRefreshView.mHeaderView.getMeasuredHeight();
        mPullToRefreshView.mHeaderView.layout(left, top - mRefreshViewHeight, left + width - right, top);
    }



}
