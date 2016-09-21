package com.parrot.fpvtoolbox;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class InactivityDetector {

    private static final double INACTIVITY_THRESOLD_RATIO = 0.2;
    private static final long ACTIVITY_THRESOLD_MS = 500;
    public static final double MOTION_DETECTION_THRESOLD = 0.07;
    public static final double ANGLE_CONVERGENCE_SPEED = 0.1;
    public static final double MOVE_RATIO_SPEED = 0.02;


    // Start with some variables
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private Vector3 mGravity;
    private Vector3 mLastGravity;

    private SensorEventListener mSensorEventListener;

    private double mMeanDeltaAngle = 0;
    private long mLastNoMoveTimestamp = 0;
    private boolean mActive;
    private InactitityListener mListener;
    private double mMoveRatio = 1;


    public InactivityDetector(FpvToolBox toolBox) {
        mSensorManager = (SensorManager)toolBox.getSystemService(toolBox.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorEventListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                    mGravity = new Vector3(event.values);

                    if(mLastGravity == null) {
                        mLastGravity = mGravity;
                        return;
                    }

                    double deltaAngle = 10000 * (1.0 - mGravity.getNormalized().dot(mLastGravity.getNormalized()));
                    mMeanDeltaAngle = mMeanDeltaAngle * (1.0 - ANGLE_CONVERGENCE_SPEED) + deltaAngle * ANGLE_CONVERGENCE_SPEED;

                    long currentTimestamp = System.currentTimeMillis();

                    float move = 0;
                    // Make this higher or lower according to how much
                    // motion you want to detect
                    if(mMeanDeltaAngle > MOTION_DETECTION_THRESOLD){
                        move = 1;
                        // Wake if move for a long duration
                        if(currentTimestamp - mLastNoMoveTimestamp > ACTIVITY_THRESOLD_MS) {
                            // Long inactivity
                            if(!mActive)
                            {
                                mActive = true;
                                mMoveRatio = 1;
                                if(mListener != null) {
                                    mListener.OnActive();
                                }
                            }
                        }
                    }
                    else {
                        move = 0;

                        if(mActive && mMoveRatio < INACTIVITY_THRESOLD_RATIO)
                        {
                            mActive = false;
                            if(mListener != null) {
                                mListener.OnInactive();
                            }
                        }
                        mLastNoMoveTimestamp = currentTimestamp;
                    }

                    mMoveRatio = mMoveRatio * (1.0 - MOVE_RATIO_SPEED) + move * MOVE_RATIO_SPEED;
                    mLastGravity = mGravity;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    public void enable() {

        mSensorManager.registerListener(mSensorEventListener, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);
        activate();
    }

    public void disable() {
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    public boolean isActive() {
        return mActive;
    }

    public void activate() {
        mLastNoMoveTimestamp = System.currentTimeMillis();
        mMeanDeltaAngle = 0;
        mMoveRatio = 1;
        mLastGravity = null;
        mActive = true;
    }

    public void registerListener(InactitityListener listener) {
        mListener = listener;
    }
}
