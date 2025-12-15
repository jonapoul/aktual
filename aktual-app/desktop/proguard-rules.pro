### Heavily borrowed from https://github.com/ryanw-mobile/OctoMeter/blob/main/composeApp/compose-desktop.pro

# App entry point
-keep class aktual.app.desktop.MainKt {
    public static void main(java.lang.String[]);
}

# For debugging
-printmapping build/outputs/mapping.txt
-printconfiguration build/outputs/configuration.txt

# Used by logback/slf4j
-dontwarn jakarta.**
-dontwarn org.codehaus.**
-dontwarn org.tukaani.xz.**

# Used by Wire: https://github.com/square/wire/blob/master/wire-runtime/src/jvmMain/kotlin/com/squareup/wire/AndroidMessage.kt
-dontwarn android.os.**

# Used by coroutines-jvm
-dontwarn android.annotation.**

# ???
-dontwarn java.lang.invoke.MethodHandle
-keep class java.lang.invoke.** { *; }

# Compose runtime - prevent type hierarchy corruption
-keep,allowobfuscation class androidx.compose.runtime.DerivedSnapshotState
-keep,allowobfuscation class androidx.compose.runtime.State
-keep,allowobfuscation class androidx.compose.runtime.SnapshotStateKt**
-keepclassmembers class androidx.compose.runtime.** {
    androidx.compose.runtime.DerivedSnapshotState derivedStateOf*(...);
    androidx.compose.runtime.State *(...);
}

# https://github.com/Kotlin/kotlinx.coroutines/issues/4025#issue-2103585503
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory { *; }

# Preserve sealed class metadata (Java 17+)
-keepattributes PermittedSubclasses
-keepattributes RuntimeVisibleAnnotations

# Keep Kotlin metadata for sealed classes
-keep class kotlin.Metadata { *; }

# SQLDelight - JDBC classes needed by JdbcSqliteDriver
-keep class java.sql.DriverManager { *; }
-keep class java.sql.Connection { *; }
-keep class java.sql.Statement { *; }
-keep class java.sql.PreparedStatement { *; }
-keep class java.sql.ResultSet { *; }
-keep class java.sql.SQLException { *; }
-keep class java.sql.Driver { *; }
-dontwarn java.sql.**
# Xerial SQLite JDBC driver - loaded via ServiceLoader
-keep class org.sqlite.JDBC { *; }
-keep class org.sqlite.** { *; }
-keepclassmembers class org.sqlite.** { *; }
# SQLDelight JDBC driver - core classes
-keep class app.cash.sqldelight.driver.jdbc.ConnectionManager { *; }
-keep class app.cash.sqldelight.driver.jdbc.ConnectionManager$Transaction { *; }
-keep class app.cash.sqldelight.driver.jdbc.JdbcDriver { *; }
-keep class app.cash.sqldelight.driver.jdbc.JdbcCursor { *; }
-keep class app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement { *; }
# SQLDelight SQLite driver
-keep class app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver { *; }
-keep class app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver$Companion { *; }
-keep class app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriverConnectionManager { *; }
-keep class app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriverKt { *; }
-keep class app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteSchemaKt { *; }
