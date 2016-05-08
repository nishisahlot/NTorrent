# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in F:\AndroidSDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#for butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

#for active android
-keep class com.activeandroid.** { *; }
-keep interface com.activeandroid.** { *; }

#for GSON
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.nishi.torrentmoviedownloader.databaseModels.**{*;}


#Torrent
-keep class com.frostwire.jlibtorrent.** { *; }
-keep interface com.frostwire.jlibtorrent.** { *; }

#for guava
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

#for android support v7
-keep class android.support.v7.internal.** { *; }
-keep interface android.support.v7.internal.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

#for volley
-keep class com.android.volley.** { *; }
-keep interface com.android.volley.** { *; }
-dontwarn com.android.volley.**
#For Acra
-keepattributes SourceFile,LineNumberTable

# ACRA needs "annotations" so add this...
# Note: This may already be defined in the default "proguard-android-optimize.txt"
# file in the SDK. If it is, then you don't need to duplicate it. See your
# "project.properties" file to get the path to the default "proguard-android-optimize.txt".
-keepattributes *Annotation*

# Keep all the ACRA classes
-keep class org.acra.** { *; }

# Don't warn about removed methods from AppCompat
-dontwarn android.support.v4.app.NotificationCompat*

# These classes are constucted with reflection.
-keep public class * implements org.acra.sender.ReportSenderFactory { public <methods>; }

#for jsoup
-keep class org.jsoup.** {*;}
#for okio
-dontwarn okio.**
-dontwarn com.squareup.okhttp.internal.huc.**

#for ads
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{
    public *;
 }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info{
    public *;
 }
 -dontwarn com.google.android.gms.**

-keep public class com.google.ads.** { public protected *; }