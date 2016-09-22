/*
 * Copyright (c) 2016, Frédéric Bertolus <frederic.bertolus@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.niavok.fpvtoolbox;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Locale;

import static com.niavok.fpvtoolbox.FpvToolBox.DEFAULT_CHROMATIC_ABERRATION_CORRECTION_MODE;

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
        final Button resetAllSettingsButton = (Button) findViewById(R.id.resetAllButton);
        resetAllSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAllSettings();
            }
        });
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


    private void resetAllSettings() {


        SharedPreferences settings = getSharedPreferences(FpvToolBox.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean("demoMode", FpvToolBox.DEFAULT_DEMO_MODE);
        editor.putBoolean("powerSave", FpvToolBox.DEFAULT_POWER_SAVE);
        editor.putFloat("deviceMargin", FpvToolBox.getDefaultDeviceMargin());

        editor.putInt("chromaticAberrationCorrection", DEFAULT_CHROMATIC_ABERRATION_CORRECTION_MODE);
        editor.putBoolean("distortionCorrection", FpvToolBox.DEFAULT_DISTORTION_CORRECTION);
        editor.putBoolean("lensLimits", FpvToolBox.DEFAULT_DISPLAY_LENS_LIMITS);
        editor.putFloat("viewScale", FpvToolBox.DEFAULT_SCALE);
        editor.putFloat("ipd", FpvToolBox.DEFAULT_IPD);
        editor.putFloat("panH", FpvToolBox.DEFAULT_PAN_H);
        editor.putFloat("panV", FpvToolBox.DEFAULT_PAN_V);

        // Commit the edits!
        editor.commit();

        // Reload to update menu
        loadSettings();
    }
}
