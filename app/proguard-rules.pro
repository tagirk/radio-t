# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-repackageclasses

-allowaccessmodification

-flattenpackagehierarchy

-dontwarn org.jetbrains.annotations.**
#-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

#===============Glide================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule { *; }
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl { *; }
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#-----------Retrofit--------------
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions


#--------------OkHttp--------------
-dontwarn okhttp3.**
-dontwarn okio.**


#---------Dagger---------------
-dontwarn com.google.errorprone.annotations.**


#------------------Enums--------------------
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
