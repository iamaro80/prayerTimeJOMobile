# Proguard rules for Prayer Time Android
-keep class jo.aliftaa.prayertimes.** { *; }
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.work.** { *; }
-keep class androidx.datastore.** { *; }
-keep class com.github.msarhan.ummalqura.** { *; }

-dontwarn com.github.msarhan.ummalqura.**
-dontwarn okio.**
