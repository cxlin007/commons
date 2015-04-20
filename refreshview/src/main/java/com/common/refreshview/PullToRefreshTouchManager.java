package com.common.refreshview;

import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.Scroller;

/**
 * 下拉刷新触摸管理器
 * Created by chenxunlin_91 on 2015/4/20.
 */
public class PullToRefreshTouchManager {

    private static final int INVALID_POINTER = -1;
    private int mDurationToCloseHeader = 1000;
    private int mActivePointerId;
    private PullToRefreshView mPullToRefreshView;
    private PullToRefreshIndicator mPullToRefreshIndicator;
    private MotionEvent mLastMoveEvent;
    private boolean mHasSendCancelEvent = false;
    /**
     * 处理放开手指后的动画
     */
    private ScrollChecker mScrollChecker;

    public PullToRefreshTouchManager(PullToRefreshView pullToRefreshView,PullToRefreshIndicator pullToRefreshIndicator){
        this.mPullToRefreshView = pullToRefreshView;
        mPullToRefreshIndicator = pullToRefreshIndicator;
        mScrollChecker = new ScrollChecker();
    }

    public boolean dispatchTouchEvent(MotionEvent ev){
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                int index = 0;
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                float x = MotionEventCompat.getX(ev, index);
                float y = MotionEventCompat.getY(ev, index);
                mPullToRefreshIndicator.onPressDown(x, y);
                mScrollChecker.abortIfWorking();
                //判断是否要传给子视图
                if (mPullToRefreshIndicator.hasStartPosition()) {
                    // do nothing, intercept child event
                } else {
                    mPullToRefreshView.dispatchTouchEventSupper(ev);
                }

                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                mLastMoveEvent = ev;

                if (mActivePointerId == INVALID_POINTER) {
                    return mPullToRefreshView.dispatchTouchEventSupper(ev);
                }

                final int index = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (index < 0) {
                    return mPullToRefreshView.dispatchTouchEventSupper(ev);
                }

                //判断是否要移动整个布局还是子布局自身移动
                float x = MotionEventCompat.getX(ev, index);
                float y = MotionEventCompat.getY(ev, index);

                mPullToRefreshIndicator.onMove(x,y);

                float offsetX = mPullToRefreshIndicator.getOffsetX();
                float offsetY = mPullToRefreshIndicator.getOffsetY();
                boolean moveDown = offsetY > 0;
                boolean moveUp = !moveDown;
                boolean canMoveUp = mPullToRefreshIndicator.hasStartPosition();

                //判断是否要移动整个布局还是子布局自身移动
                if(moveDown && canChildScrollUp(mPullToRefreshView.mContent)){
                    return mPullToRefreshView.dispatchTouchEventSupper(ev);
                }

                if ((moveUp && canMoveUp) || moveDown) {
                    movePos(offsetY);
                    return true;
                }

                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = -1;
                mPullToRefreshIndicator.onRelease();
                if(mPullToRefreshIndicator.hasStartPosition()){
                    mPullToRefreshView.onRelease();
                }
                if (mPullToRefreshIndicator.hasMovedAfterPressedDown()) {
                    sendCancelEvent();
                    return true;
                }

                break;

            case MotionEventCompat.ACTION_POINTER_DOWN:
                mHasSendCancelEvent = false;
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                float x = MotionEventCompat.getX(ev, pointerIndex);
                float y = MotionEventCompat.getY(ev, pointerIndex);
                mPullToRefreshIndicator.onPressDown(x, y);
                mScrollChecker.abortIfWorking();

                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        return mPullToRefreshView.dispatchTouchEventSupper(ev);
    }

    private void movePos(float deltaY){
        //已经到顶部
        if(deltaY<0 && mPullToRefreshIndicator.isInStartPosition()){
            return;
        }

        int to = mPullToRefreshIndicator.getCurrentPosY() + (int)deltaY;
        //over top
        if(mPullToRefreshIndicator.willOverTop(to)){
            to = mPullToRefreshIndicator.POS_START;
        }

        mPullToRefreshIndicator.setCurrentPos(to);
        int change = to - mPullToRefreshIndicator.getLastPosY();
        updatePos(change);
    }

    private void updatePos(int change){
        if(change == 0){
            return;
        }

        boolean isUnderTouch = mPullToRefreshIndicator.isUnderTouch();
        if(isUnderTouch && !mHasSendCancelEvent && mPullToRefreshIndicator.hasMovedAfterPressedDown()){
            mHasSendCancelEvent = true;
            sendCancelEvent();
        }

        // back to initiated position
        if(mPullToRefreshIndicator.hasJustBackToStartPosition()){
            // recover event to children
            if (isUnderTouch) {
                sendDownEvent();
            }
        }

        mPullToRefreshView.mContent.offsetTopAndBottom(change);
        mPullToRefreshView.mHeaderView.offsetTopAndBottom(change);
        mPullToRefreshView.invalidate();
    }

    private void sendCancelEvent() {
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        mPullToRefreshView.dispatchTouchEventSupper(e);
    }

    private void sendDownEvent() {
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        mPullToRefreshView.dispatchTouchEventSupper(e);
    }

    /**
     * 设置第二个触摸点移开后的操作
     *
     * @param ev
     */
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
            float x = MotionEventCompat.getX(ev, newPointerIndex);
            float y = MotionEventCompat.getY(ev, newPointerIndex);
            mPullToRefreshIndicator.onPressDown(x, y);
        }
    }

    private boolean canChildScrollUp(View view){
        if(android.os.Build.VERSION.SDK_INT < 14){
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return view.getScrollY() > 0;
            }

        }else{
            return view.canScrollVertically(-1);
        }
    }

    void tryToScrollTo(int to,int duration){
        mScrollChecker.tryToScrollTo(to,duration);
    }

    void tryScrollBackToTop(){
        if(!mPullToRefreshView.mPullToRefreshIndicator.isUnderTouch()){
            mPullToRefreshView.mPullToRefreshTouchManager.tryToScrollTo(PullToRefreshIndicator.POS_START, mDurationToCloseHeader);
        }
    }

    boolean isRunning(){
        return mScrollChecker.mIsRunning;
    }

    class ScrollChecker implements Runnable {

        private Scroller mScroller;
        private int mLastFlingY;
        private boolean mIsRunning = false;
        private int mStart;
        private int mTo;


        public ScrollChecker(){
            mScroller = new Scroller(mPullToRefreshView.getContext());
        }

        @Override
        public void run() {
            boolean finish = !mScroller.computeScrollOffset() || mScroller.isFinished();
            int curY = mScroller.getCurrY();
            int deltaY = curY - mLastFlingY;

            if(!finish){
                mLastFlingY = curY;
                movePos(deltaY);
                mPullToRefreshView.post(this);
            }else{
                finish();
            }
        }

        private void finish(){
            onScrollFinish();
            reset();
        }

        private void reset(){
            mIsRunning = false;
            mLastFlingY = 0;
            mPullToRefreshView.removeCallbacks(this);
        }

        public void abortIfWorking(){
            if(mIsRunning){
                if(!mScroller.isFinished()){
                    mScroller.forceFinished(true);
                }
            }
            onScrollAbort();
            reset();
        }

        private void onScrollAbort(){

        }

        private void onScrollFinish(){

        }

        public void tryToScrollTo(int to,int duration){
            if(mPullToRefreshIndicator.isAlreadyHere(to)){
                return;
            }

            mStart = mPullToRefreshIndicator.getCurrentPosY();
            mTo = to;
            int distance = to - mStart;
            mPullToRefreshView.removeCallbacks(this);

            mLastFlingY = 0;
            if(!mScroller.isFinished()){
                mScroller.forceFinished(true);
            }
            mScroller.startScroll(0,0,0,distance,duration);
            mPullToRefreshView.post(this);
            mIsRunning = true;
        }
    }
}
