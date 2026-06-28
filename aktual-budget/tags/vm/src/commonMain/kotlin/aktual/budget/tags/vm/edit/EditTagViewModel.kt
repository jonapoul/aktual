package aktual.budget.tags.vm.edit

import aktual.budget.db.dao.DatabaseTables.TAGS
import aktual.budget.db.dao.TagsDao
import aktual.budget.model.BudgetSyncController
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.TagId
import aktual.budget.model.messageValue
import aktual.budget.model.tombstone
import aktual.budget.tags.vm.list.toHex
import aktual.budget.tags.vm.list.toTagItem
import aktual.core.model.UuidGenerator
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@AssistedInject
class EditTagViewModel(
  @Assisted private val tagId: TagId?,
  private val tagsDao: TagsDao,
  private val uuidGenerator: UuidGenerator,
  private val syncController: BudgetSyncController,
) : ViewModel() {
  private val mutableLoaded = MutableStateFlow<Loaded?>(null)
  private val mutableFailure = MutableStateFlow<EditTagState.Failure?>(null)
  private val mutableExistingNames = MutableStateFlow(persistentSetOf<String>())
  private val mutableTag = MutableStateFlow("")
  private val mutableDescription = MutableStateFlow("")
  private val mutableColor = MutableStateFlow<Color?>(null)
  private val mutableColorError = MutableStateFlow(false)

  // the reason the last save attempt failed, shown in an error dialog so the user isn't left
  // guessing
  private val mutableSaveError = MutableStateFlow<String?>(null)
  val saveError: StateFlow<String?> = mutableSaveError.asStateFlow()

  private val mutableEvents =
    MutableSharedFlow<EditTagEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )
  val events: SharedFlow<EditTagEvent> = mutableEvents.asSharedFlow()

  val state: StateFlow<EditTagState> =
    viewModelScope.launchMolecule(Immediate) {
      val loaded by mutableLoaded.collectAsState()
      val failure by mutableFailure.collectAsState()
      val color by mutableColor.collectAsState()
      failure?.let {
        return@launchMolecule it
      }
      when (val l = loaded) {
        null -> EditTagState.Loading
        else ->
          EditTagState.Editing(
            initialTag = l.tag,
            initialDescription = l.description,
            color = color,
            isNew = l.isNew,
          )
      }
    }

  // true when the working edits differ from what was originally loaded
  val hasUnsavedChanges: StateFlow<Boolean> =
    viewModelScope.launchMolecule(Immediate) {
      val loaded by mutableLoaded.collectAsState()
      val tag by mutableTag.collectAsState()
      val description by mutableDescription.collectAsState()
      val color by mutableColor.collectAsState()
      val l = loaded
      l != null && (tag != l.tag || description != l.description || color != l.color)
    }

  // true when the entered name already belongs to another tag
  val isDuplicateName: StateFlow<Boolean> =
    viewModelScope.launchMolecule(Immediate) {
      val tag by mutableTag.collectAsState()
      val existingNames by mutableExistingNames.collectAsState()
      tag.trim() in existingNames
    }

  // a tag is saveable when its name is non-blank, unique, and the colour field isn't in an error
  // state
  val canSave: StateFlow<Boolean> =
    viewModelScope.launchMolecule(Immediate) {
      val tag by mutableTag.collectAsState()
      val colorError by mutableColorError.collectAsState()
      val existingNames by mutableExistingNames.collectAsState()
      val trimmed = tag.trim()
      trimmed.isNotBlank() && !colorError && trimmed !in existingNames
    }

  init {
    load()
  }

  private fun load() {
    viewModelScope.launch {
      mutableExistingNames.update { loadOtherTagNames() }

      if (tagId == null) {
        reset(Loaded(isNew = true))
        return@launch
      }

      val existing =
        try {
          tagsDao.getTag(tagId)?.toTagItem()
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          logcat.e(e) { "Failed loading tag $tagId" }
          mutableFailure.update { EditTagState.Failure(cause = e.requireMessage()) }
          return@launch
        }

      if (existing == null) {
        // the tag was requested but doesn't exist — don't pretend it's a new one
        mutableFailure.update { EditTagState.Failure(cause = null) }
      } else {
        reset(
          Loaded(
            tag = existing.tag,
            description = existing.description,
            color = existing.color,
            isNew = false,
          )
        )
      }
    }
  }

  // the active tag names other than the one being edited, so renaming a tag to itself stays allowed
  private suspend fun loadOtherTagNames(): PersistentSet<String> =
    try {
      tagsDao
        .getTags()
        .asSequence()
        .mapNotNull { it.toTagItem() }
        .filter { it.id != tagId }
        .map { it.tag }
        .toPersistentSet()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.e(e) { "Failed loading existing tag names" }
      persistentSetOf()
    }

  // seed the working edits from the loaded values, so there are initially no unsaved changes
  private fun reset(loaded: Loaded) {
    mutableTag.update { loaded.tag }
    mutableDescription.update { loaded.description }
    mutableColor.update { loaded.color }
    mutableLoaded.update { loaded }
  }

  fun setTag(tag: String) = mutableTag.update { tag }

  fun setDescription(description: String) = mutableDescription.update { description }

  fun setColor(color: Color?) = mutableColor.update { color }

  fun setColorError(isError: Boolean) = mutableColorError.update { isError }

  fun dismissSaveError() = mutableSaveError.update { null }

  fun save() {
    viewModelScope.launch {
      mutableSaveError.update { null }
      try {
        val tag = mutableTag.value.trim()
        val description = mutableDescription.value.trim()
        val color = mutableColor.value?.toHex()

        // another row may already own this name — the tag column is UNIQUE. getTag returns null
        // for tombstoned rows, so a non-null owner that getTag can't see is a deleted tag still
        // squatting on the name. Only the rename path needs the extra lookup, so skip it on create
        val owner = tagsDao.getTagIdByName(tag)
        val tombstonedOwner = tagId?.let { id ->
          owner?.takeIf { it != id && tagsDao.getTag(it) == null }
        }

        if (tagId != null && tombstonedOwner != null) {
          // renaming onto a deleted tag's name: writing the name onto our row would trip the
          // UNIQUE constraint, so retire the row we were editing and merge the edits onto the
          // deleted row, resurrecting it under its original id
          tagsDao.insert(id = tombstonedOwner, tag = tag, color = color, description = description)
          syncController.syncChanges(
            listOf(tombstone(dataset = TAGS, row = tagId.toString())) +
              insertChanges(tombstonedOwner, tag, color, description)
          )
        } else {
          // creating a tag reuses any existing row with the same name — even a tombstoned one —
          // so the old id is resurrected rather than colliding with the UNIQUE constraint
          // (matches createTag)
          val id = tagId ?: owner ?: uuidGenerator(::TagId)
          tagsDao.insert(id = id, tag = tag, color = color, description = description)
          syncController.syncChanges(insertChanges(id, tag, color, description))
        }
        mutableEvents.tryEmit(EditTagEvent.FinishedSaving)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed saving tag $tagId" }
        mutableSaveError.update { e.requireMessage() }
      }
    }
  }

  private fun insertChanges(
    id: TagId,
    tag: String,
    color: String?,
    description: String?,
  ): List<LocalChange> {
    fun change(column: String, value: MessageValue) =
      LocalChange(TAGS, id.toString(), column, value)

    return listOf(
      change("id", id.toString().messageValue()),
      change("tag", tag.messageValue()),
      change("color", color.messageValue()),
      change("description", description.messageValue()),
      change("tombstone", false.messageValue()),
    )
  }

  private data class Loaded(
    val tag: String = "",
    val description: String = "",
    val color: Color? = null,
    val isNew: Boolean = true,
  )

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(tagId: TagId?): EditTagViewModel
  }
}
