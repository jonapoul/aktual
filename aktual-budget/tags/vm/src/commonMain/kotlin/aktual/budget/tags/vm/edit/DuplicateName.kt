package aktual.budget.tags.vm.edit

// Another tag already has the entered name. Hidden tags don't show in the list, so the editor
// says so to explain the clash
enum class DuplicateName {
  Visible,
  Hidden,
}
