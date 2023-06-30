package com.niya.gps.presentation.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import com.niya.gps.R

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var timePref: Preference
    private lateinit var colorPref: Preference
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preference, rootKey)
        init()
    }

    private fun init() {
        val changeListener = onChangeListener()
        timePref = findPreference(UPDATE_TIME_KEY)!!
        colorPref = findPreference(COLOR_KEY)!!
        timePref.onPreferenceChangeListener = changeListener
        colorPref.onPreferenceChangeListener = changeListener
        initPref()
    }

    private fun initPref() {
        val prefTime = timePref.preferenceManager.sharedPreferences
        val nameArray = resources.getStringArray(R.array.time_update_name)
        val valueArray = resources.getStringArray(R.array.time_update_value)
        val title = timePref.title
        timePref.title = "$title: ${
            nameArray[valueArray
                .indexOf(prefTime?.getString(UPDATE_TIME_KEY, "3000"))]
        }"
        val trackColor = prefTime?.getString(COLOR_KEY, "#FF4FC3F7")
        colorPref.icon?.setTint(Color.parseColor(trackColor))
    }

    private fun onTimeChange(value: String) {
        val nameArray = resources.getStringArray(R.array.time_update_name)
        val valueArray = resources.getStringArray(R.array.time_update_value)
        val title = timePref.title.toString().substringBefore(":")
        timePref.title = "$title: ${nameArray[valueArray.indexOf(value)]}"

    }

    private fun onChangeListener(): OnPreferenceChangeListener {
        return OnPreferenceChangeListener { pref, value ->
            when (pref.key) {
                UPDATE_TIME_KEY -> onTimeChange(value.toString())
                COLOR_KEY -> pref.icon?.setTint(Color.parseColor(value.toString()))
            }
            true
        }
    }


    companion object {
        const val UPDATE_TIME_KEY = "update_time_key"
        const val COLOR_KEY = "color_key"
    }
}
