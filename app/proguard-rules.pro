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
-keepnames class kotlinx.** { *; }

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep class su.tagir.apps.radiot.model.entries.RemarkUser
-keep class su.tagir.apps.radiot.model.entries.Comment
-keep class su.tagir.apps.radiot.model.entries.CommentList

#-----------Retrofit--------------
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions

#--------------OkHttp--------------
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform

#------------------Enums--------------------
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
