/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.reusable.dialogs.DAVx5IncompatibleDialog
import at.techbee.jtx.ui.reusable.dialogs.DatePickerDialog
import at.techbee.jtx.util.DateTimeUtils
import java.util.*

@Composable
fun ListBottomAppBar(
    module: Module,
    iCal4ListLive: LiveData<List<ICal4List>>,
    listSettings: ListSettings,
    showQuickEntry: MutableState<Boolean>,
    multiselectEnabled: MutableState<Boolean>,
    selectedEntries: SnapshotStateList<Long>,
    allowNewEntries: Boolean,
    isBiometricsEnabled: Boolean,
    isBiometricsUnlocked: Boolean,
    isDAVx5Incompatible: Boolean,
    onAddNewEntry: () -> Unit,
    onFilterIconClicked: () -> Unit,
    onGoToDateSelected: (Long) -> Unit,
    onDeleteSelectedClicked: () -> Unit,
    onUpdateSelectedClicked: () -> Unit,
    onToggleBiometricAuthentication: () -> Unit
) {

    var showGoToDatePicker by remember { mutableStateOf(false) }
    var showDAVx5IncompatibleDialog by remember { mutableStateOf(false) }
    val iCal4List by iCal4ListLive.observeAsState(emptyList())

    val isFilterActive = listSettings.searchCategories.value.isNotEmpty()
                //|| searchOrganizers.value.isNotEmpty()
            || (listSettings.searchStatus.value.isNotEmpty())
            || listSettings.searchClassification.value.isNotEmpty()
            || listSettings.searchCollection.value.isNotEmpty()
            || listSettings.searchAccount.value.isNotEmpty()
            || listSettings.isExcludeDone.value
            || listSettings.isFilterStartInPast.value
            || listSettings.isFilterStartToday.value
            || listSettings.isFilterStartTomorrow.value
            || listSettings.isFilterStartFuture.value
            || (module == Module.TODO && listSettings.isFilterOverdue.value)
            || (module == Module.TODO && listSettings.isFilterDueToday.value)
            || (module == Module.TODO && listSettings.isFilterDueTomorrow.value)
            || (module == Module.TODO && listSettings.isFilterDueFuture.value)
            || (module == Module.TODO && listSettings.isFilterNoDatesSet.value)


    if(showGoToDatePicker) {
        var dates = iCal4List.map { it.dtstart ?: System.currentTimeMillis() }.toList()
        if (dates.isEmpty())
            dates = listOf(System.currentTimeMillis())

        // finds the closes number in a list of long
        fun List<Long>.findClosest(input: Long) = fold(null) { acc: Long?, num ->
            val closest = if (num <= input && (acc == null || num > acc)) num else acc
            if (closest == input) return@findClosest closest else return@fold closest
        }

        DatePickerDialog(
            datetime = DateTimeUtils.getTodayAsLong(),
            timezone = TimeZone.getDefault().id,
            allowNull = false,
            onConfirm = { selectedDate, _ ->
                selectedDate?.let { selected ->
                    val closestDate = dates.findClosest(selected)
                    iCal4List.find { it.dtstart == closestDate }?.let { foundEntry ->
                        onGoToDateSelected(foundEntry.id)
                    }
                }
            },
            onDismiss = { showGoToDatePicker = false },
            dateOnly = true,
            minDate = dates.minOf { it },
            maxDate =  dates.maxOf { it }
        )
    }

    if(showDAVx5IncompatibleDialog) {
        DAVx5IncompatibleDialog(onDismiss = { showDAVx5IncompatibleDialog = false } )
    }

    BottomAppBar(
        actions = {

            AnimatedVisibility(!multiselectEnabled.value) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { multiselectEnabled.value = true }) {
                        Icon(
                            Icons.Outlined.LibraryAddCheck,
                            contentDescription = "select multiple"
                        )
                    }
                    IconButton(onClick = { onFilterIconClicked() }) {
                        Icon(
                            Icons.Outlined.FilterList,
                            contentDescription = stringResource(id = R.string.filter),
                            tint = if (isFilterActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    AnimatedVisibility(allowNewEntries) {
                        IconButton(onClick = { showQuickEntry.value = !showQuickEntry.value }) {
                            Icon(
                                painterResource(
                                    id = R.drawable.ic_add_quick
                                ),
                                contentDescription = stringResource(
                                    id = when (module) {
                                        Module.JOURNAL -> R.string.menu_list_quick_journal
                                        Module.NOTE -> R.string.menu_list_quick_note
                                        Module.TODO -> R.string.menu_list_quick_todo
                                    }
                                ),
                                tint = if (showQuickEntry.value) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }

                    AnimatedVisibility(visible = module == Module.JOURNAL && listSettings.groupBy.value == null) {
                        IconButton(onClick = { showGoToDatePicker = true }) {
                            Icon(
                                Icons.Outlined.DateRange,
                                contentDescription = stringResource(id = R.string.menu_list_gotodate)
                            )
                        }
                    }

                    AnimatedVisibility(isBiometricsEnabled) {
                        IconButton(onClick = { onToggleBiometricAuthentication() }) {
                            Crossfade(isBiometricsUnlocked) {
                                if(it) {
                                    Icon(
                                        painterResource(id = R.drawable.ic_shield_lock_open),
                                        contentDescription = stringResource(id = R.string.list_biometric_protected_entries_unlocked),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        painterResource(id = R.drawable.ic_shield_lock),
                                        contentDescription = stringResource(id = R.string.list_biometric_protected_entries_locked),
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(isDAVx5Incompatible) {
                        IconButton(onClick = { showDAVx5IncompatibleDialog = true }) {
                            Icon(
                                Icons.Outlined.SyncProblem,
                                contentDescription = stringResource(id = R.string.dialog_davx5_outdated_title),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }


            AnimatedVisibility(multiselectEnabled.value) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        selectedEntries.clear()
                        multiselectEnabled.value = false
                    }) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = stringResource(id = R.string.cancel)
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                    )


                    IconButton(
                        onClick = { onDeleteSelectedClicked() },
                        enabled = selectedEntries.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = stringResource(id = R.string.delete),
                        )
                    }
                    IconButton(
                        onClick = { onUpdateSelectedClicked() },
                        enabled = selectedEntries.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = stringResource(id = R.string.more),
                        )
                    }

                    Text(
                        text = stringResource(R.string.x_selected, selectedEntries.size, iCal4List.size),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    TextButton(onClick = {
                        when(selectedEntries.size) {
                            0 -> selectedEntries.addAll(iCal4List.map { it.id })
                            iCal4List.size -> selectedEntries.clear()
                            else -> {
                                selectedEntries.clear()
                                selectedEntries.addAll(iCal4List.map { it.id })
                            }
                        }
                    }) {
                        Crossfade(selectedEntries.size) {
                            when (it) {
                                0 -> Text(stringResource(R.string.select_all))
                                iCal4List.size -> Text(stringResource(R.string.select_none))
                                else -> Text(stringResource(R.string.select_all))
                            }
                        }
                    }

                }
            }
        },
        floatingActionButton = {
            // TODO(b/228588827): Replace with Secondary FAB when available.
            AnimatedVisibility(allowNewEntries && !multiselectEnabled.value) {
                FloatingActionButton(
                    onClick = { onAddNewEntry() },
                ) {
                    Crossfade(module) {
                        when (it) {
                            Module.JOURNAL -> Icon(Icons.Outlined.EventNote, stringResource(R.string.toolbar_text_add_journal))
                            Module.NOTE -> Icon(Icons.Outlined.NoteAdd, stringResource(R.string.toolbar_text_add_note))
                            Module.TODO -> Icon(Icons.Outlined.AddTask, stringResource(R.string.toolbar_text_add_task))
                        }
                    }
                }
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Journal() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(ListViewModel.PREFS_LIST_JOURNALS, Context.MODE_PRIVATE)
        val listSettings = ListSettings.fromPrefs(prefs)

        ListBottomAppBar(
            module = Module.JOURNAL,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = listSettings,
            allowNewEntries = true,
            isBiometricsEnabled = false,
            isBiometricsUnlocked = false,
            isDAVx5Incompatible = false,
            multiselectEnabled = remember { mutableStateOf(false) },
            selectedEntries = remember { mutableStateListOf() },
            onAddNewEntry = { },
            showQuickEntry = remember { mutableStateOf(true) },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onDeleteSelectedClicked = { },
            onUpdateSelectedClicked = { },
            onToggleBiometricAuthentication = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Note() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(ListViewModel.PREFS_LIST_NOTES, Context.MODE_PRIVATE)
        val listSettings = ListSettings.fromPrefs(prefs)

        ListBottomAppBar(
            module = Module.NOTE,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = listSettings,
            allowNewEntries = false,
            isBiometricsEnabled = false,
            isBiometricsUnlocked = false,
            isDAVx5Incompatible = true,
            multiselectEnabled = remember { mutableStateOf(true) },
            selectedEntries = remember { mutableStateListOf() },
            onAddNewEntry = { },
            showQuickEntry = remember { mutableStateOf(false) },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onDeleteSelectedClicked = { },
            onUpdateSelectedClicked = { },
            onToggleBiometricAuthentication = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Todo() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(ListViewModel.PREFS_LIST_TODOS, Context.MODE_PRIVATE)
        val listSettings = ListSettings.fromPrefs(prefs)

        ListBottomAppBar(
            module = Module.TODO,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = listSettings,
            allowNewEntries = true,
            isDAVx5Incompatible = true,
            isBiometricsEnabled = true,
            isBiometricsUnlocked = false,
            multiselectEnabled = remember { mutableStateOf(false) },
            selectedEntries = remember { mutableStateListOf() },
            onAddNewEntry = { },
            showQuickEntry = remember { mutableStateOf(true) },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onDeleteSelectedClicked = { },
            onUpdateSelectedClicked = { },
            onToggleBiometricAuthentication = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview_Todo_filterActive() {
    MaterialTheme {

        val application = LocalContext.current.applicationContext
        val prefs = application.getSharedPreferences(ListViewModel.PREFS_LIST_TODOS, Context.MODE_PRIVATE)
        val listSettings = ListSettings.fromPrefs(prefs)

        ListBottomAppBar(
            module = Module.TODO,
            iCal4ListLive = MutableLiveData(emptyList()),
            listSettings = listSettings,
            allowNewEntries = true,
            isBiometricsEnabled = true,
            isBiometricsUnlocked = true,
            isDAVx5Incompatible = true,
            multiselectEnabled = remember { mutableStateOf(false) },
            selectedEntries = remember { mutableStateListOf() },
            onAddNewEntry = { },
            showQuickEntry = remember { mutableStateOf(true) },
            onFilterIconClicked = { },
            onGoToDateSelected = { },
            onDeleteSelectedClicked = { },
            onUpdateSelectedClicked = { },
            onToggleBiometricAuthentication = { }
        )
    }
}
