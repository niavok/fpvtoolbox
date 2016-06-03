package com.parrot.fpvtoolbox;

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


    @Override
    public void onDrawFrame(GL10 gl){
        synchronized (this){
            // update texture
            mSurfaceTexture.updateTexImage();

        }
    }

    public void enableVideo(Context context, String url)
    {
        disableVideo();
        Log.e("ViewToGLRenderer", "enableVideo "+ url);
        mVideoPlayer = new MediaPlayer();
        try {
            mVideoPlayer.setDataSource(url);
            mVideoPlayer.setSurface(mSurface);
            mVideoPlayer.setLooping(true);

            mVideoPlayer.prepare();
            mVideoWidth = mVideoPlayer.getVideoWidth();
            mVideoHeight = mVideoPlayer.getVideoHeight();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mVideoPlayer.start();

    }

    public void disableVideo()
    {
        Log.e("ViewToGLRenderer", "disableVideo");
        if(mVideoPlayer != null) {
            mVideoPlayer.stop();
            mVideoPlayer.reset();
            mVideoPlayer = null;
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height){
        releaseSurface();

        mGlSurfaceTexture = createTexture();
        if (mGlSurfaceTexture > 0){
            //attach the texture to a surface.
            //It's a clue class for rendering an android view to gl level
            mSurfaceTexture = new SurfaceTexture(mGlSurfaceTexture);
            Log.e("ViewToGLRenderer", "onSurfaceChanged mTextureWidth"+mTextureWidth + " mTextureHeight="+mTextureHeight);
            mSurfaceTexture.setDefaultBufferSize(mTextureWidth, mTextureHeight);
            mSurface = new Surface(mSurfaceTexture);
            if(mVideoPlayer != null) {
                mVideoPlayer.setSurface(mSurface);
            }
        }
    }

    public void releaseSurface(){
        if(mSurface != null){
            mSurface.release();
        }
        if(mSurfaceTexture != null){
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
}