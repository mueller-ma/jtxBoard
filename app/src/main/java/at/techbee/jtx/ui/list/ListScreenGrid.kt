/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.theme.jtxCardCornerShape


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreenGrid(
    list: State<List<ICal4List>>,
    subtasksLive: LiveData<List<ICal4ListRel>>,
    selectedEntries: SnapshotStateList<Long>,
    scrollOnceId: MutableLiveData<Long?>,
    settingLinkProgressToSubtasks: Boolean,
    onProgressChanged: (itemId: Long, newPercent: Int) -> Unit,
    onClick: (itemId: Long, list: List<ICal4List>) -> Unit,
    onLongClick: (itemId: Long, list: List<ICal4List>) -> Unit
) {

    val subtasks by subtasksLive.observeAsState(emptyList())
    val scrollId by scrollOnceId.observeAsState(null)
    val gridState = rememberLazyStaggeredGridState()

    if(scrollId != null) {
        LaunchedEffect(list) {
            val index = list.value.indexOfFirst { iCalObject -> iCalObject.id == scrollId }
            if(index > -1) {
                gridState.animateScrollToItem(index)
                scrollOnceId.postValue(null)
            }
        }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = list.value,
            key = { item -> item.id }
        )
        { iCalObject ->

            val currentSubtasks = subtasks.filter { iCal4ListRel -> iCal4ListRel.relatedto.any { relatedto -> relatedto.reltype == Reltype.PARENT.name && relatedto.text == iCalObject.uid } }.map { it.iCal4List }

            ListCardGrid(
                iCalObject,
                selected = selectedEntries.contains(iCalObject.id),
                progressUpdateDisabled = settingLinkProgressToSubtasks && currentSubtasks.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(jtxCardCornerShape)
                    //.animateItemPlacement()
                    .combinedClickable(
                        onClick = { onClick(iCalObject.id, list.value) },
                        onLongClick = {
                            if (!iCalObject.isReadOnly)
                                onLongClick(iCalObject.id, list.value)
                        }
                    ),
                onProgressChanged = onProgressChanged,
                )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun ListScreenGrid_TODO() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            id = 1L
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = Status.IN_PROCESS.status
            classification = Classification.PUBLIC.classification
            dtstart = null
            due = null
            numAttachments = 0
            numSubnotes = 0
            numSubtasks = 0
            summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        val icalobject2 = ICal4List.getSample().apply {
            id = 2L
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = Status.IN_PROCESS.status
            classification = Classification.CONFIDENTIAL.classification
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            colorItem = Color.Blue.toArgb()
        }
        ListScreenGrid(
            list = remember { mutableStateOf(listOf(icalobject, icalobject2)) },
            subtasksLive = MutableLiveData(emptyList()),
            selectedEntries = remember { mutableStateListOf() },
            scrollOnceId = MutableLiveData(null),
            settingLinkProgressToSubtasks = false,
            onProgressChanged = { _, _ -> },
            onClick = { _, _ -> },
            onLongClick = { _, _ -> }
        )
    }
}



@Preview(showBackground = true)
@Composable
fun ListScreenGrid_JOURNAL() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            id = 1L
            component = Component.VJOURNAL.name
            module = Module.JOURNAL.name
            percent = 89
            status = Status.FINAL.status
            classification = Classification.PUBLIC.classification
            dtstart = null
            due = null
            numAttachments = 0
            numSubnotes = 0
            numSubtasks = 0
            summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        val icalobject2 = ICal4List.getSample().apply {
            id = 2L
            component = Component.VJOURNAL.name
            module = Module.JOURNAL.name
            percent = 89
            status = Status.IN_PROCESS.status
            classification = Classification.CONFIDENTIAL.classification
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            colorItem = Color.Blue.toArgb()
        }
        ListScreenGrid(
            list = remember { mutableStateOf(listOf(icalobject, icalobject2)) },
            subtasksLive = MutableLiveData(emptyList()),
            selectedEntries = remember { mutableStateListOf() },
            scrollOnceId = MutableLiveData(null),
            settingLinkProgressToSubtasks = false,
            onProgressChanged = { _, _ -> },
            onClick = { _, _ -> },
            onLongClick = { _, _ -> }
        )
    }
}

