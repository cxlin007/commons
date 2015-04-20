package com.common.refreshview;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


/**
 * 下拉刷新视图
 * Created by chenxunlin_91 on 2015/4/13.
 */
public class PullToRefreshView extends ViewGroup {
    /**
     * 头部布局id
     */
    int mHeaderId;
    /**
     * 内容布局id
     */
    int mContainterId;

    View mHeaderView;

    View mContent;

    private int mDurationToClose = 200;

    PullToRefreshLayoutManager mPullToRefreshLayoutManager;
    PullToRefreshTouchManager mPullToRefreshTouchManager;
    PullToRefreshIndicator mPullToRefreshIndicator;
    PullToRefreshStatusManager mPullToRefreshStatusManager;


    public PullToRefreshView(Context context) {
        this(context, null);
    }

    public PullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);

        mPullToRefreshIndicator = new PullToRefreshIndicator();
        mPullToRefreshLayoutManager = new PullToRefreshLayoutManager(this);
        mPullToRefreshTouchManager = new PullToRefreshTouchManager(this,mPullToRefreshIndicator);
        mPullToRefreshStatusManager = new PullToRefreshStatusManager(this);
    }

    public void setPullToRefreshLinstener(PullToRefreshLinstener pullToRefreshLinstener){
        mPullToRefreshStatusManager.setPullToRefreshLinstener(pullToRefreshLinstener);
    }

    @Override
    protected void onFinishInflate() {
        mPullToRefreshLayoutManager.initLayout();
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mPullToRefreshLayoutManager.measureLayout(widthMeasureSpec,heightMeasureSpec);
        int headerHeight = mHeaderView.getMeasuredHeight();
        mPullToRefreshIndicator.setHeaderHeight(headerHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mPullToRefreshLayoutManager.layout(changed, l, t, r, b);
    }

    public boolean dispatchTouchEventSupper(MotionEvent e) {
        return super.dispatchTouchEvent(e);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return super.dispatchTouchEvent(ev);
        }

       return mPullToRefreshTouchManager.dispatchTouchEvent(ev);
    }

   void onRelease(){
       mPullToRefreshStatusManager.tryToPerformRefresh();
        if(mPullToRefreshStatusManager.isLoading() && mPullToRefreshIndicator.isOverOffsetToRefresh()){
            mPullToRefreshTouchManager.tryToScrollTo(mPullToRefreshIndicator.getHeaderHeight(),mDurationToClose);

        }else{
            mPullToRefreshTouchManager.tryScrollBackToTop();
        }
    }

    public void refreshComplete(){
        mPullToRefreshStatusManager.onRefreshComplete();
    }

}
