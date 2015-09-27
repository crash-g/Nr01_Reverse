package com.example.crash.nr01_reverse;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by crash on 20/06/2015.
 */
public class MainView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Renderer mRenderer;
    private boolean hasSurface;

    private int width, height;
    private Paint myPaint;

    public MainView(Context context, Resources res) {
        super(context);
        init(context, res);
    }
    private void init(Context context, Resources res) {
        mHolder = getHolder();
        mHolder.addCallback(this);
        hasSurface = false;


        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public MainView(Context context, AttributeSet ats, int defaultStyle) {
        super(context, ats, defaultStyle);
    }
    public MainView(Context context, AttributeSet ats) {
        super(context, ats);
    }

    private void resume() {
        if (mRenderer == null) {
            mRenderer = new Renderer(this.getContext(),mHolder,getResources());
        }
        if (hasSurface) {
            mRenderer.start();
        }
    }
    private void pause() {
        if (mRenderer != null) {
            mRenderer.requestExitAndWait();
            mRenderer = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        hasSurface = true;
        resume();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (mRenderer != null) {

        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
        pause();
    }

    @Override
    protected void onMeasure(int wMeasureSpec, int hMeasureSpec) {
        int measuredWidth = measureSide(wMeasureSpec);
        int measuredHeight = measureSide(hMeasureSpec);

        setMeasuredDimension(measuredWidth, measuredHeight);

        width = getMeasuredWidth();
        height = getMeasuredHeight();

        Grid.measure(width,height);
    }
    private int measureSide(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int result = 500;

        if (specMode == MeasureSpec.AT_MOST || specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        return result;
    }
//	@Override
//	protected void onDraw(Canvas canvas) {
//		int cx = width / 2;
//		int cy = height / 2;
//
//		controlSphere.draw(canvas, cx, cy, controlSpherePaint);
//	}

    private Point start = null;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                start = new Point((int) x, (int) y);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                Point finish = new Point((int) x, (int) y);
                if(start != null) {
                    Renderer.getActiveGrid().touch(start, finish);
                }
                break;
            default:
                return super.onTouchEvent(e);
        }
        return true;
    }
}
