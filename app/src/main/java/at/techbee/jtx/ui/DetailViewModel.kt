/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.util.Ical4androidUtil
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private var database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao

    lateinit var icalEntity: LiveData<ICalEntity?>
    lateinit var relatedSubnotes: LiveData<List<ICal4List>>
    lateinit var relatedSubtasks: LiveData<List<ICal4List>>
    lateinit var recurInstances: LiveData<List<ICalObject?>>

    lateinit var allCategories: LiveData<List<String>>
    lateinit var allResources: LiveData<List<String>>
    lateinit var allCollections: LiveData<List<ICalCollection>>

    var icsFormat: MutableLiveData<String?> = MutableLiveData(null)
    var icsFileWritten: MutableLiveData<Boolean?> = MutableLiveData(null)

    var entryDeleted = mutableStateOf(false)
    var navigateToId = mutableStateOf<Long?>(null)

    val mediaPlayer = MediaPlayer()

    init {

        viewModelScope.launch {

            // insert a new value to initialize the item or load the existing one from the DB
            icalEntity = MutableLiveData<ICalEntity?>().apply {
                    postValue(ICalEntity(ICalObject(), null, null, null, null, null))
                }

            allCategories = database.getAllCategoriesAsText()
            allResources = database.getAllResourcesAsText()
            allCollections = Transformations.switchMap(icalEntity) {
                when (it?.property?.component) {
                    Component.VTODO.name -> database.getAllWriteableVTODOCollections()
                    Component.VJOURNAL.name -> database.getAllWriteableVJOURNALCollections()
                    else -> database.getAllCollections() // should not happen!
                }
            }

            relatedSubnotes = MutableLiveData(emptyList())
            relatedSubtasks = MutableLiveData(emptyList())

            recurInstances = Transformations.switchMap(icalEntity) {
                it?.property?.id?.let { originalId -> database.getRecurInstances(originalId) }
            }
        }
    }

    fun load(icalObjectId: Long) {
        viewModelScope.launch {
            icalEntity = database.get(icalObjectId)

            relatedSubnotes = Transformations.switchMap(icalEntity) {
                it?.property?.uid?.let { parentUid -> database.getAllSubnotesOf(parentUid) }
            }

            relatedSubtasks = Transformations.switchMap(icalEntity) {
                it?.property?.uid?.let { parentUid ->
                    database.getAllSubtasksOf(parentUid)
                }
            }
        }

    }

/*
    fun insertRelated(newIcalObject: ICalObject, attachment: Attachment?) {

        this.icalEntity.value?.property?.let {
            makeRecurringExceptionIfNecessary(it)
        }

        viewModelScope.launch {
            newIcalObject.collectionId = icalEntity.value?.ICalCollection?.collectionId ?: 1L
            val newNoteId = database.insertICalObject(newIcalObject)

            // We insert both directions in the database - deprecated, only one direction
            //database.insertRelatedto(Relatedto(icalObjectId = icalEntity.value!!.property.id, linkedICalObjectId = newNoteId, reltype = Reltype.CHILD.name, text = newIcalObject.uid))
            database.insertRelatedto(Relatedto(icalObjectId = newNoteId, reltype = Reltype.PARENT.name, text = icalEntity.value!!.property.uid))

            if(attachment != null) {
                attachment.icalObjectId = newNoteId
                database.insertAttachment(attachment)
            }

            //database.updateSetDirty(icalItemId, System.currentTimeMillis())
            SyncUtil.notifyContentObservers(getApplication())
        }
    }

 */


    fun updateProgress(id: Long, newPercent: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = database.getICalObjectById(id) ?: return@launch
            if(icalEntity.value?.property?.isRecurLinkedInstance == true) {
                ICalObject.makeRecurringException(icalEntity.value?.property!!, database)
                Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_SHORT).show()
            }
            item.setUpdatedProgress(newPercent)
            database.update(item)
            SyncUtil.notifyContentObservers(getApplication())
        }
    }

    fun updateSummary(icalObjectId: Long, newSummary: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val icalObject = database.getICalObjectById(icalObjectId) ?: return@launch
            icalObject.summary = newSummary
            icalObject.makeDirty()
            database.update(icalObject)
            SyncUtil.notifyContentObservers(getApplication())
        }
    }


    fun save(iCalObject: ICalObject,
             categories: List<Category>,
             comments: List<Comment>,
             attendees: List<Attendee>,
             resources: List<Resource>,
             attachments: List<Attachment>,
             alarms: List<Alarm>
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            if(icalEntity.value?.categories != categories) {
                iCalObject.makeDirty()
                database.deleteCategories(iCalObject.id)
                categories.forEach { changedCategory ->
                    changedCategory.icalObjectId = iCalObject.id
                    database.insertCategory(changedCategory)
                }
            }

            if(icalEntity.value?.comments != comments) {
                iCalObject.makeDirty()
                database.deleteComments(iCalObject.id)
                comments.forEach { changedComment ->
                    changedComment.icalObjectId = iCalObject.id
                    database.insertComment(changedComment)
                }
            }

            if(icalEntity.value?.attendees != attendees) {
                iCalObject.makeDirty()
                database.deleteAttendees(iCalObject.id)
                attendees.forEach { changedAttendee ->
                    changedAttendee.icalObjectId = iCalObject.id
                    database.insertAttendee(changedAttendee)
                }
            }

            if(icalEntity.value?.resources != resources) {
                iCalObject.makeDirty()
                database.deleteResources(iCalObject.id)
                resources.forEach { changedResource ->
                    changedResource.icalObjectId = iCalObject.id
                    database.insertResource(changedResource)
                }
            }

            if(icalEntity.value?.attachments != attachments) {
                iCalObject.makeDirty()
                database.deleteAttachments(iCalObject.id)
                attachments.forEach { changedAttachment ->
                    changedAttachment.icalObjectId = iCalObject.id
                    database.insertAttachment(changedAttachment)
                }
                Attachment.scheduleCleanupJob(getApplication())
            }

            if(icalEntity.value?.alarms != alarms) {
                iCalObject.makeDirty()
                database.deleteAlarms(iCalObject.id)
                alarms.forEach { changedAlarm ->
                    changedAlarm.icalObjectId = iCalObject.id
                    database.insertAlarm(changedAlarm)
                    //changedAlarm.scheduleNotification()   // TODO!
                }
            }

            if(icalEntity.value?.property != iCalObject) {
                iCalObject.makeDirty()
                database.update(iCalObject)

                if(icalEntity.value?.property?.isRecurLinkedInstance == true) {
                    ICalObject.makeRecurringException(icalEntity.value?.property!!, database)
                    Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_SHORT).show()
                }
            }
            SyncUtil.notifyContentObservers(getApplication())
        }
    }


    fun addSubEntry(subEntry: ICalObject, attachment: Attachment?) {
        viewModelScope.launch(Dispatchers.IO) {
            subEntry.collectionId = icalEntity.value?.property?.collectionId!!
            val subEntryId = database.insertICalObject(subEntry)

            attachment?.let {
                it.icalObjectId = subEntryId
                database.insertAttachment(it)
            }

            database.insertRelatedto(
                Relatedto(
                    icalObjectId = subEntryId,
                    reltype = Reltype.PARENT.name,
                    text = icalEntity.value?.property?.uid!!
                )
            )
            if(icalEntity.value?.property?.isRecurLinkedInstance == true) {
                ICalObject.makeRecurringException(icalEntity.value?.property!!, database)
                Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_SHORT).show()
            }
            SyncUtil.notifyContentObservers(getApplication())
        }
    }


    fun delete() {
        viewModelScope.launch(Dispatchers.IO) {
            if(icalEntity.value?.property?.isRecurLinkedInstance == true) {
                ICalObject.makeRecurringException(icalEntity.value?.property!!, database)
                Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_SHORT).show()
            }
            icalEntity.value?.property?.id?.let { id ->
                ICalObject.deleteItemWithChildren(id, database)
                entryDeleted.value = true
            }
        }
    }

    /**
     * Delete function for subtasks and subnotes
     * @param [icalObjectId] of the subtask/subnote to be deleted
     */
    fun deleteById(icalObjectId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            /*
            if(icalEntity.value?.property?.isRecurLinkedInstance == true) {
                ICalObject.makeRecurringException(icalEntity.value?.property!!, database)
                Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_SHORT).show()
            }
             */
            icalEntity.value?.property?.id?.let { id ->
                ICalObject.deleteItemWithChildren(icalObjectId, database)
                //entryDeleted.value = true
            }
        }
    }

    fun createCopy(newModule: Module) {
        icalEntity.value?.let { createCopy(it, newModule) }
    }

    private fun createCopy(icalEntityToCopy: ICalEntity, newModule: Module, newParentUID: String? = null) {
        val newEntity = icalEntityToCopy.getIcalEntityCopy(newModule)

        viewModelScope.launch(Dispatchers.IO) {
            val newId = database.insertICalObject(newEntity.property)
            newEntity.alarms?.forEach { alarm ->
                database.insertAlarm(alarm.copy(alarmId = 0L, icalObjectId = newId ))   // TODO: Schedule Alarm!
            }
            newEntity.attachments?.forEach { attachment ->
                database.insertAttachment(attachment.copy(icalObjectId = newId, attachmentId = 0L))
            }
            newEntity.attendees?.forEach { attendee ->
                database.insertAttendee(attendee.copy(icalObjectId = newId, attendeeId = 0L))
            }
            newEntity.categories?.forEach { category ->
                database.insertCategory(category.copy(icalObjectId = newId, categoryId = 0L))
            }
            newEntity.comments?.forEach { comment ->
                database.insertComment(comment.copy(icalObjectId = newId, commentId = 0L))
            }
            newEntity.resources?.forEach { resource ->
                database.insertResource(resource.copy(icalObjectId = newId,  resourceId = 0L))
            }
            newEntity.unknown?.forEach { unknown ->
                database.insertUnknownSync(unknown.copy(icalObjectId = newId, unknownId = 0L))
            }
            newEntity.organizer?.let { organizer ->
                database.insertOrganizer(organizer.copy(icalObjectId = newId, organizerId = 0L))
            }

            newEntity.relatedto?.forEach { relatedto ->
                if (relatedto.reltype == Reltype.PARENT.name && newParentUID != null) {
                    database.insertRelatedto(relatedto.copy(relatedtoId = 0L, icalObjectId = newId, text = newParentUID))
                }
            }

            val children = database.getRelatedChildren(icalEntityToCopy.property.id)
            children.forEach { child ->
                database.getSync(child)?.let { createCopy(icalEntityToCopy = it, newModule = it.property.getModuleFromString(), newParentUID = newEntity.property.uid) }
            }

            if(newParentUID == null)   // we navigate only to the parent (not to the children that are invoked recursively)
                navigateToId.value = newId
        }
    }

    fun retrieveICSFormat() {

        viewModelScope.launch(Dispatchers.IO)  {
            val account = icalEntity.value?.ICalCollection?.getAccount() ?: return@launch
            val collectionId = icalEntity.value?.property?.collectionId ?: return@launch
            val iCalObjectId = icalEntity.value?.property?.id ?: return@launch
            val ics = Ical4androidUtil.getICSFormatFromProvider(account, getApplication(), collectionId, iCalObjectId) ?: return@launch
            icsFormat.postValue(ics)
        }
    }

    fun writeICSFile(os: ByteArrayOutputStream) {

        viewModelScope.launch(Dispatchers.IO)  {
            val account = icalEntity.value?.ICalCollection?.getAccount() ?: return@launch
            val collectionId = icalEntity.value?.property?.collectionId ?: return@launch
            val iCalObjectId = icalEntity.value?.property?.id ?: return@launch
            icsFileWritten.postValue(Ical4androidUtil.writeICSFormatFromProviderToOS(account, getApplication(), collectionId, iCalObjectId, os))
        }

    }
}
