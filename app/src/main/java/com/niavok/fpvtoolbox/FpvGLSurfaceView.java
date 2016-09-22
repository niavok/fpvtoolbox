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

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;

public class FpvGLSurfaceView extends GLSurfaceView {

    private FpvGLRenderer mRenderer = null;

    public FpvGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public FpvGLSurfaceView(Context context){
        super(context);
        Init();
    }

    private void Init() {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);


        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mRenderer = new FpvGLRenderer(getContext(), this);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);


    }

    public void SetRootView(View view) {
        mRenderer.setRootView(view);
        getHolder().setFormat(PixelFormat.RGBA_8888);

    }

    public FpvGLRenderer getRenderer() {
        return mRenderer;
    }

    public void setForceRedraw(boolean forceRedraw) {
        mRenderer.setForceRedraw(forceRedraw);
    }

    public void setViewScale(float viewScale) {
        mRenderer.setViewScale(1/viewScale);
    }

    public void setIpd(float ipd) {
        mRenderer.setIpd(ipd);
    }
}
