package com.parrot.fpvtoolbox;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.ArrayList;

public class FpvToolBox extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FpvToolBox";
    private static final float DEFAULT_IPD = 63.0f;
    private static final float DEFAULT_SCALE = 0.75f;


    private FpvGLSurfaceView mGLView;

    float LastX = 0;
    float LastY = 0;
    float LastZ = 0;
    private WebView mWebView;
    private GLRelativeLayout mGLLinearLayout;
    boolean mIsFinished = false;
    ArrayList<String> mScenes;
    ArrayList<String> mSceneNames;
    private long mLastDate = 0;
    private boolean mChromaticAberrationCorrection = true;
    private boolean mDistortionCorrection = true;
    private boolean mLensLimits = false;
    private int mCurrentSceneIndex = 1;
    private TextView mNotificationTextView;
    private Handler mNotificationHandler;
    private float mViewScale = DEFAULT_SCALE;
    private float mIpd = DEFAULT_IPD;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fpv_tool_box);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        /*ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);*/
        //drawer.setDrawerListener(toggle);
        //toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mScenes = new ArrayList<String>();
        mScenes.add("http://niavok.com/fpv/grid.html");
        mScenes.add("http://niavok.com/fpv/lotr.html");
        mScenes.add("http://niavok.com/fpv/parrot.html");
        mScenes.add("http://niavok.com/fpv/ssme.html");

        mSceneNames = new ArrayList<String>();
        mSceneNames.add("Grid");
        mSceneNames.add("The Shire");
        mSceneNames.add("Parrots");
        mSceneNames.add("SSME");


        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mGLView = (FpvGLSurfaceView) findViewById(R.id.gl_surface);
        mNotificationTextView = (TextView) findViewById(R.id.notification_text);
        //setContentView(mGLView);

        /*SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        SensorEventListener linAcc = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                float X = event.values[0];
                float Y = event.values[1];
                float Z = event.values[2];

                float DeltaX = X - LastX;
                float DeltaY = Y - LastY;
                float DeltaZ = Z - LastZ;

                float Thresold = 4.0f;
                if(Math.abs(DeltaX) > Thresold || Math.abs(DeltaY) > Thresold|| Math.abs(DeltaZ) > Thresold )
                Log.e(TAG, "Delta Accelero x="+ DeltaX + " y="+ DeltaY + " z="+DeltaZ);

                LastX = X;
                LastY = Y;
                LastZ = Z;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorManager.registerListener(linAcc,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);*/
        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mGLLinearLayout = (GLRelativeLayout) findViewById(R.id.gl_layout);
        mGLView.SetRootView(mGLLinearLayout);

        mGLLinearLayout.setViewToGLRenderer(mGLView.GetRenderer());
        mNotificationHandler = new Handler();

    }

    private void sendNotification(String text)
    {
        mNotificationTextView.setText(text);
        mNotificationHandler.removeCallbacksAndMessages(null);
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                mNotificationTextView.setText("");
            }
        };
        mNotificationHandler.postDelayed(runnable, 2000);

    }




    @Override
    protected void onResume() {
        super.onResume();

        updateScene();
        mGLView.setViewScale(mViewScale);
        mGLView.setIpd(mIpd);

        mWebView.setWebViewClient(new WebViewClient() {


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mGLView.setForceRedraw(true);
            }

            public void onPageFinished(WebView view, String url) {
                mGLView.setForceRedraw(false);

                final Handler handler = new Handler();
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        mGLLinearLayout.invalidate();

                    }
                };
                handler.postDelayed(runnable, 500);
            }
        });

        mGLView.GetRenderer().setChromaticAberrationCorrect(mChromaticAberrationCorrection);
        mGLView.GetRenderer().setDistortionCorrection(mDistortionCorrection);
        mGLView.GetRenderer().setChromaticAberrationCorrect(mChromaticAberrationCorrection);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = false;

        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD)
                == InputDevice.SOURCE_GAMEPAD) {
            if (event.getAction() ==  KeyEvent.ACTION_DOWN) {

                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BUTTON_A:
                        toogleChromaticAberrationCorrection();
                        break;
                    case KeyEvent.KEYCODE_BUTTON_B:
                        toogleDistortionCorrection();
                        break;
                    case KeyEvent.KEYCODE_BUTTON_Y:
                        toogleLensLimits();
                        break;
                    case KeyEvent.KEYCODE_BUTTON_START:
                        resetSettings();
                        break;
                    case KeyEvent.KEYCODE_BUTTON_L1:
                        decreaseIPD();
                        break;
                    case KeyEvent.KEYCODE_BUTTON_R1:
                        increaseIPD();
                        break;
                }
                Log.e("plop", "Key =" + event.getKeyCode() + " event.getAction()= "+ event.getAction());

            }
            handled = true;
        }
        else if ((event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK)
                == InputDevice.SOURCE_CLASS_JOYSTICK) {

            if (event.getAction() ==  KeyEvent.ACTION_DOWN) {

                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        nextScene();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        previousScene();
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        increaseScale();
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        decreaseScale();
                        break;
                }
                Log.e("plop", "Key =" + event.getKeyCode() + " event.getAction()= "+ event.getAction());

            }
            Log.e("plop","Dpad ="+ event.getKeyCode());
            handled = true;
        }
        else
        {
            Log.e("plop","event form source : "+ event.getSource());
        }

        return handled;
    }

    private void resetSettings() {
        mIpd = DEFAULT_IPD;
        mViewScale = DEFAULT_SCALE;
        mChromaticAberrationCorrection = true;
        mDistortionCorrection = true;
        mLensLimits = false;

        mGLView.setIpd(mIpd);
        mGLView.setViewScale(mViewScale);
        mGLView.GetRenderer().setChromaticAberrationCorrect(mChromaticAberrationCorrection);
        mGLView.GetRenderer().setDistortionCorrection(mDistortionCorrection);
        mGLView.GetRenderer().setShowLensLimits(mLensLimits);


        sendNotification("Settings reset");
    }

    private void increaseScale() {
        mViewScale += 0.01;
        mGLView.setViewScale(mViewScale);
        sendNotification("Scale: "+ String.format("%1.0f", mViewScale * 100) + "%");
    }

    private void decreaseScale() {
        mViewScale -= 0.01;
        mGLView.setViewScale(mViewScale);
        sendNotification("Scale: "+ String.format("%1.0f", mViewScale * 100) + "%");
    }

    private void increaseIPD() {
        mIpd += 0.1;
        mGLView.setIpd(mIpd);
        sendNotification("IPD: "+ String.format("%1.1f", mIpd) + "mm");
    }

    private void decreaseIPD() {
        mIpd -= 0.1;
        mGLView.setIpd(mIpd);
        sendNotification("IPD: "+ String.format("%1.1f", mIpd) + "mm");
    }

    private void nextScene() {
        mCurrentSceneIndex++;
        if(mCurrentSceneIndex > mScenes.size()-1)
        {
            mCurrentSceneIndex = 0;
        }
        Log.e("plop","nextScene");
        updateScene();
    }

    private void previousScene() {
        mCurrentSceneIndex--;
        if(mCurrentSceneIndex < 0)
        {
            mCurrentSceneIndex = mScenes.size() -1;
        }
        Log.e("plop","previousScene");

        updateScene();
    }

    private void updateScene()
    {
        String url = mScenes.get(mCurrentSceneIndex);
        Log.e("plop","updateScene : "+ url);
        mWebView.loadUrl(url);
        mGLLinearLayout.invalidate();
        sendNotification("Scene: "+mSceneNames.get(mCurrentSceneIndex));

    }

    private void toogleChromaticAberrationCorrection() {
        mChromaticAberrationCorrection = !mChromaticAberrationCorrection;
        mGLView.GetRenderer().setChromaticAberrationCorrect(mChromaticAberrationCorrection);
        sendNotification("Chromatic aberration correction: "+(mChromaticAberrationCorrection ? "ON" : "OFF"));
    }

    private void toogleLensLimits() {
        mLensLimits = !mLensLimits;
        mGLView.GetRenderer().setShowLensLimits(mLensLimits);
        sendNotification("Show lens limits: "+(mLensLimits ? "ON" : "OFF"));
    }

    private void toogleDistortionCorrection() {
        mDistortionCorrection = !mDistortionCorrection;
        mGLView.GetRenderer().setDistortionCorrection(mDistortionCorrection);
        float ScaleCorrection = 1.5f;
        if(mDistortionCorrection)
        {
            mViewScale /= ScaleCorrection;
        }
        else
        {
            mViewScale *= ScaleCorrection;
        }
        mGLView.setViewScale(mViewScale);
        sendNotification("Distortion correction: "+(mDistortionCorrection ? "ON" : "OFF"));
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putInt("currentSceneIndex", mCurrentSceneIndex);
        outState.putBoolean("chromaticAberrationCorrection", mChromaticAberrationCorrection);
        outState.putBoolean("lensLimits", mLensLimits);
        outState.putBoolean("distortionCorrection", mDistortionCorrection);
        outState.putDouble("viewScale", mViewScale);
        outState.putDouble("ipd", mIpd);

        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState.containsKey("currentSceneIndex"))
        {
            mCurrentSceneIndex = savedInstanceState.getInt("currentSceneIndex", 0);
        }

        if(savedInstanceState.containsKey("chromaticAberrationCorrection"))
        {
            mChromaticAberrationCorrection = savedInstanceState.getBoolean("chromaticAberrationCorrection", true);
        }

        if(savedInstanceState.containsKey("chromaticAberrationCorrection"))
        {
            mDistortionCorrection = savedInstanceState.getBoolean("distortionCorrection", true);
        }

        if(savedInstanceState.containsKey("ipd"))
        {
            mIpd = savedInstanceState.getFloat("ipd", 63.f);
        }

        if(savedInstanceState.containsKey("viewScale"))
        {
            mViewScale = savedInstanceState.getFloat("viewScale", 63.f);
        }

        if(savedInstanceState.containsKey("lensLimits"))
        {
            mLensLimits = savedInstanceState.getBoolean("lensLimits", false);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fpv_tool_box, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.e("plop", "onNavigationItemSelected");

        if (id == R.id.nav_camera) {
            nextScene();
        } else if (id == R.id.nav_gallery) {
            previousScene();
        } else if (id == R.id.nav_slideshow) {
            toogleChromaticAberrationCorrection();
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
