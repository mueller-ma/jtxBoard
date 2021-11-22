/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import java.lang.ClassCastException


class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val ENFORCE_DARK_THEME = "settings_enfore_dark_theme"
        const val SHOW_SUBTASKS_IN_LIST = "settings_show_subtasks_in_list"
        const val SHOW_ATTACHMENTS_IN_LIST = "settings_show_attachments_in_list"
        const val SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST = "settings_show_progress_for_maintasks_in_list"
        const val SHOW_PROGRESS_FOR_SUBTASKS_IN_LIST = "settings_show_progress_for_subtasks_in_list"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        //val mainActivity = activity as MainActivity


        // register a change listener for the theme to update the UI immediately
        val settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        settings.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            if(key == ENFORCE_DARK_THEME) {
                val enforceDark = sharedPreferences.getBoolean(ENFORCE_DARK_THEME, false)
                if (enforceDark)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    override fun onResume() {

        try {
            val activity = requireActivity() as MainActivity
            activity.setToolbarTitle(getString(R.string.toolbar_text_settings), null)
        } catch (e: ClassCastException) {
            Log.d("setToolbarText", "Class cast to MainActivity failed (this is common for tests but doesn't really matter)\n$e")
        }
        super.onResume()
    }

}