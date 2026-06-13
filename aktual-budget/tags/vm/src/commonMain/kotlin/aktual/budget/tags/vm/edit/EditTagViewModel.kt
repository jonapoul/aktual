package aktual.budget.tags.vm.edit

import aktual.budget.db.dao.DatabaseTables.TAGS
import aktual.budget.db.dao.TagsDao
import aktual.budget.model.BudgetSyncController
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.TagId
import aktual.budget.model.messageValue
import aktual.budget.tags.vm.list.toHex
import aktual.budget.tags.vm.list.toTagItem
import aktual.core.model.UuidGenerator
import aktual.di.BudgetScope
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

  // the working edits live here so they survive recomposition and config changes
  private val mutableTag = MutableStateFlow("")
  private val mutableDescription = MutableStateFlow("")
  private val mutableColor = MutableStateFlow<Color?>(null)

  // set by the UI when the colour field holds an invalid hex code, which blocks saving
  private val mutableColorError = MutableStateFlow(false)

  // emitted once the tag has been persisted, so the screen can navigate away
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
      val color by mutableColor.collectAsState()
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

  // a tag is saveable when it has a non-blank name and the colour field isn't in an error state
  val canSave: StateFlow<Boolean> =
    viewModelScope.launchMolecule(Immediate) {
      val tag by mutableTag.collectAsState()
      val colorError by mutableColorError.collectAsState()
      tag.isNotBlank() && !colorError
    }

  init {
    load()
  }

  private fun load() {
    viewModelScope.launch {
      if (tagId == null) {
        // creating a fresh tag — nothing to load
        reset(Loaded(isNew = true))
        return@launch
      }

      val existing =
        runCatching { tagsDao.getTag(tagId) }
          .onFailure { e -> logcat.e(e) { "Failed loading tag $tagId" } }
          .getOrNull()
          ?.toTagItem()

      if (existing == null) {
        reset(Loaded(isNew = true))
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

  fun save() {
    viewModelScope.launch {
      try {
        val id = tagId ?: uuidGenerator(::TagId)
        val tag = mutableTag.value.trim()
        val description = mutableDescription.value.trim()
        val color = mutableColor.value?.toHex()
        tagsDao.insert(id = id, tag = tag, color = color, description = description)
        syncController.syncChanges(insertChanges(id, tag, color, description))
        mutableEvents.tryEmit(EditTagEvent.FinishedSaving)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed saving tag $tagId" }
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
