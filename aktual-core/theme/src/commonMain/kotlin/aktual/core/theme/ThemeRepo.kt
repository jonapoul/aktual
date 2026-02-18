package aktual.core.theme

import kotlinx.serialization.Serializable

@Serializable(ThemeRepoSerializer::class)
data class ThemeRepo(val userName: String, val repoName: String)
