# Proguard rules for Supabase, Ktor, Room, and Serialization

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Supabase and Ktor
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# ML Kit Barcode
-keep class com.google.mlkit.vision.barcode.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
