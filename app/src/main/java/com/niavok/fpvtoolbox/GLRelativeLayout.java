/*
 * Copyright (c) 2016, Frédéric Bertolus <frederic.bertolus@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.niavok.fpvtoolbox;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

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