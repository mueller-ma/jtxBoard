/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import at.techbee.jtx.R

enum class SwitchSetting(
    val key: String,
    val icon: @Composable () -> Unit,
    val title: Int,
    val subtitle: Int? = null,
    val default: Boolean
) {
    SETTING_ENABLE_JOURNALS(
        key = "settings_enable_journals",
        icon = { Icon(Icons.Outlined.EventNote, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_modules_enable_journals,
        default = true
    ),
    SETTING_ENABLE_NOTES(
        key = "settings_enable_notes",
        icon = { Icon(Icons.Outlined.NoteAdd, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_modules_enable_notes,
        default = true
    ),
    SETTING_ENABLE_TASKS(
        key = "settings_enable_tasks",
        icon = { Icon(Icons.Outlined.AddTask, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_modules_enable_tasks,
        default = true
    ),
    SETTING_AUTO_EXPAND_SUBTASKS(
        key = "settings_auto_expand_subtasks",
        icon = { Icon(Icons.Outlined.TaskAlt, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_default_expand_subtasks,
        default = false
    ),
    SETTING_AUTO_EXPAND_SUBNOTES(
        key = "settings_auto_expand_subnotes",
        icon = { Icon(Icons.Outlined.Note, null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_default_expand_subnotes,
        default = false
    ),
    SETTING_AUTO_EXPAND_ATTACHMENTS(
        key = "settings_auto_expand_attachments",
        icon = { Icon(
            Icons.Outlined.Attachment, null, modifier = Modifier.padding(
            16.dp
        ))  },
        title = R.string.settings_default_expand_attachments,
        default = false
    ),
    SETTING_SHOW_PROGRESS_FOR_MAINTASKS(
        key = "settings_show_progress_for_maintasks_in_list",
        icon = { Icon(
            painterResource(id = R.drawable.ic_progress_task), null, modifier = Modifier.padding(
            16.dp
        ))  },
        title = R.string.settings_show_progress_for_maintasks,
        default = false
    ),
    SETTING_SHOW_PROGRESS_FOR_SUBTASKS(
        key = "settings_show_progress_for_subtasks_in_list",
        icon = { Icon(
            painterResource(id = R.drawable.ic_progress_subtask), null, modifier = Modifier.padding(
            16.dp
        ))  },
        title = R.string.settings_show_progress_for_subtasks,
        default = false
    ),
    SETTING_DISABLE_ALARMS_FOR_READONLY(
    key = "settings_disable_alarms_for_readonly",
    icon = { Icon(Icons.Outlined.AlarmOff, contentDescription = null, modifier = Modifier.padding(16.dp)) },
    title = R.string.settings_disable_alarms_for_readonly,
    default = false
    ),
    SETTING_LINK_PROGRESS_TO_SUBTASKS(
        key = "settings_link_progress_to_subtasks",
        icon = { Icon(Icons.Outlined.DoneAll, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_link_progress_to_subtasks,
        subtitle = R.string.settings_attention_experimental_feature,
        default = false
    ),
    SETTING_KEEP_STATUS_PROGRESS_COMPLETED_IN_SYNC(
    key = "settings_keep_status_progress_completed_in_sync",
    icon = { Icon(Icons.Outlined.PublishedWithChanges, contentDescription = null, modifier = Modifier.padding(16.dp)) },
    title = R.string.settings_keep_status_progress_completed_in_sync,
    default = true
    ),
    SETTING_JOURNALS_SET_DEFAULT_CURRENT_LOCATION(
        key = "settings_journals_set_default_current_location",
        icon = { Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_create_with_current_location,
        default = false
    ),
    SETTING_NOTES_SET_DEFAULT_CURRENT_LOCATION(
        key = "settings_notes_set_default_current_location",
        icon = { Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_create_with_current_location,
        default = false
    ),
    SETTING_TASKS_SET_DEFAULT_CURRENT_LOCATION(
        key = "settings_tasks_set_default_current_location",
        icon = { Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.padding(16.dp)) },
        title = R.string.settings_create_with_current_location,
        default = false
    );
    fun save(newSwitchValue: Boolean, context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, newSwitchValue).apply()
    }
}
