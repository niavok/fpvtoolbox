package com.parrot.fpvtoolbox;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
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
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class FpvToolBox extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FpvToolBox";
    public static final float DEFAULT_IPD = 63.0f;
    public static final float DEFAULT_SCALE = 0.75f;
    public static final float DEFAULT_PAN_H = 0.0f;
    public static final float DEFAULT_PAN_V = 0.0f;

    public static final float PAN_DEAD_ZONE = 0.01f;
    public static final float PAN_MAX_SPEED = 10f;
    public static final float PAN_MAX_OFFSET = 50f;

    public static final float DEFAULT_DEVICE_MARGIN = 4.0f;

    public static final int DEFAULT_CHROMATIC_ABERRATION_CORRECTION_MODE = 2;
    public static final boolean DEFAULT_DISTORTION_CORRECTION = true;
    public static final boolean DEFAULT_DISPLAY_LENS_LIMITS = false;
    public static final String PREFS_NAME = "FpvToolvoxPrefs";
    public static final boolean DEFAULT_DEMO_MODE = false;
    public static final boolean DEFAULT_POWER_SAVE = true;



    // Settings
    private int mChromaticAberrationCorrection = DEFAULT_CHROMATIC_ABERRATION_CORRECTION_MODE;
    private boolean mDistortionCorrection = DEFAULT_DISTORTION_CORRECTION;
    private boolean mLensLimits = DEFAULT_DISPLAY_LENS_LIMITS;
    private int mCurrentSceneIndex = 0;
    private float mViewScale = DEFAULT_SCALE;
    private float mIpd = DEFAULT_IPD;
    private float mPanH = DEFAULT_PAN_H;
    private float mPanV = DEFAULT_PAN_V;
    private boolean mDemoMode = DEFAULT_DEMO_MODE;
    private boolean mPowerSave = DEFAULT_POWER_SAVE;
    private float mDeviceMargin = DEFAULT_DEVICE_MARGIN; // In mm

    private FpvGLSurfaceView mGLView;
    private FpvGLSurfaceView mGLVideoView;

    private WebView mWebView;
    private GLRelativeLayout mGLLinearLayout;
    ArrayList<FpvScene> mScenes;

    private TextView mNotificationTextView;
    private TextView mNotificationSubTextView;
    private Handler mNotificationHandler;

    private float mHPanCommand;
    private float mVPanCommand;
    private Handler mPanHandler;

    private ImageView mImageView;
    private InactivityDetector mInactivityDetector;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fpv_tool_box);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mGLVideoView = (FpvGLSurfaceView) findViewById(R.id.gl_video_surface);

        mGLView = (FpvGLSurfaceView) findViewById(R.id.gl_surface);
        mNotificationTextView = (TextView) findViewById(R.id.notification_text);
        mNotificationSubTextView = (TextView) findViewById(R.id.notification_subtext);


        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mImageView = (ImageView) findViewById(R.id.image_view);

        mGLLinearLayout = (GLRelativeLayout) findViewById(R.id.gl_layout);
        mGLView.SetRootView(mGLLinearLayout);

        mGLLinearLayout.setViewToGLRenderer(mGLView.getRenderer());
        mNotificationHandler = new Handler();
        mPanHandler = new Handler();


        View navigationHeader = navigationView.inflateHeaderView(R.layout.nav_header_fpv_tool_box);

        Button settingButton = (Button) navigationHeader.findViewById(R.id.settingsButton);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FpvToolBox.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        mInactivityDetector = new InactivityDetector(this);
        mInactivityDetector.registerListener(new InactitityListener() {
            @Override
            public void OnActive() {
                if (mPowerSave) {
                    Log.i(TAG, "Activate");
                    reload();
                    activate();
                }
            }

            @Override
            public void OnInactive() {
                if (mPowerSave) {
                    Log.i(TAG, "Deactivate");
                    disableAll();
                    deactivate();
                }
            }
        });


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
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
            }
        };
        mNotificationHandler.postDelayed(runnable, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "on resume");

        loadSettings();

        mInactivityDetector.enable();
        activate();
        generateScenes();

        updateScene();
        setViewScale(mViewScale);
        setIpd(mIpd);
        setPan(mPanH, mPanV);
        setDeviceMargin(mDeviceMargin);

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



        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                updatePan(30);
                mPanHandler.postDelayed(this, 30);
            }

        };
        mPanHandler.postDelayed(runnable, 30);

    }

    private void loadSettings() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mChromaticAberrationCorrection = settings.getInt("chromaticAberrationCorrection", DEFAULT_CHROMATIC_ABERRATION_CORRECTION_MODE);
        mDistortionCorrection = settings.getBoolean("distortionCorrection", DEFAULT_DISTORTION_CORRECTION);
        mLensLimits = settings.getBoolean("lensLimits", DEFAULT_DISPLAY_LENS_LIMITS);
        mCurrentSceneIndex = settings.getInt("currentSceneIndex", 0);
        mViewScale = settings.getFloat("viewScale", DEFAULT_SCALE);
        mIpd = settings.getFloat("ipd", DEFAULT_IPD);
        mPanH = settings.getFloat("panH", DEFAULT_PAN_H);
        mPanV = settings.getFloat("panV", DEFAULT_PAN_V);
        mDeviceMargin = settings.getFloat("deviceMargin", getDefaultDeviceMargin());

        mDemoMode = settings.getBoolean("demoMode", DEFAULT_DEMO_MODE);
        mPowerSave = settings.getBoolean("powerSave", DEFAULT_POWER_SAVE);
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static float getDefaultDeviceMargin() {
        String deviceName = getDeviceName();
        String realName = null;
        float margin = DEFAULT_DEVICE_MARGIN;

        if (deviceName.equals("Samsung SM-G900F")) {
            realName = "Galaxy S5";
            margin = 4.0f;
        } else if (deviceName.equals("LGE LG-H815")) {
            realName = "LG G4";
            margin = 5.0f;
        }


        if (realName != null) {
            Log.i(TAG, realName+" detected. Margin: "+margin+"mm");
        } else {
            Log.w(TAG, "Unknown device model '"+deviceName+"'. Use fallback device margin: "+margin+"mm");
        }

        return margin;
    }

    private void saveSettings() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("chromaticAberrationCorrection", mChromaticAberrationCorrection);
        editor.putBoolean("distortionCorrection", mDistortionCorrection);
        editor.putBoolean("lensLimits", mLensLimits);
        editor.putInt("currentSceneIndex", mCurrentSceneIndex);
        editor.putFloat("viewScale", mViewScale);
        editor.putFloat("ipd", mIpd);
        editor.putFloat("panH", mPanH);
        editor.putFloat("panV", mPanV);

        // Commit the edits!
        editor.commit();

    }


    private void activate() {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 1F;
        getWindow().setAttributes(layout);
    }

    private void deactivate() {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 0F;
        getWindow().setAttributes(layout);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "on pause");
        mPanHandler.removeCallbacksAndMessages(null);
        mInactivityDetector.disable();

        saveSettings();

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

        generateVideoScenes();
        generateImageScenes();

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

                    if(videoFile.isDirectory())
                    {
                        continue;
                    }

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

    private void generateImageScenes() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
        } else {
            File imagesPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);


            if(imagesPath.exists()) {

                File[] fileList = imagesPath.listFiles();
                Arrays.sort(fileList);
                for(File imageFile : fileList) {


                    if(imageFile.isDirectory())
                    {
                        continue;
                    }

                    Log.e("Plop", "Analyse image "+imageFile.getName());

                    String pickedImagePath = "path/of/the/selected/file";
                    BitmapFactory.Options bitMapOption=new BitmapFactory.Options();
                    bitMapOption.inJustDecodeBounds=true;
                    BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bitMapOption);

                    if(bitMapOption.outWidth != -1) {
                        int width = bitMapOption.outWidth;
                        int height = bitMapOption.outHeight;
                        mScenes.add(new FpvScene(imageFile.getName(), imageFile.getAbsolutePath(), FpvScene.SceneType.IMAGE, "" + width + " px x " + height + " px image"));
                    }
                }
            }
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = false;
        Log.e("plop","dispatchKeyEvent "+ event.toString());

        mInactivityDetector.activate();

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

        mInactivityDetector.activate();

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


    public void enableWeb(String url) {
        disableAll();
        mWebView.loadUrl(url);
        mWebView.setVisibility(View.VISIBLE);
    }

    public void enableImage(String url) {
        disableAll();
        mImageView.setVisibility(View.VISIBLE);
        mImageView.setImageURI(Uri.parse(url));
        mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
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
            disableAll();
            mGLVideoView.setVisibility(View.VISIBLE);
            mGLVideoView.getRenderer().enableVideo(getApplicationContext(), url);
        }
    }

    public void disableAll()
    {
        mGLVideoView.setVisibility(View.INVISIBLE);
        mGLVideoView.getRenderer().disableVideo();
        mWebView.setVisibility(View.INVISIBLE);
        mImageView.setVisibility(View.INVISIBLE);
    }

    private void reload() {
        updateScene();
    }

    private void resetSettings() {
        mChromaticAberrationCorrection = DEFAULT_CHROMATIC_ABERRATION_CORRECTION_MODE;
        mDistortionCorrection = DEFAULT_DISTORTION_CORRECTION;
        mLensLimits = DEFAULT_DISPLAY_LENS_LIMITS;


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
        Log.e("plop","mScenes.size() " +mScenes.size());
        Log.e("plop","mCurrentSceneIndex " + mCurrentSceneIndex);

        if(mScenes.get(mCurrentSceneIndex).getType() == FpvScene.SceneType.WEB)
        {
            String url = mScenes.get(mCurrentSceneIndex).getUrl();
            Log.e("plop","updateScene : "+ url);
            enableWeb(url);

            mGLLinearLayout.invalidate();
        }
        else if(mScenes.get(mCurrentSceneIndex).getType() == FpvScene.SceneType.VIDEO)
        {
            String url = mScenes.get(mCurrentSceneIndex).getUrl();
            Log.e("plop","updateScene movie : "+ url);

            enableVideo(url);
        }
        else if(mScenes.get(mCurrentSceneIndex).getType() == FpvScene.SceneType.IMAGE)
        {
            String url = mScenes.get(mCurrentSceneIndex).getUrl();
            Log.e("plop","updateScene image : "+ url);

            enableImage(url);
        }
        if(!mDemoMode) {
            sendNotification("Scene: " + mScenes.get(mCurrentSceneIndex).getName(), mScenes.get(mCurrentSceneIndex).getSubtitle());
        }
    }

    private void toogleChromaticAberrationCorrection() {

        if(mChromaticAberrationCorrection == 2)
        {
            setChromaticAberrationCorrect(0);
        }
        else
        {
            setChromaticAberrationCorrect(mChromaticAberrationCorrection+1);
        }

        String mode = "Unknown";
        switch (mChromaticAberrationCorrection)
        {
            case 0:
                mode = "OFF";
            break;
            case 1:
                mode = "OLD";
                break;
            case 2:
                mode = "NEW";
                break;
        }
        sendNotification("Chromatic aberration correction: "+mode);
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
        outState.putInt("chromaticAberrationCorrection", mChromaticAberrationCorrection);
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
            mChromaticAberrationCorrection = savedInstanceState.getInt("chromaticAberrationCorrection", DEFAULT_CHROMATIC_ABERRATION_CORRECTION_MODE);
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


    public void setChromaticAberrationCorrect(int chromaticAberrationCorrection) {
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

    private void setDeviceMargin(float deviceMargin) {
        mDeviceMargin = deviceMargin;

        mGLView.getRenderer().setDeviceMargin(mDeviceMargin);
        mGLVideoView.getRenderer().setDeviceMargin(mDeviceMargin);
    }
}
