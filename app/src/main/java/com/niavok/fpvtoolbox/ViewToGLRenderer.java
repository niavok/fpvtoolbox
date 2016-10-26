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
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by user on 3/12/15.
 */
public class ViewToGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = ViewToGLRenderer.class.getSimpleName();

    private static final int DEFAULT_TEXTURE_WIDTH = 1280;
    private static final int DEFAULT_TEXTURE_HEIGHT = 1280;

    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    private int mGlSurfaceTexture;
    private Canvas mSurfaceCanvas;

    private int mTextureWidth = DEFAULT_TEXTURE_WIDTH;
    private int mTextureHeight = DEFAULT_TEXTURE_HEIGHT;
    protected MediaPlayer mVideoPlayer;
    private int mVideoWidth;
    private int mVideoHeight;
    private GLSurfaceView mSurfaceView;
    private int mRotation = 0;
    private boolean mEnabled = true;

    public ViewToGLRenderer(GLSurfaceView surfaceView) {

        mSurfaceView = surfaceView;
    }


    @Override
    synchronized public void onDrawFrame(GL10 gl){
        synchronized (this){
            // update texture
            mSurfaceTexture.updateTexImage();

            if(!mEnabled && mSurfaceView.getVisibility() != View.INVISIBLE)
            {
                mSurfaceView.post(new Runnable() {
                    @Override
                    public void run() {
                        mSurfaceView.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
    }

    synchronized public void enableVideo(Context context, String url, int rotation)
    {
        disableVideo();
        Log.e("ViewToGLRenderer", "enableVideo "+ url);
        mRotation = rotation;

        if(mVideoPlayer == null)
        {
            mVideoPlayer = new MediaPlayer();
            mVideoPlayer.setSurface(mSurface);

            mVideoPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    Log.e("ViewToGLRenderer", "setOnVideoSizeChangedListener");
                    mSurfaceView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("ViewToGLRenderer", "VISIBLE ");
                            mSurfaceView.setVisibility(View.VISIBLE);
                        }
                    }, 300);
                }
            });
        }

        try {
            mVideoPlayer.setDataSource(url);
            mVideoPlayer.setLooping(true);

            try {
                mVideoPlayer.prepare();
                mVideoWidth = mVideoPlayer.getVideoWidth();
                mVideoHeight = mVideoPlayer.getVideoHeight();
            } catch (IllegalStateException e)
            {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mVideoPlayer.start();
        mEnabled = true;
    }

    synchronized  public void disableVideo()
    {
        Log.e("ViewToGLRenderer", "disableVideo");
        mEnabled = false;
        if(mVideoPlayer != null) {
            mVideoPlayer.stop();
            mVideoPlayer.reset();
            mRotation = 0;
        }
    }

    @Override
    synchronized public void onSurfaceChanged(GL10 gl, int width, int height){

        if(mSurfaceTexture != null && mSurface != null)
        {
            return;
        }

        releaseSurface();

        mGlSurfaceTexture = createTexture();
        if (mGlSurfaceTexture > 0){
            //attach the texture to a surface.
            //It's a clue class for rendering an android view to gl level
            mSurfaceTexture = new SurfaceTexture(mGlSurfaceTexture);
            mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    mSurfaceView.requestRender();

                }
            });
            Log.e("ViewToGLRenderer", "onSurfaceChanged mTextureWidth"+mTextureWidth + " mTextureHeight="+mTextureHeight);
            mSurfaceTexture.setDefaultBufferSize(mTextureWidth, mTextureHeight);

            mSurface = new Surface(mSurfaceTexture);
            if(mVideoPlayer != null) {
                mVideoPlayer.setSurface(mSurface);
            }
        }
    }

    synchronized public void releaseSurface(){
        if(mSurface != null){
            mSurface.release();
        }
        if(mSurfaceTexture != null){
            mSurfaceTexture.setOnFrameAvailableListener(null);
            mSurfaceTexture.release();
        }
        mSurface = null;
        mSurfaceTexture = null;

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config){
        final String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        Log.d(TAG, extensions);
    }


    private int createTexture(){
        int[] textures = new int[1];

        // Generate the texture to where android view will be rendered
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, textures, 0);
        checkGlError("Texture generate");

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        checkGlError("Texture bind");

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return textures[0];
    }

    public int getGLSurfaceTexture(){
        return mGlSurfaceTexture;
    }



    public Canvas onDrawViewBegin(){
        mSurfaceCanvas = null;
        if (mSurface != null) {
            try {
                mSurfaceCanvas = mSurface.lockCanvas(null);
            }catch (Exception e){
                Log.e(TAG, "error while rendering view to gl: " + e);
            }
        }
        return mSurfaceCanvas;
    }

    public void onDrawViewEnd(){
        if(mSurfaceCanvas != null) {
            mSurface.unlockCanvasAndPost(mSurfaceCanvas);
        }
        mSurfaceCanvas = null;
    }


    public void checkGlError(String op)
    {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }


    public int getTextureWidth() {
        if(mVideoPlayer == null) {
            return mTextureWidth;
        }
        else
        {
            return mVideoWidth;
        }
    }

    public void setTextureWidth(int textureWidth) {
        mTextureWidth = textureWidth;
    }

    public int getTextureHeight() {

        if(mVideoPlayer == null) {
            return mTextureHeight;
        }
        else
        {
            return mVideoHeight;
        }
    }

    public void setTextureHeight(int textureHeight) {
        mTextureHeight = textureHeight;
    }

    public int getRotation() {
        return mRotation;
    }
}