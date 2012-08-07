/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.custommapsapp.android.storage;

import com.custommapsapp.android.AboutDialog;
import com.custommapsapp.android.InertiaScroller;
import com.custommapsapp.android.MemoryUtil;
import com.custommapsapp.android.R;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

/**
 * EditPreferences is the PreferenceActivity for Custom Maps. Currently
 * supported preferences: <ul>
 * <li> isMetric (bool) - selects between metric and English units
 * <li> useMultitouch (bool) - selects if multitouch is enabled (if available)
 * </ul>
 *
 * @author Marko Teittinen
 */
public class EditPreferences extends PreferenceActivity {
  private static final String LOG_TAG = "Custom Maps";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getPreferenceManager().setSharedPreferencesName(PreferenceStore.SHARED_PREFS_NAME);
    setPreferenceScreen(createPreferenceScreen());
  }

  private PreferenceScreen createPreferenceScreen() {
    // Root
    PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

    // Units preference
    CheckBoxPreference isMetric = new CheckBoxPreference(this);
    isMetric.setDefaultValue(PreferenceStore.isMetricLocale());
    isMetric.setKey(PreferenceStore.PREFS_METRIC);
    isMetric.setTitle(getString(R.string.metric_title));
    isMetric.setSummaryOff(getString(R.string.metric_use_non_metric));
    isMetric.setSummaryOn(getString(R.string.metric_use_metric));
    root.addPreference(isMetric);

    // Multitouch preference, aka "pinch zoom"
    if (InertiaScroller.isMultitouchAvailable()) {
      CheckBoxPreference useMultitouch = new CheckBoxPreference(this);
      useMultitouch.setDefaultValue(Boolean.TRUE);
      useMultitouch.setKey(PreferenceStore.PREFS_MULTITOUCH);
      useMultitouch.setTitle(getString(R.string.multitouch_title));
      useMultitouch.setSummaryOff(getString(R.string.multitouch_dont_use_pinch));
      useMultitouch.setSummaryOn(getString(R.string.multitouch_use_pinch));
      root.addPreference(useMultitouch);
    }

    // Display distance to center of screen
    CheckBoxPreference distanceDisplay = new CheckBoxPreference(this);
    distanceDisplay.setDefaultValue(false);
    distanceDisplay.setKey(PreferenceStore.PREFS_SHOW_DISTANCE);
    distanceDisplay.setTitle(getString(R.string.distance_title));
    distanceDisplay.setSummaryOn(getString(R.string.distance_show));
    distanceDisplay.setSummaryOff(getString(R.string.distance_dont_show));
    root.addPreference(distanceDisplay);

    // Display safety reminder when map is changed preference
    CheckBoxPreference safetyReminder = new CheckBoxPreference(this);
    safetyReminder.setDefaultValue(true);
    safetyReminder.setKey(PreferenceStore.PREFS_SHOW_REMINDER);
    safetyReminder.setTitle(getString(R.string.safety_reminder_title));
    safetyReminder.setSummaryOn(getString(R.string.safety_reminder_show));
    safetyReminder.setSummaryOff(getString(R.string.safety_reminder_dont_show));
    root.addPreference(safetyReminder);

    // About dialog
    Preference about = createAboutPreference();
    if (about != null) {
      root.addPreference(about);
    }

    // Maximum image size info
    Preference imageSizeInfo = createImageSizeInfo();
    if (imageSizeInfo != null) {
      root.addPreference(imageSizeInfo);
    }

    return root;
  }

  private Preference createImageSizeInfo() {
    Preference imageSizeInfo = new Preference(this);
    imageSizeInfo.setSelectable(false);
    imageSizeInfo.setTitle(getString(R.string.max_map_img_size_title));
    float megaPixels = MemoryUtil.getMaxImagePixelCount(this) / 1E6f;
    imageSizeInfo.setSummary(String.format(getString(R.string.max_map_img_size), megaPixels));
    return imageSizeInfo;
  }

  private Preference createAboutPreference() {
    Preference aboutPreference = new Preference(this);
    aboutPreference.setTitle(getString(R.string.about_custom_maps));
    aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        showAboutDialog();
        return true;
      }
    });
    return aboutPreference;
  }

  // --------------------------------------------------------------------------
  // About dialog

  private AboutDialog aboutDialog;

  private void showAboutDialog() {
    aboutDialog = new AboutDialog(this);
    String version = PreferenceStore.instance(this).getVersion();
    aboutDialog.setVersion(version);
    aboutDialog.useSingleButton();
    aboutDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialog) {
        AboutDialog aboutDialog = (AboutDialog) dialog;
        // Remove listener, and clear internal variable
        aboutDialog.setOnDismissListener(null);
        EditPreferences.this.aboutDialog = null;
      }
    });
    aboutDialog.show();
  }

  // --------------------------------------------------------------------------
  // Lifecycle methods

  private static final String PREFIX = EditPreferences.class.getName();
  private static final String ABOUT_SHOWING = PREFIX + ".AboutShowing";

  @Override
  protected void onRestoreInstanceState(Bundle state) {
    super.onRestoreInstanceState(state);
    if (state.getBoolean(ABOUT_SHOWING, false)) {
      showAboutDialog();
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(ABOUT_SHOWING, aboutDialog != null);
  }
}
