package at.techbee.jtx.widgets.elements

import androidx.compose.runtime.Composable
import androidx.glance.unit.ColorProvider
import at.techbee.jtx.database.views.ICal4List


@Composable
fun ListSubEntries(
    parentUID: String?,
    groupedList: Map<String?, List<ICal4List>>,
    textColor: ColorProvider,
    containerColor: ColorProvider,
    checkboxEnd: Boolean
) {
    if( parentUID== null)
        return
    val sublist = groupedList.get(parentUID) ?: return

    sublist.forEach { entry ->
        ListEntry(
            obj = entry,
            groupedList = groupedList,
            textColor = textColor,
            containerColor = containerColor,
            checkboxEnd = checkboxEnd
        )
    }
}
