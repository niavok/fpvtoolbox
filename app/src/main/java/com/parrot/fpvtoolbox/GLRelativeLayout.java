package com.parrot.fpvtoolbox;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.opengl.GLES20;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by user on 3/15/15.
 */
public class GLRelativeLayout extends RelativeLayout implements GLRenderable {

    private ViewToGLRenderer mViewToGLRenderer;

    // default constructors

    public GLRelativeLayout(Context context) {
        super(context);
    }

    public GLRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public GLRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // drawing magic
    @Override
    public void draw(Canvas canvas) {
        Log.e("GLRelativeLayout", "draw");

        if(mViewToGLRenderer == null)
        {
            return;
        }

        Canvas glAttachedCanvas = mViewToGLRenderer.onDrawViewBegin();
        if(glAttachedCanvas != null) {
            //prescale canvas to make sure content fits



            float xScale = glAttachedCanvas.getWidth() / (float)canvas.getWidth();
            float yScale = glAttachedCanvas.getHeight() / (float)canvas.getHeight();
            //glAttachedCanvas.scale(xScale, xScale);

            Log.e("GLRelativeLayout", "glAttachedCanvas.getWidth()="+glAttachedCanvas.getWidth());
            Log.e("GLRelativeLayout", "canvas.getWidth()="+canvas.getWidth());
            Log.e("GLRelativeLayout", "xScale="+xScale);
            Log.e("GLRelativeLayout", "yScale="+yScale);
            glAttachedCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);



            //draw the view to provided canvas
            super.draw(glAttachedCanvas);
        }
        // notify the canvas is updated
        mViewToGLRenderer.onDrawViewEnd();
    }

    public void setViewToGLRenderer(ViewToGLRenderer viewToGLRenderer){
        mViewToGLRenderer = viewToGLRenderer;
    }
}