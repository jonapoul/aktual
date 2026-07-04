package aktual.prefs

import aktual.budget.model.TagSort

interface TagPreferences {
  // The "#RRGGBB" colour last applied to a tag, used to seed the colour for the next new tag.
  // Null until the user has saved a coloured tag
  val lastUsedTagColor: NullablePreference<String>

  val sortField: Preference<TagSort.Field>
  val sortDirection: Preference<TagSort.Direction>
}
