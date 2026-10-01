# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep BuildConfig
-keep class com.example.BuildConfig { *; }

# ==========================================
# ROOM DATABASE
# ==========================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ==========================================
# MOSHI JSON
# ==========================================
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonQualifier interface *
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
  @com.squareup.moshi.FromJson <methods>;
  @com.squareup.moshi.ToJson <methods>;
}
-keepnames @kotlin.Metadata class com.example.data.**
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** {
  <init>(...);
  <fields>;
}

# ==========================================
# RETROFIT
# ==========================================
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
  @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# ==========================================
# OKHTTP
# ==========================================
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ==========================================
# FIREBASE
# ==========================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep Firestore model classes
-keepclassmembers class * {
  @com.google.firebase.firestore.PropertyName <fields>;
  @com.google.firebase.firestore.Exclude <fields>;
}

# ==========================================
# KOTLIN SERIALIZATION
# ==========================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
  *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
  kotlinx.serialization.KSerializer serializer(...);
}

# ==========================================
# KOTLIN
# ==========================================
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
  <fields>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
  static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# ==========================================
# JETPACK COMPOSE
# ==========================================
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }

# ==========================================
# COROUTINES
# ==========================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
  volatile <fields>;
}

