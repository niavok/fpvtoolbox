package com.parrot.fpvtoolbox;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class FpvToolBox extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FpvToolBox";


    private FpvGLSurfaceView mGLView;

    float LastX = 0;
    float LastY = 0;
    float LastZ = 0;
    private WebView mWebView;
    private GLRelativeLayout mGLLinearLayout;
    boolean mIsFinished = false;
    String mUrlString = "http://niavok.com/fpv/fpv.html";
    private long mLastDate = 0;

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


        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mGLView = (FpvGLSurfaceView) findViewById(R.id.gl_surface);
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
        //mWebView.getSettings().setBuiltInZoomControls(true);
        //mWebView.getSettings().setSupportZoom(true);
        //mWebView.getSettings().setLoadWithOverviewMode(true);

        mGLLinearLayout = (GLRelativeLayout) findViewById(R.id.gl_layout);
        mGLView.SetRootView(mGLLinearLayout);

        mGLLinearLayout.setViewToGLRenderer(mGLView.GetRenderer());
        //WebSettings webSettings = mWebView.getSettings();
        //webSettings.setJavaScriptEnabled(true);
        //mWebView.setWebViewClient(new WebViewClient());
        //mWebView.setWebChromeClient(new WebChromeClient());
        //mWebView.loadUrl("http://helium-rain.com");
        //WebView.loadUrl("http://techslides.com/demos/sample-videos/small.mp4");
        //mWebView.loadUrl("http://cdn.spacetelescope.org/archives/images/screen/heic0602a.jpg");
        //mWebView.loadUrl("http://science-all.com/images/wallpapers/images-of-parrots/images-of-parrots-16.jpg");
        String url = "http://science-all.com/images/wallpapers/images-of-parrots/images-of-parrots-16.jpg";
        //String url = "http://www.flights.com/blog/wp-content/uploads/2015/03/LOTR-Hobbiton-New-Zeland.jpg";
        //String url = "http://www.allodocteurs.fr/media/8871-09ADGrilleDepistage.jpg";
        //String url = "http://www.sp-services.co.uk/Tutorial/10x10gridwhite.gif";



        String content = "<body style=\"background-color:black;margin: 0; padding: 0;\"><img style=\"margin:0; display: inline;height: auto;max-width: 100%; position: absolute; top: 50%; transform: translateY(-50%);\" src=\""+url+"\" /><body>";
        //mWebView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);

        //WebView.loadUrl("http://172.20.31.106/fpv/fpv.html");




       // mWebView.loadUrl(mUrlString);

        /*final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {

            }
        };
        handler.postDelayed(runnable, 1000);*/
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e("plop", "on resume2 "+ mUrlString);



        mWebView.loadUrl(mUrlString);
        mGLLinearLayout.invalidate();

        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
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

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putString("url", mUrlString);
        Log.e("plop", "on save "+ mUrlString);

        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState.containsKey("url"))
        {


            mUrlString = savedInstanceState.getString("url");
        }
        Log.e("plop", "on restore"+ mUrlString);

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
            mUrlString = "http://niavok.com/fpv/grid.html";
        } else if (id == R.id.nav_gallery) {
            mUrlString = "http://niavok.com/fpv/lotr.html";
        } else if (id == R.id.nav_slideshow) {
            mUrlString = "http://niavok.com/fpv/parrot.html";
        } else if (id == R.id.nav_manage) {
            mUrlString = "http://niavok.com/fpv/ssme.html";
        } else if (id == R.id.nav_share) {
            mWebView.loadUrl(mUrlString);
        } else if (id == R.id.nav_send) {

        }

         mWebView.loadUrl(mUrlString);

        mGLLinearLayout.invalidate();




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
