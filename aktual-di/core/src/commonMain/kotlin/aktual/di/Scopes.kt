package aktual.di

/** Global dependencies, initialised just once and kept alive until process death */
sealed interface AppScope

/** Started when the user selects a server URL */
sealed interface ServerChosenScope

/** Started when the user successfully authenticates on a given server and receives a token */
sealed interface LoggedInScope

/** Started when a budget database has been chosen, loaded from file, migrated and fully set up */
sealed interface BudgetScope
