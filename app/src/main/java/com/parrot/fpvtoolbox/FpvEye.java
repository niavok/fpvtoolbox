package com.parrot.fpvtoolbox;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by fred on 27/05/16.
 */
public class FpvEye {
    private static final String TAG = "FpvEye";
    private final FloatBuffer mPositionBuffer;
    private final FloatBuffer mColorBuffer;
    private final FloatBuffer mTextCoordBuffer;
    private final IntBuffer mIndicesBuffer;

    private FpvGLRenderer mRenderer;
    private final int mProgram;
    private int mIndicesBufferHandle;
    private int mColorBufferHandle;
    private int mPositionBufferHandle;
    private int mTextCoord0BufferHandle;
    private int mTextCoord1BufferHandle;
    private int mTextCoord2BufferHandle;


    private int mProgramTexCoord2;
    private int mProgramTexture;
    private int mProgramEyeToSourceUVOffset;
    private int mProgramEyeToSourceUVScale;
    private int mProgramEyeToSourceOffset;
    private int mProgramEyeToSourceScale;
    private int mProgramChromaticAberrationCorrection;
    private int mProgramLensLimits;
    private int mProgramPosition;
    private int mProgramColor;
    private int mProgramTexCoord0;
    private int mProgramTexCoord1;

    private float mEyeWidth = 10; // mm
    private float mEyeHeight = 10; // mm
    private float mEyeOffsetX = 0; // mm
    private float mEyeOffsetY = 0; // mm


    /*static int mockIndices[] = {
            0, 3, 1 ,
            0, 2, 3,
    };

    float mockPosition[] = {
            -1,-1,
            -1,1,
            1, -1,
            1, 1,
    };

    float mockColor[] = {
            1, 1, 1, 1,
            1, 1, 1, 1,
            1, 1, 1, 1,
            1, 1, 1, 1,
    };

    float mockTextCoord[] = {
            0,1,
            0,0,
            1,1,
            1,0,
    };*/


    public FpvEye(FpvGLRenderer fpvGLRenderer, int program, IntBuffer indices, FloatBuffer positions, FloatBuffer colors,  FloatBuffer textureCoords) {
        this.mRenderer = fpvGLRenderer;

        mProgram = program;

        initProgram();

        /*mIndicesBuffer = AllocIntBuffer(mockIndices);
        mPositionBuffer = AllocFloatBuffer(mockPosition);
        mColorBuffer = AllocFloatBuffer(mockColor);
        mTextCoordBuffer = AllocFloatBuffer(mockTextCoord);*/

        mIndicesBuffer = indices;
        mPositionBuffer = positions;
        mColorBuffer = colors;
        mTextCoordBuffer = textureCoords;

        Log.d(TAG, "positions float buffer with limit="+positions.limit());
        Log.d(TAG, "mPositionBuffer float buffer with limit="+mPositionBuffer.limit());


        loadBuffers();

        Log.d(TAG, "mPositionBuffer float buffer with limit="+mPositionBuffer.limit());
    }

    private IntBuffer AllocIntBuffer(int[] data) {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                data.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        IntBuffer buffer = bb.asIntBuffer();
        // add the coordinates to the FloatBuffer
        buffer.put(data);
        // set the buffer to read the first coordinate
        buffer.position(0);

        return buffer;
    }

    private FloatBuffer AllocFloatBuffer(float[] data) {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                data.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        FloatBuffer buffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        buffer.put(data);
        // set the buffer to read the first coordinate
        buffer.position(0);

        return buffer;
    }

    private void loadBuffers() {
        mIndicesBufferHandle = generateBuffer();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndicesBufferHandle);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndicesBuffer.limit() * 4, mIndicesBuffer, GLES20.GL_STATIC_DRAW);
        Log.d(TAG, "" + mIndicesBuffer.limit() + " indices loaded");

        mPositionBufferHandle = generateBuffer();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mPositionBufferHandle);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mPositionBuffer.limit() * 4 , mPositionBuffer, GLES20.GL_STATIC_DRAW);
        Log.d(TAG, "" + mPositionBuffer.limit() + " positions loaded");

        mColorBufferHandle = generateBuffer();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mColorBufferHandle);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mColorBuffer.limit() * 4, mColorBuffer, GLES20.GL_STATIC_DRAW);
        Log.d(TAG, "" + mColorBuffer.limit() + " colors loaded");

        mTextCoord0BufferHandle = generateBuffer();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTextCoord0BufferHandle);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mTextCoordBuffer.limit() * 4, mTextCoordBuffer, GLES20.GL_STATIC_DRAW);
        Log.d(TAG, "" + mTextCoordBuffer.limit() + " tex coords loaded");

        mTextCoord1BufferHandle = generateBuffer();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTextCoord1BufferHandle);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mTextCoordBuffer.limit() * 4, mTextCoordBuffer, GLES20.GL_STATIC_DRAW);

        mTextCoord2BufferHandle = generateBuffer();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTextCoord2BufferHandle);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mTextCoordBuffer.limit() * 4, mTextCoordBuffer, GLES20.GL_STATIC_DRAW);
    }

    private int generateBuffer() {
        int[] buffer = new int[1];
        GLES20.glGenBuffers( 1, buffer, 0);
        return buffer[0];
    }

    private void initProgram() {
        Log.d(TAG, "mProgram="+mProgram);
        mProgramTexture = GLES20.glGetUniformLocation(mProgram, "Texture0");
        if(mProgramTexture < 0)
        {
            Log.e(TAG, "Fail to get uniform 'Texture0' location loading program");
        }

        mProgramEyeToSourceUVScale = GLES20.glGetUniformLocation(mProgram, "EyeToSourceUVScale");
        if(mProgramEyeToSourceUVScale < 0)
        {
            Log.e(TAG, "Fail to get uniform 'EyeToSourceUVScale' location loading program");
        }

        mProgramEyeToSourceUVOffset = GLES20.glGetUniformLocation(mProgram, "EyeToSourceUVOffset");
        if(mProgramEyeToSourceUVOffset < 0)
        {
            Log.e(TAG, "Fail to get uniform 'EyeToSourceUVOffset' location loading program");
        }

        mProgramEyeToSourceScale = GLES20.glGetUniformLocation(mProgram, "EyeToSourceScale");
        if(mProgramEyeToSourceScale < 0)
        {
            Log.e(TAG, "Fail to get uniform 'EyeToSourceScale' location loading program");
        }

        mProgramEyeToSourceOffset = GLES20.glGetUniformLocation(mProgram, "EyeToSourceOffset");
        if(mProgramEyeToSourceOffset < 0)
        {
            Log.e(TAG, "Fail to get uniform 'EyeToSourceOffset' location loading program");
        }

        mProgramChromaticAberrationCorrection = GLES20.glGetUniformLocation(mProgram, "ChromaticAberrationCorrection");
        if(mProgramChromaticAberrationCorrection < 0)
        {
            Log.e(TAG, "Fail to get uniform 'ChromaticAberrationCorrection' location loading program");
        }

        mProgramLensLimits = GLES20.glGetUniformLocation(mProgram, "LensLimits");
        if(mProgramLensLimits < 0)
        {
            Log.e(TAG, "Fail to get uniform 'ChromaticAberrationCorrection' location loading program");
        }

        mProgramPosition = GLES20.glGetAttribLocation(mProgram, "Position");
        if(mProgramPosition < 0)
        {
            Log.e(TAG, "Fail to get attribute 'Position' location loading program");
        }

        mProgramColor = GLES20.glGetAttribLocation(mProgram, "Color");
        if(mProgramColor < 0)
        {
            Log.e(TAG, "Fail to get attribute 'Color' location loading program");
        }

        mProgramTexCoord0 = GLES20.glGetAttribLocation(mProgram, "TexCoord0");
        if(mProgramTexCoord0 < 0)
        {
            Log.e(TAG, "Fail to get attribute 'TexCoord0' location loading program");
        }
        mProgramTexCoord1 = GLES20.glGetAttribLocation(mProgram, "TexCoord1");
        if(mProgramTexCoord1 < 0)
        {
            Log.e(TAG, "Fail to get attribute 'TexCoord1' location loading program");
        }
        mProgramTexCoord2 = GLES20.glGetAttribLocation(mProgram, "TexCoord2");
        if(mProgramTexCoord2 < 0)
        {
            Log.e(TAG, "Fail to get attribute 'TexCoord2' location loading program");
        }
    }

    public void draw() {



        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,  mRenderer.getGLSurfaceTexture());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(mProgramTexture, 0);


        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndicesBufferHandle);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mPositionBufferHandle);
        GLES20.glEnableVertexAttribArray(mProgramPosition);
        GLES20.glVertexAttribPointer(mProgramPosition, 2, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mColorBufferHandle);
        GLES20.glEnableVertexAttribArray(mProgramColor);
        GLES20.glVertexAttribPointer(mProgramColor, 4, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTextCoord0BufferHandle);
        GLES20.glEnableVertexAttribArray(mProgramTexCoord0);
        GLES20.glVertexAttribPointer(mProgramTexCoord0, 2, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTextCoord1BufferHandle);
        GLES20.glEnableVertexAttribArray(mProgramTexCoord1);
        GLES20.glVertexAttribPointer(mProgramTexCoord1, 2, GLES20.GL_FLOAT, false, 0, 0);


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTextCoord2BufferHandle);
        GLES20.glEnableVertexAttribArray(mProgramTexCoord2);
        GLES20.glVertexAttribPointer(mProgramTexCoord2, 2, GLES20.GL_FLOAT, false, 0, 0);

        //GLES20.glUniform2f(mProgramEyeToSourceUVScale, 2f, 2f);

        //Texture aspect ratio
        if(mRenderer.getTextureWidth() == mRenderer.getTextureHeight())
        {
            GLES20.glUniform2f(mProgramEyeToSourceUVScale, mRenderer.getViewScale(), mRenderer.getViewScale());
            GLES20.glUniform2f(mProgramEyeToSourceUVOffset,mRenderer.getPanH() / mRenderer.getMetricsWidth(), mRenderer.getPanV() / mRenderer.getMetricsHeight());
        }
        else
        {
            float ratio = (float) mRenderer.getTextureWidth() / (float) mRenderer.getTextureHeight();
            if(ratio > 1)
            {
                GLES20.glUniform2f(mProgramEyeToSourceUVScale, mRenderer.getViewScale(), mRenderer.getViewScale() * ratio);
                GLES20.glUniform2f(mProgramEyeToSourceUVOffset,mRenderer.getPanH() / mRenderer.getMetricsWidth(), ratio * mRenderer.getPanV() / mRenderer.getMetricsHeight());
            }
            else
            {
                GLES20.glUniform2f(mProgramEyeToSourceUVScale, mRenderer.getViewScale() / ratio, mRenderer.getViewScale());
                GLES20.glUniform2f(mProgramEyeToSourceUVOffset,mRenderer.getPanH() / (ratio *mRenderer.getMetricsWidth()), ratio * mRenderer.getPanV() / mRenderer.getMetricsHeight());
            }
        }



        if(mRenderer.isChromaticAberrationCorrection())
        {
            GLES20.glUniform1i(mProgramChromaticAberrationCorrection, 1);
        }
        else
        {
            GLES20.glUniform1i(mProgramChromaticAberrationCorrection, 0);
        }

        if(mRenderer.isShowLensLimits())
        {
            GLES20.glUniform1i(mProgramLensLimits, 1);
        }
        else
        {
            GLES20.glUniform1i(mProgramLensLimits, 0);
        }

        GLES20.glUniform2f(mProgramEyeToSourceScale, 2.f / mRenderer.getMetricsWidth(), 2.f / mRenderer.getMetricsHeight());
        GLES20.glUniform2f(mProgramEyeToSourceOffset, 2.0f * mEyeOffsetX / mRenderer.getMetricsWidth(), 2.0f * mEyeOffsetY / mRenderer.getMetricsHeight());





        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndicesBuffer.limit(), GLES20.GL_UNSIGNED_INT, 0);

        GLES20.glDisableVertexAttribArray(mProgramPosition);
    }

    public void setEyeWidth(float eyeWidth) {
        mEyeWidth = eyeWidth;
    }

    public void setEyeHeight(float eyeHeight) {
        mEyeHeight = eyeHeight;
    }

    public void setEyeOffsetX(float eyeOffsetX) {
        mEyeOffsetX = eyeOffsetX;
    }

    public void setEyeOffsetY(float eyeOffsetY) {
        mEyeOffsetY = eyeOffsetY;
    }
}
