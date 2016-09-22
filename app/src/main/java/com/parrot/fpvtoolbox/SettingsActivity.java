package com.parrot.fpvtoolbox;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Locale;

public class SettingsActivity extends Activity {

    private CheckBox mDemoModeCheckBox;
    private CheckBox mPowerSaveCheckBox;
    private EditText mDeviceMarginEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mPowerSaveCheckBox = (CheckBox) findViewById(R.id.powerSaveCheckBox);
        mDemoModeCheckBox = (CheckBox) findViewById(R.id.demoModeCheckBox);
        mDeviceMarginEditText = (EditText) findViewById(R.id.deviceMarginEditText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
    }

    @Override
    protected void onPause() {
        saveSettings();
        super.onPause();
    }


    private void loadSettings() {
        SharedPreferences settings = getSharedPreferences(FpvToolBox.PREFS_NAME, 0);

        mDemoModeCheckBox.setChecked(settings.getBoolean("demoMode", FpvToolBox.DEFAULT_DEMO_MODE));
        mPowerSaveCheckBox.setChecked(settings.getBoolean("powerSave", FpvToolBox.DEFAULT_POWER_SAVE));
        mDeviceMarginEditText.setText(String.format(Locale.US, "%.1f", settings.getFloat("deviceMargin", FpvToolBox.getDefaultDeviceMargin())));
    }

    private void saveSettings() {
        SharedPreferences settings = getSharedPreferences(FpvToolBox.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean("demoMode", mDemoModeCheckBox.isChecked());
        editor.putBoolean("powerSave", mPowerSaveCheckBox.isChecked());
        float deviceMargin = Float.parseFloat(mDeviceMarginEditText.getText().toString());
        editor.putFloat("deviceMargin", deviceMargin);
        // Commit the edits!
        editor.commit();
    }

}
