# Keep Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }

# Koin
-keep class org.koin.** { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* <methods>;
}

# Keep classes used by Koin for dependency injection
-keepnames class * extends androidx.lifecycle.ViewModel
