package com.parrot.fpvtoolbox;

import javax.microedition.khronos.egl.EGLConfig;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by fred on 27/05/16.
 */
public class FpvGLRenderer  extends ViewToGLRenderer {
    private static final String TAG = "FpvGLRenderer";
    private static final float HMD_OFFSET = 34.66f;

    private Context mContext;
    private int mEyeProgram;
    private FpvEye mLeftEye = null;
    private FpvEye mRightEye = null;
    private FpvEye mLeftEyeNoDistortionCorrection = null;
    private FpvEye mRightEyeNoDistortionCorrection = null;
    float mIpd = 63;
    float mPanH = 0;
    float mPanV = 0;
    private float mEyeSize = 50;

    private float mMetricsWidth; // In millimeters
    private float mMetricsHeight; // In millimeters
    private View mRootView;
    private int mChromaticAberrationCorrection = 0;
    private boolean mDistortionCorrection = true;
    private boolean mForceRedraw = false;
    private float mViewScale;
    private boolean mShowLensLimits;
    private float mDeviceMargin;


    public FpvGLRenderer(Context context, FpvGLSurfaceView surfaceView) {
        super(surfaceView);
        mContext = context;
    }


    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);

        try {
            initEyeShaders();
            if(mEyeProgram != 0) {
                // Distortion correction
                {
                    IntBuffer indices = LoadIntBuffer("indices");
                    FloatBuffer colors = LoadFloatBuffer("colors");
                    FloatBuffer positions = LoadFloatBuffer("positions");
                    FloatBuffer texCoordsRed = LoadFloatBuffer("tex_coords_red");
                    FloatBuffer texCoordsGreen = LoadFloatBuffer("tex_coords_green");
                    FloatBuffer texCoordsBlue = LoadFloatBuffer("tex_coords_blue");

                    // TODO : don't duplicate array loading in gpu memory
                    mLeftEye = new FpvEye(this, mEyeProgram, indices, positions, colors, texCoordsRed, texCoordsGreen, texCoordsBlue);
                    mRightEye = new FpvEye(this, mEyeProgram, indices, positions, colors, texCoordsRed, texCoordsGreen, texCoordsBlue);
                }
                // No distortion correction
                {
                    IntBuffer indices = LoadIntBuffer("indices_no_dc");
                    FloatBuffer colors = LoadFloatBuffer("colors_no_dc");
                    FloatBuffer positions = LoadFloatBuffer("positions_no_dc");
                    FloatBuffer texCoords = LoadFloatBuffer("tex_coords_no_dc");

                    // TODO : don't duplicate array loading in gpu memory
                    mLeftEyeNoDistortionCorrection = new FpvEye(this, mEyeProgram, indices, positions, colors, texCoords, texCoords, texCoords);
                    mRightEyeNoDistortionCorrection = new FpvEye(this, mEyeProgram, indices, positions, colors, texCoords, texCoords, texCoords);
                }
                setupEyes();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        gl.glDisable(GL10.GL_DITHER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_FASTEST);



        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Use culling to remove back faces.
//        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Enable depth testing
  //      GLES20.glEnable(GLES20.GL_DEPTH_TEST);

    }

    private IntBuffer LoadIntBuffer(String assetName) throws IOException {
        AssetManager am = mContext.getAssets();

        StringBuilder buffer = new StringBuilder();
        InputStream codeFile = am.open(assetName);
        BufferedReader in = new BufferedReader(new InputStreamReader(codeFile, "UTF-8"));
        String str;

        ArrayList<Integer> IntList = new ArrayList<Integer>();


        while ((str=in.readLine()) != null) {
            IntList.add( Integer.parseInt(str));
        }


        ByteBuffer bb = ByteBuffer.allocateDirect(IntList.size() * 4);
        bb.order(ByteOrder.nativeOrder());

        IntBuffer intBuffer = bb.asIntBuffer();

        for(Integer i : IntList)
        {
            intBuffer.put(i);
        }

        intBuffer.position(0);



        return intBuffer;
    }

    private FloatBuffer LoadFloatBuffer(String assetName) throws IOException {
        AssetManager am = mContext.getAssets();

        StringBuilder buffer = new StringBuilder();
        InputStream codeFile = am.open(assetName);
        BufferedReader in = new BufferedReader(new InputStreamReader(codeFile, "UTF-8"));
        String str;

        ArrayList<Float> FloatList = new ArrayList<Float>();


        while ((str=in.readLine()) != null) {
            FloatList.add( Float.parseFloat(str));
        }


        ByteBuffer bb = ByteBuffer.allocateDirect(FloatList.size() * 4);
        bb.order(ByteOrder.nativeOrder());

        FloatBuffer floatBuffer = bb.asFloatBuffer();

        for(Float f : FloatList)
        {
            floatBuffer.put(f);
        }

        floatBuffer.position(0);

        Log.d(TAG, "Create float buffer with limit="+floatBuffer.limit());

        return floatBuffer;
    }

    private void setupEyes() {
        float yOffset = HMD_OFFSET - mDeviceMargin;

        if(mLeftEye != null) {
            mLeftEye.setEyeWidth(mEyeSize);
            mLeftEye.setEyeHeight(mEyeSize);
            mLeftEye.setEyeOffsetX(-mIpd / 2);
            mLeftEye.setEyeOffsetY(yOffset);
        }


        if(mRightEye != null) {
            mRightEye.setEyeWidth(mEyeSize);
            mRightEye.setEyeHeight(mEyeSize);
            mRightEye.setEyeOffsetX(mIpd / 2);
            mRightEye.setEyeOffsetY(yOffset);
        }

        if(mLeftEyeNoDistortionCorrection != null) {
            mLeftEyeNoDistortionCorrection.setEyeWidth(mEyeSize);
            mLeftEyeNoDistortionCorrection.setEyeHeight(mEyeSize);
            mLeftEyeNoDistortionCorrection.setEyeOffsetX(-mIpd / 2);
            mLeftEyeNoDistortionCorrection.setEyeOffsetY(yOffset);
        }


        if(mRightEyeNoDistortionCorrection != null) {
            mRightEyeNoDistortionCorrection.setEyeWidth(mEyeSize);
            mRightEyeNoDistortionCorrection.setEyeHeight(mEyeSize);
            mRightEyeNoDistortionCorrection.setEyeOffsetX(mIpd / 2);
            mRightEyeNoDistortionCorrection.setEyeOffsetY(yOffset);
        }
    }

    private void initEyeShaders() throws IOException {

        String vertexShaderCode;
        String fragmentShaderCode;

        vertexShaderCode = LoadShaderCode("syros.vert");
        fragmentShaderCode = LoadShaderCode("syros.frag");

        mEyeProgram = loadProgram(vertexShaderCode, fragmentShaderCode);
    }


    /*int axis = 1;
    float step = 0.00f;
    float min = 55;
    float max = 70;*/

    public void onDrawFrame(GL10 gl) {

        if(mForceRedraw)
        {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Log.d("UI thread", "I am the UI thread");
                    mRootView.invalidate();
                }
            });
        }
        super.onDrawFrame(gl);
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
/*
        if(axis > 0)
        {
            mIpd += step;
        }
        else
        {
            mIpd -= step;
        }

        if(mIpd < min)
        {
            axis = 1;
            mIpd = min;
        }

        if(mIpd > max)
        {
            axis = -1;
            mIpd = max;
        }

        Log.e(TAG, "setupEyes mIpd=" + mIpd);

        setupEyes(10, mIpd);
*/
        if(mDistortionCorrection) {
            if (mLeftEye != null) {
                //GLES20.glScissor(0,0, mPixelWidth / 2, mPixelHeight);
                mLeftEye.draw();
            }

            if (mRightEye != null) {
                //GLES20.glScissor(mPixelWidth / 2,0, mPixelWidth / 2, mPixelHeight);
                mRightEye.draw();
            }
        }
        else
        {
            if (mLeftEyeNoDistortionCorrection != null) {
                mLeftEyeNoDistortionCorrection.draw();
            }

            if (mRightEyeNoDistortionCorrection != null) {
                mRightEyeNoDistortionCorrection.draw();
            }
        }

    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        GLES20.glViewport(0, 0, width, height);
        Log.e("FpvGLRenderer", "onSurfaceChanged width=" + width + " height="+height);

        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        Log.e("FpvGLRenderer", "onSurfaceChanged metrics.xdpi=" + metrics.xdpi + " metrics.ydpi="+metrics.ydpi);

        mMetricsWidth = (width / metrics.xdpi)  * 25.4f;
        mMetricsHeight = (height / metrics.ydpi)  * 25.4f;

        Log.e("FpvGLRenderer", "onSurfaceChanged mMetricsWidth=" + mMetricsWidth + " mm mMetricsHeighti="+mMetricsHeight+" mm");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d("UI thread", "I am the UI thread");
                if(mRootView != null) {
                    mRootView.invalidate();
                }
            }
        });
    }

    public static int loadProgram(String vertexShaderCode, String fragmentShaderCode)
    {
        int vertexShader = FpvGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = FpvGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        int program = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(program, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(program, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }

        Log.d(TAG, "loadProgram > "+ program);
        return program;
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + type + ":");
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        Log.d(TAG, "loadShader > "+ shader);
        return shader;
    }

    public String LoadShaderCode(String assetName) throws IOException {
        AssetManager am = mContext.getAssets();

        StringBuilder buffer = new StringBuilder();
        InputStream codeFile = am.open(assetName);
        BufferedReader in = new BufferedReader(new InputStreamReader(codeFile, "UTF-8"));
        String str;

        while ((str=in.readLine()) != null) {
            buffer.append(str);
            buffer.append(System.getProperty("line.separator"));
        }

        Log.d(TAG, "LoadShaderCode "+assetName+" > "+ buffer.toString());

        return buffer.toString();

    }

    public float getMetricsWidth() {
        return mMetricsWidth;
    }

    public float getMetricsHeight() {
        return mMetricsHeight;
    }

    public void setRootView(View rootView) {
        mRootView = rootView;
    }

    public void setChromaticAberrationCorrect(int chromaticAberrationCorrection) {

        mChromaticAberrationCorrection = chromaticAberrationCorrection;
    }

    public int getChromaticAberrationCorrectionMode() {
        return mChromaticAberrationCorrection;
    }

    public void setForceRedraw(boolean forceRedraw) {
        mForceRedraw = forceRedraw;
    }

    public void setViewScale(float viewScale) {
        mViewScale = viewScale;
    }

    public void setIpd(float ipd) {
        mIpd = ipd;
        setupEyes();
    }


    public float getIpd() {
        return mIpd;
    }

    public float getViewScale() {
        return mViewScale;
    }

    public void setDeviceMargin(float deviceMargin) {
        mDeviceMargin = deviceMargin;
        setupEyes();
    }

    public void setDistortionCorrection(boolean distortionCorrection) {
        mDistortionCorrection = distortionCorrection;
    }

    public void setShowLensLimits(boolean showLensLimits) {
        mShowLensLimits = showLensLimits;
    }

    public boolean isShowLensLimits() {
        return mShowLensLimits;
    }

    public void setPan(float panH, float panV) {
        mPanH = panH;
        mPanV = panV;
        setupEyes();
    }

    public float getPanH() {
        return mPanH;
    }

    public float getPanV() {
        return mPanV;
    }
}
