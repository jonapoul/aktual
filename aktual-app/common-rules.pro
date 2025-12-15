### Heavily borrowed from https://github.com/ryanw-mobile/OctoMeter/blob/main/composeApp/compose-desktop.pro

# Preserve ServiceLoader metadata for runtime service discovery
-keepclassmembers class * { *** MODULE$; }
-keepnames class * implements java.io.Serializable
-keep class * implements java.sql.Driver { *; }

# Logging
-keep class org.slf4j.** { *; }
-keep class org.slf4j.impl.** { *; }
-keep class ch.qos.logback.** { *; }
-dontwarn org.slf4j.**

# kotlinx.serialization
-dontwarn kotlinx.serialization.KSerializer
-dontwarn kotlinx.serialization.Serializable
-if @kotlinx.serialization.Serializable class ** { public static ** INSTANCE; }
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class kotlinx.serialization.descriptors.** { *; }
-keep class kotlinx.serialization.internal.*Serializer { *; }
-keep class kotlinx.serialization.internal.PluginGeneratedSerialDescriptor { *; }
-keep class **$$serializer { *; }
-keepclassmembers class **$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** { *** Companion; }

# Enums, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# https://www.guardsquare.com/manual/configuration/examples#annotations
# Annotated interfaces (including methods which are also kept in implementing classes)
-keepattributes Annotation
-keepattributes *Annotation*

# Keeping the implementations of exception handlers and Main dispatchers
-keep class * implements kotlinx.coroutines.internal.MainDispatcherFactory
-keep class * implements kotlinx.coroutines.CoroutineExceptionHandler

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembers class kotlin.coroutines.SafeContinuation { volatile <fields>; }

# Additional dontwarn rules for common issues (handled at runtime)
-dontwarn java.awt.**
-dontwarn javax.annotation.**
-dontwarn kotlin.concurrent.atomics.**
-dontwarn kotlin.jvm.internal.**

# Ktor - Keep only what's needed for runtime
-keep class io.ktor.client.engine.** { *; }
-keep class io.ktor.client.plugins.** { *; }
-keep class io.ktor.serialization.** { *; }
-keep,allowobfuscation class io.ktor.client.HttpClient
-keep,allowobfuscation class io.ktor.client.HttpClientConfig
-dontwarn io.ktor.**

# Ktor network uses AtomicReferenceFieldUpdater on volatile fields (similar to coroutines)
-keepclassmembers class io.ktor.network.** { volatile <fields>; }

# Okio - Preserve type hierarchies to prevent VerifyError
-keep,allowobfuscation interface okio.BufferedSource
-keep,allowobfuscation interface okio.BufferedSink
-keep,allowobfuscation class okio.RealBufferedSource
-keep,allowobfuscation class okio.RealBufferedSink
-keep,allowobfuscation interface okio.Source
-keep,allowobfuscation interface okio.Sink
-keep class okio.Okio { *; }
-keep class okio.Okio__OkioKt { *; }

# Suppress duplicate resource file notes (e.g., META-INF/MANIFEST.MF)
#-dontnote **
