package com.parrot.fpvtoolbox;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by fred on 27/05/16.
 */
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



        mRenderer = new FpvGLRenderer(getContext());

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);




    }

    public void SetRootView(View view) {
        mRenderer.setRootView(view);
    }

    public FpvGLRenderer GetRenderer() {
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
