# Preserve annotation metadata used by Compose, Kotlin, and serialization.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Dynamic .textension Android runtimes are compiled outside the launcher APK and
# link against these names at load time through DexClassLoader.
-keep class com.dreamyloong.tlauncher.core.** { *; }
-keep class com.dreamyloong.tlauncher.sdk.** { *; }
-keep class com.cauth.android.** { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class androidx.core.** { *; }
-keep class androidx.fragment.** { *; }
-keep class androidx.activity.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.savedstate.** { *; }
