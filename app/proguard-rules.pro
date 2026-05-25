# Keep line numbers for crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*

# ─── Firestore ────────────────────────────────────────────────────────────────
# Firestore uses reflection on Java-bean style getters/setters of DTO classes.
-keep class com.example.data.model.** { *; }

# ─── KotlinX Serialization (type-safe Compose Navigation routes) ──────────────
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** serializer(...);
}
-keep,includedescriptorclasses class com.example.autoai.navigation.Route { *; }
-keep,includedescriptorclasses class com.example.autoai.navigation.Route$* { *; }

# ─── Google Gen AI SDK (Gemini) ───────────────────────────────────────────────
-keep class com.google.genai.** { *; }
-dontwarn com.google.genai.**
-keep class com.fasterxml.jackson.** { *; }
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.JsonProperty <fields>;
    @com.fasterxml.jackson.annotation.JsonCreator <init>(...);
}
-dontwarn com.fasterxml.jackson.**

# ─── Coroutines ───────────────────────────────────────────────────────────────
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ─── Domain models ────────────────────────────────────────────────────────────
-keepclassmembers class com.example.domain.model.** { *; }

# ─── ViewModels (Koin resolves constructors reflectively) ─────────────────────
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# ─── WorkManager ──────────────────────────────────────────────────────────────
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
