/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.settings

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.preference.PreferenceManager
import at.techbee.jtx.R

enum class DropdownSetting(
    val key: String,
    val icon: ImageVector,
    val title: Int,
    val subtitle: Int? = null,
    val options: List<DropdownSettingOption>,
    val default: DropdownSettingOption
) {
    SETTING_THEME(
        key = "settings_theme",
        icon = Icons.Outlined.FormatPaint,
        title = R.string.settings_select_theme,
        options = listOf(
            DropdownSettingOption.THEME_SYSTEM,
            DropdownSettingOption.THEME_LIGHT,
            DropdownSettingOption.THEME_DARK,
            DropdownSettingOption.THEME_TRUE_DARK,
            DropdownSettingOption.THEME_CONTRAST
        ),
        default = DropdownSettingOption.THEME_SYSTEM
    ),
    SETTING_AUDIO_FORMAT(
        key = "setting_audio_format",
        icon = Icons.Outlined.MusicNote,
        title = R.string.settings_select_mimetype_for_audio,
        options = listOf(
            DropdownSettingOption.AUDIO_FORMAT_3GPP,
            DropdownSettingOption.AUDIO_FORMAT_AAC,
            DropdownSettingOption.AUDIO_FORMAT_OGG
        ),
        default = DropdownSettingOption.AUDIO_FORMAT_3GPP
    ),

    SETTING_DEFAULT_JOURNALS_DATE(
        key = "setting_default_journals_date",
        icon = Icons.Outlined.EditCalendar,
        title = R.string.settings_default_journals_date,
        options = listOf(
            DropdownSettingOption.DEFAULT_JOURNALS_DATE_PREVIOUS_DAY,
            DropdownSettingOption.DEFAULT_JOURNALS_DATE_CURRENT_DAY,
            DropdownSettingOption.DEFAULT_JOURNALS_DATE_CURRENT_HOUR,
            DropdownSettingOption.DEFAULT_JOURNALS_DATE_CURRENT_15MIN,
            DropdownSettingOption.DEFAULT_JOURNALS_DATE_CURRENT_5MIN,
            DropdownSettingOption.DEFAULT_JOURNALS_DATE_CURRENT_MIN
            ),
        default = DropdownSettingOption.DEFAULT_JOURNALS_DATE_CURRENT_DAY
    ),

    SETTING_DEFAULT_START_DATE(
        key = "setting_default_start_date",
        icon = Icons.Outlined.EditCalendar,
        title = R.string.settings_default_start_date,
        options = listOf(
            DropdownSettingOption.DEFAULT_DATE_NONE,
            DropdownSettingOption.DEFAULT_DATE_SAME_DAY,
            DropdownSettingOption.DEFAULT_DATE_NEXT_DAY,
            DropdownSettingOption.DEFAULT_DATE_TWO_DAYS,
            DropdownSettingOption.DEFAULT_DATE_THREE_DAYS,
            DropdownSettingOption.DEFAULT_DATE_ONE_WEEK,
            DropdownSettingOption.DEFAULT_DATE_TWO_WEEKS,
            DropdownSettingOption.DEFAULT_DATE_FOUR_WEEKS,
        ),
        default = DropdownSettingOption.DEFAULT_DATE_NONE
    ),
    SETTING_DEFAULT_DUE_DATE(
        key = "setting_default_due_date",
        icon = Icons.Outlined.EditCalendar,
        title = R.string.settings_default_due_date,
        options = listOf(
            DropdownSettingOption.DEFAULT_DATE_NONE,
            DropdownSettingOption.DEFAULT_DATE_SAME_DAY,
            DropdownSettingOption.DEFAULT_DATE_NEXT_DAY,

            DropdownSettingOption.DEFAULT_DATE_NEXT_DAY,
            DropdownSettingOption.DEFAULT_DATE_TWO_DAYS,
            DropdownSettingOption.DEFAULT_DATE_THREE_DAYS,
            DropdownSettingOption.DEFAULT_DATE_ONE_WEEK,
            DropdownSettingOption.DEFAULT_DATE_TWO_WEEKS,
            DropdownSettingOption.DEFAULT_DATE_FOUR_WEEKS,
        ),
        default = DropdownSettingOption.DEFAULT_DATE_NONE
    ),
    SETTING_PROGRESS_STEP(
        key = "setting_progress_step",
        icon = Icons.Outlined.Tune,
        title = R.string.settings_progress_step,
        options = listOf(
            DropdownSettingOption.PROGRESS_STEP_1,
            DropdownSettingOption.PROGRESS_STEP_2,
            DropdownSettingOption.PROGRESS_STEP_5,
            DropdownSettingOption.PROGRESS_STEP_10,
            DropdownSettingOption.PROGRESS_STEP_20,
            DropdownSettingOption.PROGRESS_STEP_25,
            DropdownSettingOption.PROGRESS_STEP_50
        ),
        default = DropdownSettingOption.PROGRESS_STEP_1
    ),
    SETTING_AUTO_ALARM(
        key = "setting_auto_alarm",
        icon = Icons.Outlined.Alarm,
        title = R.string.settings_auto_alarm,
        subtitle = R.string.settings_auto_alarm_sub,
        options = listOf(
            DropdownSettingOption.AUTO_ALARM_OFF,
            DropdownSettingOption.AUTO_ALARM_ON_START,
            DropdownSettingOption.AUTO_ALARM_ON_DUE,
            DropdownSettingOption.AUTO_ALARM_ALWAYS_ON_DUE
        ),
        default = DropdownSettingOption.AUTO_ALARM_OFF
    ),
    SETTING_PROTECT_BIOMETRIC(
        key = "setting_protect_biometric",
        icon = Icons.Outlined.Fingerprint,
        title = R.string.settings_protect_biometric,
        subtitle = R.string.settings_protect_biometric_sub,
        options = listOf(
            DropdownSettingOption.PROTECT_BIOMETRIC_OFF,
            DropdownSettingOption.PROTECT_BIOMETRIC_PRIVATE_CONFIDENTIAL,
            DropdownSettingOption.PROTECT_BIOMETRIC_CONFIDENTIAL,
            DropdownSettingOption.PROTECT_BIOMETRIC_ALL
        ),
        default = DropdownSettingOption.PROTECT_BIOMETRIC_OFF
    ),
    ;

    fun save(newDropdownSettingOption: DropdownSettingOption, context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(key, newDropdownSettingOption.key).apply()
    }
}
