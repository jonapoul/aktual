package aktual.budget.db.dao

import aktual.budget.db.model.Category
import aktual.budget.db.model.CategoryWithTemplateNote
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query

@Dao
interface CategoriesDao {
  @Insert suspend fun insert(category: Category)

  @Query("SELECT * FROM categories WHERE id = :id") suspend fun getById(id: CategoryId): Category?

  @Query("SELECT is_income FROM categories WHERE id = :id")
  suspend fun isIncome(id: CategoryId): Boolean?

  @Query("SELECT id FROM categories WHERE cat_group = :groupId AND tombstone = 0")
  suspend fun idsMatchingGroup(groupId: CategoryGroupId): List<CategoryId>

  @Query("SELECT * FROM categories WHERE tombstone = 0 AND cat_group = :groupId")
  suspend fun matchingGroup(groupId: CategoryGroupId): List<Category>

  @Query(
    """
    UPDATE categories
    SET goal_def = NULL
    WHERE id NOT IN (
      SELECT n.id
      FROM notes n
      WHERE lower(note) LIKE '%#template%'
      OR lower(note) LIKE '%#goal%'
    )
    """
  )
  suspend fun clearGoalDef()

  @Query(
    """
    SELECT c.id AS id, c.name AS name, n.note AS note
    FROM notes n
    JOIN categories c ON n.id = c.id
    WHERE c.id = n.id
    AND c.tombstone = 0
    AND (
      LOWER(note) LIKE '%#template%'
      OR LOWER(note) LIKE '%#goal%'
    )
    """
  )
  suspend fun categoriesWithTemplateNotes(): List<CategoryWithTemplateNote>

  @Query("SELECT * FROM categories WHERE tombstone = 0 ORDER BY sort_order, id")
  suspend fun categories(): List<Category>

  @Query("SELECT * FROM categories WHERE id IN (:ids) AND tombstone = 0 ORDER BY sort_order, id")
  suspend fun categoriesWithIds(ids: List<CategoryId>): List<Category>

  @Query(
    "SELECT * FROM categories WHERE cat_group IN (:ids) AND tombstone = 0 ORDER BY sort_order, id"
  )
  suspend fun categoriesInGroup(ids: List<CategoryGroupId>): List<Category>

  @Query("DELETE FROM categories WHERE id = :id") suspend fun delete(id: CategoryId)
}
