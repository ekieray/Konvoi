# ProGuard / R8 Rules for KonvoiKu Touring App

# Keep data & state models intact for reflection/serialization if needed
-keep class com.konvoiku.touring.model.** { *; }

# Keep FileProvider paths & support classes
-keep class androidx.core.content.FileProvider { *; }

# Keep Kotlin coroutines & ViewModel metadata
-keepattributes *Annotation*
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
