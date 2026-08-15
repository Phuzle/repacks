# =============================================================================
# Repacks — ProGuard / R8 rules
# =============================================================================
# R8 is enabled for release builds (isMinifyEnabled = true in build.gradle.kts).
# Third-party libraries (OkHttp, Coroutines, Room, Coil) ship their own consumer
# rules; the rules below cover the remainder — reflection-based Kotlin metadata
# and this app's Room entities/DAOs.
# =============================================================================

-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepattributes SourceFile, LineNumberTable

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

-keep class com.phuzle.labs.repacks.data.local.** { *; }
