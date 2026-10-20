# ProGuard and R8 Rules for UPI Sentinel Production Release

# Keep Retrofit and OkHttp classes
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*

# Moshi rules
-keep class com.example.data.model.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Room Database rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# CameraX & ZXing rules
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**
-keep class androidx.camera.** { *; }

# Keep Model and Entity data structures
-keep class com.example.data.local.** { *; }
-keep class com.example.engine.** { *; }
