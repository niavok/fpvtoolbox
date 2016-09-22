package com.parrot.fpvtoolbox;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class SettingsActivity extends Activity {

    private CheckBox mDemoModeCheckBox;
    private CheckBox mPowerSaveCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mPowerSaveCheckBox = (CheckBox) findViewById(R.id.powerSaveCheckBox);
        mDemoModeCheckBox = (CheckBox) findViewById(R.id.demoModeCheckBox);
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
    }

    private void saveSettings() {
        SharedPreferences settings = getSharedPreferences(FpvToolBox.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean("demoMode", mDemoModeCheckBox.isChecked());
        editor.putBoolean("powerSave", mPowerSaveCheckBox.isChecked());

        // Commit the edits!
        editor.commit();
    }

}
