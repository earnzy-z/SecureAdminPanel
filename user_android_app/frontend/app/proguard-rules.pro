# --------------------------------------------------------------------------------
# Earnzy - ProGuard / R8 rules (production)
# - Consolidated and cleaned for R8 (Android Gradle Plugin)
# - Keep this file in app/proguard-rules.pro
# --------------------------------------------------------------------------------

# -------------------------
# Keep JavaScriptInterface methods used by WebView
# -------------------------
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface

# -------------------------
# Keep application entry points and important components
# -------------------------
-keep class com.earnzy.app.MainActivity { *; }
-keep class com.earnzy.app.MyFirebaseMessagingService { *; }

# Keep LoginActivity and public/native methods
-keep class com.earnzy.app.LoginActivity {
    public <methods>;
    native <methods>;
}

# -------------------------
# SecurityKeys (keep required methods; native methods kept)
# -------------------------
-keep class com.earnzy.app.SecurityKeys {
    public static java.lang.String getDeviceID(android.content.Context);
    public static native java.lang.String getNativeDeviceID(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String);
    public static native java.lang.String getApiUrl();
    public static native java.lang.String getApiKey();
    public static native java.lang.String getAppSecret();
}

# Keep any class members that declare native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Duplicate guard (safe to keep)
-keepclassmembers class * {
    native <methods>;
}

# -------------------------
# Firebase & Play services
# -------------------------
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Google Sign-In specifics
-keep class com.google.android.gms.auth.api.signin.** { *; }
-keep class com.google.android.gms.common.api.** { *; }

# -------------------------
# Glide
# -------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-dontwarn com.bumptech.glide.**

# -------------------------
# Kotlin Coroutines
# -------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# -------------------------
# ViewBinding
# -------------------------
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** inflate(...);
    public static *** bind(...);
}

# -------------------------
# Gson (keep serialized fields)
# -------------------------
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# -------------------------
# Security / Crypto / Keystore
# -------------------------
-keep class javax.crypto.** { *; }
-keep class androidx.security.crypto.** { *; }
-keep class android.security.keystore.* { *; }
-keep class java.security.* { *; }

# -------------------------
# Remove debug logging in release builds
# -------------------------
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# -------------------------
# Warnings to ignore
# -------------------------
-dontwarn org.xmlpull.v1.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.squareup.okhttp3.**
-dontwarn javax.annotation.**
-dontwarn kotlin.Metadata

# -------------------------
# Keep annotations and signatures
# -------------------------
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# Keep source/line attributes for Crashlytics stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# -------------------------
# Optimization & obfuscation options
# -------------------------
# Files for custom obfuscation dictionaries — set filenames in project root if used
-obfuscationdictionary obfuscation-dictionary.txt
-classobfuscationdictionary obfuscation-dictionary.txt
-packageobfuscationdictionary obfuscation-dictionary.txt

# Allow some aggressive optimizations but avoid unsafe merges
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# If you want to repackage classes to a short package (optional)
# (Uncomment if desired — keep in mind it may affect reflection)
#-repackageclasses 'o'
#-flattenpackagehierarchy 'o'

# -------------------------
# Smaller output: strip debug metadata while keeping crash traces
# -------------------------
# Prevent retaining SourceDebugExtension (strip debug-only)
-keepattributes !SourceDebugExtension

# -------------------------
# Misc
# -------------------------
# Prevent preverification (for Android builds this is generally safe)
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# --------------------------------------------------------------------------------
# Notes:
# - Keep native method declarations if you rely on JNI/native libs.
# - If you use reflection for any other classes, add explicit -keep rules.
# - Do NOT commit keystore passwords or sensitive values into VCS.
# - If R8 removes something important, add a targeted -keep for that class.
# --------------------------------------------------------------------------------

# AWS SDK
-keep class com.amazonaws.** { *; }
-keepattributes Signature, *Annotation*
-dontwarn com.amazonaws.**