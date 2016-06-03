package com.parrot.fpvtoolbox;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;

public class FpvToolBox extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FpvToolBox";
    private static final float DEFAULT_IPD = 63.0f;
    private static final float DEFAULT_SCALE = 0.75f;


    private FpvGLSurfaceView mGLView;
    private FpvGLSurfaceView mGLVideoView;

    float LastX = 0;
    float LastY = 0;
    float LastZ = 0;
    private WebView mWebView;
    private GLRelativeLayout mGLLinearLayout;
    boolean mIsFinished = false;
    ArrayList<FpvScene> mScenes;
    private long mLastDate = 0;
    private boolean mChromaticAberrationCorrection = true;
    private boolean mDistortionCorrection = true;
    private boolean mLensLimits = false;
    private int mCurrentSceneIndex = 1;
    private TextView mNotificationTextView;
    private TextView mNotificationSubTextView;
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

        mScenes = new ArrayList<FpvScene>();
        mScenes.add(new FpvScene("Grid", "http://niavok.com/fpv/grid.html", FpvScene.SceneType.WEB, "1000 px x 1000 px picture"));
        mScenes.add(new FpvScene("Avatar", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)+ "/h264_dts_avatar.1080p-sample.mkv", FpvScene.SceneType.VIDEO, "1080p"));
        mScenes.add(new FpvScene("Bebop 2", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)+ "/Bebop_2_2016-05-15T152815+0200_5E885D.mp4", FpvScene.SceneType.VIDEO, "720p recording video"));
        mScenes.add(new FpvScene("Tears of  steel", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)+ "/tears_of_steel_720p.mov", FpvScene.SceneType.VIDEO, "720p"));
        mScenes.add(new FpvScene("Tears of  steel", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)+ "/tears_of_steel_1080p.mov", FpvScene.SceneType.VIDEO, "1080p"));
        mScenes.add(new FpvScene("Big Buck Bunny",  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)+ "/mov_bbb.mp4", FpvScene.SceneType.VIDEO, "320 px x 176 px video"));
        mScenes.add(new FpvScene("The Shire", "http://niavok.com/fpv/lotr.html", FpvScene.SceneType.WEB, "1620 px x 1080 px picture"));
        mScenes.add(new FpvScene("SSME", "http://niavok.com/fpv/ssme.html", FpvScene.SceneType.WEB, "1080 px x 1350 px picture"));
        mScenes.add(new FpvScene("Parrots", "http://niavok.com/fpv/parrot.html", FpvScene.SceneType.WEB, "1080p"));




        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.

        mGLVideoView = (FpvGLSurfaceView) findViewById(R.id.gl_video_surface);

        mGLView = (FpvGLSurfaceView) findViewById(R.id.gl_surface);
        mNotificationTextView = (TextView) findViewById(R.id.notification_text);
        mNotificationSubTextView = (TextView) findViewById(R.id.notification_subtext);
        //setContentView(mGLView);


        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        //mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);


        mGLLinearLayout = (GLRelativeLayout) findViewById(R.id.gl_layout);
        mGLView.SetRootView(mGLLinearLayout);

        mGLLinearLayout.setViewToGLRenderer(mGLView.getRenderer());
        mNotificationHandler = new Handler();



    }

    private void sendNotification(String text)
    {
        sendNotification(text, "");
    }

    private void sendNotification(String text, String subtext)
    {
        mNotificationTextView.setText(text);
        mNotificationSubTextView.setText(subtext);
        mNotificationHandler.removeCallbacksAndMessages(null);
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                mNotificationTextView.setText("");
                mNotificationSubTextView.setText("");
             //   mGLLinearLayout.invalidate();
            }
        };
        mNotificationHandler.postDelayed(runnable, 2000);
        //mGLLinearLayout.invalidate();

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e("plop", "on resume");


        updateScene();
        setViewScale(mViewScale);
        setIpd(mIpd);


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

        setChromaticAberrationCorrect(mChromaticAberrationCorrection);
        setDistortionCorrection(mDistortionCorrection);
        setChromaticAberrationCorrect(mChromaticAberrationCorrection);



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
                    case KeyEvent.KEYCODE_BUTTON_X:
                        reload();
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

    public void enableVideo(String url)
    {
       // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                0);


        mWebView.setVisibility(View.INVISIBLE);
        mGLVideoView.getRenderer().enableVideo(getApplicationContext(), url);
    }

    public void disableVideo()
    {
        mGLVideoView.getRenderer().disableVideo();
        mWebView.setVisibility(View.VISIBLE);
    }

    private void reload() {
        updateScene();
    }

    private void resetSettings() {
        mChromaticAberrationCorrection = true;
        mDistortionCorrection = true;
        mLensLimits = false;


        setIpd(DEFAULT_IPD);
        setViewScale(DEFAULT_SCALE);
        setChromaticAberrationCorrect(mChromaticAberrationCorrection);
        setDistortionCorrection(mDistortionCorrection);
        setShowLensLimits(mLensLimits);


        sendNotification("Settings reset");
    }

    private void increaseScale() {
        setViewScale(mViewScale + 0.01f);
        sendNotification("Scale: "+ String.format("%1.0f", mViewScale * 100) + "%");
    }

    private void decreaseScale() {
        setViewScale(mViewScale - 0.01f);
        sendNotification("Scale: "+ String.format("%1.0f", mViewScale * 100) + "%");
    }

    private void increaseIPD() {
        setIpd(mIpd + 0.1f);
        sendNotification("IPD: "+ String.format("%1.1f", mIpd) + "mm");
    }

    private void decreaseIPD() {
        setIpd(mIpd - 0.1f);
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
        if(mScenes.get(mCurrentSceneIndex).getType() == FpvScene.SceneType.WEB)
        {
            String url = mScenes.get(mCurrentSceneIndex).getUrl();
            Log.e("plop","updateScene : "+ url);
            disableVideo();



            mWebView.loadUrl(url);
            mGLLinearLayout.invalidate();
        }
        else if(mScenes.get(mCurrentSceneIndex).getType() == FpvScene.SceneType.VIDEO)
        {
            String url = mScenes.get(mCurrentSceneIndex).getUrl();
            Log.e("plop","updateScene movie : "+ url);

            enableVideo(url);
        }
        sendNotification("Scene: "+mScenes.get(mCurrentSceneIndex).getName(), mScenes.get(mCurrentSceneIndex).getSubtitle());

    }

    private void toogleChromaticAberrationCorrection() {
        setChromaticAberrationCorrect(!mChromaticAberrationCorrection);
        sendNotification("Chromatic aberration correction: "+(mChromaticAberrationCorrection ? "ON" : "OFF"));
    }

    private void toogleLensLimits() {
        setShowLensLimits(!mLensLimits);
        sendNotification("Show lens limits: "+(mLensLimits ? "ON" : "OFF"));
    }

    private void toogleDistortionCorrection() {
        setDistortionCorrection(!mDistortionCorrection);
        float ScaleCorrection = 1.5f;
        if(mDistortionCorrection)
        {
            setViewScale(mViewScale / ScaleCorrection);
        }
        else
        {
            setViewScale(mViewScale * ScaleCorrection);
        }
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

    public void setViewScale(float viewScale) {
        mViewScale = viewScale;
        mGLView.setViewScale(mViewScale);
        mGLVideoView.setViewScale(mViewScale);
    }

    public void setIpd(float ipd) {
        mIpd = ipd;
        mGLView.setIpd(mIpd);
        mGLVideoView.setIpd(mIpd);
    }


    public void setChromaticAberrationCorrect(boolean chromaticAberrationCorrection) {
        mChromaticAberrationCorrection = chromaticAberrationCorrection;
        mGLView.getRenderer().setChromaticAberrationCorrect(mChromaticAberrationCorrection);
        mGLVideoView.getRenderer().setChromaticAberrationCorrect(mChromaticAberrationCorrection);


    }

    public void setDistortionCorrection(boolean distortionCorrection) {
        mDistortionCorrection = distortionCorrection;
        mGLView.getRenderer().setDistortionCorrection(mDistortionCorrection);
        mGLVideoView.getRenderer().setDistortionCorrection(mDistortionCorrection);
    }

    public void setShowLensLimits(boolean lensLimits) {
        mLensLimits = lensLimits;
        mGLView.getRenderer().setShowLensLimits(mLensLimits);
        mGLVideoView.getRenderer().setShowLensLimits(mLensLimits);
    }
}
