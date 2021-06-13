package at.bitfire.notesx5.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import at.bitfire.notesx5.MainActivity
import at.bitfire.notesx5.R


class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val ENFORCE_DARK_THEME = "settings_enfore_dark_theme"
        const val SHOW_USER_CONSENT = "show_user_consent"
        const val SHOW_SUBTASKS_IN_LIST = "settings_show_subtasks_in_list"
        const val SHOW_ATTACHMENTS_IN_LIST = "settings_show_attachments_in_list"
        const val SHOW_PROGRESS_IN_LIST = "settings_show_progress_in_list"
        const val SHOW_ADS = "settings_show_ads"



    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val mainActivity = activity as MainActivity


        // register a change listener for the theme to update the UI immediately
        val settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        settings.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            val enforceDark = sharedPreferences.getBoolean(ENFORCE_DARK_THEME, false)
            if (enforceDark)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        preferenceScreen.get<Preference>(SHOW_USER_CONSENT)?.setOnPreferenceClickListener {
            Log.d("showUserConsent", "Clicked on Show User Consent")
            val mainActivity = requireActivity() as MainActivity
            mainActivity.resetUserConsent()
            return@setOnPreferenceClickListener true
        }

        preferenceScreen.get<SwitchPreference>(SHOW_ADS)?.setOnPreferenceChangeListener { preference, adsEnabled ->
            preferenceScreen.get<Preference>(SHOW_USER_CONSENT)?.isEnabled = adsEnabled as Boolean
            if(adsEnabled)
                mainActivity.initializeUserConsent()     // show user consent if ads get enabled (despite the user bought the full version?)
            return@setOnPreferenceChangeListener true
        }

        if(mainActivity.isTrialPeriod()) {
            preferenceScreen.get<Preference>(SHOW_ADS)?.isEnabled = false
            preferenceScreen.get<Preference>(SHOW_USER_CONSENT)?.isEnabled = false
        }

    }
}