package com.common.refreshview;

/**
 * 下拉刷新状态管理器
 *
 * Created by chenxunlin_91 on 2015/4/20.
 */
public class PullToRefreshStatusManager {

    public final static byte REFRESH_STATUS_INIT = 1;
    public final static byte REFRESH_STATUS_LOADING = 2;
    private byte mStatus = REFRESH_STATUS_INIT;

    private PullToRefreshView mPullToRefreshView;
    private PullToRefreshLinstener mPullToRefreshLinstener;

    public PullToRefreshStatusManager(PullToRefreshView pullToRefreshView){
        this.mPullToRefreshView = pullToRefreshView;
    }

    public void setPullToRefreshLinstener(PullToRefreshLinstener pullToRefreshLinstener){
        this.mPullToRefreshLinstener = pullToRefreshLinstener;
    }

    boolean isLoading(){
        return mStatus == REFRESH_STATUS_LOADING;
    }

    public boolean performRefresh(){
        if(mPullToRefreshLinstener == null){
            return false;
        }

        return mPullToRefreshLinstener.onRefreshing();
    }

    void tryToPerformRefresh(){
        if(mStatus!=REFRESH_STATUS_INIT){
            return;
        }

        if(mPullToRefreshView.mPullToRefreshIndicator.isOverOffsetToRefresh()){
            if(performRefresh()){
                mStatus = REFRESH_STATUS_LOADING;
            }
        }
    }

    final public void onRefreshComplete(){
        mStatus = REFRESH_STATUS_INIT;
        if(mPullToRefreshView.mPullToRefreshTouchManager.isRunning()){
            return;
        }

        mPullToRefreshView.mPullToRefreshTouchManager.tryScrollBackToTop();
    }
}
