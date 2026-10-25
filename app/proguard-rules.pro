# Keep line numbers so release crash reports point at real source lines.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# kotlinx.serialization: keep @Serializable models and their generated serializers
# (Supabase rows and navigation routes are decoded by name).
-keepattributes *Annotation*, InnerClasses
-keepclassmembers @kotlinx.serialization.Serializable class com.kush.swych.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.kush.swych.**$$serializer { *; }
-keep @kotlinx.serialization.Serializable class com.kush.swych.** { *; }

# Ktor / Supabase pull in optional JVM-only classes that don't exist on Android.
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
-dontwarn io.ktor.util.debug.**
-dontwarn javax.naming.**

# WorkManager creates its Room database by reflection; without this the release
# build crashes on launch (NoSuchMethodException: WorkDatabase_Impl.<init>).
-keep class * extends androidx.room.RoomDatabase { <init>(); }
