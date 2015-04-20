package com.common.refreshview;

import android.graphics.PointF;

/**
 *  下拉刷新指示器
 * Created by chenxunlin_91 on 2015/4/16.
 */
public class PullToRefreshIndicator {

    /**
     * 上一次移动的位置
     */
    private PointF mLastMove = new PointF();
    /**
     * 按下时视图的位置
     */
    private int mPressedPos = 0;
    /**
     * 当前视图位置
     */
    private int mCurrentPos = 0;
    /**
     * 上一次的视图位置
     */
    private int mLastPos = 0;
    /**
     * 起始位置
     */
    public final static int POS_START = 0;

    private int mOffsetToRefresh = 0;
    private float mResistance = 1.7f;
    private float mRatioOfHeaderHeightToRefresh = 1.2f;
    private float mOffsetX;
    private float mOffsetY;
    private boolean mIsUnderTouch = false;
    private int mHeaderHeight;

    private void setOffset(float x,float y){
        mOffsetX = x;
        mOffsetY = y;
    }

    public float getOffsetX(){
        return mOffsetX;
    }

    public float getOffsetY(){
        return mOffsetY;
    }

    public void onRelease() {
        mIsUnderTouch = false;
    }

    public void onPressDown(float x,float y){
        mIsUnderTouch = true;
        mPressedPos = mCurrentPos;
        mLastMove.set(x,y);
    }

    public void onMove(float x,float y){
        float offsetX = x - mLastMove.x;
        float offsetY = y - mLastMove.y;
        processOnMove(offsetX,offsetY);
        mLastMove.set(x,y);
    }

    private void processOnMove(float mOffsetX,float mOffsetY){
        setOffset(mOffsetX,mOffsetY/mResistance);
    }

    /**
     * 是否视图已经在起始方向移动
     * @return
     */
    public boolean hasStartPosition(){
        return mCurrentPos > POS_START;
    }

    public boolean isInStartPosition(){
        return mCurrentPos == POS_START;
    }

    public boolean willOverTop(int to){
        return to < POS_START;
    }

    public boolean hasMovedAfterPressedDown(){
        return mCurrentPos!= mPressedPos;
    }

    public boolean hasJustBackToStartPosition() {
        return mLastPos != POS_START && isInStartPosition();
    }

    public boolean isOverOffsetToRefresh(){
        return mCurrentPos >=getOffsetToRefresh();
    }

    public int getOffsetToRefresh() {
        return mOffsetToRefresh;
    }
    public boolean hasJustStartPosition(){
        return mLastPos == POS_START && hasStartPosition();
    }

    public boolean isAlreadyHere(int to){
        return mCurrentPos == to;
    }

    public int getCurrentPosY(){
        return mCurrentPos;
    }

    public void setHeaderHeight(int headerHeight){
        mHeaderHeight = headerHeight;
        updatOffsetToRefreshHeight();
    }

    public int getHeaderHeight(){
        return mHeaderHeight;
    }

    public void updatOffsetToRefreshHeight(){
        mOffsetToRefresh =(int)(mHeaderHeight*mRatioOfHeaderHeightToRefresh);
    }

    /**
     * Update current position before update the UI
     */
    public void setCurrentPos(int current){
        mLastPos = mCurrentPos;
        mCurrentPos = current;
    }

    public int getLastPosY(){
        return mLastPos;
    }

    public boolean isUnderTouch() {
        return mIsUnderTouch;
    }


}
