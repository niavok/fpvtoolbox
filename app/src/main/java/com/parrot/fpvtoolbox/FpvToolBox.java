package com.parrot.fpvtoolbox;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class FpvToolBox extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FpvToolBox";
    private static final float DEFAULT_IPD = 63.0f;
    private static final float DEFAULT_SCALE = 0.75f;
    private static final float DEFAULT_PAN_H = 0.0f;
    private static final float DEFAULT_PAN_V = 0.0f;

    private static final float PAN_DEAD_ZONE = 0.01f;
    private static final float PAN_MAX_SPEED = 10f;
    private static final float PAN_MAX_OFFSET = 50f;


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
    private float mHPanCommand;
    private float mVPanCommand;
    private Handler mPanHandler;
    private float mPanH = DEFAULT_PAN_H;
    private float mPanV = DEFAULT_PAN_V;


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

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/


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
        mPanHandler = new Handler();


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


        generateScenes();


        updateScene();
        setViewScale(mViewScale);
        setIpd(mIpd);
        setPan(mPanH, mPanV);

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


        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                updatePan(30);
                mPanHandler.postDelayed(this, 30);
            }

        };
        mPanHandler.postDelayed(runnable, 30);

    }

    @Override
    protected void onPause() {
        mPanHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }


    private void updatePan(int deltaMs) {

        float command = (float) Math.sqrt(mHPanCommand * mHPanCommand + mVPanCommand * mVPanCommand);
        float angle = (float) Math.atan2(mVPanCommand, mHPanCommand);
        float clampedCommand = 0;
        if (command > PAN_DEAD_ZONE) {
            clampedCommand = (command - PAN_DEAD_ZONE) / (1 - PAN_DEAD_ZONE);
        }

        if(clampedCommand > 0)
        {
            float deltaPosition = clampedCommand * clampedCommand * PAN_MAX_SPEED * (deltaMs / 1000.f);
            float deltaPositionH = -(float) Math.cos(angle) * deltaPosition;
            float deltaPositionV = - (float) Math.sin(angle) * deltaPosition;


            setPan(mPanH + deltaPositionH, mPanV + deltaPositionV);
            sendNotification("Pan H = "+String.format("%1.1f", mPanH)+"mm, Pan V = "+ String.format("%1.1f", mPanV) +"mm");
        }


    }

    private void generateScenes() {

        mScenes = new ArrayList<FpvScene>();
        mScenes.add(new FpvScene("Grid", "http://niavok.com/fpv/grid.html", FpvScene.SceneType.WEB, "1000 px x 1000 px picture"));

        generateVideoScenes();

        mScenes.add(new FpvScene("The Shire", "http://niavok.com/fpv/lotr.html", FpvScene.SceneType.WEB, "1620 px x 1080 px picture"));
        mScenes.add(new FpvScene("SSME", "http://niavok.com/fpv/ssme.html", FpvScene.SceneType.WEB, "1080 px x 1350 px picture"));
        mScenes.add(new FpvScene("Parrots", "http://niavok.com/fpv/parrot.html", FpvScene.SceneType.WEB, "1080p"));

        if(mCurrentSceneIndex >= mScenes.size())
        {
            mCurrentSceneIndex = 1;
        }
    }

    private void generateVideoScenes() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
        } else {
            File videosPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);


            if(videosPath.exists()) {

                File[] fileList = videosPath.listFiles();
                Arrays.sort(fileList);
                for(File videoFile : fileList) {

                    Log.e("Plop", "Analyse video "+videoFile.getName());
                    MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                    metaRetriever.setDataSource(videoFile.getAbsolutePath());
                    if(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) != null)
                    {
                        String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                        String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

                        mScenes.add(new FpvScene(videoFile.getName(),  videoFile.getAbsolutePath(), FpvScene.SceneType.VIDEO, ""+width+" px x "+height+" px video"));
                    }


                }
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = false;
        Log.e("plop","dispatchKeyEvent "+ event.toString());


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
            //Log.e("plop","Dpad ="+ event.getKeyCode(), " "+ e.t);
            handled = true;
        }
        else
        {
            Log.e("plop","event form source : "+ event.getSource());
        }

        return handled;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        mHPanCommand = ev.getAxisValue(MotionEvent.AXIS_Z);
        mVPanCommand = ev.getAxisValue(MotionEvent.AXIS_RZ);

        /*Log.e("plop","dispatchGenericMotionEvent "+ ev.toString());
        Log.e("plop","getAction "+ ev.getAction());

        Log.e("plop","AXIS_HAT_X "+  ev.getAxisValue(MotionEvent.AXIS_HAT_X));
        Log.e("plop","AXIS_HAT_Y "+  ev.getAxisValue(MotionEvent.AXIS_HAT_Y));
        Log.e("plop","AXIS_X "+  ev.getAxisValue(MotionEvent.AXIS_X));
        Log.e("plop","AXIS_Y "+  ev.getAxisValue(MotionEvent.AXIS_Y));
        Log.e("plop","AXIS_Z "+  ev.getAxisValue(MotionEvent.AXIS_Z));
        Log.e("plop","AXIS_RX "+  ev.getAxisValue(MotionEvent.AXIS_RX));
        Log.e("plop","AXIS_RY "+  ev.getAxisValue(MotionEvent.AXIS_RY));
        Log.e("plop","AXIS_RZ "+  ev.getAxisValue(MotionEvent.AXIS_RZ));*/



        return super.dispatchGenericMotionEvent(ev);
    }

    public void enableVideo(String url)
    {
       // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                0);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        0);
        } else {
            mWebView.setVisibility(View.INVISIBLE);
            mGLVideoView.setVisibility(View.VISIBLE);
            mGLVideoView.getRenderer().enableVideo(getApplicationContext(), url);
        }
    }

    public void disableVideo()
    {
        mGLVideoView.setVisibility(View.INVISIBLE);
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
        setPan(DEFAULT_PAN_H, DEFAULT_PAN_V);


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

        if (id == R.id.nav_next) {
            nextScene();
        } else if (id == R.id.nav_previous) {
            previousScene();
        } else if (id == R.id.nav_toogle_cac) {
            toogleChromaticAberrationCorrection();
        } else if (id == R.id.nav_toogle_dc) {
            toogleDistortionCorrection();
        } else if (id == R.id.nav_toogle_limits) {
            toogleLensLimits();
        } else if (id == R.id.nav_reset_settings) {
            resetSettings();
        } else if (id == R.id.nav_reload) {
            reload();
        } else if (id == R.id.nav_increase_ipd) {
            increaseIPD();
        } else if (id == R.id.nav_decrease_ipd) {
            decreaseIPD();
        } else if (id == R.id.nav_increase_scale) {
            increaseScale();
        } else if (id == R.id.nav_decrease_scale) {
            decreaseScale();
        } else if (id == R.id.nav_pan_up) {
            mVPanCommand = -1;
            updatePan(100);
            mVPanCommand = 0;
        } else if (id == R.id.nav_pan_down) {
            mVPanCommand = 1;
            updatePan(100);
            mVPanCommand = 0;
        } else if (id == R.id.nav_pan_left) {
            mHPanCommand = 1;
            updatePan(100);
            mHPanCommand = 0;
        } else if (id == R.id.nav_pan_right) {
            mHPanCommand = -1;
            updatePan(100);
            mHPanCommand = 0;
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


    private void setPan(float panH, float panV) {
        mPanH = panH;
        mPanV = panV;
        if(mPanH < -PAN_MAX_OFFSET)
        {
            mPanH = -PAN_MAX_OFFSET;
        }
        if(mPanH > PAN_MAX_OFFSET)
        {
            mPanH = PAN_MAX_OFFSET;
        }

        if(mPanV < -PAN_MAX_OFFSET)
        {
            mPanV = -PAN_MAX_OFFSET;
        }
        if(mPanV > PAN_MAX_OFFSET)
        {
            mPanV = PAN_MAX_OFFSET;
        }

        mGLView.getRenderer().setPan(mPanH, mPanV);
        mGLVideoView.getRenderer().setPan(mPanH, mPanV);
    }
}