package aktual.budget.reports.vm.report

import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.TagsDao
import aktual.budget.reports.vm.QueryEditorDataLoader
import aktual.budget.reports.vm.categoryGroupChoices
import aktual.budget.tags.domain.toTagLabels
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal fun customReportEditorData(
  queryEditorDataLoader: QueryEditorDataLoader,
  budgetDao: BudgetDao,
  tagsDao: TagsDao,
): Flow<CustomReportEditorData> =
  combine(
    queryEditorDataLoader.load(),
    budgetDao.observeCategoryGroups(),
    budgetDao.observeCategories(),
    tagsDao.observeTags(),
  ) { query, groups, categories, tags ->
    CustomReportEditorData(
      query = query,
      categories = categoryGroupChoices(groups, categories),
      tags = tags.toTagLabels(),
    )
  }
